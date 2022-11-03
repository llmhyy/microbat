package microbat.baseline.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import microbat.baseline.factorgraph.VarIDConverter;
import microbat.baseline.probpropagation.BeliefPropagation;
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
	protected BitRepresentation bitRepresentation; 
	
	/**
	 * The index of conclusion variable
	 */
	protected int conclusionIdx;

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
	
	/**
	 * Maximum number of combination of status for this constraint
	 */
	protected final int maxCaseNo;
	
	/**
	 * Indicate some index is not avaiable
	 */
	public static final int NaN = -1;
	
	/**
	 * Order of trace node that this constraint is based
	 */
	private int order = -1;
	
	/**
	 * Constructor
	 * @param node Target node that this constraint is based on
	 * @param conclusionIdx Index of conclusion variable in bit representation
	 * @param probProbability Propagation probability of this constraint
	 * @param constraintID ID of this constraint
	 */
	public Constraint(TraceNode node, double probProbability, String constraintID) {
		TraceNode copiedTraceNode = Constraint.removeDupVars(node);
		
		if (Constraint.countPreds(copiedTraceNode) >= 30) {
			throw new WrongConstraintConditionException("By now, constraint will not handle the case that predicates is more than 30 because it is too expensive to calculate");
		}
		this.bitRepresentation = this.genBitRepresentation(copiedTraceNode);
		this.conclusionIdx = this.defineConclusionIdx(copiedTraceNode);
		this.propProbability = probProbability;
		this.constraintID = constraintID;
		this.order = node.getOrder();
		this.maxCaseNo = 1 << this.getBitLength();
	}
	
	/**
	 * Constructor
	 * @param varsIncluded Bit representation of variable included of this constraint
	 * @param conclusionIdx Index of conclusion variable in bit representation
	 * @param propProbability Propagation probability
	 * @param constraintID Id of this constraint
	 * @param order Order of trace node this constraint based on
	 */
	public Constraint(BitRepresentation varsIncluded, int conclusionIdx, double propProbability, String constraintID, int order) {
		this.bitRepresentation = varsIncluded;
		this.constraintID = constraintID;
		this.conclusionIdx = conclusionIdx;
		this.propProbability = propProbability;
		this.order = order;
		this.maxCaseNo = 1 << this.getBitLength();
	}
	
	/**
	 * Deep Copy Constructor
	 * @param constraint Other constraint
	 */
	public Constraint(Constraint constraint) {
		this.bitRepresentation = constraint.bitRepresentation.clone();
		this.conclusionIdx = constraint.conclusionIdx;
		this.constraintID = constraint.constraintID;
		this.propProbability = constraint.propProbability;
		this.order = constraint.order;
		this.maxCaseNo = constraint.maxCaseNo;
		this.readVarIDs.addAll(constraint.readVarIDs);
		this.writeVarIDs.addAll(constraint.writeVarIDs);
	}
	
	/**
	 * Calculate the result probability based on the given case number.
	 * 
	 * Different kind of constraint have different way to calculate the
	 * probability, so that it is need to be implemented by child classes.
	 * 
	 * @param caseNo Case number
	 * @return Result probability
	 */
	abstract protected double calProbability(final int caseNo);
	
	/**
	 * Generate the bit representation based on the type of constraint
	 * 
	 * Different kind of constraint have different way to generate the
	 * bit representation, so that it is needed to be implemented by child classes.
	 * 
	 * @param node Target trace node for bit representation
	 * @return Generated bit representation
	 */
	abstract protected BitRepresentation genBitRepresentation(TraceNode node);
	
	/**
	 * Define the index of conclusion variable in bit representation
	 * 
	 * Different kind of constraint have different way to define the
	 * conclusion index, so that it is needed to be implemented by child classes.
	 * @return
	 */
	abstract protected int defineConclusionIdx(TraceNode node);
	
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
		return this.bitRepresentation.size();
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
		if (node.getControlDominator() != null) {
			this.setControlDomID(Constraint.extractControlDomVar(node.getControlDominator()).getVarID());
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
	
	public int getConclusionIdx() {
		return this.conclusionIdx;
	}
	
	/**
	 * Get the maximum case number for this constraint
	 * @return Maximum case number
	 */
	public int getMaxCaseNo() {
		return this.maxCaseNo;
	}
	
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
	
	/**
	 * Count number of read variables of given node
	 * @param node Target node
	 * @return Number of read variables
	 */
	public static int countReadVars(TraceNode node) {
		return node.getReadVariables().size();
	}
	
	/**
	 * Count number of written variables of given node
	 * @param node Target node
	 * @return	Number of written variables
	 */
	public static int countWrittenVars(TraceNode node) {
		return node.getWrittenVariables().size();
	}
	
	/**
	 * Count number of predicates involved with target node
	 * @param node Target Node
	 * @return Number of predicates
	 */
	public static int countPreds(TraceNode node) {
		int varCount = Constraint.countReadVars(node) + Constraint.countWrittenVars(node);
		return node.getControlDominator() == null ? varCount : varCount + 1;
	}
	
	/**
	 * Count number of predicates for statement constraint 
	 * @param node Target node
	 * @return Number of predicates
	 */
	public static int countStatementPreds(TraceNode node) {
		return Constraint.countPreds(node) + 1;
	}
	
	/**
	 * Extract the control dominator variable of the given control dominator
	 * @param controlDom Target control dominator
	 * @return Control dominator variable. Null if it does not exist.
	 */
	public static VarValue extractControlDomVar(TraceNode controlDom) {
		for (VarValue writeVar : controlDom.getWrittenVariables()) {
			if (writeVar.getVarID().startsWith(BeliefPropagation.CONDITION_RESULT_ID_PRE)) {
				return writeVar;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "Variables map: " + this.bitRepresentation + " Conclusions: " + this.conclusionIdx + "(" + this.propProbability + ")"; 
	}
	
	/**
	 * Reset the id of all constraints
	 */
	public static void resetID() {
		VariableConstraint.resetID();
		PriorConstraint.resetID();
		StatementConstraint.resetID();
	}
}
