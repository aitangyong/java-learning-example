package jdk8.lambda.stream.interference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Stream;

public class InterferenceMain0 {

	// ConcurrentModificationException
	public static void main(String[] args) {
		List<String> arrayList = new ArrayList<>(Arrays.asList("one", "two"));
		List<String> safeList = Collections.synchronizedList(arrayList);

		// false
		System.out.println(isConcurrent(safeList.stream()));

		// This will fail as the peek operation will attempt to add the
		// string "three" to the source after the terminal operation has
		// commenced.
		String concatenatedString = safeList.stream()
				// Don't do this! Interference occurs here.
				.peek(s -> safeList.add("three")).reduce((a, b) -> a + " " + b).get();
		System.out.println("Concatenated string: " + concatenatedString);
	}

	private static boolean isConcurrent(Stream<?> stream) {
		return stream.spliterator().hasCharacteristics(Spliterator.CONCURRENT);
	}
}
