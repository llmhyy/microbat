package microbat.baseline.constraints;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * StatementConstraintA4 is the constraint for special
 * case that the statement only have read/written variables
 * 
 * The constraint represent the case that, under the condition
 * of control dominator is correct, if the one of the
 * read/written variable is wrong, then the statement
 * is wrong
 * 
 * @author David
 *
 */
public class StatementConstraintA4 extends StatementConstraint {

	private static int count = 0;
	
	public StatementConstraintA4(TraceNode node, VarValue var, double propProbability) {
		super(node, propProbability, StatementConstraintA4.genID(), StatementConstraintA4.getWriteStartIndex(node));
		
		if (Constraint.countReadVars(node)==0 && Constraint.countWrittenVars(node)==0) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " do not have any read or written variables to construct the Statement Constraint A4");
		}
		
		if (Constraint.countReadVars(node) != 0 && Constraint.countWrittenVars(node) != 0) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " have both read and written variables. Statement Constraint A4 only handle the case that the node have only read or written variable");
		}
		
		if (!node.isReadVariablesContains(var.getVarID()) && !node.isWrittenVariablesContains(var.getVarID())) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " does not contain the variable " + var.getVarID());
		}
		
		this.addReadVarID(var.getVarID());
		if (node.getControlDominator() != null) {
			this.setControlDomID(StatementConstraint.getControlDomVar(node.getControlDominator()).getVarID());
		}
		
		this.conclusionIndexes.clear();
		this.conclusionIndexes.add(node.getControlDominator() != null ? 2 : 1);
	}
	
//	public StatementConstraintA4(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, int writeVarStarintIdx, int statementOrder) {
//		super(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, StatementConstraintA4.genID(), statementOrder);
//
//	}
//	
//	public StatementConstraintA4(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, int writeVarStarintIdx, int statementOrder, String controlDomID) {
//		super(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, StatementConstraintA4.genID(), statementOrder, controlDomID);
//	}
	
	@Override
	protected double calProbability(int caseNo) {
		BitRepresentation binValue = this.filter(caseNo);
		double prob = -1;
		
		/**
		 * For constraint A4, the invalid case is that, under the condition
		 * that the control dominator is correct, when there are wrong
		 * read/written variable, the statement is still correct.
		 */
		for (int conclusionIndex : this.conclusionIndexes) {
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
				if (binValue.get(conclusionIndex)) {
					prob = 1 - this.propProbability;
				} else {
					prob = this.propProbability;
				}
			} else {
				prob = this.propProbability;
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
	
	private static int getWriteStartIndex(TraceNode node) {
		final int totalLen = node.getControlDominator() != null ? 3 : 2;
		final int writeLen = Constraint.countWrittenVars(node);
		return writeLen == 0 ? totalLen-1 : 0;
	}
	
	@Override
	public String toString() {
		return "Stat Constraint A4 " + super.toString();
	}
	
	private static String genID() {
		return "SC4_" + StatementConstraintA4.count++;
	}
	
	public static void resetID() {
		StatementConstraintA4.count = 0;
	}

}
