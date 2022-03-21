package microbat.baseline.constraints;

import java.util.HashMap;
import java.util.BitSet;

public abstract class Constraint {
	// Arranged based on read variables first then written variables
	private BitSet variablesIncluded;
	private int conclusionIndex;
	private HashMap<Integer, Double> probTable;
	private double defaultProb;
	
	public Constraint(BitSet variablesIncluded, int conclusionIndex, double prob) {
		this.variablesIncluded = variablesIncluded;
		this.conclusionIndex = conclusionIndex;
		this.probTable = new HashMap<>();
		this.defaultProb = prob;
	}
	
	public double getProbability(int bin) {
		/*
		 * Memoization to save time needed
		 */
		if (probTable.containsKey(bin))
			return probTable.get(bin);
		double probability = getProb(bin);
		this.probTable.put(bin, probability);
		return probability;
	}
	
	protected BitSet getBin(int bin) {
		String binString = Integer.toBinaryString(bin);
		int padding = variablesIncluded.length() - binString.length();
		if (padding < 0)
			throw new IndexOutOfBoundsException(binString + " too long for premises");
		BitSet result = new BitSet(variablesIncluded.length());
		for (int i = 0; i < binString.length(); i++) {
			char c = binString.charAt(i);
			if (c == '0')
				result.clear(i + padding);
			else
				result.set(i + padding);
		}
		return result;
	}
	
	protected double getProb(int bin) {
		BitSet binValues = getBin(bin);
		// only look at the bits that we are interested in
		binValues.and(variablesIncluded);
		int numTrue = binValues.cardinality();
		// there is only one false value and the false value is the conclusion -> false
		if ((numTrue == variablesIncluded.cardinality() - 1) && !binValues.get(conclusionIndex)) {
			return 1 - defaultProb;
		}
		return defaultProb;
	}
}
