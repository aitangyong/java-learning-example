package jdk8.lambda.stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Stream00_generate {

	public static void main(String[] args) {

		// 通过list或者set得到stream
		List<Integer> intList = new ArrayList<>();
		intList.add(1);
		intList.add(2);
		intList.add(3);
		Optional<Integer> min = intList.stream().min(Integer::compareTo);
		System.out.println(min.get());// 1

		// 通过数组获得stream
		int[] ints = { 1, 2, 3, 4, 5 };
		IntStream inStream = Arrays.stream(ints);
		System.out.println(inStream.sum());// 15

		// 通过Stream接口中的静态方法获取stream
		Stream<Integer> stream = Stream.of(1, 2, 3);
		Set<Integer> result = stream.filter(e -> e > 2).collect(Collectors.toSet());
		System.out.println(result);// 3

	}

}
