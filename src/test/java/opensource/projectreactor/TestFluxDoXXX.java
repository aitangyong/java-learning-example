package opensource.projectreactor;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Signal;

/**
 * TODO doOnDiscard
 */
public class TestFluxDoXXX {

    @Test
    public void doOnSubscribe() {
        // doOnSubscribe在订阅者的onSubscribe之前被调用
        // triggered when the {@link Flux} is done being subscribed, that is to say when a {@link Subscription}
        // has been produced by the {@link Publisher} and passed to the {@link Subscriber#onSubscribe(Subscription)}
        Flux.just(1)
            .doOnSubscribe(subscription -> System.out.println("doOnSubscribe"))
            .subscribe(new ReactorSampleSubscriber<>("s1", 2, 1));

        // 创建一个处于错误状态的Flux仍然可以触发doOnSubscribe
        Flux.error(new NullPointerException("for test"))
            .doOnSubscribe(subscription -> System.out.println("doOnSubscribe"))
            .subscribe(new ReactorSampleSubscriber<>("s2", 2, 1));
    }

    @Test
    public void doFirst() {
        // case1: 声明的顺序是three-two-one, 实际的执行顺序是one-two-three
        // Note that when several #doFirst(Runnable) operators are used anywhere in a chain of operators,
        // their order of execution is reversed compared to the declaration order
        // (as subscribe signal flows backward, from the ultimate subscriber to the source publisher)
        Flux.just(1)
            .doFirst(() -> System.out.println("three"))
            .doFirst(() -> System.out.println("two"))
            .doFirst(() -> System.out.println("one"))
            .subscribe(element -> System.out.println("case1..." + element));


        // case2: 如果doFirst中出现异常, directly propagated to the subscriber along with a no-op subscription
        // doFirst异常会创建1个EmptySubscription, doOnSubscribe不会被触发, 但是订阅者的onSubscribe和onError会被触发
        // doFirst执行时机在doOnSubscribe之前
        // (which is triggered once the Subscription has been set up and passed to the Subscriber)
        ReactorSampleSubscriber<Integer> subscriber = new ReactorSampleSubscriber<>("s1", 2, 1);
        Flux.just(1)
            .doOnSubscribe(subscription -> System.out.println("doOnSubscribe"))
            .doFirst(() -> {
                throw new NullPointerException("unknown");
            }).subscribe(subscriber);
    }

    @Test
    public void doOnTerminate() {
        // doAfterTerminate在subscriber.onComplete()/subscriber.onError()之后执行
        // doOnTerminate在subscriber.onComplete()/subscriber.onError()之前执行
        ReactorSampleSubscriber<Integer> subscriber = new ReactorSampleSubscriber<>("s1", 2, 1);
        Flux.just(1)
            .doOnTerminate(() -> System.out.println("doOnTerminate"))
            .doAfterTerminate(() -> System.out.println("doAfterTerminate"))
            .subscribe(subscriber);
    }

    @Test
    public void doOnComplete() {
        ReactorSampleSubscriber<Integer> subscriber = new ReactorSampleSubscriber<>("s1", 20, 1);
        Flux.range(1, 3)
            .doOnRequest(i -> System.out.println("Flux.doOnRequest:" + i))
            .doOnNext(i -> System.out.println("Flux.doOnNext:" + i))
            .doOnComplete(() -> System.out.println("Flux.doOnComplete"))
            .doOnError(e -> System.out.println("Flux.doOnError:" + e))
            .doFinally(signal -> System.out.println("Flux.doFinally:" + signal))
            .subscribe(subscriber);
    }

    @Test
    public void doOnError() {
        ReactorSampleSubscriber<Integer> subscriber = new ReactorSampleSubscriber<>("s1", 20, 1);
        Flux.just(1)
            .map(e -> e / (1 - e))
            .doOnRequest(i -> System.out.println("Flux.doOnRequest:" + i))
            .doOnNext(i -> System.out.println("Flux.doOnNext:" + i))
            .doOnComplete(() -> System.out.println("Flux.doOnComplete"))
            .doOnError(e -> System.out.println("Flux.doOnError:" + e))
            .doFinally(signal -> System.out.println("Flux.doFinally:" + signal))
            .subscribe(subscriber);
    }

    @Test
    public void doOnCancel() {
        ReactorSampleSubscriber<Integer> subscriber = new ReactorSampleSubscriber<>("s1", 3, 1);
        Flux.range(1, 10)
            .doOnComplete(() -> System.out.println("Flux.doOnComplete"))
            .doOnError(e -> System.out.println("Flux.doOnError:" + e))
            .doFinally(signal -> System.out.println("Flux.doFinally:" + signal))
            .doOnCancel(() -> System.out.println("Flux.doOnCancel"))
            .subscribe(subscriber);
    }

    @Test
    public void doOnEach() {
        // onNext和onComplete会触发doOnEach
        Flux.just(1, 2)
            .doOnEach(signal -> System.out.println("Flux.doOnEach:" + signal.getType()))
            .subscribe(new ReactorSampleSubscriber<>("s1", 10, 1));

        // onError会触发doOnEach, 而onCancel不会触发
        Flux.error(new NullPointerException())
            .doOnEach(signal -> System.out.println("Flux.doOnEach:" + signal.getType()))
            .subscribe(new ReactorSampleSubscriber<>("s1", 10, 1));
    }

    @Test
    public void materialize() {
        // Transform incoming onNext, onError and onComplete signals into {@link Signal} instances
        // materialize将异常转换成了Signal对象, 所以flux是正常完成, 会触发subscriber的onNext
        ReactorSampleSubscriber<Signal<Integer>> subscriber = new ReactorSampleSubscriber<>("s1", 5, 1);
        Flux.just(1)
            .map(e -> e / (e - 1))
            .materialize()
            .doOnComplete(() -> System.out.println("Flux.doOnComplete"))
            .subscribe(subscriber);
    }
}
