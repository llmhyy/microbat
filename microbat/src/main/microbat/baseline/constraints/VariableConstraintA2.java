package microbat.baseline.constraints;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class VariableConstraintA2 extends VariableConstraint {

	private static int count = 0;
	
	public VariableConstraintA2(TraceNode node, VarValue conclusionVar, double propProbability) {
		super(node, Constraint.getBitIndex(Constraint.removeDupVars(node), conclusionVar), propProbability, VariableConstraintA2.genID());
		
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
		return "Var Constraint A2 " + super.toString();
	}
	
	private static String genID() {
		return "VC2_" + VariableConstraintA2.count++;
	}
	
	public static void resetID() {
		VariableConstraintA2.count = 0;
	}

}
