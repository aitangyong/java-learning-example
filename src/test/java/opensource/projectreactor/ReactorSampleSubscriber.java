package opensource.projectreactor;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.SignalType;

import java.util.concurrent.atomic.AtomicInteger;

public class ReactorSampleSubscriber<T> extends BaseSubscriber<T> {

    // 订阅者的id
    private final String id;

    // 订阅者需要处理的元素个数,当达到这个值后订阅者主动取消订阅
    private final int maxElements;

    // 每个元素的处理耗时
    private final long costMills;

    // 已经处理的元素个数
    private AtomicInteger alreadyProcessed = new AtomicInteger(0);

    public ReactorSampleSubscriber(String id, int maxElements, int costMills) {
        this.id = id;
        this.maxElements = Math.max(maxElements, 0);
        this.costMills = Math.max(costMills, 0);
    }

    public void hookOnSubscribe(Subscription subscription) {
        System.out.println(Thread.currentThread().getName() + "..." + id + "...hookOnSubscribe..." + subscription.getClass());
        if (alreadyProcessed.get() < maxElements) {
            this.request(1);
        }
    }

    public void hookOnNext(T value) {
        System.out.println(Thread.currentThread().getName() + "..." + id + "...hookOnNext..." + value);
        processItem(value);
        if (alreadyProcessed.get() < maxElements) {
            this.request(1);
        } else {
            this.cancel();
        }
    }

    private void processItem(T value) {
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        while (end - start < costMills) {
            end = System.currentTimeMillis();
        }
        alreadyProcessed.incrementAndGet();
    }

    @Override
    protected void hookOnComplete() {
        System.out.println(Thread.currentThread().getName() + "..." + id + "...hookOnComplete...");
    }

    @Override
    protected void hookOnError(Throwable throwable) {
        System.err.println(Thread.currentThread().getName() + "..." + id + "...hookOnError..." + throwable.getMessage());
    }

    @Override
    protected void hookOnCancel() {
        System.out.println(Thread.currentThread().getName() + "..." + id + "...hookOnCancel...");
    }

    // Optional hook executed after any of the termination events (onError, onComplete, cancel)
    @Override
    protected void hookFinally(SignalType type) {
        System.out.println(Thread.currentThread().getName() + "..." + id + "...hookFinally..." + type);
    }

}