package microbat.baseline.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import microbat.baseline.BitRepresentation;
import microbat.baseline.encoders.ProbabilityEncoder;
import microbat.baseline.factorgraph.VarIDConverter;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

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
	
	protected final String constraintID;
	
//	protected int controlDomOrder;
	
	protected String controlDomID;
	
	protected List<String> readVarIDs;
	
	protected List<String> writeVarIDs;
	
	public static final int NaN = -1;
	
	public static final String controlDomPre = "CD_";
	
	
	/**
	 * Constructor
	 * @param varsIncluded Bit representation of which variables are involved in this constraint
	 * @param conclusionIndexes The list of index of conclusion variable
	 * @param propProbability Propagation probability of this constraint
	 */
	public Constraint(BitRepresentation varsIncluded, Collection<Integer> conclusionIndexes, double propProbability, String constraintID) {
		this.constraintID = constraintID;
		this.varsIncluded = varsIncluded;
		this.conclusionIndexes = new HashSet<>(conclusionIndexes);
		this.memoTable = new HashMap<>();
		this.propProbability = propProbability;
//		this.controlDomOrder = Constraint.NaN;
		this.controlDomID = "";
		this.readVarIDs = new ArrayList<>();
		this.writeVarIDs = new ArrayList<>();
		
	}
	
	/**
	 * Constructor
	 * @param varsIncluded Bit representation of which variables are involved in this constraint
	 * @param conclusionIndex Index of conclusion variable
	 * @param propProbability Propagation probability of this constraint
	 */
	public Constraint(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, String name) {
		this.constraintID = name;
		this.varsIncluded = varsIncluded;
		this.conclusionIndexes = new HashSet<>();
		this.conclusionIndexes.add(conclusionIndex); // conclusionIndex is the index of write variable
		this.memoTable = new HashMap<>();
		this.propProbability = propProbability;
		this.controlDomID = "";
		
		this.readVarIDs = new ArrayList<>();
		this.writeVarIDs = new ArrayList<>();
		
	}
	
	/**
	 * Get the result probability based on the given case number.
	 * Result probability will be saved to avoid repeated calculated
	 * @param caseNo Case number
	 * @return Result probability
	 */
	public double getProbability(final int caseNo) {
//		if (this.memoTable.containsKey(caseNo)){
//			return this.memoTable.get(caseNo);
//		}
		double prob = this.calProbability(caseNo);
//		this.memoTable.put(caseNo, prob);
		return prob;
	}
	
	/**
	 * Get the number of variables or statement involved in this constraints
	 * @return Number of variables or statement
	 */
	public int getVarsCount() {
		return this.varsIncluded.size();
	}
	
	public void addReadVarID(final String varID) {
		this.readVarIDs.add(varID);
	}
	
	public void addWriteVarID(final String varID) {
		this.writeVarIDs.add(varID);
	}
	
	public List<String> getReadVarIDs() {
		return this.readVarIDs;
	}
	
	public int getReadVarCount() {
		return this.readVarIDs.size();
	}
	
	public List<String> getWriteVarIDs() {
		return this.writeVarIDs;
	}
	
	public int getWriteVarCount() {
		return this.writeVarIDs.size();
	}
	
	public List<String> getInvolvedVarIDs() {
		List<String> varIDs = new ArrayList<>();
		varIDs.addAll(this.readVarIDs);
		varIDs.addAll(this.writeVarIDs);
		return varIDs;
	}
	
	public List<String> getInvolvedPredIDs() {
		List<String> ids = new ArrayList<>();
		ids.addAll(this.getInvolvedVarIDs());
		
		if (this.haveControlDom()) {
			ids.add(this.genControlDomID());
		}
		return ids;
	}
	
	protected String genControlDomID() {
		if (this.haveControlDom()) {
//			return Constraint.controlDomPre + this.getControlDomOrder();
			return this.controlDomID;
		} else {
			return null;
		}
	}
	
	public static boolean isControlDomID(final String id) {
		return id.startsWith(Constraint.controlDomPre);
	}
	
	public static int extractNodeOrderFromCDID(final String id) {
		return Integer.valueOf(id.replace(Constraint.controlDomPre, ""));
	}
	
	public void setVarsID(TraceNode node) {
		for (VarValue readVar : node.getReadVariables()) {
			this.readVarIDs.add(readVar.getVarID());
		}
		for (VarValue writeVar : node.getWrittenVariables()) {
			this.readVarIDs.add(writeVar.getVarID());
		}
		
		TraceNode controlDom = node.getControlDominator();
//		if (controlDom != null) {
//			this.setControlDomOrder(controlDom.getOrder());
//		}
		if (controlDom != null) {
			for (VarValue writeVar : controlDom.getWrittenVariables()) {
				if (writeVar.getVarID().startsWith(ProbabilityEncoder.CONDITION_RESULT_ID_PRE)) {
					this.setControlDomID(writeVar.getVarID());
					break;
				}
			}
		}
	}
	
	public int getVarCount() {
		return this.readVarIDs.size() + this.writeVarIDs.size();
	}
	
	// This function have to be override in Statement Constraint
	public int getPredicateCount() {
		return this.haveControlDom() ? this.getVarCount() + 1 : this.getVarCount();
	}
	
	public void setControlDomID(final String controlDomID) {
		this.controlDomID = controlDomID;
	}
	
	public String getControlDomID() {
		return this.controlDomID;
	}
	
	public boolean haveControlDom() {
		return this.controlDomID != "";
	}
	
//	public void setControlDomOrder(final int order) {
//		this.controlDomOrder = order;
//	}
//	
//	public int getControlDomOrder() {
//		return this.controlDomOrder;
//	}
//	
//	public boolean haveControlDom() {
//		return this.controlDomOrder != Constraint.NaN;
//	}
	
	public String getConstraintID() {
		return this.constraintID;
	}
	
	public HashSet<Integer> getConclusionIdxes() {
		return this.conclusionIndexes;
	}
	
	/**
	 * Calculate the result probability based on the given case number
	 * @param caseNo Case number
	 * @return Result probability
	 */
	abstract protected double calProbability(final int caseNo);
	
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
