package jdk8.lambda.stream.ordering;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * The unordered() operation doesn't do any actions to explicitly unorder the
 * stream. What it does is that it removes the constraint on the stream that it
 * must remain ordered, thereby allowing subsequent operations to use
 * optimizations that don't have to take ordering into consideration.
 *
 * http://stackoverflow.com/questions/21350195/stream-ordered-unordered-problems
 */
public class Ordering4_parallel_order {

	private static Set<Integer> l = new TreeSet<>(Arrays.asList(1, 10, 3, -3, -4));

	public static void main(String[] args) {
		unorderInSequential();

		unorderInParallel();
	}

	public static void unorderInParallel() {

		// This result will always be the same, no matter how many times you
		// execute the code.
		l.stream().parallel().skip(2).findFirst().ifPresent(System.out::print);
		System.out.println();

		// This result may (or may not) change every time you execute the code,
		// because we specified that the order is not important.
		l.stream().unordered().parallel().skip(2).findFirst().ifPresent(System.out::print);
		System.out.println();
	}

	public static void unorderInSequential() {
		// -4 -3 1 3 10
		l.stream().unordered().map(s -> s + " ").forEach(System.out::print);
		System.out.println();

		// -4 -3 1 3 10
		l.stream().unordered().map(s -> s + " ").forEach(System.out::print);
		System.out.println();

		// 3 -3 10 -4 1
		l.stream().map(s -> s + " ").parallel().forEach(System.out::print);
		System.out.println();
	}

}
