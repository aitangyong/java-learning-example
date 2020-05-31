package opensource.projectreactor.operator;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.util.concurrent.Queues;

import java.time.Duration;
import java.util.Random;

public class TestMerge {

    /**
     * concat和merge的区别: concat是顺序的, merge则是按照元素的实际产生顺序
     */
    @Test
    public void merge() {
        Flux<String> flux1 = Flux.interval(Duration.ofMillis(100)).take(5).map(e -> "flux1..." + e);
        Flux<String> flux2 = Flux.interval(Duration.ofMillis(100)).take(5).map(e -> "flux2..." + e);

        // 如果是concat则先输出flux1然后输出flux2;如果是merge则flux1和flux2交替输出
        Flux.merge(flux1, flux2).toStream().forEach(System.out::println);
    }

    @Test
    public void mergeAtConcurrency() {
        Flux<String> flux1 = Flux.interval(Duration.ofMillis(100)).take(5).map(e -> "flux1..." + e);
        Flux<String> flux2 = Flux.interval(Duration.ofMillis(100)).take(5).map(e -> "flux2..." + e);
        Flux<Flux<String>> sources = Flux.just(flux1, flux2);

        // inner sources are subscribed to eagerly (but at most {concurrency} sources are subscribed to at the same time
        int concurrency = 1;

        // concurrency=1意味着串行效果等于concat;concurrency>1则flux1和flux2交替
        Flux.merge(sources, concurrency, Queues.XS_BUFFER_SIZE).toStream().forEach(System.out::println);
    }

    @Test
    public void mergeSequential() {
        Flux<String> flux1 = Flux.interval(Duration.ofMillis(100)).take(5).map(e -> "flux1..." + e)
                                 .doOnSubscribe(e -> System.out.println("flux1...doOnSubscribe"));
        Flux<String> flux2 = Flux.interval(Duration.ofMillis(100)).take(5).map(e -> "flux2..." + e)
                                 .doOnSubscribe(e -> System.out.println("flux2...doOnSubscribe"));
        // mergeSequential直接订阅所有的flux
        // concat是处理完1个flux,再订阅下一个flux
        Flux.mergeSequential(flux1, flux2).toStream().forEach(System.out::println);
    }

    @Test
    public void mergeOrdered() {
        int numbers = 1;
        Flux<Integer> flux1 = Flux.interval(Duration.ofMillis(100)).take(numbers).map(e -> {
            int value = new Random().nextInt(10000);
            System.out.println("flux1[" + e + "]=" + value);
            return value;
        });
        Flux<Integer> flux2 = Flux.interval(Duration.ofMillis(100)).take(numbers).map(e -> {
            int value = new Random().nextInt(10000);
            System.out.println("flux2[" + e + "]=" + value);
            return value;
        });
        Flux<Integer> flux3 = Flux.interval(Duration.ofMillis(100)).take(numbers).map(e -> {
            int value = new Random().nextInt(10000);
            System.out.println("flux3[" + e + "]=" + value);
            return value;
        });

        // this operator considers only one value from each source and picks the smallest of all these values
        Flux.mergeOrdered(flux1, flux2, flux3).toStream().forEach(System.out::println);
    }
}
