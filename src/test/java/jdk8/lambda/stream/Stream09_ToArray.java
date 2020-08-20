package jdk8.lambda.stream;

import java.util.Arrays;
import java.util.stream.Stream;

public class Stream09_ToArray {

	// toArray
	public static void main(String[] args) {
		Object[] results1 = Stream.of("one", "two", "three", "four").toArray();

		String[] results2 = Stream.of("one", "two", "three", "four").toArray(String[]::new);

		System.out.println(Arrays.toString(results1));

		System.out.println(Arrays.toString(results2));
	}

}
