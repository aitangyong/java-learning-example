package jdk8.lambda.stream.stateful;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * http://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html#Statelessness
 * 
 * Stream pipeline results may be nondeterministic or incorrect if the
 * behavioral parameters to the stream operations are stateful. A stateful
 * lambda (or other object implementing the appropriate functional interface) is
 * one whose result depends on any state which might change during the execution
 * of the stream pipeline.Note also that attempting to access mutable state from
 * behavioral parameters presents you with a bad choice with respect to safety
 * and performance; if you do not synchronize access to that state, you have a
 * data race and therefore your code is broken, but if you do synchronize access
 * to that state, you risk having contention undermine the parallelism you are
 * seeking to benefit from. The best approach is to avoid stateful behavioral
 * parameters to stream operations entirely; there is usually a way to
 * restructure the stream pipeline to avoid statefulness.
 * 
 * Avoid using stateful lambda expressions as parameters in stream operations.
 * 
 * http://docs.oracle.com/javase/tutorial/collections/streams/parallelism.html#stateful_lambda_expressions
 */
public class StatefulLambdaMain {

	public static void main(String[] args) {
		Integer[] intArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
		List<Integer> listOfIntegers = new ArrayList<>(Arrays.asList(intArray));

		System.out.println("use non thread safe ArrayList");
		t1(listOfIntegers);

		System.out.println("use thread safe Collections.synchronizedList");
		t2(listOfIntegers);

		t3();
	}

	// 输出顺序不确定,每次运行的结果可能不一致
	public static void t3() {
		List<Integer> integers = Arrays.asList(1, 2, 3, 3, 2, 5, 6);
		Set<Integer> seen = Collections.synchronizedSet(new HashSet<>());

		integers.stream().parallel().map(e -> {
			if (seen.add(e))
				return 0;
			else
				return e;
		}).forEachOrdered(System.out::print);
	}

	// 使用线程安全的数据结构,不会出现null但是list顺序是不确定的
	public static void t2(List<Integer> listOfIntegers) {
		List<Integer> serialStorage = new ArrayList<>();
		System.out.println("Serial stream:");
		listOfIntegers.stream()
				// Don't do this! It uses a stateful lambda expression.
				.map(e -> {
					serialStorage.add(e);
					return e;
				}).forEachOrdered(e -> System.out.print(e + " "));
		System.out.println();

		serialStorage.stream().forEachOrdered(e -> System.out.print(e + " "));
		System.out.println("");

		System.out.println("Parallel stream:");
		List<Integer> parallelStorage = Collections.synchronizedList(new ArrayList<>());
		listOfIntegers.parallelStream()
				// Don't do this! It uses a stateful lambda expression.
				.map(e -> {
					parallelStorage.add(e);
					return e;
				}).forEachOrdered(e -> System.out.print(e + " "));
		System.out.println();

		// parallelStorage can vary every time the code is run
		parallelStorage.stream().forEachOrdered(e -> System.out.print(e + " "));
		System.out.println();
	}

	// 非线程安全的数据结构,使用stateful lambda导致错误数据null
	public static void t1(List<Integer> listOfIntegers) {
		List<Integer> serialStorage = new ArrayList<>();
		System.out.println("Serial stream:");
		listOfIntegers.stream()
				// Don't do this! It uses a stateful lambda expression.
				.map(e -> {
					serialStorage.add(e);
					return e;
				}).forEachOrdered(e -> System.out.print(e + " "));
		System.out.println();

		serialStorage.stream().forEachOrdered(e -> System.out.print(e + " "));
		System.out.println("");

		System.out.println("Parallel stream:");
		List<Integer> parallelStorage = new ArrayList<>();
		listOfIntegers.parallelStream()
				// Don't do this! It uses a stateful lambda expression.
				.map(e -> {
					parallelStorage.add(e);
					return e;
				}).forEachOrdered(e -> System.out.print(e + " "));
		System.out.println();

		// null null 3 4 1 7 8 2
		parallelStorage.stream().forEachOrdered(e -> System.out.print(e + " "));
		System.out.println();
	}

}
