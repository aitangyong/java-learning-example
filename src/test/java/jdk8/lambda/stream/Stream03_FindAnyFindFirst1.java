package jdk8.lambda.stream;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * No effort will be made to randomize the element returned, it just does not
 * give the same guarantees as findFirst(), and might therefore be faster.
 * 
 * The behavior of findAny() operation is explicitly nondeterministic; it is
 * free to select any element in the stream. This is to allow for maximal
 * performance in parallel operations; the cost is that multiple invocations on
 * the same source may not return the same result. If a stable result is
 * desired, use findFirst() instead.
 * 
 */
public class Stream03_FindAnyFindFirst1 {

	private static Predicate<Integer> condition = e -> {
		System.out.println(Thread.currentThread().getName() + " -> " + e);
		return e > 3;
	};

	public static void main(String[] args) {
		findAny();
	}

	// findAny
	// short-circuiting terminal operation
	// 返回值explicitly nondeterministic
	public static void findAny() {
		List<Integer> intergers = Arrays.asList(1, 2, 3, 4, 5);
		boolean is4 = false, is5 = false;

		while (!is4 || !is5) {
			int v = intergers.parallelStream().filter(condition).findAny().get();
			if (v == 4) {
				is4 = true;
			}

			if (v == 5) {
				is5 = true;
			}
		}

		System.out.println("can reach at here.");
	}

	// findFirst
	// short-circuiting terminal operation
	// If a stable result is desired, use findFirst instead
	public static void findFirst() {
		List<Integer> intergers = Arrays.asList(1, 2, 3, 4, 5);

		boolean is4 = false, is5 = false;
		while (!is4 || !is5) {
			int v = intergers.parallelStream().filter(condition).findFirst().get();
			if (v == 4) {
				is4 = true;
			}

			if (v == 5) {
				is5 = true;
			}
		}

		System.out.println("can not reach at here.");
	}
}