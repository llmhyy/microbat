package microbat.probability.BP.constraint;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * 
 * StatementConstraintA5 is the constraint for special
 * case that the statement only have read/written variables
 * 
 * If the given read/written variable and the control dominator
 * is correct then the statement is correct.
 * 
 * @author David
 *
 */
public class StatementConstraintA5 extends StatementConstraint {

	/**
	 * Number of StatementConstraintA5 generated
	 */
	private static int count = 0;
	
	/**
	 * Constructor
	 * @param node Target trace node
	 * @param var Target variable
	 * @param propProbability Propagation probability
	 */
	public StatementConstraintA5(TraceNode node, VarValue var, double propProbability) {
		super(node, propProbability, StatementConstraintA5.genID());
		
		if (Constraint.countReadVars(node)==0 && Constraint.countWrittenVars(node)==0) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " do not have any read or written variables to construct the Statement Constraint A5");
		}
		
		if (Constraint.countReadVars(node) != 0 && Constraint.countWrittenVars(node) != 0) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " have both read and written variables. Statement Constraint A5 only handle the case that the node have only read or written variable");
		}
		
		if (!node.isReadVariablesContains(var.getVarID()) && !node.isWrittenVariablesContains(var.getVarID())) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " does not contain the variable " + var.getVarID());
		}
		
		this.addReadVarID(var.getVarID());
		if (node.getControlDominator() != null) {
			this.setControlDomID(Constraint.extractControlDomVar(node.getControlDominator()).getVarID());
		}
	}
	
	/**
	 * Constructor
	 * @param varsIncluded Bit representation of this constraint
	 * @param conclusionIdx Index of conclusion variable
	 * @param propProbability Propagation probability
	 * @param constraintID Constraint ID
	 * @param order Order of trace node that this constraint based on
	 * @param writeVarStartIdx Index of bit that start to represent written variable
	 * @param predIdx Index of control dominator
	 */
	public StatementConstraintA5(BitRepresentation varsIncluded, int conclusionIdx, double propProbability, int order, int writeVarStarintIdx, int predIdx) {
		super(varsIncluded, conclusionIdx, propProbability, StatementConstraintA5.genID(), order, writeVarStarintIdx, predIdx);
	}
	
	/**
	 * Deep Copy Constructor
	 * @param constraint Other constraint
	 */
	public StatementConstraintA5(StatementConstraintA5 constraint) {
		super(constraint);
	}
	
	@Override
	protected double calProbability(int caseNo) {
		BitRepresentation binValue = this.filter(caseNo);
		double prob = -1;
		
		boolean haveWrongWriteVar = this.checkWrongWriteVars(caseNo);
		boolean haveWrongReadVar = this.checkWrongReadVars(caseNo);
		
		if (this.haveControlDom()) {
			boolean correctControlDom = binValue.get(this.predIdx);
			if (!correctControlDom) {
				prob = this.propProbability;
				return prob;
			}
		}
		
		if (haveWrongWriteVar || haveWrongReadVar) {
			prob = this.propProbability;
		} else {
			if (binValue.get(this.conclusionIdx)) {
				prob = this.propProbability;
			} else {
				prob = 1 - this.propProbability;
			}
		}
		
		
		return prob;
	}
	
	@Override
	protected BitRepresentation genBitRepresentation(TraceNode node) {
		final int totalLen = node.getControlDominator() != null ? 3 : 2;
		BitRepresentation bitRepresentation = new BitRepresentation(totalLen);
		bitRepresentation.set(0, totalLen);
		return bitRepresentation;
	}
	
	@Override
	protected int defineWriteStartIdx(TraceNode node) {
		final int totalLen = node.getControlDominator() != null ? 3 : 2;
		final int writeLen = Constraint.countWrittenVars(node);
		return writeLen == 0 ? totalLen-1 : 0;
	}
	
	@Override
	protected int defineConclusionIdx(TraceNode node) {
		return node.getControlDominator() != null ? 2 : 1;
	}
	
	@Override
	public String toString() {
		return "Stat Constraint A5 " + super.toString();
	}
	
	private static String genID() {
		return "SC5_" + StatementConstraintA5.count++;
	}
	
	public static void resetID() {
		StatementConstraintA5.count = 0;
	}
}
