package jdk8.lambda.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Stream06_SkipLimit {

	// skip,limit
	public static void main(String[] args) {
		limitAndSkipNoShortCircuiting();
	}

	// 有一种情况是limit/skip无法达到short-circuiting目的，就是把它们放在Stream的排序操作后
	public static void limitAndSkipNoShortCircuiting() {
		List<Person> persons = new ArrayList<>();
		for (int i = 1; i <= 5; i++) {
			Person person = new Person(i, "name" + i);
			persons.add(person);
		}

		// short-circuiting
		List<String> result1 = persons.stream().map(Person::getName).limit(2).sorted().collect(Collectors.toList());
		System.out.println(result1);

		// 因为sort之后的limit不知道顺序,没有短路效果
		List<String> result2 = persons.stream().map(Person::getName).sorted().limit(2).collect(Collectors.toList());
		System.out.println(result2);
	}

	public static void limitAndSkip() {
		IntStream.range(1, 20).skip(5).limit(10).forEach(e -> System.out.print(e + " "));
		System.out.println();
		IntStream.range(1, 20).limit(10).skip(5).forEach(e -> System.out.print(e + " "));
	}

	private static class Person {
		private int id;
		private String name;

		public Person(int id, String name) {
			this.id = id;
			this.name = name;
		}

		public String getName() {
			System.out.println(name);
			if (id < 3) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return name;
		}
	}
}
