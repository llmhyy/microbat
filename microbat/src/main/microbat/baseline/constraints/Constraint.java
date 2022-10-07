package microbat.baseline.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import microbat.baseline.encoders.ProbabilityEncoder;
import microbat.baseline.factorgraph.VarIDConverter;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * Abstract super of all kind of constraints used in the probability inference model
 * @author David, Siang Hwee
 */
public abstract class Constraint {
	
	/**
	 * Bit representation of which variables are involved in this constraint
	 */
	protected BitRepresentation varsIncluded; 
	
	/**
	 * The list of index of conclusion variable
	 */
	protected Set<Integer> conclusionIndexes = new HashSet<>();

	/**
	 * Propagation probability of this constraint
	 */
	protected double propProbability;
	
	/**
	 * ID of this constraint
	 */
	protected final String constraintID;
	
	/**
	 * Store the result probability of different case number in memory table to avoid repeated calculation.
	 */
	protected Map<Integer, Double> memoTable = new HashMap<>();	
	
	/**
	 * ID of control dominator variable (The condition result of control dominator node)
	 */
	protected String controlDomID = "";
	
	/**
	 * List of ID of read variables. Used when constructing the factor graph in python
	 */
	protected List<String> readVarIDs = new ArrayList<>();
	
	/**
	 * List of ID of written variables. Uses when constructing the factor graph in python
	 */
	protected List<String> writeVarIDs  = new ArrayList<>();
	
	protected final int maxCaseNo;
	
	public static final int NaN = -1;
	
	private int order = -1;

//	public static final String controlDomPre = "CD_";

	public Constraint(TraceNode node, Collection<Integer> conclusionIdxes, double probProbability, String constraintID) {
		TraceNode copiedTraceNode = Constraint.removeDupVars(node);
		if (Constraint.countPreds(copiedTraceNode) >= 30) {
			throw new WrongConstraintConditionException("By now, constraint will not handle the case that predicates is more than 30 because it is too expensive to calculate");
		}
		this.varsIncluded = this.genBitRepresentation(copiedTraceNode);
		this.conclusionIndexes = new HashSet<>(conclusionIdxes);
		this.propProbability = probProbability;
		this.constraintID = constraintID;
		this.order = node.getOrder();
		this.maxCaseNo = 1 << this.getBitLength();
	}
	
	public Constraint(TraceNode node, int conclusionIdx, double probProbability, String constraintID) {
		TraceNode copiedTraceNode = Constraint.removeDupVars(node);
		if (Constraint.countPreds(copiedTraceNode) >= 30) {
			throw new WrongConstraintConditionException("By now, constraint will not handle the case that predicates is more than 30 because it is too expensive to calculate");
		}
		this.varsIncluded = this.genBitRepresentation(copiedTraceNode);
		this.conclusionIndexes.add(conclusionIdx);
		this.propProbability = probProbability;
		this.constraintID = constraintID;
		this.order = node.getOrder();
		this.maxCaseNo = 1 << this.getBitLength();
	}
	
	/**
	 * Constructor
	 * @param varsIncluded Bit representation of which variables are involved in this constraint
	 * @param conclusionIndexes The list of index of conclusion variable
	 * @param propProbability Propagation probability of this constraint
	 */
//	public Constraint(BitRepresentation varsIncluded, Collection<Integer> conclusionIndexes, double propProbability, String constraintID) {
//		this.constraintID = constraintID;
//		this.varsIncluded = varsIncluded;
//		this.conclusionIndexes = new HashSet<>(conclusionIndexes);
//		this.propProbability = propProbability;
//	}
	
	/**
	 * Constructor
	 * @param varsIncluded Bit representation of which variables are involved in this constraint
	 * @param conclusionIndex Index of conclusion variable
	 * @param propProbability Propagation probability of this constraint
	 */
//	public Constraint(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, String name) {
//		this.constraintID = name;
//		this.varsIncluded = varsIncluded;
//		this.conclusionIndexes = new HashSet<>();
//		this.conclusionIndexes.add(conclusionIndex); // conclusionIndex is the index of write variable
//		this.propProbability = propProbability;
//	}
	
