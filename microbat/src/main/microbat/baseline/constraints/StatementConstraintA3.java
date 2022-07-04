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

	public StatementConstraintA3(BitRepresentation varsIncluded, int conclusionIndex, double propProbability,
			int writeVarStarintIdx, boolean haveControlDom) {
		super(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, "STAT_A3_CONSTRAINT", haveControlDom);
	}

	public StatementConstraintA3(BitRepresentation varsIncluded, int conclusionIndex, double propProbability,
			int writeVarStarintIdx) {
		super(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, "STAT_A3_CONSTRAINT");
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
			 * If the control dominator is wrong, then we can directly say that this statement is correct
			 */
			
			if (this.haveControlDom) {
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
}
