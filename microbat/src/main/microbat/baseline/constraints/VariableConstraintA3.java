package microbat.baseline.constraints;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class VariableConstraintA3 extends VariableConstraint {

	private static int count = 0;
	
	public VariableConstraintA3(TraceNode node, double propProbability) {
		super(node, Constraint.countPreds(node) - 1, propProbability, VariableConstraintA3.genID());
		
		if (node.getControlDominator() == null) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " do not have control dominator. Cannot construct Variable Constraint A3");
		}
		
		if (Constraint.countReadVars(node) == 0 && Constraint.countWrittenVars(node) == 0) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " do not have any read or written variable to constraint Variable Constraint A3");
		}
		
		this.setVarsID(node);
	}

	@Override
	protected BitRepresentation genBitRepresentation(TraceNode node) {
		final int totalLen = Constraint.countPreds(node);
		BitRepresentation varsIncluded = new BitRepresentation(totalLen);
		varsIncluded.set(0, totalLen);
		return varsIncluded;
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
