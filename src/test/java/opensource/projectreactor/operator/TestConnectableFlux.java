package opensource.projectreactor.operator;

import opensource.projectreactor.ReactorSampleSubscriber;
import org.junit.Test;
import reactor.core.Disposable;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 普通的Flux在观察者订阅的时候就会发射数据, 但是有的时候我们想自己控制数据的发射, 比如在有指定的观察者或者全部的观察者订阅后开始发射数据,
 * 这个时候我们就要用到ConnectableFlux
 * https://www.jianshu.com/p/575ce5b98389
 */
public class TestConnectableFlux {

    /**
     * 调用connect()后,不管connectableFlux是否有显示的订阅者,都会触发原始的flux的onSubscribe->onNext->onComplete
     */
    @Test
    public void t1() {
        // 普通的flux
        Flux<Integer> basicFlux = Flux.just(1, 2, 3)
                                      .doOnSubscribe(e -> System.out.println("doOnSubscribe..."))
                                      .doOnNext(e -> System.out.println("doOnNext..." + e))
                                      .doOnComplete(() -> System.out.println("doOnComplete..."));

        // 将普通的Flux转换为可连接的Flux
        ConnectableFlux<Integer> connectableFlux = basicFlux.publish();
        connectableFlux.connect();
    }

    /**
     * ConnectableFlux并不会在被订阅时开始发射数据,而是直到使用了Connect操作符时才会开始,所以可以用来更灵活的控制数据发射的时机
     * <p>
     * 无论ConnectableFlux有多少订阅者(甚至是没有订阅者), basicFlux的doOnSubscribe和doOnComplete只会执行一次
     */
    @Test
    public void t2() {
        // 普通的flux
        Flux<Integer> basicFlux = Flux.just(1, 2, 3)
                                      .doOnSubscribe(e -> System.out.println("doOnSubscribe..."))
                                      .doOnNext(e -> System.out.println("doOnNext..." + e))
                                      .doOnComplete(() -> System.out.println("doOnComplete..."));

        // 将普通的Flux转换为可连接的Flux
        ConnectableFlux<Integer> connectableFlux = basicFlux.publish();

        // 第一次订阅
        connectableFlux.subscribe(e -> System.out.println("subscribe1-next: " + e),
                e -> System.err.println("subscribe1-error" + e),
                () -> System.out.println("subscribe1-complete"));

        // 第二次订阅
        connectableFlux.subscribe(e -> System.out.println("subscribe2-next: " + e),
                e -> System.err.println("subscribe2-error" + e),
                () -> System.out.println("subscribe2-complete"));

        // 显示触发订阅
        connectableFlux.connect();
    }

    /**
     * 如果发射数据已经开始了再进行订阅只能接收以后发射的数据;
     * 当一个订阅者取消对ConnectableFlux的订阅，不会影响其他订阅者收到消息
     */
    @Test
    public void t3() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final ConnectableFlux<Long> connectableFlux = Flux.interval(Duration.ofSeconds(1)).publish();
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        final Disposable disposable = connectableFlux.connect();

        // 订阅者1
        new Thread(() -> {
            boolean subscribe = false;
            while (true) {
                if (atomicInteger.get() == 3 && !subscribe) {
                    subscribe = true;
                    connectableFlux.subscribe(new ReactorSampleSubscriber<>("s1", 1000, 1));
                }
            }
        }, "t1").start();

        // 订阅者2
        new Thread(() -> {
            boolean subscribe = false;
            while (true) {
                if (atomicInteger.get() == 5 && !subscribe) {
                    subscribe = true;
                    connectableFlux.subscribe(new ReactorSampleSubscriber<>("s2", 1000, 1));
                }
            }
        }, "t2").start();

        // 停止线程
        new Thread(() -> {
            while (true) {
                if (atomicInteger.get() == 10) {
                    disposable.dispose();
                    countDownLatch.countDown();
                    break;
                }
            }
        }).start();

