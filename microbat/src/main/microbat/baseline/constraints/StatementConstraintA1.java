package microbat.baseline.constraints;

import java.util.List;

import microbat.model.trace.TraceNode;

/**
 * Statement constraint A1
 * 
 * If all the involved variable are correct as well as the control dominator,
 * then the trace node is likely to be correct.
 * 
 * Condition:
 * The node should have both read and written variables.
 * 
 * @author David
 *
 */
public class StatementConstraintA1 extends StatementConstraint {
	
	private static int count = 0;
	
	public StatementConstraintA1(TraceNode node, double propProbability) {
		super(node, propProbability, StatementConstraintA1.genID(), StatementConstraintA1.getWriteStartIndex(node));
		
		if (Constraint.countReadVars(node) == 0 || Constraint.countWrittenVars(node) == 0) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " do not have both read and written variable. Cannot construct Statement Constraint A1");
		}
		
		this.setVarsID(node);
	}
	
//	public StatementConstraintA1(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, int writeVarStarintIdx, int statementOrder) {
//		super(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, StatementConstraintA1.genID(), statementOrder);
//
//	}
//	
//	public StatementConstraintA1(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, int writeVarStarintIdx, int statementOrder, String controlDomID) {
//		super(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, StatementConstraintA1.genID(), statementOrder, controlDomID);
//	}
	
	@Override
	protected double calProbability(int caseNo) {
		BitRepresentation binValue = this.filter(caseNo);
		double prob = -1;
		
		int numVarsIncluded = this.varsIncluded.getCardinality();
		int numTrue = binValue.getCardinality();
		int numFalse = numVarsIncluded - numTrue;
		
		/*
		 * For A1, the only invalid case is that all the
		 * predicates are correct but the conclusion is
		 * wrong
		 */

		for (int conclusionIndex : this.conclusionIndexes) {
			if (numFalse == 1 && !binValue.get(conclusionIndex)) {
				prob = 1 - this.propProbability;
			} else {
				prob = this.propProbability;
			}
		}
		return prob;
	}
	
	private static int getWriteStartIndex(TraceNode node) {
		final int readLen = Constraint.countReadVars(node);
		final int writeLen = Constraint.countWrittenVars(node);
		
		if (readLen == 0 && writeLen == 0) {
			return Constraint.countPreds(node) + 1;
		} else {
			return readLen;
		}
	}
	
	@Override
	public String toString() {
		return "Stat Constraint A1 " + super.toString();
	}
	
	private static String genID() {
		return "SC1_" + StatementConstraintA1.count++;
	}
	
	public static void resetID() {
		StatementConstraintA1.count = 0;
	}
}
