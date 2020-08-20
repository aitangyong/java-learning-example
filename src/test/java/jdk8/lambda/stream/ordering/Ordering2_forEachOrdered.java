package jdk8.lambda.stream.ordering;

import java.util.Arrays;
import java.util.List;

/**
 * Further, some terminal operations may ignore encounter order, such as
 * forEach().
 * 
 * If a stream is ordered, most operations are constrained to operate on the
 * elements in their encounter order; if the source of a stream is a List
 * containing [1, 2, 3], then the result of executing map(x -> x*2) must be [2,
 * 4, 6]. However, if the source has no defined encounter order, then any
 * permutation of the values [2, 4, 6] would be a valid result.
 */
public class Ordering2_forEachOrdered {

	// forEach VS forEachOrdered
	// http://stackoverflow.com/questions/32797579/foreach-vs-foreachordered-in-java-8-stream
	public static void main(String[] args) {
		List<Integer> intList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);

		// 无序
		intList.stream().parallel().forEach(System.out::println);

		// 有序
		intList.stream().parallel().forEachOrdered(System.out::println);
	}

}
