package microbat.baseline.constraints;

import microbat.baseline.BitRepresentation;

/**
 * Statement constraint A1
 * If all the involved variable are correct as well as the control dominator,
 * then the trace node is likely to be correct
 * @author David
 *
 */
public class StatementConstraintA1 extends StatementConstraint {
	
	public StatementConstraintA1(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, int writeVarStarintIdx) {
		super(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, "STAT_A1_CONSTRAINT");

	}
	
	public StatementConstraintA1(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, int writeVarStarintIdx, boolean haveControlDom) {
		super(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, "STAT_A1_CONSTRAINT", haveControlDom);
	}
	
	@Override
	protected double calProbability(int caseNo) {
		BitRepresentation binValue = this.filter(caseNo);
		double prob = -1;
		
		int numVarsIncluded = this.varsIncluded.getCardinality();
		int numTrue = binValue.getCardinality();
		int numFalse = numVarsIncluded - numTrue;
		
		// For A1, the only invalid case is that all the predicate are correct but
		// the conclusion is wrong
		for (int conclusionIndex : this.conclusionIndexes) {
			if (numFalse == 1 && !binValue.get(conclusionIndex)) {
				prob = 1 - this.propProbability;
			} else {
				prob = this.propProbability;
			}
		}
		return prob;
	}
	
	@Override
	public String toString() {
		return "Stat Constraint A1 " + super.toString();
	}
}
