package jdk8.lambda.stream;

import java.util.Arrays;
import java.util.List;

public class Stream07_ForEach {

	// forEach,forEachOrdered
	// terminal operation
	public static void main(String[] args) {
		List<Integer> intList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);

		// 无序
		intList.stream().parallel().forEach(System.out::println);

		// 有序
		intList.stream().parallel().forEachOrdered(System.out::println);
	}

}
