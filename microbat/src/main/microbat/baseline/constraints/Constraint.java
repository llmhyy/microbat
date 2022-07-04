package microbat.baseline.constraints;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import microbat.baseline.BitRepresentation;

/**
 * Abstract super of all kind of constraints used in the probability inference model
 * @author Siang Hwee
 */
public abstract class Constraint {
	
	/**
	 * Bit representation of which variables are involved in this constraint
	 */
	protected BitRepresentation varsIncluded; 
	
	/**
	 * The list of index of conclusion variable
	 */
	protected HashSet<Integer> conclusionIndexes;
	
	/**
	 * Store the result probability of different case number in memory table to avoid repeated calculation.
	 */
	protected HashMap<Integer, Double> memoTable;
	
	/**
	 * Propagation probability of this constraint
	 */
	protected double propProbability;
	
	private final String name;
	
	/**
	 * Constructor
	 * @param varsIncluded Bit representation of which variables are involved in this constraint
	 * @param conclusionIndexes The list of index of conclusion variable
	 * @param propProbability Propagation probability of this constraint
	 */
	public Constraint(BitRepresentation varsIncluded, Collection<Integer> conclusionIndexes, double propProbability, String name) {
		this.name = name;
		this.varsIncluded = varsIncluded;
		this.conclusionIndexes = new HashSet<>(conclusionIndexes);
		this.memoTable = new HashMap<>();
		this.propProbability = propProbability;
	}
	
	/**
	 * Constructor
	 * @param varsIncluded Bit representation of which variables are involved in this constraint
	 * @param conclusionIndex Index of conclusion variable
	 * @param propProbability Propagation probability of this constraint
	 */
	public Constraint(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, String name) {
		this.name = name;
		this.varsIncluded = varsIncluded;
		this.conclusionIndexes = new HashSet<>();
		this.conclusionIndexes.add(conclusionIndex); // conclusionIndex is the index of write variable
		this.memoTable = new HashMap<>();
		this.propProbability = propProbability;
	}
	
	/**
	 * Get the result probability based on the given case number.
	 * Result probability will be saved to avoid repeated calculated
	 * @param caseNo Case number
	 * @return Result probability
	 */
	public double getProbability(final int caseNo) {
		if (this.memoTable.containsKey(caseNo)){
			return this.memoTable.get(caseNo);
		}
		double prob = this.calProbability(caseNo);
		this.memoTable.put(caseNo, prob);
		return prob;
	}
	
	/**
	 * Get the number of variables or statement involved in this constraints
	 * @return Number of variables or statement
	 */
	public int getVarsCount() {
		return this.varsIncluded.size();
	}
	
	/**
	 * Calculate the result probability based on the given case number
	 * @param caseNo Case number
	 * @return Result probability
	 */
	abstract protected double calProbability(final int caseNo);
	
	/**
	 * Get the name of the constraint.
	 * @return Name of the constraint.
	 */
	public String getName() {
		return this.name;
	};
	
	@Override
	public String toString() {
		return "Variables map: " + this.varsIncluded + " Conclusions: " + this.conclusionIndexes + "(" + this.propProbability + ")"; 
	}
	
//	public double getProbability(int bin) {
//	if (memoTable.containsKey(bin))
//		return memoTable.get(bin);
//	double prob = getProb(bin);
//	memoTable.put(bin, prob);
//	return prob;
//}
}
//public class Constraint{
//	protected BitRepresentation variablesIncluded;
//	protected HashSet<Integer> conclusionIndexes;
//	protected double probability;
//	protected HashMap<Integer, Double> memoTable = new HashMap<>();
//	protected ConstraintType constraintType;
//	
//	public Constraint(BitRepresentation variablesIncluded, Collection<Integer> conclusionIndexes, double probability) {
//		this.variablesIncluded = variablesIncluded;
//		this.conclusionIndexes = new HashSet<>(conclusionIndexes);
//		this.probability = probability;
//	}
//	
//	/**
//	 * Constructor of Constraint
//	 * @param variablesIncluded Bit representation of variables included
//	 * @param conclusionIndex Index of the conclusion variable
//	 * @param probability propagate probability
//	 * @param type Type of this constraint
//	 */
//	public Constraint(BitRepresentation variablesIncluded, int conclusionIndex, double probability, ConstraintType type) {
//		this.variablesIncluded = variablesIncluded;
//		this.conclusionIndexes = new HashSet<>();
//		this.conclusionIndexes.add(conclusionIndex); // conclusionIndex is the index of write variable
//		this.probability = probability;
//		this.constraintType = type;
//	}
//	
//	public double getProbability(int bin) {
//		if (memoTable.containsKey(bin))
//			return memoTable.get(bin);
//		double prob = getProb(bin);
//		memoTable.put(bin, prob);
//		return prob;
//	}
//	
//	public void setType(ConstraintType type) {
//		this.constraintType = type;
//	}
//	
//	protected double getProb(int bin) {
//		BitRepresentation binValue = BitRepresentation.parse(bin, variablesIncluded.size());
//		binValue.and(variablesIncluded);
//		int numVarsIncluded = variablesIncluded.getCardinality();
//		int numTrue = binValue.getCardinality();
//		int numFalse = numVarsIncluded - numTrue;
//		if (numFalse <= conclusionIndexes.size() ) {
//			for (Integer index : conclusionIndexes) {
//				if (binValue.get(index))
//					continue;
//				// one of the conclusion index is false
//				numFalse -= 1;
//				if (numFalse == 0) {
//					// all the false values are the written var
//					// early termination
//					return 1 - this.probability;
//				}
//			}
//		}
//		
//		/*
//		 *  if the number of vars that are false is more than the number of conclusion index
//		 *  we know that at least one of the non-conclusion var is wrong and thus this statement
//		 *  is veraciously true 
//		 */
//		return this.probability;
//	}
//	
//	public String toString() {
//		return this.constraintType.name() + " Variables map: " + variablesIncluded + " Conclusions: " + conclusionIndexes + "(" + probability + ")"; 
//	}
//
//	public static double tau(int n) {
//		return 0.5 + 0.5 * (2 * Configs.HIGH - 1) * (1/n);
//	}
//}
