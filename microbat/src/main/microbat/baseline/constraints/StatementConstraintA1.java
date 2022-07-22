package microbat.baseline.constraints;

import java.util.List;

import microbat.baseline.BitRepresentation;

/**
 * Statement constraint A1
 * If all the involved variable are correct as well as the control dominator,
 * then the trace node is likely to be correct
 * @author David
 *
 */
public class StatementConstraintA1 extends StatementConstraint {
	
	private static int count = 0;
	
	public StatementConstraintA1(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, int writeVarStarintIdx, int statementOrder) {
		super(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, StatementConstraintA1.genID(), statementOrder);

	}
	
	public StatementConstraintA1(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, int writeVarStarintIdx, int statementOrder, int controlDomOrder) {
		super(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, StatementConstraintA1.genID(), statementOrder, controlDomOrder);
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
	
	private static String genID() {
		return "SC_A1_" + StatementConstraintA1.count++;
	}
}
