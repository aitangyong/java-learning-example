package opensource.projectreactor.operator;

import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class TestTransform {

    @Test
    public void transform() {
        // The transform operator lets you encapsulate a piece of an operator chain into a function
        Function<Flux<String>, Flux<String>> filterAndMap = f -> f.filter(color -> !color.equals("orange"))
                                                                  .map(String::toUpperCase);
        Flux.fromIterable(Arrays.asList("blue", "green", "orange", "purple"))
            .doOnNext(System.out::println)
            .transform(filterAndMap)
            .subscribe(d -> System.out.println("Subscriber to Transformed MapAndFilter: " + d));
    }

    @Test
    public void transformDeferred() {
        AtomicInteger ai = new AtomicInteger();

        Function<Flux<String>, Flux<String>> filterAndMap = f -> {
            if (ai.incrementAndGet() == 1) {
                return f.filter(color -> !color.equals("orange"))
                        .map(String::toUpperCase);
            } else {
                return f.filter(color -> !color.equals("purple"))
                        .map(String::toUpperCase);
            }
        };

        // the function can actually produce a different operator chain for each subscription (by maintaining some state).
        Flux<String> composedFlux =
                Flux.fromIterable(Arrays.asList("blue", "green", "orange", "purple"))
                    .doOnNext(System.out::println)
                    .transformDeferred(filterAndMap);
        composedFlux.subscribe(d -> System.out.println("Subscriber 1 to Composed MapAndFilter :" + d));
        composedFlux.subscribe(d -> System.out.println("Subscriber 2 to Composed MapAndFilter: " + d));
    }
}
