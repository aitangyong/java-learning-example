package jdk8.lambda.stream.ordering;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * <pre>
 * 怎么理解Set集合中元素是无序的? 
 * 考虑到Set是个接口,接口的契约很单纯，不会做过多的保证。
 * Set的契约重点就是“元素不重复的集合”，而对顺序不做保证（也就是不做限制，有序无序都可以）。
 * 实现该接口的类既可以提供有序的实现，也可以提供无序的实现。
 * HashSet在保存数据的时候显然还是得按一定顺序放入其背后的数组中，但顺序不是用户可控制的，对用户来说就是“无序”。
 * 
 * http://stackoverflow.com/questions/41894173/java-8-findfirst-and-encounter-order
 * 
 * </pre>
 */
public class Ordering1_HashSet {
	public static void main(String[] args) {
		t1();
		t2();
	}

	/**
	 * What happened is that by adding and removing a bunch of elements, we've
	 * caused the set to increase its internal table size, requiring the
	 * elements to be rehashed. The original elements end up in different
	 * relative positions in the new table, even after the new elements have
	 * been removed.
	 */
	public static void t2() {
		List<String> wordList = Arrays.asList("this", "is", "a", "stream", "of", "strings");
		Set<String> words = new HashSet<>(wordList);

		Set<String> words2 = new HashSet<>(wordList);
		IntStream.range(0, 50).forEachOrdered(i -> words2.add(String.valueOf(i)));
		words2.retainAll(wordList);

		// true
		System.out.println(words.equals(words2));

		// [a, strings, stream, of, this, is]
		System.out.println(words);

		// [this, is, strings, stream, of, a]
		System.out.println(words2);
	}

	/**
	 * JDK8下 无论遍历set2多少次,顺序都是固定的.当然我们也可以自己实现一个Set,每遍历一次就把属性打乱.
	 * 
	 * Note that in JDK 9, the new immutable sets (and maps) are actually
	 * randomized, so their iteration orders will change from run to run, even
	 * if they are initialized the same way every time.
	 */
	public static void t1() {
		Set<Integer> set2 = new HashSet<>(Arrays.asList(0, 1, 2, 19, 8, 11));

		String result = "0 1 2 19 8 11";

		for (int i = 0; i < 10000; i++) {
			StringBuilder sb = new StringBuilder();
			for (int a : set2) {
				sb.append(a).append(" ");
			}
			sb.deleteCharAt(sb.length() - 1);

			if (!result.equals(sb.toString())) {
				System.out.println("not=" + sb);
			}
		}

	}

}
