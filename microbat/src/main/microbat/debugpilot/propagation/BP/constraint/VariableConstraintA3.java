package microbat.debugpilot.propagation.BP.constraint;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * A3 Variable Constraint
 * 
 * If all the read and written variables are correct,
 * then the control dominator is correct as well.
 * 
 * @author David
 *
 */
public class VariableConstraintA3 extends VariableConstraint {

	/**
	 * Number of A3 Variable Constraint generated
	 */
	private static int count = 0;
	
	/**
	 * Constructor
	 * @param node Target node
	 * @param propProbability Propagation probability
	 */
	public VariableConstraintA3(TraceNode node, double propProbability) {
		super(node, propProbability, VariableConstraintA3.genID());
		
		if (node.getControlDominator() == null) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " do not have control dominator. Cannot construct Variable Constraint A3");
		}
		
		if (Constraint.countReadVars(node) == 0 && Constraint.countWrittenVars(node) == 0) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " do not have any read or written variable to constraint Variable Constraint A3");
		}
		
		this.setVarsID(node);
	}

	/**
	 * Constructor
	 * @param bitRepresentation Bit representation of this constraint
	 * @param conclusionIdx Index of conclusion variable
	 * @param propProbability Propagation probability
	 * @param order Order of trace node that this constraint based on
	 */
	public VariableConstraintA3(BitRepresentation bitRepresentation, int conclusionIdx, double propProbability, int order) {
		super(bitRepresentation, conclusionIdx, propProbability, VariableConstraintA3.genID(), order);
	}
	
	/**
	 * Deep Copy Constructor
	 * @param constraint Other constraint
	 */
	public VariableConstraintA3(VariableConstraintA3 constraint) {
		super(constraint);
	}
	
	@Override
	protected BitRepresentation genBitRepresentation(TraceNode node) {
		final int totalLen = Constraint.countPreds(node);
		BitRepresentation varsIncluded = new BitRepresentation(totalLen);
		varsIncluded.set(0, totalLen);
		return varsIncluded;
	}

	@Override
	protected int defineConclusionIdx(TraceNode node) {
		return Constraint.countPreds(node)-1;
	}
	
	@Override
	public String toString() {
		return "Var Constraint A3 " + super.toString();
	}
	
	private static String genID() {
		return "VC3_" + VariableConstraintA3.count++;
	}
	
	public static void resetID() {
		VariableConstraintA3.count = 0;
	}


}
