package microbat.baseline;

import java.util.BitSet;

public class BitWrapper {
	private BitSet inner;
	// the size of the BitWrapper will always be the same
	private final int size;
	
	public BitWrapper(int size) {
		this.inner = new BitSet(size);
		this.size = size;
	}
	
	public void set(int index) {
		if (index >= size)
			throw new IndexOutOfBoundsException();
		inner.set(index);
	}
	
	public void set(int start, int end) {
		if (start >= size || end >= size)
			throw new IndexOutOfBoundsException();
		inner.set(start, end);
	}
	
	public void set(int index, boolean value) {
		if (value)
			set(index);
		else
			clear(index);
	}
	
	public void clear(int index) {
		if (index >= size)
			throw new IndexOutOfBoundsException();
		inner.clear(index);
	}
	
	public void clear(int start, int end) {
		if (start >= size || end >= size)
			throw new IndexOutOfBoundsException();
		inner.clear(start, end);
	}
	
	public void and(BitWrapper other) {
		/*
		 * Need to handle padding. For example, if we use 5 & 3, it should be
		 * 101 & 011 -> 001. However, from the current implementation, it will
		 * be 101 & 11 -> 100.
		 * Note that the BitWrapper that calls this operation will be changed.
		 */
		int padding = this.size - other.size;
		if (padding < 0)
			throw new IllegalArgumentException(other.toString() + " is too long for " + this.toString());
		if (padding > 0) {
			other = other.pad(padding);
		}
		this.inner.and(other.inner);
	}
	
	private BitWrapper pad(int padding) {
		/*
		 * This returns a new BitWrapper as we do not allow for
		 * size to be changed
		 */
		BitWrapper result = new BitWrapper(this.size + padding);
		for (int i = 0; i < this.size; i++)
			result.set(i + padding, get(i));
		return result;
	}
	
	public boolean get(int index) {
		if (index >= size)
			throw new IndexOutOfBoundsException();
		return inner.get(index);
	}
	
	public int cardinality() {
		return this.inner.cardinality();
	}
	
	public int size() {
		return this.size;
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)	return true;
		if (o == null) return false;
		if (this.getClass() != o.getClass()) return false;
		BitWrapper other = (BitWrapper) o;
		return this.inner.equals(other.inner) && this.size == other.size;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(inner.get(i) ? 1 : 0);
		}
		return sb.toString();
	}
	
	public static BitWrapper parse(String s, int size) {
		BitWrapper bw = new BitWrapper(size);
		int padding = size - s.length();
		if (padding < 0) {
			throw new IllegalArgumentException("Size must be >= length of string");
		}
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '1')
				bw.set(i + padding);
		}
		return bw;
	}
	
	public static BitWrapper parse(String s) {
		return parse(s, s.length());
	}
	
	public static BitWrapper parse(int value, int size) {
		return parse(Integer.toBinaryString(value), size);
	}
	
	public static BitWrapper parse(int value) {
		return parse(Integer.toBinaryString(value));
	}
}
