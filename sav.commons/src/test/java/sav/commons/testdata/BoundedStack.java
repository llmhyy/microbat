package sav.commons.testdata;


/**
 * Bounded Stack implementation, cased adapted from paper "Active Automata
 * learning: From DFAs to Interface Programs and Beyond" by Bernhard Steffen,
 * etc..
 * 
 * @author Spencer Xiao
 * 
 */
public class BoundedStack {

	private static final int MaxSize = 3;
	private int size;
	private int[] data;

	public BoundedStack() {
		size = 0;
		data = new int[MaxSize];
	}

	public int size() {
		return size;
	}

	public boolean push(Integer element) {
		System.out.println(size);
		if (size == MaxSize) {
			throw new RuntimeException("Push on full stack.");
		}

		data[size] = element;
		size++;
		return true;
	}

	public int pop() throws Exception {
		if (size == 0) {
			throw new Exception("Pop an empty stack.");
		}
		Integer ret = data[size - 1];
		size--;
		return ret;

	}
}
