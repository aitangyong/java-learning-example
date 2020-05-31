package opensource.projectreactor.operator;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class TestCollect {

    @Test
    public void collectList() {
        Mono<List<Integer>> mono = Flux.just(1, 2, 3).collectList();
        mono.subscribe(System.out::println);
    }

    @Test
    public void collectSortedList() {
        Mono<List<Integer>> mono = Flux.just(3, 1, 2)
                                       .collectSortedList(Comparator.comparing(Function.<Integer>identity()).reversed());
        mono.subscribe(System.out::println);
    }

    @Test
    public void collector() {
        //  Collectors.averagingInt()
        Supplier<int[]> supplier = () -> new int[2];
        BiConsumer<int[], String> accumulator = (a, t) -> {
            a[0] += Integer.parseInt(t);
            a[1]++;
        };
        BinaryOperator<int[]> combiner = (a, b) -> {
            a[0] += b[0];
            a[1] += b[1];
            return a;
        };
        Function<int[], Double> finisher = a -> (a[1] == 0) ? 0.0d : (double) a[0] / a[1];

        Collector<String, ?, Double> collector = Collector.of(supplier, accumulator, combiner, finisher, Collector.Characteristics.UNORDERED);
        Mono<Double> mono = Flux.just("1", "2", "3").collect(collector);
        mono.subscribe(System.out::println);

        // collect all emitted elements into a user-defined container
        // by applying a collector {@link BiConsumer} taking the container and each element
        Supplier<List<Integer>> containerSupplier = ArrayList::new;
        BiConsumer<List<Integer>, String> talkCollector = (container, element) -> {
            container.add(Integer.parseInt(element));
        };
        Flux.just("1", "2", "3").collect(containerSupplier, talkCollector).subscribe(System.out::println);
    }

    @Test
    public void collectMap() {
        class Student {
            int id;
            String name;

            public Student(int id, String name) {
                this.id = id;
                this.name = name;
            }
        }

        // key-value
        Mono<Map<Integer, String>> mapMono = Flux.just(new Student(1, "a"), new Student(2, "b"))
                                                 .collectMap(e -> e.id, e -> e.name);
        mapMono.subscribe(System.out::println);

        // key-values
        Mono<Map<Integer, Collection<String>>> multiMono = Flux.just(new Student(1, "a"), new Student(1, "b"))
                                                               .collectMultimap(e -> e.id, e -> e.name);
        multiMono.subscribe(System.out::println);
    }
}
