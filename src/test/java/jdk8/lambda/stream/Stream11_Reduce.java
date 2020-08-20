package jdk8.lambda.stream;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

public class Stream11_Reduce {

	public static void main(String[] args) {
		threeArgs();
	}

	public static void threeArgs() {
		List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		Stream<Integer> intStream = numbers.stream();

		// The accumulator function takes a partial result and the next element,
		// and produces a new partial result
		BiFunction<String, ? super Integer, String> accumulator = (String left, Integer right) -> {
			System.out.println("accumulator:left=" + left + ",right=" + right);
			return left + "/" + right;
		};

		BinaryOperator<String> combiner = (String first, String second) -> {
			System.out.println("combiner:left=" + first + ",right=" + second);
			return "";
		};

		String result = intStream.parallel().reduce("initial=", accumulator, combiner);
		System.out.println(result);
	}

	public static void twoArgs() {
		List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		Integer sumWithInitial = numbers.stream().reduce(10, Integer::sum);
		System.out.println(sumWithInitial);// 65

		Integer initialValue = numbers.stream().filter(p -> p > 100).reduce(10, Integer::sum);
		System.out.println(initialValue);// 10
	}

	public static void oneArg() {
		List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

		BinaryOperator<Integer> accumulator = (first, second) -> {
			System.out.println("first=" + first + ",second=" + second);
			return first + second;
		};

		Optional<Integer> sumOptional = numbers.stream().reduce(accumulator);
		System.out.println("sum=" + sumOptional.get());

		Optional<Integer> empty = numbers.stream().filter(p -> p > 100).reduce(accumulator);
		System.out.println(empty.isPresent());

	}

}
