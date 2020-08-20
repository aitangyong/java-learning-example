package jdk8.lambda.stream.interference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InterferenceMain1 {

	// NullPointerException
	public static void main(String[] args) {
		List<String> arrayList = new ArrayList<>(Arrays.asList("one", "two"));
		List<String> safeList = Collections.synchronizedList(arrayList);
		safeList.stream().forEach(s -> {
			if (s.length() < 12)
				safeList.remove(s);
		});
	}

}
