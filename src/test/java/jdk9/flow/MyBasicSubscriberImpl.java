package jdk9.flow;

import java.util.concurrent.Flow;

class MyBasicSubscriberImpl<T> implements Flow.Subscriber<T> {
    // 订阅者的id
    private final String id;

    // 订阅者需要处理的元素个数,当达到这个值后订阅者主动取消订阅
    private final int maxElements;

    // 每个元素的处理耗时
    private final long costMills;

    private Flow.Subscription subscription;

    // 已经处理的元素个数
    private int alreadyProcessed = 0;

    /**
     * 只建立订阅关系,但是不消费数据
     */
    public static <T> MyBasicSubscriberImpl<T> emptySubscriber() {
        return new MyBasicSubscriberImpl<>("empty", 0, 0);
    }

    MyBasicSubscriberImpl(String id, int maxElements, int costMills) {
        this.id = id;
        this.maxElements = Math.max(maxElements, 0);
        this.costMills = Math.max(costMills, 0);
    }

    /**
     * 需要在subscriber内部保存这个subscription实例,因为后面会需要通过它向publisher发送信号来完成,请求更多数据或者取消订阅
     */
    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        System.out.println(Thread.currentThread().getName() + "..." + id + "...onSubscribe");
        this.subscription = subscription;
        // 建立订阅关系后,必须调用request(n)向生产者请求数据,否则会导致生产者为其维护的buffer变满,导致生产者阻塞
        if (alreadyProcessed < maxElements) {
            subscription.request(1);
        }
    }

    @Override
    public void onNext(T item) {
        System.out.println(Thread.currentThread().getName() + "..." + id + "...onNext..." + item);
        processItem(item);
        if (alreadyProcessed < maxElements) {
            subscription.request(1);
        } else {
            subscription.cancel();
        }
    }

    private void processItem(T item) {
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        while (end - start < costMills) {
            end = System.currentTimeMillis();
        }
        alreadyProcessed++;
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println(Thread.currentThread().getName() + "..." + id + "...onError..." + throwable.getMessage());
    }

    @Override
    public void onComplete() {
        System.out.println(Thread.currentThread().getName() + "..." + id + "...onComplete");
    }
}
