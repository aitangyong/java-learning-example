package jdk8.lambda.collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jdk8.lambda.Student;

/**
 * <p>
 * The following are examples of using the predefined collectors to perform
 * common mutable reduction tasks:
 *
 * <pre>
 * {@code
 *     // Accumulate names into a List
 *     List<String> list = people.stream().map(Person::getName).collect(Collectors.toList());
 *
 *     // Accumulate names into a TreeSet
 *     Set<String> set = people.stream().map(Person::getName)
 *     					.collect(Collectors.toCollection(TreeSet::new));
 *
 *     // Convert elements to strings and concatenate them, separated by commas
 *     String joined = things.stream()
 *                           .map(Object::toString)
 *                           .collect(Collectors.joining(", "));
 *
 *     // Compute sum of salaries of employee
 *     int total = employees.stream()
 *                          .collect(Collectors.summingInt(Employee::getSalary)));
 *
 *     // Group employees by department
 *     Map<Department, List<Employee>> byDept
 *         = employees.stream()
 *                    .collect(Collectors.groupingBy(Employee::getDepartment));
 *
 *     // Compute sum of salaries by department
 *     Map<Department, Integer> totalByDept
 *         = employees.stream()
 *                    .collect(Collectors.groupingBy(Employee::getDepartment,
 *                                                   Collectors.summingInt(Employee::getSalary)));
 *
 *     // Partition students into passing and failing
 *     Map<Boolean, List<Student>> passingFailing =
 *         students.stream()
 *                 .collect(Collectors.partitioningBy(s -> s.getGrade() >= PASS_THRESHOLD));
 *
 * }
 * </pre>
 *
 */
public class CollectorMain {
	public static void main(String[] args) {
		reducing();
	}

	public static void reducing() {
		// 函数有两个参数:第一个参数是上次函数执行的返回值, 第二个参数是stream中的元素
		// 函数返回值会被赋给下次执行这个函数的第一个参数
		// 第一次执行的时候第一个参数的值是Stream的第一个元素, 第二个参数是Stream的第二个元素
		BinaryOperator<Integer> operator = (sum, item) -> {
			System.out.println("sum=" + sum + ",item=" + item);
			return sum + item;
		};
		Optional<Integer> sumOptional = Stream.of(1, 2, 3, 4, 5).collect(Collectors.reducing(operator));
		System.out.println(sumOptional.get());// 15

		// 给默认值的reducing
		Optional<Integer> empty = Stream.of(1).filter(e -> e == 2).collect(Collectors.reducing(operator));
		System.out.println(empty.isPresent());
		int defaultValue = Stream.of(1).filter(e -> e == 2).collect(Collectors.reducing(1, operator));
		System.out.println(defaultValue == 1);

		// 3个参数的reducing
		List<Student> students = buildStudents();
		Comparator<String> byLength = Comparator.comparing(String::length);
		String maxLengthPhone = students.stream().collect(Collectors.reducing("", Student::getPhone, BinaryOperator.maxBy(byLength)));
		System.out.println(maxLengthPhone);
	}

	public static void counting() {
		List<Student> students = buildStudents();
		long count = students.stream().filter(e -> e.getAge() < 30).collect(Collectors.counting());
		System.out.println(count);
	}

	public static void partitioningBy() {
		List<Student> students = buildStudents();

		Map<Boolean, List<Student>> passingFailingStudent = students.stream()
				.collect(Collectors.partitioningBy(s -> s.getAge() >= 30));
		System.out.println(passingFailingStudent);
	
		Collector<Student, ?, List<String>> downstream = Collectors.mapping(Student::getName, Collectors.toList());
		Map<Boolean, List<String>> passingFailingName = students.stream()
				.collect(Collectors.partitioningBy(s -> s.getAge() >= 30, downstream));
		System.out.println(passingFailingName);

		Map<Boolean, Long> partiCount = Stream.of(1, 2, 3, 4, 5)
				.collect(Collectors.partitioningBy(it -> it.intValue() % 2 == 0, Collectors.counting()));
		System.out.println("partiCount: " + partiCount);
	}

	public static void groupingBy() {
		List<Student> students = buildStudents();

		Collector<Student, ?, Map<String, List<Student>>> collector = Collectors.groupingBy(Student::getName);
		Collector<Student, ?, List<Integer>> furtherCollector = Collectors.mapping(Student::getAge,
				Collectors.toList());

		Map<String, List<Student>> nameStudent = students.stream().collect(collector);
		System.out.println(nameStudent);

		Map<String, List<Integer>> groupAge = students.stream()
				.collect(Collectors.groupingBy(Student::getName, furtherCollector));
		System.out.println(groupAge);
		System.out.println(groupAge.getClass());// java.util.HashMap

		// 使用TreeMap作为收集器
		Map<String, List<Integer>> treeMapName = students.stream()
				.collect(Collectors.groupingBy(Student::getName, TreeMap::new, furtherCollector));
		System.out.println(treeMapName);
		System.out.println(treeMapName.getClass());// java.util.TreeMap
	}

