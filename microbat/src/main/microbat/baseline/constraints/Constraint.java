package microbat.baseline.constraints;

import java.util.HashMap;

import microbat.baseline.BitRepresentation;

public abstract class Constraint {
	// Arranged based on read variables first then written variables
	private BitRepresentation variablesIncluded;
	private int conclusionIndex;
	private HashMap<Integer, Double> probTable;
	private double defaultProb;
	
	public Constraint(BitRepresentation variablesIncluded, int conclusionIndex, double prob) {
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
	
	protected double getProb(int bin) {
		BitRepresentation binValues = BitRepresentation.parse(bin, variablesIncluded.size());
		// only look at the bits that we are interested in
		binValues.and(variablesIncluded);
		int numTrue = binValues.getCardinality();
		// there is only one false value and the false value is the conclusion -> false
		if ((numTrue == variablesIncluded.getCardinality() - 1) && !binValues.get(conclusionIndex)) {
			return 1 - defaultProb;
		}
		return defaultProb;
	}
}
