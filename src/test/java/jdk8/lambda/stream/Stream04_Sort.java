package jdk8.lambda.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jdk8.lambda.Student;

public class Stream04_Sort {
	public static void main(String[] args) {
		stable();
	}

	public static void stable() {
		Random random = new Random();
		List<Student> original = new ArrayList<>();
		for (int i = 0; i < 100 * 10000; i++) {
			int id = random.nextInt(100);
			original.add(new Student(id, i, "aty" + i, ""));
		}

		Comparator<Student> comparator = Comparator.comparing(Student::getId);

		// case1
		System.out.println("Collections.sort()是稳定排序");
		List<Student> dest = new ArrayList<>();
		for (Student each : original) {
			dest.add(each);
		}
		Collections.sort(dest, comparator);
		checkStable(original, dest);

		// case2
		System.out.println("sequential是稳定排序");
		List<Student> sequential = original.stream().sorted(comparator).collect(Collectors.toList());
		checkStable(original, sequential);

		// case3
		System.out.println("parallel不稳定排序");
		List<Student> parallel = original.stream().parallel().sorted(comparator).collect(Collectors.toList());
		checkStable(original, parallel);
	}

	// 验证是否稳定排序
	private static void checkStable(List<Student> original, List<Student> afterSorted) {
		Map<Integer, List<Integer>> id2orders = new HashMap<>();
		for (int i = 0; i < original.size(); i++) {
			int studentId = original.get(i).getId();
			if (id2orders.containsKey(studentId)) {
				id2orders.get(studentId).add(i);
			} else {
				List<Integer> sameList = new ArrayList<>();
				sameList.add(i);
				id2orders.put(studentId, sameList);
			}
		}

		// 重复的id
		Set<Integer> repeatIds = new HashSet<>();
		for (int studentId : id2orders.keySet()) {
			if (id2orders.get(studentId).size() > 1) {
				repeatIds.add(studentId);
			}
		}

		// 验证排序是否稳定
		boolean stable = true;
		outer: for (int studentId : repeatIds) {
			List<Student> allWithId = afterSorted.stream().parallel().filter(p -> p.getId() == studentId)
					.collect(Collectors.toList());
			for (int i = 0; i < allWithId.size(); i++) {
				Student s2 = original.get(id2orders.get(studentId).get(i));
				if (s2.getAge() != allWithId.get(i).getAge()) {
					stable = false;
					break outer;
				}
			}
		}

		System.out.println("stable=" + stable);

	}

	/**
	 * stream().sorted(),Comparator.naturalOrder(),Comparator.reverseOrder()
	 * 要求元素必须实现Comparable接口。
	 * 
	 * sorted(Comparator<? super T> comparator)元素可以不实现Comparable接口
	 */
	public static void basicUsage() {
		Comparator<Integer> natureOrder = Integer::compare;

		// 默认按照自然顺序1,元素必须实现Comparable接口,否则运行抛异常
		Stream.of(5, 2, 1, 4, 3).sorted().forEach(System.out::print);
		System.out.println();

		// 自然顺序2
		Stream.of(5, 2, 1, 4, 3).sorted(Comparator.naturalOrder()).forEach(System.out::print);
		System.out.println();

		// 自然顺序3
		Stream.of(5, 2, 1, 4, 3).sorted(natureOrder).forEach(System.out::print);
		System.out.println();

		// 自然顺序反序1
		Stream.of(5, 2, 1, 4, 3).sorted(natureOrder.reversed()).forEach(System.out::print);
		System.out.println();

		// 自然顺序反序2
		Stream.of(5, 2, 1, 4, 3).sorted(Comparator.reverseOrder()).forEach(System.out::print);
		System.out.println();
	}
}
