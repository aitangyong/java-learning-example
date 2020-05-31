package opensource.projectreactor.operator;

import com.google.common.collect.Lists;
import org.junit.Test;
import reactor.core.publisher.Flux;


public class TestConcat {

    @Test
    public void concat() {
        // Concatenation is achieved by sequentially subscribing to the first source then
        // waiting for it to complete before subscribing to the next, and so on until the
        // last source completes.
        // concat是在前一个流完成后再连接新的流
        Flux.concat(Flux.just(1, 2, 3), Flux.just(4, 5, 6)).subscribe(System.out::println);

        // Any error interrupts the sequence immediately and is forwarded downstream.
        // 40不会打印, 遇到异常concat就会中断
        Flux.concat(Flux.just(10), Flux.error(new NullPointerException()), Flux.just(40))
            .subscribe(System.out::println, System.err::println);
    }

    @Test
    public void concat2() {
        // Publisher<? extends Publisher<? extends T>> sources
        Flux<Flux<Integer>> intFlux = Flux.just(0, 1, 2, 3).window(2);
        Flux.concat(intFlux).subscribe(System.out::println);
    }

    @Test
    public void concatDelayError() {
        // Errors do not interrupt the main sequence but are propagated
        // after the rest of the sources have had a chance to be concatenated.
        // 打印10和40和Exception, 到错误不提前拦截, 而是等到最后发布的事件处理完成后
        Flux<Integer> flux1 = Flux.just(10);
        Flux<Integer> flux2 = Flux.error(new NullPointerException());
        Flux<Integer> flux3 = Flux.just(40);
        Flux.concatDelayError(flux1, flux2, flux3)
            .subscribe(System.out::println, System.err::println);

        // delayUntilEnd=true延迟异常, false则立刻借结束
        Flux.concatDelayError(Flux.just(flux1, flux2, flux3), false, 1)
            .subscribe(System.out::println, System.err::println);
    }

    @Test
    public void concatWith() {
        Flux.just(10).concatWith(Flux.just(20)).subscribe(System.out::println);
        Flux.just(1).concatWithValues(2).subscribe(System.out::println);
    }

    @Test
    public void concatMap() {
        // 每一个integer都对应一个Publisher<String>(一个数据变多个数据)
        Flux.just(0, 1, 2).concatMap(order -> {
            String[] parts = {order + "a", order + "b"};
            return Flux.just(parts);
        }).subscribe(System.out::println);

        // 每一个integer都对应一个Iterable<String>(一个数据变多个数据)
        Flux.just(0, 1, 2).concatMapIterable(order -> {
            String[] parts = {order + "a", order + "b"};
            return Lists.newArrayList(parts);
        }).subscribe(System.out::println);
    }

    @Test
    public void concatMapDelayError() {
        // delayUntilEnd设置成false,遇到异常后立刻终止
        Flux.just(0, 1, 2).concatMapDelayError(order -> {
            String[] parts = {order + "a", order + "b"};
            return order == 1 ? Flux.error(new NullPointerException()) : Flux.just(parts);
        }, false, 1).subscribe(System.out::println, System.err::println);
    }
}
