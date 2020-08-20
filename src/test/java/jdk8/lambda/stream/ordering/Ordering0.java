package jdk8.lambda.stream.ordering;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Stream;

/**
 * http://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html#Ordering
 * 
 * Streams may or may not have a defined encounter order. Whether or not a
 * stream has an encounter order depends on the source and the intermediate
 * operations. Certain stream sources (such as List or arrays) are intrinsically
 * ordered, whereas others (such as HashSet) are not.Some intermediate
 * operations, such as sorted(), may impose an encounter order on an otherwise
 * unordered stream, and others may render an ordered stream unordered, such as
 * BaseStream.unordered().
 */
public class Ordering0 {

	public static void main(String[] args) {
		soure();
		intermediate();
	}

	public static void intermediate() {
		List<Integer> intList = Arrays.asList(1, 2, 5);
		Set<Integer> intSet = new HashSet<>(Arrays.asList(4, 3, 5));

		// false
		System.out.println(isOrderedStream(intList.stream().unordered()));

		// true
		System.out.println(isOrderedStream(intSet.stream().sorted()));
	}

	public static void soure() {
		// true
		System.out.println(isOrderedStream(Stream.of(1, 2, 3)));

		// false
		System.out.println(isOrderedStream(Stream.generate(Math::random)));

		// true
		List<Integer> intList = Arrays.asList(1, 2, 5);
		System.out.println(isOrderedStream(intList.stream()));

		// false
		Set<Integer> intSet = new HashSet<>(Arrays.asList(4, 3, 5));
		System.out.println(isOrderedStream(intSet.stream()));
	}

	private static boolean isOrderedStream(Stream<?> stream) {
		return stream.spliterator().hasCharacteristics(Spliterator.ORDERED);
	}
}
