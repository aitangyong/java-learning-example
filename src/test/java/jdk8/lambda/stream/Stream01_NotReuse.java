package jdk8.lambda.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Stream01_NotReuse {

	public static void main(String[] args) {

		List<Integer> intList = new ArrayList<>();
		intList.add(1);
		intList.add(2);
		intList.add(3);

		Stream<Integer> stream = intList.stream();

		// 使用stream获取最小值
		Optional<Integer> min = stream.min(Integer::compareTo);
		System.out.println(min.get());// 1

		// 想要再次使用stream求和,会报错IllegalStateException:
		// stream has already been operated upon or closed
		long count = stream.count();
		System.out.println(count);

	}

}
