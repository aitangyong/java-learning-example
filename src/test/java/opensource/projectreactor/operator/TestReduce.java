package opensource.projectreactor.operator;

import org.junit.Test;
import reactor.core.publisher.Flux;

public class TestReduce {

    /**
     * reduce和reduceWith操作符对流中包含的所有元素进行累积操作,得到一个包含计算结果的Mono序列
     */
    @Test
    public void reduce() {
        // 没有设置初始值, 累加所有元素结果是15
        Flux.range(1, 5).reduce(Integer::sum).subscribe(System.out::println);

        // 设置初始值10, 累加所有元素结果是25
        Flux.range(1, 5).reduce(10, Integer::sum).subscribe(System.out::println);

        // initial seed有点懒加载的味道
        Flux.range(1, 5).reduceWith(() -> 1, Integer::sum).subscribe(System.out::println);
    }
}
