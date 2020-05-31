package opensource.projectreactor;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

public class TestDemo {

    private static class RemoteResource {
        private int scale;

        RemoteResource() {
            this(1);
        }

        RemoteResource(int scale) {
            this.scale = scale;
            System.out.println("RemoteResource..." + scale);
        }

        Integer[] remoteGet() {
            System.out.println("remoteGet..." + scale);
            return new Integer[]{scale, 2 * scale, 3 * scale};
        }

        void close() {
            System.out.println("close..." + scale);
//            throw new NullPointerException("fake..." + scale);
        }
    }

    @Test
    public void using() {
        // Operators.onErrorDropped()
        Hooks.onErrorDropped(e -> System.err.println("hook..." + e.getMessage()));

        Callable<RemoteResource> resourceSupplier = RemoteResource::new;
        Function<RemoteResource, Flux<Integer>> sourceSupplier = resource -> Flux.just(resource.remoteGet());
        Consumer<RemoteResource> resourceCleanup = RemoteResource::close;

        // eager=false, doOnComplete会被触发执行, 会触发onErrorDropped
        // eager=true, doOnError会被触发执行, 不会触发onErrorDropped
        Flux.using(resourceSupplier, sourceSupplier, resourceCleanup, false)
            .doOnSubscribe(e -> System.out.println("doOnSubscribe"))
            .doOnComplete(() -> System.out.println("doOnComplete"))
            .doOnError(e -> System.out.println("doOnError..." + e.getMessage()))
            .subscribe(System.out::println, System.err::println);
    }

    @Test
    public void usingWhen() {
        Flux<RemoteResource> resourceSupplier = Flux.just(new RemoteResource(1),
                new RemoteResource(10));
        Function<RemoteResource, Flux<String>> resourceClosure = resource ->
                Flux.just(resource.remoteGet()).map(e -> "-" + e);
        Function<RemoteResource, Flux<Void>> asyncCleanup = resource -> Flux.<Void>empty()
               .doOnComplete(() -> System.out.println("asyncCleanup-doOnComplete"))
               .doOnError(e -> System.out.println("asyncCleanup-doOnComplete"));

        Flux.usingWhen(resourceSupplier, resourceClosure, asyncCleanup)
            .doOnSubscribe(e -> System.out.println("doOnSubscribe"))
            .doOnComplete(() -> System.out.println("doOnComplete"))
            .doOnError(e -> System.out.println("doOnError..." + e.getMessage()))
            .subscribe(System.out::println, System.err::println);
    }
}
