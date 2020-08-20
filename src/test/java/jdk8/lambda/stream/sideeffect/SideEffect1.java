package jdk8.lambda.stream.sideeffect;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SideEffect1 {

	public static void main(String[] args) throws Exception {
		ArrayList<Integer> inputs = new ArrayList<>();
		inputs.add(1);
		inputs.add(2);
		inputs.add(3);

		List<Integer> results = 
		inputs.stream().filter(i -> i > 1).collect(Collectors.toList());
		System.out.println(results);

	}

}
