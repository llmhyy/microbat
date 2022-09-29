package microbat.baseline.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	
	/**
	 * ID of this constraint
	 */
	protected final String constraintID;
	
	/**
	 * ID of control dominator variable (The condition result of control dominator node)
	 */
	protected String controlDomID;
	
	/**
	 * List of ID of read variables. Used when constructing the factor graph in python
	 */
	protected Set<String> readVarIDs;
	
	/**
	 * List of ID of written variables. Uses when constructing the factor graph in python
	 */
	protected Set<String> writeVarIDs;
	
	public static final int NaN = -1;
	
	private int order = -1;

//	public static final String controlDomPre = "CD_";
	
	
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
		this.readVarIDs = new HashSet<>();
		this.writeVarIDs = new HashSet<>();
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
		
		this.readVarIDs = new HashSet<>();
		this.writeVarIDs = new HashSet<>();
		
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
	
	public void addReadVarID(final String varID) {
		this.readVarIDs.add(varID);
	}
	
	public void addWriteVarID(final String varID) {
		this.writeVarIDs.add(varID);
	}
	
	public Set<String> getReadVarIDs() {
		return this.readVarIDs;
	}
	
	public int getReadVarCount() {
		return this.readVarIDs.size();
	}
	
	public Set<String> getWriteVarIDs() {
		return this.writeVarIDs;
	}
	
	public int getWriteVarCount() {
		return this.writeVarIDs.size();
	}
	
	/**
	 * Get the ID of all involved variable.
	 * Note that it does not include the control dominator variable
	 * @return List of variable ID
	 */
	public List<String> getInvolvedVarIDs() {
		List<String> varIDs = new ArrayList<>();
		varIDs.addAll(this.readVarIDs);
		varIDs.addAll(this.writeVarIDs);
		return varIDs;
	}
	
	/**
	 * Get the ID of all involved predicates.
	 * This method must be override by statement constraint
	 * because it have extra predicates
	 * @return List of predicates ID
	 */
	public List<String> getInvolvedPredIDs() {
		List<String> ids = new ArrayList<>();
		ids.addAll(this.getInvolvedVarIDs());
		
		if (this.haveControlDom()) {
			ids.add(this.controlDomID);
		}
		return ids;
	}
	
//	protected String genControlDomID() {
//		if (this.haveControlDom()) {
////			return Constraint.controlDomPre + this.getControlDomOrder();
//			return this.controlDomID;
//		} else {
//			return null;
//		}
//	}
	
//	public static boolean isControlDomID(final String id) {
//		return id.startsWith(Constraint.controlDomPre);
//	}
//	
//	public static int extractNodeOrderFromCDID(final String id) {
//		return Integer.valueOf(id.replace(Constraint.controlDomPre, ""));
//	}
	
	/**
	 * Add all the involved predicates ID of node into record
	 * @param node Target trace node
	 */
	public void setVarsID(TraceNode node) {
		
		for (VarValue readVar : node.getReadVariables()) {
			this.addReadVarID(readVar.getVarID());
		}
		
		for (VarValue wirteVar : node.getWrittenVariables()) {
			this.addWriteVarID(wirteVar.getVarID());
		}
		
		// We assume that the control dominator is included when it exists
		TraceNode controlDom = node.getControlDominator();
		if (controlDom != null) {
			for (VarValue writeVar : controlDom.getWrittenVariables()) {
				if (writeVar.getVarID().startsWith(ProbabilityEncoder.CONDITION_RESULT_ID_PRE)) {
					this.setControlDomID(writeVar.getVarID());
					break;
				}
			}
		}
		
		this.setOrder(node.getOrder());
	}
	
	public int getVarCount() {
		return this.readVarIDs.size() + this.writeVarIDs.size();
	}
	
	public void setOrder(int order) {
		this.order = order;
	}
	
	public int getOrder() {
		return this.order;
	}
	
	/**
	 * Count the number of predicates involved in this constraint
	 * This method must be override by statement encoder because
	 * it has different way to count the predicate.
	 * @return Number of predicates
	 */
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
	
	public static void resetID() {
		VariableConstraint.resetID();
		PriorConstraint.resetID();
		
		StatementConstraint.resetID();
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
