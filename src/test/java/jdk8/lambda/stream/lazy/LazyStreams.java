package jdk8.lambda.stream.lazy;

import java.util.Arrays;
import java.util.List;

public class LazyStreams {

	private static int length(String name) {
		System.out.println("getting length for " + name);
		return name.length();
	}

	private static String toUpper(String name) {
		System.out.println("converting to uppercase: " + name);
		return name.toUpperCase();
	}

	public static void main(final String[] args) {
		List<String> names = Arrays.asList("Brad", "Kate", "Kim", "Jack", "Joe", "Mike", "Susan", "George", "Robert",
				"Julia", "Parker", "Benson");

		String firstNameWith3Letters = names.stream()
				.filter(name -> length(name) == 3)
				.map(name -> toUpper(name))
				.findFirst().get();

		System.out.println(firstNameWith3Letters);
	}
}
