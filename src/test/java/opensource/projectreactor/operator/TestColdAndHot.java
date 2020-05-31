package opensource.projectreactor.operator;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

import java.util.Arrays;

/**
 * cold: They generate data anew for each subscription. If no subscription is created, data never gets generated.
 * <p>
 * Hot publishers, on the other hand, do not depend on any number of subscribers.
 * They might start publishing data right away and would continue doing so
 * whenever a new Subscriber comes in (in which case, the subscriber would see only new
 * elements emitted after it subscribed).
 */
public class TestColdAndHot {
    @Test
    public void testCodeSequence() {

        Flux<String> source = Flux.fromIterable(Arrays.asList("blue", "green", "orange", "purple"))
                                  .map(String::toUpperCase);

        source.subscribe(d -> System.out.println("Subscriber 1: " + d));
        System.out.println();
        source.subscribe(d -> System.out.println("Subscriber 2: " + d));
    }


    @Test
    public void testHotSequence() {
        // UnicastProcessor是一个热发布者
        UnicastProcessor<String> hotSource = UnicastProcessor.create();
        Flux<String> hotFlux = hotSource.publish()
                                        .autoConnect()
                                        .map(String::toUpperCase);

        hotFlux.subscribe(d -> System.out.println("Subscriber 1 to Hot Source: " + d));

        hotSource.onNext("blue");
        hotSource.onNext("green");

        hotFlux.subscribe(d -> System.out.println("Subscriber 2 to Hot Source: " + d));

        hotSource.onNext("orange");
        hotSource.onNext("purple");
        hotSource.onComplete();
    }
}
