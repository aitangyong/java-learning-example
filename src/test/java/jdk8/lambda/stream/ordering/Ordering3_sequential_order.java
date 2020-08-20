package jdk8.lambda.stream.ordering;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * For sequential streams, the presence or absence of an encounter order does
 * not affect performance, only determinism. If a stream is ordered, repeated
 * execution of identical stream pipelines on an identical source will produce
 * an identical result; if it is not ordered, repeated execution might produce
 * different results.
 */
public class Ordering3_sequential_order {
	public static void main(String[] args) {
		List<String> wordList = Arrays.asList("this", "is", "a", "stream", "of", "strings");
		Set<String> words1 = new HashSet<>(wordList);
		// Optional[a]
		System.out.println(words1.stream().findFirst());

		// 没有改变words2
		Set<String> words2 = new HashSet<>(wordList);
		IntStream.range(0, 50).forEachOrdered(i -> words2.add(String.valueOf(i)));
		words2.retainAll(wordList);

		// Optional[this]
		System.out.println(words2.stream().findFirst());
	}

}
