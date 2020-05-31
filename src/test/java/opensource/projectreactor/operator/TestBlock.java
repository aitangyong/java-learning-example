package opensource.projectreactor.operator;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class TestBlock {

    @Test
    public void blockFirst() {
        // emptyFlux发出onComplete信号, blockFirst立刻返回null
        Integer empty = Flux.<Integer>empty()
                .doOnSubscribe(e -> System.out.println("doOnSubscribe"))
                .doOnComplete(() -> System.out.println("doOnComplete")).blockFirst();
        System.out.println(empty);

        // 异常信号, blockFirst抛异常
        try {
            Flux.error(new IOException("io-error")).blockFirst();
        } catch (RuntimeException e) {
            System.err.println(e.getCause().getMessage());
        }

        // 无限期等待,直到onNext或者onComplete信号
        Stopwatch stopwatch = Stopwatch.createStarted();
        Long v2 = Flux.interval(Duration.ofSeconds(1))
                      .doOnSubscribe(e -> System.out.println("doOnSubscribe"))
                      .doOnCancel(() -> System.out.println("doOnCancel")).blockFirst();
        System.out.println("cost=" + stopwatch.elapsed(TimeUnit.MILLISECONDS) + ",v2=" + v2);

        // 限时等待
        try {
            Flux.interval(Duration.ofSeconds(1)).blockFirst(Duration.ofMillis(200));
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    public void blockLast() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Long v2 = Flux.interval(Duration.ofSeconds(1)).take(3)
                      .doOnSubscribe(e -> System.out.println("doOnSubscribe"))
                      .doOnCancel(() -> System.out.println("doOnCancel")).blockLast();
        System.out.println("cost=" + stopwatch.elapsed(TimeUnit.MILLISECONDS) + ",v2=" + v2);
    }

    @Test
    public void toIterable() {
        List<Integer> list1 = Lists.newArrayList(
                Flux.just(1, 2, 3).doOnRequest(t -> System.out.println("doOnRequest..." + t)).toIterable()
        );
        System.out.println(list1);

        List<Integer> list2 = Lists.newArrayList(
                Flux.just(1, 2, 3).doOnRequest(t -> System.out.println("doOnRequest..." + t)).toIterable(1)
        );
        System.out.println(list2);
    }

    @Test
    public void toStream() {
        Stream<Integer> stream = Flux.just(1, 2, 3).doOnRequest(t -> System.out.println("doOnRequest..." + t)).toStream();
        stream.forEach(System.out::println);
    }
}
