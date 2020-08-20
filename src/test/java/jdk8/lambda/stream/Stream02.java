package jdk8.lambda.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Stream02 {

	public static void main(String[] args) {
		filter();
	}

	// filter
	// intermediate operation
	public static void filter() {
		// 可以多次调用filter,添加多个筛选条件
		Set<Integer> uneven = Stream.of(1, 2, 3, 4, 5).filter(e -> e % 2 == 1).filter(e -> e > 3)
				.collect(Collectors.toSet());
		System.out.println(uneven);// 5
	}

	// allMatch,anyMatch,noneMatch
	// short-circuiting terminal operation
	public static void match() {
		Predicate<Integer> matchFilter = e -> {
			System.out.println("Predicate..." + e);
			return e >= 3;
		};

		// 只遍历第一个元素就会知道结果
		System.out.println(Stream.of(1, 2, 3, 4, 5).allMatch(matchFilter));// false
		System.out.println();

		// 遍历到3的时候就会知道结果
		System.out.println(Stream.of(1, 2, 3, 4, 5).anyMatch(matchFilter));// true
		System.out.println();

		// 空的stream,filter根本不会被调用
		System.out.println(Stream.empty().anyMatch(o -> {
			System.out.println("not called");
			return true;
		}));
	}

	// max,min
	// terminal operation
	// min和 max的功能也可以通过对Stream元素先排序，再 findFirst来实现
	// max和min性能更好,为 O(n);而 sorted的成本是 O(nlogn)
	public static void maxMin() {
		List<Integer> datas = new ArrayList<>();
		int maxValue = 10000 * 10000;
		int size = 100 * 10000;
		Random seed = new Random();
		for (int i = 0; i < size; i++) {
			datas.add(seed.nextInt(maxValue));
		}

		long t1 = System.currentTimeMillis();
		Optional<Integer> actualMin1 = datas.stream().min(Integer::compare);
		long t2 = System.currentTimeMillis();

		long t3 = System.currentTimeMillis();
		Optional<Integer> actualMin2 = datas.stream().sorted().findFirst();
		long t4 = System.currentTimeMillis();

		// 54ms
		System.out.println("actualMin1=" + actualMin1.get() + ",time=" + (t2 - t1));
		// 388ms
		System.out.println("actualMin2=" + actualMin2.get() + ",time=" + (t4 - t3));
	}

	// count
	// terminal operation
	public static void count() {
		long count = Stream.of(1, 2, 3, 4, 5).filter(e -> e % 2 == 1).count();
		System.out.println(count == 3);
	}

}
