package microbat.baseline.constraints;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import microbat.baseline.BitRepresentation;
import microbat.baseline.Configs;

public class Constraint{
	private BitRepresentation variablesIncluded;
	private HashSet<Integer> conclusionIndexes;
	private double probability;
	private HashMap<Integer, Double> memoTable = new HashMap<>();
	
	public Constraint(BitRepresentation variablesIncluded, Collection<Integer> conclusionIndexes, double probability) {
		this.variablesIncluded = variablesIncluded;
		this.conclusionIndexes = new HashSet<>(conclusionIndexes);
		this.probability = probability;
	}
	
	public Constraint(BitRepresentation variablesIncluded, int conclusionIndex, double probability) {
		this.variablesIncluded = variablesIncluded;
		this.conclusionIndexes = new HashSet<>();
		this.conclusionIndexes.add(conclusionIndex);
		this.probability = probability;
	}
	
	public double getProbability(int bin) {
		if (memoTable.containsKey(bin))
			return memoTable.get(bin);
		double prob = getProb(bin);
		memoTable.put(bin, prob);
		return prob;
	}
	
	private double getProb(int bin) {
		BitRepresentation binValue = BitRepresentation.parse(bin, variablesIncluded.size());
		binValue.and(variablesIncluded);
		int numVarsIncluded = variablesIncluded.getCardinality();
		int numTrue = binValue.getCardinality();
		int numFalse = numVarsIncluded - numTrue;
		if (numFalse <= conclusionIndexes.size() ) {
			for (Integer index : conclusionIndexes) {
				if (binValue.get(index))
					continue;
				// one of the conclusion index is false
				numFalse -= 1;
				if (numFalse == 0) {
					// all the false values are the written var
					// early termination
					return 1 - this.probability;
				}
			}
		}
		/*
		 *  if the number of vars that are false is more than the number of conclusion index
		 *  we know that at least one of the non-conclusion var is wrong and thus this statement
		 *  is veraciously true 
		 */
		return this.probability;
	}
	
	public String toString() {
		return "Variables map: " + variablesIncluded + " Conclusions: " + conclusionIndexes + "(" + probability + ")"; 
	}
	
	
	public static double tau(int n) {
		return 0.5 + 0.5 * (2 * Configs.HIGH - 1) * (1/n);
	}
}
