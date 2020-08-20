package jdk8.lambda.stream;

import java.util.ArrayList;
import java.util.List;

// If a stable result is desired, use findFirst() instead.
public class Stream03_FindAnyFindFirst2 {

	public static void main(String[] args) {
		List<Worker> workers = new ArrayList<>();
		workers.add(new Worker(1));
		workers.add(new Worker(2));
		workers.add(new Worker(3));

		Worker resultFindAny = workers.parallelStream().filter(Worker::finish).findAny().orElse(null);
		Worker resultFindFirst = workers.parallelStream().filter(Worker::finish).findFirst().orElse(null);

		// work=3,睡眠时间最短的那个
		System.out.println(resultFindAny);

		// Work=1,第一个元素
		System.out.println(resultFindFirst);
	}

	private static class Worker {
		int id;

		Worker(int id) {
			this.id = id;
		}

		boolean finish() {
			int t = 4000 - id * 1000;
			System.out.println(Thread.currentThread().getName() + " -> " + t);
			try {
				Thread.sleep(t);
			} catch (InterruptedException ignored) {
			}
			return true;
		}

		@Override
		public String toString() {
			return "Work=" + id;
		}
	}
}