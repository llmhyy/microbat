package microbat.baseline.constraints;

import microbat.baseline.beliefpropagation.PropabilityInference;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * A1 Variable Constraint
 * 
 * If all the read variable and control dominator is correct,
 * the target written variable is also correct
 * @author David
 *
 */
public class VariableConstraintA1 extends VariableConstraint {
	
	/**
	 * Number of A1 variable constraint generated
	 */
	private static int count = 0;
	
	/**
	 * Constructor
	 * @param node	Trace node
	 * @param conclusionVar Conclusion variable which should be the written variable of the given trace node
	 * @param probProbability Propagation probability
	 */
	public VariableConstraintA1(TraceNode node, VarValue conclusionVar, double probProbability) {
		super(node, probProbability, VariableConstraintA1.genID());
		
		if (!node.isWrittenVariablesContains(conclusionVar.getVarID())) {
			throw new WrongConstraintConditionException("Conclusion variable; " + conclusionVar.getVarID() + " does not belongs to this node: " + node.getOrder());
		}
		
		if (Constraint.countWrittenVars(node) == 0) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " does not have any written variable to form a A1 Variable Constraint");
		}
		
		if (Constraint.countReadVars(node) == 0 && node.getControlDominator() == null) {
			throw new WrongConstraintConditionException("Cannot form Variable Constraint A1 without any read variables and control dominator for node: " + node.getOrder());
		}
		
		// Cannot use setVarID method here because we will only consider one written variable here
		for (VarValue readVar : node.getReadVariables()) {
			this.addReadVarID(readVar.getVarID());
		}
		
		this.addWriteVarID(conclusionVar.getVarID());
		
		TraceNode controlDom = node.getControlDominator();
		if (controlDom != null) {
			for (VarValue writeVar : controlDom.getWrittenVariables()) {
				if (writeVar.getVarID().startsWith(PropabilityInference.CONDITION_RESULT_ID_PRE)) {
					this.setControlDomID(writeVar.getVarID());
					break;
				}
			}
		}
	}
	
	/**
	 * Constructor
	 * @param bitRepresentation Bit representation of constraint
	 * @param conclusionIdx Index of conclusion index in bit representation
	 * @param propProbability Propagation probability
	 * @param order Order of trace node that this constraint is based on
	 */
	public VariableConstraintA1(BitRepresentation bitRepresentation, int conclusionIdx, double propProbability, int order) {
		super(bitRepresentation, conclusionIdx, propProbability, VariableConstraintA1.genID(), order);
	}
	
	/**
	 * Deep copy constructor
	 * @param constraint Other constraint
	 */
	public VariableConstraintA1(VariableConstraintA1 constraint) {
		super(constraint);
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
	protected int defineConclusionIdx(TraceNode node) {
		return Constraint.countReadVars(node);
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
