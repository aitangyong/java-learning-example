package opensource.projectreactor.operator;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class TestParallelFlux {

    @Test
    public void parallel() {
        // To obtain a ParallelFlux, you can use the parallel() operator on any Flux. By itself, this method does
        // not parallelize the work. Rather, it divides the workload into “rails”
        // (by default, as many rails as there are CPU cores).
        // 没有指定runOn,虽然指定了parallel()实际上还是串行
        Flux.range(1, 10)
            .parallel(2)
            .subscribe(i -> System.out.println(Thread.currentThread().getName() + " -> " + i));

        Flux.range(1, 10)
            .parallel(5)
            .runOn(Schedulers.newParallel("cas", 3))
            .subscribe(i -> System.out.println(Thread.currentThread().getName() + " -> " + i));
    }
}
