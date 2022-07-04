package microbat.baseline.constraints;

import microbat.baseline.BitRepresentation;

/**
 * Abstract class for different kind of statement constraints
 * @author David
 *
 */
public abstract class StatementConstraint extends Constraint {
	
	/**
	 * First index of write variable
	 */
	protected int writeVarStartingIdx;
	
	/**
	 * True if this constraint consider the control dominator
	 */
	protected boolean haveControlDom;
	
	/**
	 * Index of control dominator. If this constraint do not consider
	 * control dominator, it will be -1. Other wise, it should be
	 * bit.size() - 2, which is the second last bit
	 */
	protected int predIdx;
	
	/*
	 * Since we do not consider the structure constraint and
	 * the naming constraint, these two class members is not used
	 */
	protected int strucIdx;
	protected int nameIdx;
	
	public StatementConstraint(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, int writeVarStarintIdx, String name) {
		this(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, name, false);
	}
	
	public StatementConstraint(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, int writeVarStarintIdx, String name, boolean haveControlDom) {
		super(varsIncluded, conclusionIndex, propProbability, name);
		this.writeVarStartingIdx = writeVarStarintIdx;
		this.haveControlDom = haveControlDom;
		if (this.haveControlDom) {
			this.predIdx = this.varsIncluded.size() - 2;
		} else {
			this.predIdx = -1;
		}
		this.strucIdx = -1;
		this.nameIdx = -1;
	}
	
	/**
	 * Check is there are any wrong write variable in the given case number
	 * @param caseNo case number
	 * @return True if there are any wrong write variable. False otherwise.
	 */
	protected boolean checkWrongWriteVars(final int caseNo) {
		BitRepresentation binValue = this.filter(caseNo);
		
		boolean haveWrongWriteVar = false;
		// Note that the last index of write variable depends on control dominator exist or not
		final int stopIdx = this.haveControlDom ? this.predIdx : this.varsIncluded.size() - 1;
		for (int idx = this.writeVarStartingIdx; idx < stopIdx; idx++) {
			if (!binValue.get(idx)) {
				haveWrongWriteVar = true;
				break;
			}
		}
		return haveWrongWriteVar;
	}
	
	/**
	 * Check is there are any wrong read variable in the given case number
	 * @param caseNo case number
	 * @return True if there are any wrong read variable. False otherwise.
	 */
	protected boolean checkWrongReadVars(final int caseNo) {
		BitRepresentation binValue = this.filter(caseNo);

		// Check is there any wrong read variable
		boolean haveWrongReadVar = false;
		for (int idx = 0; idx < this.writeVarStartingIdx; idx++) {
			if (!binValue.get(idx)) {
				haveWrongReadVar = true;
				break;
			}
		}
		
		return haveWrongReadVar;
	}
	
	/**
	 * Filter out the variable that is not involved in this constraint
	 * @param caseNo Case number
	 * @return Bit representation of related variables
	 */
	protected BitRepresentation filter(final int caseNo) {
		BitRepresentation binValue = BitRepresentation.parse(caseNo, this.varsIncluded.size());
		binValue.and(this.varsIncluded);
		return binValue;
	}

}
