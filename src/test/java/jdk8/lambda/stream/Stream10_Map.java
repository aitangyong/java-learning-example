package jdk8.lambda.stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import jdk8.lambda.Student;

public class Stream10_Map {

	public static void main(String[] args) {
		flatMapOne2Many();
	}

	public static void flatMapOne2Many() {
		Stream<List<Integer>> inputStream = Stream.of(
				Arrays.asList(1), 
				Arrays.asList(2, 3),
				Arrays.asList(4, 5, 6));
		Stream<Integer> outputStream = inputStream.flatMap((intList) -> intList.stream());
		outputStream.forEach(System.out::println);
	}

	// map,mapToInt,mapToLong,mapToDouble
	public static void mapOne2One() {
		List<Student> students = new ArrayList<>();
		students.add(new Student(1, 10, "aty1", "11111"));
		students.add(new Student(2, 20, "aty2", "1111"));
		students.add(new Student(3, 30, "aty3", "111"));
		students.add(new Student(3, 40, "aty4", "11"));
		students.add(new Student(5, 50, "aty5", "1"));

		Stream<String> nameStream = students.stream().map(Student::getName);
		nameStream.forEach(System.out::println);
	}

}
