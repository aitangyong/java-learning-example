package jdk8.lambda.stream.ordering;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * For sequential streams, the presence or absence of an encounter order does
 * not affect performance, only determinism. If a stream is ordered, repeated
 * execution of identical stream pipelines on an identical source will produce
 * an identical result; if it is not ordered, repeated execution might produce
 * different results.
 * 
 * For parallel streams, relaxing the ordering constraint can sometimes enable
 * more efficient execution. Certain aggregate operations, such as filtering
 * duplicates (distinct()) or grouped reductions (Collectors.groupingBy()) can
 * be implemented more efficiently if ordering of elements is not
 * relevant(相关的,有重大意义的). Similarly, operations that are intrinsically(本质上) tied
 * to encounter order, such as limit(), may require buffering to ensure proper
 * ordering, undermining(暗中破坏) the benefit of parallelism. In cases where the
 * stream has an encounter order, but the user does not particularly care about
 * that encounter order, explicitly de-ordering the stream with unordered() may
 * improve parallel performance for some stateful or terminal operations.
 * However, most stream pipelines, such as the "sum of weight of blocks" example
 * above, still parallelize efficiently even under ordering constraints.
 *
 */
public class Ordering5_performance {
	public static void main(String[] args) {
		t1(); // 4058
		t2();// 1002
	}

	private static void t2() {
		Set<Integer> intSet = new LinkedHashSet<>(Arrays.asList(3, 4, 1, 2, 8, 7, 5, 6));

		long start = System.currentTimeMillis();
		intSet.parallelStream().sorted().forEach(a -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.print(a);
		});
		long end = System.currentTimeMillis();
		System.out.println();
		System.out.println("cost time=" + (end - start));
	}

	private static void t1() {
		Set<Integer> intSet = new LinkedHashSet<>(Arrays.asList(3, 4, 1, 2, 8, 7, 5, 6));

		long start = System.currentTimeMillis();
		intSet.parallelStream().sorted().forEachOrdered(a -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.print(a);
		});
		long end = System.currentTimeMillis();
		System.out.println();
		System.out.println("cost time=" + (end - start));
	}

}