	public static void mapping() {
		List<Student> students = buildStudents();

		// age收集器
		Collector<Integer, ?, List<Integer>> ageCollector = Collectors.toList();

		// 从Student提取age
		Collector<Student, ?, List<Integer>> student2Age = Collectors.mapping(Student::getAge, ageCollector);

		// 按照Student对象的name属性值groupBy
		Collector<Student, ?, Map<String, List<Integer>>> ageGroupByName = Collectors.groupingBy(Student::getName,
				student2Age);

		Map<String, List<Integer>> resultMap = students.stream().collect(ageGroupByName);
		System.out.println(resultMap);
	}

	public static void maxByAndMinBy() {
		List<Student> students = buildStudents();
		Comparator<Student> comparator = Comparator.comparingInt(Student::getAge);
		Optional<Student> maxStudent = students.stream().collect(Collectors.maxBy(comparator));
		Optional<Student> minStudent = students.stream().collect(Collectors.minBy(comparator));
		System.out.println(maxStudent.get());
		System.out.println(minStudent.get());
	}

	public static void summingAndSummarizing() {
		List<Student> students = buildStudents();
		int sumAge = students.stream().collect(Collectors.summingInt(Student::getAge));
		System.out.println(sumAge);
		double averageAge = students.stream().collect(Collectors.averagingInt(Student::getAge));
		System.out.println(averageAge);

		IntSummaryStatistics ageStatistics = students.stream().collect(Collectors.summarizingInt(Student::getAge));
		System.out.println(ageStatistics.getSum());
		System.out.println(ageStatistics.getAverage());
		System.out.println(ageStatistics.getMax());
		System.out.println(ageStatistics.getMin());
		System.out.println(ageStatistics.getCount());
	}

	// 需要弄懂CONCURRENT, UNORDERED
	public static void toMapAndToConcurrentMap() {
		// Collector is not concurrent.
		Collector<Student, ?, Map<String, String>> nonConcurrent = Collectors.toMap(Student::getName,
				Student::getPhone);
		System.out.println(nonConcurrent.characteristics());

		// if encounter order is not required, may offer better parallel
		// performance
		Collector<Student, ?, ConcurrentMap<String, String>> concurrent = Collectors.toConcurrentMap(Student::getName,
				Student::getPhone);
		// CONCURRENT, UNORDERED
		System.out.println(concurrent.characteristics());
	}

	// 集合转成set/list/map/自定义collection
	public static void differentCollections() {
		List<Student> students = buildStudents();

		// 基于hashcode()&equals()作为相等条件过滤
		Set<Student> set = students.stream().collect(Collectors.toSet());
		System.out.println(set);

		// list
		List<Student> list = students.stream().collect(Collectors.toList());
		System.out.println(list);

		// 只会被调用一次,其返回结果用来接收stream中的元素,返回值必须是Collection子类
		Supplier<Collection<Student>> collectionFactory = () -> {
			System.out.println("be called only once.");
			return new TreeSet<Student>();
		};

		Collection<Student> anyCollection = students.stream().collect(Collectors.toCollection(collectionFactory));
		System.out.println(anyCollection);

		// map,不允许重复key
		try {
			Map<String, String> namePhone = students.stream()
					.collect(Collectors.toMap(Student::getName, Student::getPhone));
			System.out.println(namePhone);
		} catch (IllegalStateException e) {
			System.err.println(e.getMessage());
		}

		// key可以允许重复,挑选多个值中最大或者最小的那个
		BinaryOperator<String> mergeFunction1 = BinaryOperator.maxBy(String::compareTo);
		// 多个值合并在一起,以逗号分隔
		BinaryOperator<String> mergeFunction2 = (v1, v2) -> v1 + "," + v2;
		Map<String, String> namePhone1 = students.stream()
				.collect(Collectors.toMap(Student::getName, Student::getPhone, mergeFunction1));
		Map<String, String> namePhone2 = students.stream()
				.collect(Collectors.toMap(Student::getName, Student::getPhone, mergeFunction2));
		System.out.println(namePhone1);
		System.out.println(namePhone2);

		// 自定义map的Supplier
		Supplier<Map<String, String>> mapSupplier = ConcurrentHashMap::new;
		Map<String, String> namePhone3 = students.stream()
				.collect(Collectors.toMap(Student::getName, Student::getPhone, mergeFunction2, mapSupplier));
		System.out.println(namePhone3);

	}

	// 集合合并成字符串
	public static void joining() {
		List<Student> students = buildStudents();
		String s1 = students.stream().map(Student::getName).collect(Collectors.joining());
		String s2 = students.stream().map(Student::getName).collect(Collectors.joining(","));
		String s3 = students.stream().map(Student::getName).collect(Collectors.joining(",", "[", "]"));
		System.out.println(s1);
		System.out.println(s2);
		System.out.println(s3);
	}

	private static List<Student> buildStudents() {
		List<Student> students = new ArrayList<>();
		students.add(new Student(1, 10, "aty1", "11111"));
		students.add(new Student(2, 20, "aty2", "1111"));
		students.add(new Student(3, 30, "aty3", "111"));
		students.add(new Student(3, 40, "aty4", "11"));
		students.add(new Student(5, 50, "aty5", "1"));
		return students;
	}

}
