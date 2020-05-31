package opensource.projectreactor;

import org.junit.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


/**
 * 操作符的示意图:
 * reactor-core-3.3.3.RELEASE-source.jar
 * <img class="marble" src="doc-files/marbles/combineLatest.svg" alt="">
 * https://projectreactor.io/docs/core/release/api/reactor/core/publisher/{src}
 *
 * <p>
 * https://www.ibm.com/developerworks/cn/java/j-cn-with-reactor-response-encode/
 * https://www.infoq.com/articles/reactor-by-example
 * https://www.cnblogs.com/yjmyzz/p/reactor-tutorial-2.html
 * https://blog.51cto.com/liukang/2090191
 * https://zhuanlan.zhihu.com/p/37355606
 */
public class TestFlux {

    /**
     * 使用JDK的lambda订阅
     */
    @Test
    public void subscribeUseLambda() {
        // 只注册onNext
        Flux.range(1, 3).subscribe(e -> System.out.println("onNext:" + e));

        // 同时注册onNext和onError
        Flux.range(1, 5)
            .map(i -> {
                if (i <= 3) return i * 10;
                throw new RuntimeException("Got to 4");
            })
            .subscribe(i -> System.out.println("onNext:" + i), error -> System.err.println("onError:" + error));

        // 同时注册onComplete和onNext和onError
        // Error signals and completion signals are both terminal events and are exclusive of one another.
        // To make the completion consumer work, we must take care not to trigger an error.
        Flux.range(1, 3)
            .subscribe(e -> System.out.println("onNext:" + e),
                    error -> System.err.println("onError:" + error),
                    () -> System.out.println("onComplete"));

        // the consumer to invoke on subscribe signal, to be used for the initial Subscription.request(long)
        Flux.range(1, 10)
            .subscribe(e -> System.out.println("onNext:" + e),
                    error -> System.err.println("onError:" + error),
                    () -> System.out.println("onComplete"),
                    subscription -> {
                        subscription.request(5);
                    });
    }

    /**
     * 使用reactor规范的subscriber订阅
     */
    @Test
    public void subscribeUseSubscriber() {
        ReactorSampleSubscriber<Integer> subscriber = new ReactorSampleSubscriber<>("s1", 2, 1);
        Flux.range(1, 10)
            .doOnSubscribe(subscription -> System.out.println("Flux.doOnSubscribe"))
            .doOnRequest(i -> System.out.println("Flux.doOnRequest:" + i))
            .doOnNext(i -> System.out.println("Flux.doOnNext:" + i))
            .doOnComplete(() -> System.out.println("Flux.doOnComplete"))
            .doOnError(e -> System.out.println("Flux.doOnError:" + e))
            .doOnTerminate(() -> System.out.println("Flux.doOnTerminate")) // completing successfully or with an error
            .doOnCancel(() -> System.out.println("Flux.doOnCancel"))
            .subscribe(subscriber);
    }

    @Test
    public void take() throws Exception {
        Flux.range(1, 1000).take(10).subscribe(System.out::println);
        Flux.range(1, 1000).takeLast(10).subscribe(System.out::println);
        // 输出1~9
        Flux.range(1, 1000).takeWhile(i -> i < 10).subscribe(System.out::println);
        // 输出1~10
        Flux.range(1, 1000).takeUntil(i -> i == 10).subscribe(System.out::println);

        // 输出0~19,1s产生1个数据(从0开始),只取前20s的数据
        Flux.interval(Duration.of(1, ChronoUnit.SECONDS)).take(Duration.ofSeconds(20))
            .subscribe(System.out::println);

        // 防止程序过早退出
        CountDownLatch latch = new CountDownLatch(1);
        latch.await();
    }


