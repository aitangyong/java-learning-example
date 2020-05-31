package opensource.projectreactor.operator;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

public class TestBuffer {

    @Test
    public void bufferUseCount() {
        // Collect all incoming values into a single {@link List} buffer
        Flux.just(1, 2, 3, 4, 5).buffer().subscribe(System.out::println);

        // bufferWithMaxSize, 最终输出3个分组[1,2] [3,4] [5]
        Flux<List<Integer>> listFlux = Flux.just(1, 2, 3, 4, 5).buffer(2);
        listFlux.subscribe(System.out::println);

        // skip参数: 每隔step个数据开始进行buffer, 组装maxSize个数据后一起发给订阅者(skip感觉就是step)
        // maxSize < skip : dropping buffers
        // skip=3进行分组, 每组内取前maxSize=2个, 最终输出3个分组[1,2] [4,5] [7,8]
        Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9).buffer(2, 3).subscribe(System.out::println);

        // maxSize > skip : overlapping buffers
        // 最终输出[1,2,3] [3,4,5] [5,6,7] [7,8,9] [9]
        Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9).buffer(3, 2).subscribe(System.out::println);

        // maxSize == skip : exact buffers
        // 最终输出[1,2,3] [4,5,6] [7,8,9]
        Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9).buffer(3, 3).subscribe(System.out::println);

        // Collect incoming values into multiple user-defined {@link Collection} buffers
        // bufferSupplier返回一个Collection
        Flux<List<Integer>> supplierFlux = Flux.just(1, 2, 3, 4, 5).buffer(2, Lists::newArrayList);
        supplierFlux.subscribe(System.out::println);
    }

    @Test
    public void bufferUsePublisher() throws InterruptedException {
        // 每100ms产生一个数据触发onNext信号, 500ms后产生一个onComplete信号
        Flux<Long> longFlux = Flux.interval(Duration.ofMillis(100)).take(5)
                                  .doOnSubscribe(e -> System.out.println("doOnSubscribe"))
                                  .doOnNext(e -> System.out.println("doOnNext..." + e))
                                  .doOnComplete(() -> System.out.println("doOnComplete"));

        // 50ms的速率产生20个元素
        Flux.interval(Duration.ofMillis(50)).take(20).buffer(longFlux).subscribe(System.out::println);

        // 上面的interval是异步的, 给足够的时间让其执行完毕
        Thread.sleep(3000);
    }

    @Test
    public void bufferUseDuration() throws InterruptedException {
        // 5个 5个 4个的分组
        Flux.interval(Duration.ofMillis(20)).take(14).buffer(Duration.ofMillis(110)).subscribe(System.out::println);
        Thread.sleep(1000);
    }

    @Test
    public void bufferTimeout() {
        // each time the buffer reaches a maximum size OR the maxTime elapses
        // 需要使用Flux.generate这种编程式的数据产生器, 就可以比较好的看出效果
        // 比如第1s数据产生很快,那么是按照maxSize分组; 后面数据产生很慢, 就按照duration分组
        Flux.just(1, 2, 3, 4).bufferTimeout(2, Duration.ofSeconds(1)).subscribe(System.out::println);
    }

    @Test
    public void bufferUntil() {
        // the element that triggers the predicate to return true is included as last element in the emitted buffer.
        // On completion, if the latest buffer is non-empty and has not been closed it is emitted.
        // [1,2] [3,4] [5] 5虽然不满足predicate条件, 但是flux完成了, 所以单独输出1个分组[5]
        Flux.just(1, 2, 3, 4, 5).bufferUntil(e -> e % 2 == 0).subscribe(System.out::println);

        // However, such a "partial" buffer isn't emitted in case of onError termination
        // 4的时候流异常了, 所以最终只输出[1,2], 不会输出[3]
        Flux.just(1, 2, 3, 4, 5)
            .map(e -> {
                if (e == 4) {
                    throw new NullPointerException("4");
                }
                return e;
            })
            .bufferUntil(e -> e % 2 == 0).subscribe(System.out::println, System.err::println);

        // cutBefore=false效果等同于bufferUntil(predicate)
        Flux.range(1, 10).bufferUntil(i -> i % 2 == 0, false).subscribe(System.out::println);


        // set it to true to include the boundary element in the newly opened buffer
        // [1] [2,3] [4]
        Flux.just(1, 2, 3, 4).bufferUntil(i -> i % 2 == 0, true).subscribe(System.out::println);
    }

    @Test
    public void bufferWhile() {
        // Each buffer continues aggregating values while the
        // given predicate returns true, and a new buffer is created as soon as the
        // predicate returns false... Note that the element that triggers the predicate
        // to return false (and thus closes a buffer) is NOT included in any emitted buffer
        // [3] [6] [9]
        Flux.range(1, 10).bufferWhile(i -> i % 3 == 0).subscribe(System.out::println);

        // [1, 2] [4, 5] [7, 8] [10]
        Flux.range(1, 10).bufferWhile(i -> i % 3 != 0).subscribe(System.out::println);
    }

    @Test
    public void bufferUntilChanged() {
        // Collect subsequent repetitions of an element (that is, if they arrive right after one another
        // [1] [2] [3,3] [4] [3,3] [6] 将连续重复的元素收集在一起
        Flux.just(1, 2, 3, 3, 4, 3, 3, 6).bufferUntilChanged().subscribe(System.out::println);

        class ComplexVo {
            int id;
            int cat;

            public ComplexVo(int id, int cat) {
                this.id = id;
                this.cat = cat;
            }

            @Override
            public String toString() {
                return "ComplexVo{" +
                        "id=" + id +
                        ", cat=" + cat +
                        '}';
            }
        }

        // keySelector提取出用于比较的函数, 这里我们提取了cat作为判断标准, 那这3个元素都是一样的
        // 如果没有设置keySelector, 那么这3个元素都是不同的
        Flux.just(new ComplexVo(1, 1000), new ComplexVo(2, 1000), new ComplexVo(3, 1000))
            .bufferUntilChanged(e -> e.cat).subscribe(System.out::println);

        // Function.identity()虽然没有提取出对象特征, 但是通过设置keyComparator比较函数, 也可以达到同样效果
        Flux.just(new ComplexVo(1, 1000), new ComplexVo(2, 1000), new ComplexVo(3, 1000))
            .bufferUntilChanged(Function.identity(), (a, b) -> a.cat == b.cat).subscribe(System.out::println);
    }

    @Test
    public void bufferWhen() throws InterruptedException {
        // 每85ms产生1个onNext信号,打开新的缓冲区
        Publisher<String> bucketOpening = Flux.interval(Duration.ofMillis(85)).map(String::valueOf);
        Function<String, Publisher<Long>> closeSelector = input -> {
            System.out.println("closeSelector..." + input);
            int timeSpan = 100 * (1 + Integer.parseInt(input)) + 5;
            return Flux.interval(Duration.ofMillis(timeSpan));
        };

        // 每20ms产生1个元素,持续时间3s,总共可以产生150个元素0~149
        // bucketOpening第一个缓冲区开始是在85ms,所以元素0 1 2 3都丢失了
        Flux.interval(Duration.ofMillis(20)).take(Duration.ofSeconds(3))
            .bufferWhen(bucketOpening, closeSelector).subscribe(System.out::println);

        Thread.sleep(5000);
    }
}
