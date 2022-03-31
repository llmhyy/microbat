package microbat.baseline;

public class BitRepresentation {
	private boolean[] representation;
	private int size;
	private int cardinality;
	
	public BitRepresentation(int size) {
		this.representation = new boolean[size];
		for (int i = 0; i < size; i++) 
			representation[i] = false;
		this.size = size;
		this.cardinality = 0;
	}
	
	public void and(BitRepresentation other) {
		int padding = this.size - other.size;
		if (padding < 0)
			throw new IllegalArgumentException(other.toString() + " is too long for " + this.toString());
		for (int i = 0; i < size; i++) {
			if (representation[i] && !other.representation[i + padding])
				this.clear(i);
		}
	}
	
	public void or(BitRepresentation other) {
		int padding = this.size - other.size;
		if (padding < 0)
			throw new IllegalArgumentException(other.toString() + " is too long for " + this.toString());
		for (int i = 0; i < size; i++) {
			if (other.representation[i + padding])
				this.set(i);
		}
	}
	
	public boolean set(int index) {
		if (representation[index])
			return false;
		representation[index] = true;
		this.cardinality += 1;
		return true;
	}
	
	public void set(int start, int end) {
		if (end > size)
			throw new IndexOutOfBoundsException();
		for (; start < end; start++)
			this.set(start);
	}
	
	public boolean clear(int index) {
		if (!representation[index])
			return false;
		representation[index] = false;
		this.cardinality -= 1;
		return true;
	}
	
	public void clear(int start, int end) {
		if (end > size)
			throw new IndexOutOfBoundsException();
		for (; start < end; start++)
			this.clear(start);
	}
	
	public boolean get(int index) {
		return representation[index];
	}
	
	public int size() {
		return this.size;
	}
	
	public int getCardinality() {
		return this.cardinality;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (boolean b : representation) {
			sb.append(b ? "1" : "0");
		}
		return sb.toString();
	}
	
	public static BitRepresentation parse(String s, int size) {
		BitRepresentation br = new BitRepresentation(size);
		int padding = size - s.length();
		if (padding < 0)
			throw new IllegalArgumentException("Size must be >= length of string");
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '1')
				br.set(i + padding);
		}
		return br;
	}
	
	public static BitRepresentation parse(String s) {
		return parse(s, s.length());
	}
	
	public static BitRepresentation parse (int i, int size) {
		return parse(Integer.toBinaryString(i), size);
	}
	
	public static BitRepresentation parse (int i) {
		return parse(Integer.toBinaryString(i));
	}
}
