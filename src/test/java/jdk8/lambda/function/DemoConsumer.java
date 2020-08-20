package jdk8.lambda.function;

import java.util.function.Consumer;

public class DemoConsumer {

	public static void main(String[] args) {
		Consumer<String> printSomething = (content)->System.out.println(content);
		printSomething.accept("abc");
		printSomething.accept("efg");
	}
}
