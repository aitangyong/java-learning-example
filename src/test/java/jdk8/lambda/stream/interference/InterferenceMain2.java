package jdk8.lambda.stream.interference;

import java.util.Queue;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * The CONCURRENT property implies that the modification of the source is
 * permitted, i.e. that it will never throw a ConcurrentModificationException,
 * but it does not imply that you can rely on a specific behavior regarding
 * whether these changes are reflected or not.
 * 
 * http://stackoverflow.com/questions/40085379/adding-elements-to-java-8-parallel-streams-on-the-fly
 */
public class InterferenceMain2 {

	public static void main(String[] args) {
		ConcurrentLinkedQueue<Integer> safeQueue = new ConcurrentLinkedQueue<>();
		// true
		System.out.println(safeQueue.stream().spliterator().hasCharacteristics(Spliterator.CONCURRENT));

		final int N = 10000;

		// true
		System.out.println(N == testSequential(N));

		// false
		System.out.println(N == testParallel1(N));

	}

	public static int testParallel1(int N) {
		final AtomicInteger counter = new AtomicInteger(0);
		final AtomicInteger check = new AtomicInteger(0);
		final Queue<Integer> queue = new ConcurrentLinkedQueue<Integer>();

		for (int i = 0; i < N / 10; ++i) {
			queue.add(counter.incrementAndGet());
		}

		Stream<Integer> stream = queue.parallelStream();
		stream.forEach(i -> {
			int j = counter.incrementAndGet();
			check.incrementAndGet();
			if (j <= N) {
				queue.add(j);
			}
		});

		return check.get();
	}

	public static int testSequential(int N) {
		final AtomicInteger counter = new AtomicInteger(0);
		final AtomicInteger check = new AtomicInteger(0);
		final Queue<Integer> queue = new ConcurrentLinkedQueue<Integer>();

		for (int i = 0; i < N / 10; ++i) {
			queue.add(counter.incrementAndGet());
		}

		Stream<Integer> stream = queue.stream();
		stream.forEach(i -> {
			int j = counter.incrementAndGet();
			check.incrementAndGet();
			if (j <= N) {
				queue.add(j);
			}
		});

		return check.get();
	}
}
