package microbat.probability.BP.constraint;

/**
 * Bit representation of constraint
 * 
 * It is a helper structure to calculate the probability for different cases
 * @author Shiang Hwee
 *
 */
public class BitRepresentation {
	
	private boolean[] representation;
	private int size;
	private int cardinality;
	
	/**
	 * Constructor
	 * @param size Number of predicates
	 */
	public BitRepresentation(int size) {
		this.representation = new boolean[size];
		for (int i = 0; i < size; i++) 
			representation[i] = false;
		this.size = size;
		this.cardinality = 0;
	}
	
	/**
	 * Deep copy
	 */
	public BitRepresentation clone() {
		BitRepresentation result = new BitRepresentation(this.size);
		result.representation = representation.clone();
		result.cardinality = cardinality;
		return result;
	}
	
	/**
	 * Perform AND operation with the other bit representation.
	 * If the other bit representation is too short for this representation, it will perform zero padding at the front for the other bit.
	 * @param other Other bit representation
	 */
	public void and(BitRepresentation other) {
		if (other.size > this.size)
			throw new IllegalArgumentException(other.toString() + " is too long for " + this.toString());
		
		if (other.size < this.size) {
			other = BitRepresentation.padding(other, this.size);
		}
		
		for (int i=0; i<size; i++) {
			if (this.representation[i] && !other.representation[i]) {
				this.clear(i);
			}
		}
	}
	
	/**
	 * Perform OR operation with the other bit representation
	 * If the other bit representation is too short for this representation, it will perform zero padding at the front for the other bit.
	 * @param other Other bit presentation
	 */
	public void or(BitRepresentation other) {
		if (other.size > this.size)
			throw new IllegalArgumentException(other.toString() + " is too long for " + this.toString());
		
		if (other.size < this.size) {
			other = BitRepresentation.padding(other, this.size);
		}
		
		for (int i=0; i<this.size; i++) {
			if (other.representation[i]) {
				this.set(i);
			}
		}
	}
	
	/**
	 * Set the bit with target index to true
	 * @param index Target index
	 * @return True if the update is success. False mean the bit is already True.
	 */
	public boolean set(int index) {
		if (index < 0) {
			throw new IllegalArgumentException("index: " + index + " is an illegal argument");
		}
		
		if (index >= this.representation.length) {
			throw new IndexOutOfBoundsException("index: " + index + " exceed representation size: " + this.representation.length);
		}
		
		if (representation[index])
			return false;
		representation[index] = true;
		this.cardinality += 1;
		return true;
	}
	
	/**
	 * Set multiple bit to true
	 * @param start Starting index of target bit
	 * @param end Exclusive ending index of target bit
	 */
	public void set(int start, int end) {
		if (start < 0 || end < 0) {
			throw new IllegalArgumentException("given range: (" + start + "," + end + ") is illegal");
		}
		if (end < start) {
			throw new IllegalArgumentException("given range: (" + start + "," + end + ") is illegal");
		}
		if (start >= this.size || end > this.size) {
			throw new IndexOutOfBoundsException("given range: (" + start + "," + end + ") exceed the representation size " + this.size);
		}
		
		for (; start < end; start++)
			this.set(start);
	}
	
	/**
	 * Change target bit to False
	 * @param index Index of target bit
	 * @return True if the update is success. False mean the bit is already True.
	 */
	public boolean clear(int index) {
		if (index < 0) {
			throw new IllegalArgumentException("index: " + index + " is illegal");
		}
		if (index >= this.size) {
			throw new IndexOutOfBoundsException("index: " + index + " exceed representation size: " + this.size);
		}
		
		if (!representation[index])
			return false;
		representation[index] = false;
		this.cardinality -= 1;
		return true;
	}
	
	/**
	 * Change multiple bit to False
	 * @param start Starting index of target bit
	 * @param end Exclusive ending index of target bit
	 */
	public void clear(int start, int end) {
		if (start < 0 || end < 0) {
			throw new IllegalArgumentException("given range: (" + start + "," + end + ") is illegal");
		}
		if (end < start) {
			throw new IllegalArgumentException("given range: (" + start + "," + end + ") is illegal");
		}
		if (start >= this.size || end > this.size) {
			throw new IndexOutOfBoundsException("given range: (" + start + "," + end + ") exceed the representation size " + this.size);
		}

		for (; start < end; start++)
			this.clear(start);
	}
	
	/**
	 * Get the boolean value of target bit
	 * @param index Index of target bit
	 * @return Boolean value of bit
	 */
	public boolean get(int index) {
		if (index < 0) {
			throw new IllegalArgumentException("index: " + index + " is illegal");
		} 
		
		if (index >= this.size) {
			throw new IndexOutOfBoundsException("index: " + index + " exceed representation size: " + this.size);
		}
		
		return representation[index];
	}
	
	/**
	 * Get the number of predicates
	 * @return Number of predicates
	 */
	public int size() {
		return this.size;
	}
	
	/**
	 * Get the number of true bit
	 * @return Number of true bit
	 */
	public int getCardinality() {
		return this.cardinality;
	}
	
	/**
	 * Perform zero padding in front to match the given size
	 * @param bitRepresentation Bit representation to be padded
	 * @param size Target size
	 * @return New bit representation with target size
	 */
	public static BitRepresentation padding(final BitRepresentation bitRepresentation, final int size) {
		if (size < bitRepresentation.size) {
			throw new IllegalArgumentException("padding size: " + size + " should not be smaller than representation size: " + bitRepresentation.size);
		}
		return BitRepresentation.parse(bitRepresentation.toString(), size);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (boolean b : representation) {
			sb.append(b ? "1" : "0");
		}
		return sb.toString();
	}
	
	/**
	 * Parse a string bit presentation to Object
	 * @param s String to be parsed
	 * @param size Size of the bit. Given size must not be smaller than length of string. It will perform zero padding at front if the size is larger than length of string
	 * @return Generated Bit Representation
	 */
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
	
	/**
	 * Parse given string into bit representation object
	 * @param s Target string
	 * @return Generated bit representation
	 */
	public static BitRepresentation parse(String s) {
		return parse(s, s.length());
	}
	
	/**
	 * Parse given integer to bit representation object
	 * @param i Target integer.
	 * @param size Target size. Given size must not be smaller than length of integer bit. It will perform zero padding at front if the size is larger than length of bit
	 * @return Generated bit representation object
	 */
	public static BitRepresentation parse (int i, int size) {
		return parse(Integer.toBinaryString(i), size);
	}
	
	/**
	 * Parse given integer to bit representation object
	 * @param i Target integer
	 * @return Generated bit representation object
	 */
	public static BitRepresentation parse (int i) {
		return parse(Integer.toBinaryString(i));
	}
}
