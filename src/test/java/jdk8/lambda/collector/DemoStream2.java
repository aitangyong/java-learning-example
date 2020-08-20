package jdk8.lambda.collector;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DemoStream2 {

	public static void main(String[] args) {
		List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);
		
		LinkedList<Integer> linked = integers.parallelStream().collect(LinkedList::new, LinkedList::add, LinkedList::addAll);
		
		System.out.println(linked.poll() == 1);
		System.out.println(linked.removeFirst() == 2);
		System.out.println(linked.poll() == 3);
		System.out.println(linked.poll() == 4);
		System.out.println(linked.removeFirst() == 5);
		System.out.println(linked.isEmpty());
		
	}
}
