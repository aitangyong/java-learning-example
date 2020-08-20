package jdk8.lambda.stream;

import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * javadoc上说： If the stream has no encounter order, then any element may be
 * returned.
 * 
 * http://stackoverflow.com/questions/41894173/java-8-findfirst-and-encounter-order
 */
public class Stream03_FindAnyFindFirst3 {
	public static void main(String[] args) {
		case2();
	}

	public static void case2() {
		List<String> equal = IntStream.range(0, 100).mapToObj(i -> new String("test")).collect(Collectors.toList());
		System.out.println(equal.size());// 100

		Map<String, Integer> map = IntStream.range(0, 100).collect(IdentityHashMap::new,
				(m, i) -> m.put(equal.get(i), i), Map::putAll);
		System.out.println(map);

		// always 0
		equal.parallelStream().distinct().map(map::get).findFirst().ifPresent(System.out::println);

		// change every time run
		equal.parallelStream().unordered().distinct().map(map::get).findFirst().ifPresent(System.out::println);
	}

	public static void case1() {
		Set<Integer> l = new TreeSet<>(Arrays.asList(1, 10, 3, -3, -4));

		// always 1
		l.stream().parallel().skip(2).findFirst().ifPresent(System.out::print);
		System.out.println();

		// This result may (or may not) change every time you execute the code
		l.stream().unordered().parallel().skip(2).findFirst().ifPresent(System.out::print);
		System.out.println();
	}

	/**
	 * By marking your stream as unordered, you are not actually making it as
	 * such (you have not made the order in your Set any different), but instead
	 * you are removing any restriction that otherwise an ordered stream might
	 * impose.
	 */
	// i was running this code via jdk-9, where it exhibits the behavior. Indeed
	// tried it with jdk-8 and it does not.
	public static void differentOnJDK9() {
		while (true) {
			Set<String> words = new HashSet<>();
			words.addAll(Arrays.asList("this", "is", "a", "stream", "of", "strings"));
			Optional<String> firstString = words.stream().parallel().findFirst();
			if (!firstString.get().equals("a")) {
				break;
			}
		}

		System.out.println("jdk8 can't reach here, but jdk9 can.");

	}

}
