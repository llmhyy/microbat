package microbat.baseline.constraints;

import microbat.baseline.BitRepresentation;

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
	
	public StatementConstraintA4(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, int writeVarStarintIdx, int statementOrder) {
		super(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, StatementConstraintA4.genID(), statementOrder);

	}
	
	public StatementConstraintA4(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, int writeVarStarintIdx, int statementOrder, String controlDomID) {
		super(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, StatementConstraintA4.genID(), statementOrder, controlDomID);
	}
	
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
	public String toString() {
		return "Stat Constraint A4 " + super.toString();
	}
	
	private static String genID() {
		return "SC_A4_" + StatementConstraintA4.count++;
	}
	
	public static void resetID() {
		StatementConstraintA4.count = 0;
	}

}
