package microbat.baseline.constraints;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class StatementConstraintA5 extends StatementConstraint {

	private static int count = 0;
	
	public StatementConstraintA5(TraceNode node, VarValue var, double propProbability) {
		super(node, propProbability, StatementConstraintA5.genID(), StatementConstraintA5.getWriteStartIndex(node));
		
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
			this.setControlDomID(StatementConstraint.getControlDomVar(node.getControlDominator()).getVarID());
		}
		this.conclusionIndexes.clear();
		this.conclusionIndexes.add(node.getControlDominator() != null ? 2 : 1);
	}
	
//	public StatementConstraintA5(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, int writeVarStarintIdx, int statementOrder) {
//		super(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, StatementConstraintA5.genID(), statementOrder);
//
//	}
//	
//	public StatementConstraintA5(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, int writeVarStarintIdx, int statementOrder, String controlDomID) {
//		super(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, StatementConstraintA5.genID(), statementOrder, controlDomID);
//	}
	
	@Override
	protected double calProbability(int caseNo) {
		BitRepresentation binValue = this.filter(caseNo);
		double prob = -1;
		
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
				prob = this.propProbability;
			} else {
				if (binValue.get(conclusionIndex)) {
					prob = this.propProbability;
				} else {
					prob = 1 - this.propProbability;
				}
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
		return "Stat Constraint A5 " + super.toString();
	}
	
	private static String genID() {
		return "SC5_" + StatementConstraintA5.count++;
	}
	
	public static void resetID() {
		StatementConstraintA5.count = 0;
	}
	

}
