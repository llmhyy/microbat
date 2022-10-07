package microbat.baseline.constraints;

import microbat.baseline.encoders.ProbabilityEncoder;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class VariableConstraintA1 extends VariableConstraint {

	private static int count = 0;
	
	public VariableConstraintA1(TraceNode node, VarValue conclusionVar, double probProbability) {
		super(node, Constraint.countReadVars(Constraint.removeDupVars(node)), probProbability, VariableConstraintA1.genID());
		
		if (!node.isWrittenVariablesContains(conclusionVar.getVarID())) {
			throw new WrongConstraintConditionException("Conclusion variable; " + conclusionVar.getVarID() + " does not belongs to this node: " + node.getOrder());
		}
		
		if (Constraint.countWrittenVars(node) == 0) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " does not have any written variable to form a A1 Variable Constraint");
		}
		
		if (Constraint.countReadVars(node) == 0 && node.getControlDominator() == null) {
			throw new WrongConstraintConditionException("Cannot form Variable Constraint A1 without any read variables and control dominator");
		}
		
		// Cannot use setVarID method here because we will only consider one written variable here
		for (VarValue readVar : node.getReadVariables()) {
			this.addReadVarID(readVar.getVarID());
		}
		
		this.addWriteVarID(conclusionVar.getVarID());
		
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

	@Override
	protected BitRepresentation genBitRepresentation(TraceNode node) {
		final int readLen = Constraint.countReadVars(node);
		final int totalLen = node.getControlDominator() != null ? readLen + 2 : readLen + 1;
		
		BitRepresentation bitRepresentation = new BitRepresentation(totalLen);
		bitRepresentation.set(0, totalLen);
		
		return bitRepresentation;
	}
	
	@Override
	public String toString() {
		return "Var Constraint A1 " + super.toString();
	}
	
	private static String genID() {
		return "VC1_" + VariableConstraintA1.count++;
	}
	
	public static void resetID() {
		VariableConstraintA1.count = 0;
	}

}