        // 计数线程
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                atomicInteger.incrementAndGet();
            }
        }).start();

        countDownLatch.await();
    }

    @Test
    public void autoConnect() throws Exception {
        // autoConnect(n) can do the same job automatically once n subscriptions have been made.
        Flux<Integer> source = Flux.range(1, 3)
                                   .doOnSubscribe(s -> System.out.println("subscribed to source"));
        Flux<Integer> flux = source.publishOn(Schedulers.newElastic("custom")).publish().autoConnect(2);

        flux.subscribe(e -> System.out.println("subscribe1..." + Thread.currentThread().getName() + "..." + e));
        Thread.sleep(500);
        flux.subscribe(e -> System.out.println("subscribe2..." + Thread.currentThread().getName() + "..." + e));
    }

    /**
     * 使用了ReactorSampleSubscriber可以看到dispose()会触发订阅者的hookOnError,但是没有触发hookOnCancel
     */
    @Test
    public void autoConnect2() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final Disposable[] disposables = new Disposable[1];

        // 计数器运行在单独的线程中
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        Flux.interval(Duration.ofSeconds(1)).subscribe(e -> atomicInteger.incrementAndGet());

        // 停止线程
        new Thread(() -> {
            while (true) {
                if (atomicInteger.get() == 3) {
                    disposables[0].dispose();
                    countDownLatch.countDown();
                    break;
                }
            }
        }).start();

        Flux<Long> flux = Flux.interval(Duration.ofSeconds(1)).publish()
                              .autoConnect(1, disposable -> {
                                  System.out.println("set disposable..." + Thread.currentThread().getName());
                                  // 注意这里不能直接调用disposable.dispose(),否则会抛出异常, 应该记录下来这个引用
                                  disposables[0] = disposable;
                              });
        flux.subscribe(new ReactorSampleSubscriber<>("s0", 1000, 1));

        countDownLatch.await();
    }

    @Test
    public void refCount() throws Exception {
        Flux<Long> source = Flux.interval(Duration.ofMillis(500))
                                .doOnSubscribe(s -> System.out.println("上游收到订阅"))
                                .doOnCancel(() -> System.out.println("上游断开连接"));
        Flux<Long> refCounted = source.publish().refCount(2);

        // sub1订阅,因为没有达到2个订阅者,所以source没有发生订阅
        System.out.println("第一个订阅者订阅");
        Disposable sub1 = refCounted.subscribe(e -> System.out.println("sub1:" + e));

        // 1s后加入sub2加入, sub1和sub2开始收到数据
        TimeUnit.SECONDS.sleep(1);
        System.out.println("第二个订阅者订阅");
        Disposable sub2 = refCounted.subscribe(l -> System.out.println("sub2: " + l));

        // 1s后sub1取消订阅, sub2继续接收数据
        TimeUnit.SECONDS.sleep(1);
        System.out.println("第一个订阅者取消订阅");
        sub1.dispose();

        // 1s后sub2取消订阅, 此时没有订阅者了, source取消订阅
        TimeUnit.SECONDS.sleep(1);
        System.out.println("第二个订阅者取消订阅");
        sub2.dispose();

        // 等待一段时间再退出进程
        Thread.sleep(5000);
    }

    /**
     * https://blog.51cto.com/liukang/2097141
     */
    @Test
    public void refCountDuration() throws Exception {
        Flux<Long> source = Flux.interval(Duration.ofMillis(500))
                                .doOnSubscribe(s -> System.out.println("上游收到订阅"))
                                .doOnCancel(() -> System.out.println("上游发布者断开连接"));
        // 当所有订阅者都取消时，如果不能在两秒内接入新的订阅者，则上游会断开连接
        Flux<Long> refCounted = source.publish().refCount(2, Duration.ofSeconds(2));

        System.out.println("第一个订阅者订阅");
        Disposable sub1 = refCounted.subscribe(l -> System.out.println("sub1: " + l));

        TimeUnit.SECONDS.sleep(1);
        System.out.println("第二个订阅者订阅");
        Disposable sub2 = refCounted.subscribe(l -> System.out.println("sub2: " + l));

        TimeUnit.SECONDS.sleep(1);
        System.out.println("第一个订阅者取消订阅");
        sub1.dispose();

        TimeUnit.SECONDS.sleep(1);
        System.out.println("第二个订阅者取消订阅");
        sub2.dispose();

        // 没有超过gracePeriod, 之前连接不会断开, 继续订阅
        TimeUnit.SECONDS.sleep(1);
        System.out.println("第三个订阅者订阅");
        Disposable sub3 = refCounted.subscribe(l -> System.out.println("sub3: " + l));

        TimeUnit.SECONDS.sleep(1);
        System.out.println("第三个订阅者取消订阅");
        sub3.dispose();

        // 等待时间超过了gracePeriod, 重头订阅
        TimeUnit.SECONDS.sleep(3);
        System.out.println("第四个订阅者订阅");
        Disposable sub4 = refCounted.subscribe(l -> System.out.println("sub4: " + l));
        TimeUnit.SECONDS.sleep(1);
        System.out.println("第五个订阅者订阅");
        Disposable sub5 = refCounted.subscribe(l -> System.out.println("sub5: " + l));
        TimeUnit.SECONDS.sleep(1);
        sub4.dispose();
        sub5.dispose();
    }


    /**
     * 如果是通过publish()创建的, 那么订阅者之后收到订阅后Cold Observable发送的数据
     * 而如果是reply(int N)创建的，那么订阅者还能额外收到N个之前Cold Observable发送的数据
     */
    @Test
    public void replay() throws Exception {
        Flux<Long> source = Flux.interval(Duration.ofMillis(1000));

        // history=2保留最后2个发出的数据
        ConnectableFlux<Long> connectableFlux = source.replay(2);

        // s1第一个元素从0开始
        connectableFlux.subscribe(e -> System.out.println("s1:" + e));
        connectableFlux.connect();

        // s2订阅后, s1收到(0,1,2,3,4), 保留2个最新元素, 所以s2第一个元素是3
        Thread.sleep(5000);
        connectableFlux.subscribe(e -> System.out.println("s2:" + e));

        TimeUnit.SECONDS.sleep(10);
    }
}
