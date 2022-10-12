package microbat.baseline.constraints;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * A2 Variable Constraint
 * 
 * If all the written variable, control dominator, and other read variable are correct,
 * then the target read variable is also correct
 * @author David
 *
 */
public class VariableConstraintA2 extends VariableConstraint {

	/**
	 * Number of A2 Variable Constraint Generated
	 */
	private static int count = 0;
	
	/**
	 * Target read variable
	 */
	private final VarValue conclusionVar;
	
	/**
	 * Constructor 
	 * @param node Trace Node
	 * @param conclusionVar Conclusion read variable which must be contained by target node
	 * @param propProbability Propagation probability
	 */
	public VariableConstraintA2(TraceNode node, VarValue conclusionVar, double propProbability) {
		super(node, propProbability, VariableConstraintA2.genID());
		
		if (!node.isReadVariablesContains(conclusionVar.getVarID())) {
			throw new WrongConstraintConditionException("Conclusion variable: " + conclusionVar.getVarID() + " does not belongs to this node: " + node.getOrder());
		}
		
		if (Constraint.countReadVars(node) == 0) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " does not contain any read variable. Cannot construct Variable Constraint A2");
		}
		
		if (Constraint.countWrittenVars(node) == 0 && node.getControlDominator() == null) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " do not have any written variable or control dominator to construct A2 Varaible Constraint");
		}
		
		this.setVarsID(node);
		this.conclusionVar = conclusionVar.clone();
		this.conclusionIdx = this._defineConclusionIdx(node);
	}
	
	/**
	 * Constructor
	 * @param bitRepresentation Bit representation
	 * @param conclusionIdx Index of conclusion variable
	 * @param propProbability Propagation probability
	 * @param order Order of target node this constraint based on
	 */
	public VariableConstraintA2(BitRepresentation bitRepresentation, int conclusionIdx, double propProbability, int order) {
		super(bitRepresentation, conclusionIdx, propProbability, VariableConstraintA2.genID(), order);
		this.conclusionVar = null;
	}
	
	/**
	 * Deep Copy constructor
	 * @param constraint Other constraint
	 */
	public VariableConstraintA2(VariableConstraintA2 constraint) {
		super(constraint);
		this.conclusionVar = constraint.conclusionVar == null ? null : constraint.conclusionVar.clone();
	}
	
	@Override
	protected BitRepresentation genBitRepresentation(TraceNode node) {
		
		if (Constraint.countPreds(node) == 0) {
			throw new WrongConstraintConditionException("The given node do not have any predicates. Cannot construct constraint.");
		}
		final int totalLen = Constraint.countPreds(node);
		BitRepresentation varsIncluded = new BitRepresentation(totalLen);
		varsIncluded.set(0, totalLen);
		return varsIncluded;
	}
	
	protected int _defineConclusionIdx(TraceNode node) {
		int index = -1;
		
		if (node.isReadVariablesContains(this.conclusionVar.getVarID())) {
			index = node.getReadVariables().indexOf(this.conclusionVar);
		} else if (node.isWrittenVariablesContains(this.conclusionVar.getVarID())) {
			index = node.getReadVariables().size() + node.getWrittenVariables().indexOf(this.conclusionVar);
		}
		
		if (index == -1) {
			throw new WrongConstraintConditionException("Trace Node: " + node.getOrder() + " do not contraint variable: " + this.conclusionVar.getVarID());
		}
		
		return index;
	}
	
	@Override
	protected int defineConclusionIdx(TraceNode node) {
		return -1;
	}
	
	@Override
	public String toString() {
		return "Var Constraint A2 " + super.toString();
	}
	
	private static String genID() {
		return "VC2_" + VariableConstraintA2.count++;
	}
	
	public static void resetID() {
		VariableConstraintA2.count = 0;
	}



}
