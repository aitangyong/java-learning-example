package jdk8.lambda.function;

import java.util.function.Function;

public class DemoFunction {

	public static void main(String[] args) {
		Function<String, Integer> string2int = (str) -> Integer.parseInt(str);
		System.out.println(string2int.apply("1") == 1);

		Function<Integer, Integer> multiInt = (id) -> id * 10;
		System.out.println(string2int.andThen(multiInt).apply("2") == 20);

		Function<Integer, Integer> identity = Function.identity();
		System.out.println(identity.apply(1) == 1);

	}
}
