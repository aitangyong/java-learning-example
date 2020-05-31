package jdk9.flow;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class TestFlow {

    @Test
    public void bufferSize() {
        // 生产者为每个订阅者维护的缓冲区大小
        int maxBufferCapacity = 10;
        SubmissionPublisher<Integer> publisher =
                new SubmissionPublisher<>(Executors.newFixedThreadPool(3), maxBufferCapacity);

        // buffer的最大容量(如果不是2的n次幂,jdk会自动调整),这里设置10实际就是16
        Assert.assertEquals(16, publisher.getMaxBufferCapacity());

        // 建立订阅关系,使用一个不消费任何元素的订阅者
        publisher.subscribe(MyBasicSubscriberImpl.emptySubscriber());

        for (int i = 1; i <= publisher.getMaxBufferCapacity() + 1; i++) {
            // submit是个block方法,当订阅者的buffer满的时候会阻塞
            // 订阅者调用request(n)的时候,生产者会从buffer中取出n个元素,并调用订阅者的onNext
            publisher.submit(i);
            System.out.println("生成数据:" + i);
        }

        // 结束后关闭发布者
        publisher.close();
    }

    /**
     * https://github.com/reactive-streams/reactive-streams-jvm
     * https://stackoverflow.com/questions/59555464/how-does-subscription-requestn-in-flow-api-perform-backpressure-at-any-n-value
     * <pre>
     * 1.subscriber可以多次调用request(n),这些n的值会累加到demands;publisher每次调用onNext,demand会减一
     *
     * Publishers cannot signal more elements than Subscribers have requested.A Publisher cannot guarantee that
     * it will be able to produce the number of elements requested;it simply might not be able to produce them all;
     * it may be in a failed state; it may be empty or otherwise already completed.
     *
     * A Subscriber MUST signal demand via Subscription.request(long n) to receive onNext signals. it is the responsibility
     *
     * The n in request indicates how many elements the subscriber can accept and gives a limit on how many items the
     * upstream Publisher can emit. Therefore, the slowing down of this generator is not per individual item but the average
     * time for each batch generated interleaved by the consumer's processing time.
     *
     * Invocations of onNext() are not supposed to run in parallel. They can run from different threads (depends on implementation),
     * but always sequentially. But even sequentially, they can be called with higher rate than the subscriber can handle.
     * So subscriber calls request(n) only when it has room for n incoming items. Usually it has room for only one value,
     * so it calls request(1) when this variable is free.
     *
     * </pre>
     */
    @Test
    public void request() throws Exception {
        // 生产者生产的元素数量
        int elementsToProduce = 10;

        // 这里将maxElements设置成elementsToProduce+1,是为了避免订阅者主动取消订阅
        MyBasicSubscriberImpl<Integer> subscriber = new MyBasicSubscriberImpl<>("t", elementsToProduce + 1, 1);

        SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>();
        publisher.subscribe(subscriber);// subscribe触发订阅者的onSubscribe

        // 严格保证顺序,先生产的先被消费
        for (int i = 1; i <= elementsToProduce; i++) {
            publisher.submit(i);// submit触发订阅者的onNext
            System.out.println("生成数据:" + i);
        }

        // 触发订阅者的onComplete
        publisher.close();

        // 触发订阅者的onError
        // publisher.closeExceptionally(new NullPointerException("tired"));

        // 保证订阅者有足够的时间处理完任务,否则进程退出的话订阅者来不及处理任务
        Thread.sleep(3000);
    }

    /**
     * 监控队列堆积情况(所有消费者中延迟最大的那个),生产速度快而消费速度慢的情况下有用
     */
    @Test
    public void estimateMaximumLag() throws Exception {
        int elementsToProduce = 10;
        SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>();
        MyBasicSubscriberImpl<Integer> subscriber1 = new MyBasicSubscriberImpl<>("t1", elementsToProduce, 1000);
        MyBasicSubscriberImpl<Integer> subscriber2 = new MyBasicSubscriberImpl<>("t2", elementsToProduce, 2000);
        publisher.subscribe(subscriber1);
        publisher.subscribe(subscriber2);
        for (int i = 1; i <= elementsToProduce; i++) {
            publisher.submit(i);
        }

        // 保证订阅者有足够的时间处理完任务,t1可以处理3个元素,t2只能处理2个元素
        Thread.sleep(3000);

        // subscriber1每个任务耗时1s, subscriber2每个任务耗时2s
        // 经过1s的等待时间后,subscriber1处理了3个任务(延迟7),subscriber2处理了1个任务(延迟9)
        // 最大延迟就是9
        System.out.println(publisher.estimateMaximumLag());
    }

    /**
     * 生产速度慢而消费速度快的情况下有用 Returns an estimate of the minimum number of items requested
     * via request(n) , but not yet produced, among all current subscribers.
     */
    @Test
    public void estimateMinimumDemand() throws Exception {
        SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>();
        Flow.Subscriber<Integer> subscriber1 = new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(10);
            }

            @Override
            public void onNext(Integer item) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        };
        Flow.Subscriber<Integer> subscriber2 = new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(20);
            }

            @Override
            public void onNext(Integer item) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        };
        publisher.subscribe(subscriber1);
        publisher.subscribe(subscriber2);
        // 保证订阅者的onSubscribe有时间执行完成(发出request(n)请求)
        Thread.sleep(100);

        // subscriber1请求10个数据,subscriber2请求20个数据,但是此时生产者没有生产数据,所以这里返回10
        System.out.println(publisher.estimateMinimumDemand());

        // 产生2个数据
        publisher.submit(1);
        publisher.submit(2);

        // 保证订阅者有足够的时间处理完任务
        Thread.sleep(100);

        // 返回8,因为生产者已经投递了2个数据
        System.out.println(publisher.estimateMinimumDemand());
    }

    /**
     * 如果consumer内部处理逻辑异常,则后续submit数据不会再出发consumer的调用,而且返回的completableFuture被设置成异常完成
     * <p>
     * publisher发布完成信号或者异常信号的时候,completableFuture被设置成正常或者异常完成
     */
    @Test
    public void consume() throws Exception {
        SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>();

        Consumer<Integer> consumer = t -> {
            System.out.println("consumer..." + t);
            // throw new NullPointerException();
        };
        CompletableFuture<Void> completableFuture = publisher.consume(consumer);
        completableFuture.whenComplete((object, throwable) -> System.out.println("completed normally or exceptionally object=" + object + ",throwable=" + throwable));

        publisher.submit(1); // 触发consumer调用
        publisher.submit(1); // 触发consumer调用
        publisher.close(); // 将completableFuture设置为正常完成
        completableFuture.join();
        Thread.sleep(1000);// 防止进程退出,内存中回调得不到执行
    }

    /**
     * submit的阻塞是不可中断的,而offer的限时阻塞是可以被中断的
     */
    @Test
    public void blockingUnInterrupt() {
        // 生产者已经生产的元素个数
        AtomicInteger alreadyProduceCounter = new AtomicInteger(0);
        Thread targetThread = Thread.currentThread();

        new Thread(() -> {
            while (true) {
                if (alreadyProduceCounter.get() == Flow.defaultBufferSize()) {
                    targetThread.interrupt();
                    break;
                }
            }
            System.out.println("targetThread is interrupted");
        }).start();

        SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>();
        publisher.subscribe(MyBasicSubscriberImpl.emptySubscriber());
        for (int i = 1; i <= Flow.defaultBufferSize() + 1; i++) {
            // 当订阅者缓冲区满而陷入阻塞的时候,不能通过中断线程来唤醒它
            publisher.submit(i);
            alreadyProduceCounter.incrementAndGet();
        }
        System.out.println("execute over..." + targetThread.getName());
    }

    /**
     * 发布者和订阅者是1对多的关系,任意一个订阅者的缓冲区满都会导致发布者阻塞(无法继续生产),这样别的订阅者也无法继续消费
     * <p>
     * submit返回当前预估的最大延迟(可以根据这个值适当降低生产速度),避免缓冲区满而阻塞
     */
    @Test
    public void blockingAnySubscriber() {
        ExecutorService executorService = new ThreadPoolExecutor(2, 2, 60,
                TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadFactoryBuilder().setNameFormat("flow-pool-%d").build());
        SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>(executorService, 16);

        // subscriber1不消费
        MyBasicSubscriberImpl<Integer> subscriber1 = MyBasicSubscriberImpl.emptySubscriber();
        // subscriber2消费速度比较快
        MyBasicSubscriberImpl<Integer> subscriber2 = new MyBasicSubscriberImpl<>("s2", 100, 0);
        publisher.subscribe(subscriber1);
        publisher.subscribe(subscriber2);

        // s1会导致发布者阻塞,所以s2也最多只能消费到16个数据
        for (int i = 1; i <= 18; i++) {
            int lag = publisher.submit(i);
            System.out.println("生产数据..." + i + ",当前最大延迟..." + lag);
        }
    }

    /**
     * 和submit方法不同之处在于,缓冲区满的时候submit会一直阻塞,直到有空闲的缓冲区,阻塞期间不响应线程中断
     * <p>
     * offer(timeout)在缓冲区满的时候会阻塞一段时间timeout,阻塞期间可以响应中断
     */
    @Test
    public void offer() throws Exception {
        ExecutorService executorService = new ThreadPoolExecutor(2, 2, 60,
                TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadFactoryBuilder().setNameFormat("flow-pool-%d").build());
        SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>(executorService, 8);
        publisher.subscribe(new MyBasicSubscriberImpl<>("s1", 100, 10));

        // 如果返回true,则会重试一次生产(将元素退给订阅者);如果返回false则丢弃后不再重试
        BiPredicate<Flow.Subscriber<? super Integer>, ? super Integer> onDrop = (subscriber, item) -> {
            System.out.println(Thread.currentThread().isInterrupted() + "...onDrop..." + item);
            return false;
        };

        // 订阅者每10ms处理完一个任务,如果timeout=4则元素9会丢弃;如果time=20则元素9不会丢弃
        for (int i = 1; i <= 9; i++) {
            publisher.offer(i, 4, TimeUnit.MILLISECONDS, onDrop);
        }

        // 等待任务执行完毕
        Thread.sleep(3000);
    }
}
