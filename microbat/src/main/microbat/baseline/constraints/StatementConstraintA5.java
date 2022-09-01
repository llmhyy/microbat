package microbat.baseline.constraints;

import microbat.baseline.BitRepresentation;

public class StatementConstraintA5 extends StatementConstraint {

	private static int count = 0;
	
	public StatementConstraintA5(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, int writeVarStarintIdx, int statementOrder) {
		super(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, StatementConstraintA5.genID(), statementOrder);

	}
	
	public StatementConstraintA5(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, int writeVarStarintIdx, int statementOrder, String controlDomID) {
		super(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, StatementConstraintA5.genID(), statementOrder, controlDomID);
	}
	
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
	public String toString() {
		return "Stat Constraint A5 " + super.toString();
	}
	
	private static String genID() {
		return "SC_A5_" + StatementConstraintA5.count++;
	}
	
	public static void resetID() {
		StatementConstraintA5.count = 0;
	}
	

}
