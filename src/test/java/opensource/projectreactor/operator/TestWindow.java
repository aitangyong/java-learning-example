package opensource.projectreactor.operator;

import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * window操作符的作用类似于buffer,所不同的是window操作符是把当前流中的元素收集到另外的Flux序列中,因此返回值类型是 Flux>
 */
public class TestWindow {

    @Test
    public void buffer() {
        UnicastProcessor<String> hotSource = UnicastProcessor.create();
        Flux<String> hotFlux = hotSource.publish().autoConnect().onBackpressureBuffer(10);

        CompletableFuture<Void> future = CompletableFuture.runAsync(
                () -> IntStream.range(0, 50).forEach(value -> hotSource.onNext("value is " + value))
        );

        hotFlux.buffer(5).subscribe(new BaseSubscriber<>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(20);
            }

            @Override
            protected void hookOnNext(List<String> value) {
                System.out.println("get value " + value);

            }
        });
        future.thenRun(hotSource::onComplete);
        future.join();
    }

    @Test
    public void window() {
        UnicastProcessor<String> hotSource = UnicastProcessor.create();
        Flux<String> hotFlux = hotSource.publish().autoConnect().onBackpressureBuffer(10);

        CompletableFuture<Void> future = CompletableFuture.runAsync(
                () -> IntStream.range(0, 50).forEach(value -> hotSource.onNext("value is " + value))
        );

        hotFlux.window(5).subscribe(new BaseSubscriber<>() {
            int windowIndex = 0;
            int elementIndex = 0;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(20);
            }

            @Override
            protected void hookOnNext(Flux<String> value) {
                value.subscribe(new BaseSubscriber<>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        System.out.println(String.format("Start window %d", windowIndex));
                        requestUnbounded();
                    }

                    @Override
                    protected void hookOnNext(String value) {
                        System.out.println(String.format("Element %d is %s", elementIndex, value));
                        elementIndex++;
                    }

                    @Override
                    protected void hookOnComplete() {
                        System.out.println(String.format("Finish window %d", windowIndex));
                        windowIndex++;
                        elementIndex = 0;
                    }
                });
            }
        });
        future.thenRun(hotSource::onComplete);
        future.join();
    }

}
