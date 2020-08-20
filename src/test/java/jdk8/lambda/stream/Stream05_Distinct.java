package jdk8.lambda.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jdk8.lambda.Student;

public class Stream05_Distinct {

	// distinct
	// stateful intermediate operation
	/**
	 * For ordered streams, the selection of distinct elements is stable (for
	 * duplicated elements, the element appearing first in the encounter order
	 * is preserved.) For unordered streams, no stability guarantees are made.
	 * Preserving stability for {@code distinct()} in parallel pipelines is
	 * relatively expensive.(requires that the operation act as a full
	 * barrier,with substantial buffering overhead)
	 */
	public static void main(String[] args) {
		List<Student> students = new ArrayList<>();
		students.add(new Student(1, 10, "aty1", "11111"));
		students.add(new Student(2, 20, "aty2", "1111"));
		students.add(new Student(3, 30, "aty3", "111"));
		students.add(new Student(3, 40, "aty4", "11"));
		students.add(new Student(5, 50, "aty5", "1"));

		Predicate<Student> fitler = p -> {
			try {
				if (p.getAge() == 30) {
					Thread.sleep(100);
				} else if (p.getAge() == 40) {
					Thread.sleep(50);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return p.getAge() > 0;
		};

		// stable, but poorer perfomance
		Set<Student> orderedSet = students.parallelStream()
				.filter(fitler).distinct().collect(Collectors.toSet());
		System.out.println(orderedSet);// name=aty3被保留,稳定的

		// better perfomance, but not stable
		Set<Student> unorderedSet = students.parallelStream()
				.unordered().filter(fitler).distinct()
				.collect(Collectors.toSet());
		System.out.println(unorderedSet);// name=aty4被保留,不稳定
	}

}
