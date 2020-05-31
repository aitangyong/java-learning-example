package opensource.projectreactor.operator;

import com.google.common.base.Joiner;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TestCombineLatest {

    /**
     * zip工作原理如下: 当每个传入zip的流都发射完毕第一次数据时, zip将这些数据合并为数组并发射出去;
     * 当这些流都发射完第二次数据时, zip再次将它们合并为数组并发射;
     * 以此类推直到其中某个流发出结束信号，整个被合并后的流结束，不再发射数据.
     * <p>
     * combineLatest与zip很相似: combineLatest一开始也会等待每个子流都发射完一次数据,但是在合并时,
     * 如果子流1在等待其他流发射数据期间又发射了新数据, 则使用子流最新发射的数据进行合并, 之后每当有某个流发射新数据,
     * 不再等待其他流同步发射数据, 而是使用其他流之前的最近一次数据进行合并
     */
    @Test
    public void zip() throws InterruptedException {
        Function<Object[], String> combinator = (items) -> Joiner.on("-").join(items);
        Flux<Long> flux1 = Flux.interval(Duration.ofMillis(20)).take(5);
        Flux<Long> flux2 = Flux.interval(Duration.ofMillis(55)).take(5);
        Flux.zip(combinator, flux1, flux2).subscribe(System.out::println);
        // 0-0 4-4总共5个压缩后的值
        Thread.sleep(1000);
    }

    /**
     * CombineLatest操作符行为类似于zip, 但是只有当原始的Observable中的每一个都发射了一条数据时zip才发射数据;
     * CombineLatest则在原始的Observable中任意一个发射了数据时发射一条数据;
     * 当原始Observables的任何一个发射了一条数据时, CombineLatest使用一个函数结合它们最近发射的数据, 然后发射这个函数的返回值
     */
    @Test
    public void combineLatest() throws InterruptedException {
        Function<Object[], String> combinator = (items) -> Joiner.on("-").join(items);
        Flux<String> flux1 = Flux.interval(Duration.ofMillis(20)).take(5).map(e -> "flux1." + e);
        Flux<String> flux2 = Flux.interval(Duration.ofMillis(55)).take(5).map(e -> "flux2." + e);
        // flux2产生第一个元素0的时候, flux1产生了元素0和1, 由于combineLatest合并最新的, 所以flux1的元素0丢弃了
        // flux2产生第二个元素1的时候, flux1所有元素都产生了, 对flux1的1~4元素来说, flux2的最新元素是0
        // 对flux2的1~4元素来说, flux1的最新元素都是4

        Flux.combineLatest(combinator, flux1, flux2).subscribe(System.out::println);
        Thread.sleep(1000);
    }

    /**
     * The operator will drop values from this {Flux} until the other {Publisher} produces any value.
     * The operator will drop values from this {Flux} until the other {Publisher} produces any value.
     */
    @Test
    public void withLatestFrom() throws InterruptedException {
        BiFunction<String, String, String> combinator = (a, b) -> a + "-" + b;
        Flux<String> flux1 = Flux.interval(Duration.ofMillis(20)).take(5).map(e -> "flux1." + e);
        Flux<String> flux2 = Flux.interval(Duration.ofMillis(30)).take(5).map(e -> "flux2." + e);
        flux1.withLatestFrom(flux2, combinator).subscribe(System.out::println);
        Thread.sleep(1000);
    }
}
