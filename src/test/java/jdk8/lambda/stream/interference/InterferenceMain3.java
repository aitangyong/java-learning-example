package jdk8.lambda.stream.interference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class InterferenceMain3 {

	public static void main(String[] args) throws Exception {
		ArrayList<Integer> unsafe1 = new ArrayList<>();
		List<Integer> safe1 = Collections.synchronizedList(new ArrayList<>());

		IntStream.range(0, 10000).filter(s -> s % 5 == 2).forEach(s -> unsafe1.add(s));
		IntStream.range(0, 10000).parallel()
								 .filter(s -> s % 5 == 2)
								 .forEach(s -> safe1.add(s));
		
		System.out.println(checkCorrect(unsafe1)); // true
		System.out.println(checkCorrect(safe1)); // true

		ArrayList<Integer> unsafe2 = new ArrayList<>();
		// 有可能抛出ArrayIndexOutOfBoundsException
		IntStream.range(0, 10000).parallel().filter(s -> s % 5 == 2)
								 .forEach(s -> unsafe2.add(s));
		System.out.println(checkCorrect(unsafe2)); // 结果有可能不正确
	}

	private static boolean checkCorrect(List<Integer> results) {
		for (Integer each : results) {
			if (each == null || each % 5 != 2) {
				return false;
			}
		}
		return true;
	}
}
