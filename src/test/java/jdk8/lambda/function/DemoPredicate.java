package jdk8.lambda.function;

import java.util.function.Predicate;

public class DemoPredicate {

	public static void main(String[] args) {

		Predicate<Integer> evenCondition = id -> id % 2 == 0;

		System.out.println(evenCondition.test(1));// false
		System.out.println(evenCondition.test(2));// true
		
		// negate是默认方法,条件取反
		System.out.println(evenCondition.negate().test(1));// true
		System.out.println(evenCondition.negate().test(2));// false

	}
}
