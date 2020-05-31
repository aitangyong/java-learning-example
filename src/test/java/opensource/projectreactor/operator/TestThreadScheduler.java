package opensource.projectreactor.operator;

import opensource.projectreactor.ReactorSampleSubscriber;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * https://www.jianshu.com/p/769f6e9824fb
 * publishOn和subscribeOn,这两个方法的作用是指定执行Reactive Streaming的scheduler(可理解为线程池).
 * 为何需要指定执行Scheduler呢?一个显而易见的原因是: 组成一个反应式流的代码有快有慢,
 * 如果将这些功能都放在一个线程里执行，快的就会被慢的影响，所以需要相互隔离.
 * 两者的区别在于影响范围,publishOn影响在其之后的operator执行的线程池,而subscribeOn则会从源头影响整个执行过程.
 * 所以, publishOn的影响范围和它的位置有关, 而subscribeOn的影响范围则和位置无关
 * <p>
 * publishOn: Typically used for fast publisher, slow consumer(s) scenarios.
 * subscribeOn: Typically used for slow publisher e.g., blocking IO, fast consumer(s) scenarios.
 */
public class TestThreadScheduler {

    /**
     * Obtaining a Flux or a Mono does not necessarily mean that it runs in a dedicated Thread.
     * Instead, most operators continue working in the Thread on which the previous operator executed.
     * Unless specified, the topmost operator (the source) runs on the Thread in which the subscribe() call was made.
     * <p>
     * Reactor offers two means of switching the execution context (Scheduler) in a reactive chain:
     * publishOn and subscribeOn.Both take a Scheduler and let you switch the execution context to that scheduler.
     * But the placement of publishOn in the chain matters, while the placement of subscribeOn does not.
     * To understand that difference, you first have to remember that nothing happens until you subscribe.
     * <p>
     * Once you subscribe, a chain of Subscriber objects is created, backward (up the chain) to the first publisher.
     * This is effectively hidden from you. All you can see is the outer layer of Flux (or Mono) and Subscription,
     * but these intermediate operator-specific subscribers are where the real work happens.
     */
    @Test
    public void first() throws InterruptedException {
        // flux对象是在main线程中被创建
        Flux<Integer> flux = Flux.just(1)
                                 .map(data -> {
                                     System.out.println("mapThread: " + Thread.currentThread().getName());
                                     return data * 10;
                                 })
                                 .filter(data -> {
                                     System.out.println("filterThread: " + Thread.currentThread().getName());
                                     return data > 0;
                                 });


        // subscribe操作是发生在demo线程中
        Runnable task = () -> {
            flux.map(msg -> {
                System.out.println("anotherMapThread: " + Thread.currentThread().getName());
                return msg;
            }).subscribe(v -> System.out.println("subscribeThread: " + Thread.currentThread().getName()));
        };

        // 上面打印的地方都是运行在demo线程中的
        Thread thread = new Thread(task);
        thread.setName("demo");
        thread.start();
        thread.join();
    }

    /**
     * publishOn applies in the same way as any other operator, in the middle of the subscriber chain.
     * It takes signals from upstream and replays them downstream while executing the callback on a worker from the associated Scheduler.
     * Consequently, it affects where the subsequent operators execute (until another publishOn is chained in).
     */
    @Test
    public void publishOn() throws InterruptedException {
        Scheduler scheduler = Schedulers.newParallel("parallel-scheduler", 4);
        Flux<String> flux = Flux
                .just(1)
                .map(i -> {
                    System.out.println("map1..." + Thread.currentThread().getName());
                    return 10 + i;
                })
                .publishOn(scheduler)
                .map(i -> {
                    System.out.println("map2..." + Thread.currentThread().getName());
                    return "value-" + i;
                });

        // 订阅发生在demo线程中, 所以第一个map运行在demo线程
        Thread thread = new Thread(() -> {
            flux.subscribe(e -> System.out.println("subscribe1..." + Thread.currentThread().getName() + "..." + e));
            // flux.subscribe(new ReactorSampleSubscriber<>("x",10,0));
        });
        thread.setName("demo");
        thread.start();
        thread.join();
        // 使用了publishOn ,map2运行在scheduler线程中,subscribe也运行在scheduler线程中
    }

    @Test
    public void publishOn2() throws InterruptedException {
        Scheduler scheduler = Schedulers.newParallel("parallel-scheduler", 1);
        Flux<Integer> flux = Flux.just(1).publishOn(scheduler);

        // 订阅发生在demo线程中,ReactorSampleSubscriber.hookOnSubscribe运行在demo线程中
        // onNext, onComplete and onError是运行在publishOn指定的线程中
        Thread thread = new Thread(() -> {
            flux.subscribe(new ReactorSampleSubscriber<>("x", 10, 0));
        });
        thread.setName("demo");
        thread.start();
        thread.join();

        // 如果没有这个sleep,hookFinally可能没有机会执行
        Thread.sleep(3000);
    }

    /**
     * subscribeOn applies to the subscription process, when that backward chain is constructed.
     * As a consequence, no matter where you place the subscribeOn in the chain,
     * it always affects the context of the source emission.
     * However, this does not affect the behavior of subsequent calls to publishOn,
     * they still switch the execution context for the part of the chain after them.
     */
    @Test
    public void subscribeOn() throws InterruptedException {
        Scheduler scheduler = Schedulers.newParallel("parallel-scheduler", 5);
        Flux<String> flux = Flux
                .just(1)
                .map(i -> {
                    System.out.println("map1..." + Thread.currentThread().getName());
                    return 10 + i;
                })
                .subscribeOn(scheduler)
                .map(i -> {
                    System.out.println("map2..." + Thread.currentThread().getName());
                    return "value " + i;
                })
                .publishOn(scheduler)
                .map(i -> {
                    System.out.println("map3..." + Thread.currentThread().getName());
                    return "value " + i;
                });

        // demo线程(where the subscription initially happens),
        // but subscribeOn immediately shifts it to one of the four scheduler threads
        Thread thread = new Thread(() -> {
            flux.subscribe(e -> System.out.println("subscribe..." + Thread.currentThread().getName() + "..." + e));
        });
        thread.setName("demo");
        thread.start();
        Thread.sleep(1000);
        // map1和map2运行在1个scheduler线程中, map3和subscribe运行在另外一个scheduler线程中
    }

    @Test
    public void basic() throws InterruptedException {
        // subscribeOn定义在publishOn之后,但是却从源头开始生效;而在publishOn执行之后,线程池变更为publishOn所定义的
        Flux.just("tom")
            .map(s -> {
                System.out.println("[map] Thread: " + Thread.currentThread().getName());
                return s.concat("@mail.com");
            })
            .publishOn(Schedulers.newElastic("thread-publishOn"))
            .filter(s -> {
                System.out.println("[filter] Thread: " + Thread.currentThread().getName());
                return s.startsWith("t");
            })
            .subscribeOn(Schedulers.newElastic("thread-subscribeOn"))
            .subscribe(s -> {
                System.out.println("[subscribe] Thread: " + Thread.currentThread().getName());
            });

        // 等待任务执行完毕
        Thread.sleep(1000);
    }
}