	/**
	 * Get the result probability based on the given case number.
	 * Result probability will be saved to avoid repeated calculated
	 * @param caseNo Case number
	 * @return Result probability
	 */
	public double getProbability(final int caseNo) {
		
		if (caseNo < 0 || caseNo >= this.maxCaseNo) {
			throw new IllegalArgumentException("TraceNode: " + this.getOrder() + " caseNo: " + caseNo + "exceed the limit: " + this.maxCaseNo);
		}
		if (this.memoTable.containsKey(caseNo)){
			return this.memoTable.get(caseNo);
		}
		double prob = this.calProbability(caseNo);
		this.memoTable.put(caseNo, prob);
		return prob;
	}
	
	/**
	 * Get the length of bit representation of this constraint
	 * @return Length of big representation
	 */
	public int getBitLength() {
		return this.varsIncluded.size();
	}
	
	public void addReadVarID(final String varID) {
		if (!this.readVarIDs.contains(varID)) {
			this.readVarIDs.add(varID);
		}
	}
	
	public void addWriteVarID(final String varID) {
		if (!this.readVarIDs.contains(varID)) {
			this.writeVarIDs.add(varID);
		}
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
	
	public String getConstraintID() {
		return this.constraintID;
	}
	
	public Set<Integer> getConclusionIdxes() {
		return this.conclusionIndexes;
	}
	
	/**
	 * Get the maximum case number for this constraint
	 * @return Maximum case number
	 */
	public int getMaxCaseNo() {
		return this.maxCaseNo;
	}
	
	/**
	 * Calculate the result probability based on the given case number
	 * @param caseNo Case number
	 * @return Result probability
	 */
	abstract protected double calProbability(final int caseNo);
	
	/**
	 * Generate the bit representation based on the type of constraint
	 * @param node Target trace node for bit representation
	 * @return Generated bit representation
	 */
	abstract protected BitRepresentation genBitRepresentation(TraceNode node);
	
	/**
	 * Helper function to determine the index of variable in the bit representation
	 * @param node Trace node that is going to be represented
	 * @param var Target variable
	 * @return Index of target variable in bit representation
	 * @throws IllegalArgumentException if target variable is not contained by given trace node
	 */
	protected static int getBitIndex(TraceNode node, VarValue var) {
		int index = -1;
		
		if (node.isReadVariablesContains(var.getVarID())) {
			index = node.getReadVariables().indexOf(var);
		} else if (node.isWrittenVariablesContains(var.getVarID())) {
			index = node.getReadVariables().size() + node.getWrittenVariables().indexOf(var);
		}
		
		if (index == -1) {
			throw new WrongConstraintConditionException("Trace Node: " + node.getOrder() + " do not contraint variable: " + var.getVarID());
		}
		
		return index;
	}
	
	/**
	 * Remove all the duplicated variable in given node.
	 * 
	 * This method will deep copy the variable such that the original node will node be affected.
	 * 
	 * @param node Node to be copied
	 * @return New trace node will all the duplicated variable is removed.
	 */
	protected static TraceNode removeDupVars(final TraceNode node) {
		TraceNode newNode = new TraceNode(node.getBreakPoint(), node.getProgramState(), node.getOrder(), node.getTrace(), node.getBytecode());
		newNode.setControlDominator(node.getControlDominator());
		
		// We do not use set for removing duplicate variable
		// because we want to maintain the order
		
		List<VarValue> newReadVars = new ArrayList<>();
		for (VarValue readVar : node.getReadVariables()) {
			if (!newReadVars.contains(readVar)) {
				newReadVars.add(readVar);
			}
		}
		newNode.setReadVariables(newReadVars);
		
		List<VarValue> newWrittenVars = new ArrayList<>();
		for (VarValue writeVar : node.getWrittenVariables()) {
			if (!newWrittenVars.contains(writeVar)) {
				newWrittenVars.add(writeVar);
			}
		}
		newNode.setWrittenVariables(newWrittenVars);

		return newNode;
	}
	
	protected static int countReadVars(TraceNode node) {
		return node.getReadVariables().size();
	}
	
	protected static int countWrittenVars(TraceNode node) {
		return node.getWrittenVariables().size();
	}
	
	protected static int countPreds(TraceNode node) {
		int varCount = Constraint.countReadVars(node) + Constraint.countWrittenVars(node);
		return node.getControlDominator() == null ? varCount : varCount + 1;
	}
	
	@Override
	public String toString() {
		return "Variables map: " + this.varsIncluded + " Conclusions: " + this.conclusionIndexes + "(" + this.propProbability + ")"; 
	}
	
	public static void resetID() {
		VariableConstraint.resetID();
		PriorConstraint.resetID();
		StatementConstraint.resetID();
	}
}
