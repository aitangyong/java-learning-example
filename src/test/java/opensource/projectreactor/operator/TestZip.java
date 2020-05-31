package opensource.projectreactor.operator;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

import java.util.function.BiFunction;
import java.util.function.Function;


public class TestZip {

    @Test
    public void zipTuple2ToTuple8() {
        // wait for all the sources to emit one element and combine these elements once into a {@link Tuple2}
        Flux<Integer> publisher1 = Flux.just(1, 2, 3);
        Flux<Integer> publisher2 = Flux.just(4, 5, 6, 7);

        Flux<Tuple2<Integer, Integer>> combineFlux = Flux.zip(publisher1, publisher2);

        // 最终生成3对
        // 这里有一个木桶原则,即元素最少的"组",决定了最后输出的"组"个数
        combineFlux.subscribe(tuple2 -> {
            System.out.println("t1=" + tuple2.getT1() + ",t2=" + tuple2.getT2());
        });

        // zip系列方式可以支持reactor.util.function.Tuple2 ~ reactor.util.function.Tuple8
    }

    @Test
    public void zipTwoPublisherUseBiFunction() {
        Flux<Integer> publisher1 = Flux.just(1, 2, 3);
        Flux<Integer> publisher2 = Flux.just(4, 5, 6, 7);

        // 将2个元素转换成1个元素
        BiFunction<Integer, Integer, String> combinator = (t1, t2) -> String.valueOf(t1 + t2);
        Flux<String> combineFlux = Flux.zip(publisher1, publisher2, combinator);

        // 最终输出5 7 9
        combineFlux.subscribe(e -> System.out.println("e=" + e));
    }

    @Test
    public void zipIterablePublisherUseFunction() {
        Flux<Integer> publisher1 = Flux.just(10, 20, 30);
        Flux<Integer> publisher2 = Flux.just(40, 50, 60);
        Flux<Integer> publisher3 = Flux.just(70, 80, 90);
        Iterable<Flux<Integer>> sources = Lists.newArrayList(publisher1, publisher2, publisher3);

        Function<? super Object[], String> combinator = (items) -> Joiner.on("-").join(items);
        Flux<String> combineFlux = Flux.zip(sources, 10, combinator);
        combineFlux.subscribe(e -> System.out.println("e=" + e));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void zipVarArgsUseFunction() {
        Flux<Integer> publisher1 = Flux.just(10, 20, 30);
        Flux<Integer> publisher2 = Flux.just(40, 50, 60);
        Flux<Integer> publisher3 = Flux.just(70, 80, 90);
        Flux<Integer>[] sources = new Flux[3];
        sources[0] = publisher1;
        sources[1] = publisher2;
        sources[2] = publisher3;

        Function<? super Object[], String> combinator = (items) -> Joiner.on("-").join(items);

        Flux.zip(combinator, 10, sources).subscribe(e -> System.out.println("e=" + e));
    }

    @Test
    public void zipPublisherOfPublisher() {
        Flux<Integer> publisher1 = Flux.just(10, 20, 30);
        Flux<Integer> publisher2 = Flux.just(40, 50, 60);
        Flux<Integer> publisher3 = Flux.just(70, 80, 90);
        Flux<Flux<Integer>> sources = Flux.just(publisher1, publisher2, publisher3);

        Function<Tuple3<Integer, Integer, Integer>, String> combinator = (items) -> Joiner.on("-").join(items);
        Flux.zip(sources, combinator).subscribe(e -> System.out.println("e=" + e));
    }

    /**
     * Prefetch is a way to tune the initial request made on these inner sequences.
     * If unspecified, most of these operators start with a demand of 32.
     */
    @Test
    public void prefetch() {
        // log(), Observe all Reactive Streams signals
        Flux<Integer> publisher1 = Flux.range(1, 10).log();
        Flux<Integer> publisher2 = Flux.range(11, 10).log();
        Iterable<Flux<Integer>> sources = Lists.newArrayList(publisher1, publisher2);

        // 比较大的prefetch, 可以减少request(n)请求次数
        Function<? super Object[], String> combinator = (items) -> Joiner.on("-").join(items);
        Flux<String> combineFlux = Flux.zip(sources, 5, combinator);
        combineFlux.subscribe(e -> System.out.println("e=" + e));
    }

    @Test
    public void zipWithPublisherOrWithIterable() {
        // 2个publisher合并
        Flux<Integer> publisher1 = Flux.just(10, 20, 30);
        Flux<Integer> publisher2 = Flux.just(40, 50, 60);
        BiFunction<Integer, Integer, String> combinator = (t1, t2) -> String.valueOf(t1 + t2);
        Flux<String> combineFlux = publisher1.zipWith(publisher2, combinator);
        combineFlux.subscribe(e -> System.out.println("e=" + e));

        // publisher和iterable合并
        Flux<Integer> publisher3 = Flux.just(10, 20, 30);
        Iterable<Integer> publisher4 = Lists.newArrayList(40, 50, 60);
        Flux<String> combineFlux2 = publisher3.zipWithIterable(publisher4, combinator);
        combineFlux2.subscribe(e -> System.out.println("e=" + e));
    }
}
