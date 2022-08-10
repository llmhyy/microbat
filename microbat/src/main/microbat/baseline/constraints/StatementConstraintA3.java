package microbat.baseline.constraints;

import microbat.baseline.BitRepresentation;

/**
 * Statement constraint A3
 * If the control dominator is correct, when all the read variable is correct, but the write variable is wrong, 
 * then the trace node is likely to be wrong
 * @author arkwa
 *
 */
public class StatementConstraintA3 extends StatementConstraint {
	
	private static int count = 0;
	
	public StatementConstraintA3(BitRepresentation varsIncluded, int conclusionIndex, double propProbability,
			int writeVarStarintIdx, int statementOrder, String controlDomID) {
		super(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, StatementConstraintA3.genID(), statementOrder, controlDomID);
	}

	public StatementConstraintA3(BitRepresentation varsIncluded, int conclusionIndex, double propProbability,
			int writeVarStarintIdx, int statementOrder) {
		super(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, StatementConstraintA3.genID(), statementOrder);
	}
	
	@Override
	protected double calProbability(int caseNo) {
		BitRepresentation binValue = this.filter(caseNo);
		double prob = -1;
		
		for (int conclusionIdx : this.conclusionIndexes) {

			boolean haveWrongWriteVar = this.checkWrongWriteVars(caseNo);
			boolean haveWrongReadVar = this.checkWrongReadVars(caseNo);
			
			/*
			 * For A3, the invalid case is that when there are at least one write variable
			 * is wrong and all the read variable is correct, the statement is still correct.
			 * 			 
			 * If the control dominator is wrong, then we can directly say that this constraint is correct
			 */
			
			if (this.haveControlDom()) {
				boolean correctControlDom = binValue.get(this.predIdx);
				if (!correctControlDom) {
					prob = this.propProbability;
					return prob;
				}
			}
			
			if (!haveWrongReadVar && haveWrongWriteVar) {
				if (binValue.get(conclusionIdx)) {
					prob =  1 - this.propProbability;
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
	public String toString() {
		return "Stat constraint A3 " + super.toString();
	}
	
	private static String genID() {
		return "SC_A3_" + StatementConstraintA3.count++;
	}
	
	public static void resetID() {
		StatementConstraintA3.count = 0;
	}
}