    @Test
    public void generate() {
        Flux.generate(synchronousSink -> {
            synchronousSink.next("hello");

            // 如果不调用 complete()方法,所产生的是一个无限序列
            synchronousSink.complete();
        }).subscribe(System.out::println);

        Flux.generate(
                () -> 0,
                (state, sink) -> {
                    sink.next("3 x " + state + " = " + 3 * state);
                    if (state == 10) sink.complete();
                    return state + 1;
                }).subscribe(System.out::println);

        Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long i = state.getAndIncrement();
                    sink.next("3 x " + i + " = " + 3 * i);
                    if (i == 10) sink.complete();
                    return state;
                }, (state) -> System.out.println("state: " + state)).subscribe(System.out::println);

    }


    /**
     * <pre>
     * In Reactive Streams, errors are terminal events. As soon as an error occurs, it stops the sequence and gets
     * propagated down the chain of operators to the last step.Even if an error-handling operator is used, it does
     * not let the original sequence continue.
     *
     * onErrorReturn: Catch and return a static default value
     * onErrorResume: Catch and execute an alternative path with a fallback method(use another Flux)
     * onErrorMap/onErrorResume: Catch and Rethrow
     * doOnError: Catch, log an error-specific message, and re-throw
     * doFinally/using: Use of the finally block to clean up resources
     * </pre>
     */
    @Test
    public void handlingErrors() {
        // onErrorReturn可以根据异常class类型或者Predicate条件来判断,是否需要用默认值替代异常
        // 只会打印1个RECOVERED,因为第一个元素map异常的时候,流就终止了;System.err不会被调用,因为异常被处理了
        Flux.just(1, 2).map(i -> {
            throw new NullPointerException(String.valueOf(i));
        }).onErrorReturn("RECOVERED")
            .subscribe(value -> System.out.println("RECEIVED " + value),
                    throwable -> System.err.println("CAUGHT " + throwable));

        // falling back to another Flux
        Flux.range(1, 6)
            .map(i -> 10 / (i - 3))
            .onErrorResume(e -> Flux.just(new Random().nextInt(6))) // 提供新的数据流
            .map(i -> i * i)
            .subscribe(System.out::println, System.err::println);

        Flux.just("key1", "key2")
            .flatMap(this::callExternalService)
            .onErrorResume(e -> getFromCache(e.getMessage()))
            .subscribe(System.out::println, System.err::println);

        // Catch and Rethrow use onErrorResume use onErrorResume or onErrorMap operator
        Flux.just("timeout1")
            .map(item -> {
                throw new NullPointerException(item);
            })
            .onErrorResume(original -> Flux.error(new TimeoutException(original.getMessage())))
            .subscribe(value -> System.out.println("RECEIVED " + value),
                    throwable -> System.err.println("CAUGHT " + throwable));

        // you want the error to continue propagating but still want to react to it without
        // modifying the sequence (logging it, for instance)
        // 整个流最终还是异常完成的(errorConsumer会被调用)
        Flux.just("unknown")
            .map(this::callExternalService)
            .doOnError(e -> {
                // 记录异常日志
                System.err.println("uh oh, falling back, service failed for key " + e);
            }).subscribe(value -> System.out.println("RECEIVED " + value),
                throwable -> System.err.println("CAUGHT " + throwable));

        // Use of the finally block to clean up resources,doFinally and using
        Flux.just("key2").map(this::callExternalService)
            .doFinally(type -> System.out.println("doFinally..." + type))
            .subscribe(value -> System.out.println("RECEIVED " + value),
                    throwable -> System.err.println("CAUGHT " + throwable));

        // using类似jdk中的try-with-resource
        AtomicBoolean isDisposed = new AtomicBoolean();

        // 资源对象,类似flux
        Disposable disposableInstance = new Disposable() {
            @Override
            public void dispose() {
                isDisposed.set(true);
            }

            @Override
            public String toString() {
                return "DISPOSABLE";
            }
        };
        // The first lambda generates the resource
        // he second lambda processes the resource, returning a Flux<T>
        // The third lambda is called when the Flux from <2> terminates or is cancelled, to clean up resources
        Flux.using(
                () -> disposableInstance,
                disposable -> Flux.just(disposable.toString()),
                Disposable::dispose
        ).subscribe(value -> System.out.println("RECEIVED " + value),
                throwable -> System.err.println("CAUGHT " + throwable));
        // After subscription and execution of the sequence, the isDisposed atomic boolean becomes true
        System.out.println(isDisposed.get());
    }

    private Flux<String> callExternalService(String key) {
        if (key.equalsIgnoreCase("key1")) {
            return Flux.just("external-" + key);
        } else {
            throw new IllegalArgumentException(key);
        }
    }

    private Flux<String> getFromCache(String key) {
        return Flux.just("cache-" + key);
    }


}
