package microbat.baseline.constraints;

import java.util.ArrayList;
import java.util.List;

import microbat.baseline.BitRepresentation;
import microbat.model.trace.TraceNode;

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
	 * Index of control dominator. If this constraint do not consider
	 * control dominator, it will be -1. Other wise, it should be
	 * bit.size() - 2, which is the second last bit
	 */
	protected int predIdx;
	
	protected int statementOrder;
	
	protected static final String statIDPre = "S_";
	
	/*
	 * Since we do not consider the structure constraint and
	 * the naming constraint, these two class members is not used
	 */
	protected int strucIdx;
	protected int nameIdx;
	
	public StatementConstraint(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, int writeVarStarintIdx, String constraintID, int statementOrder) {
//		this(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, constraintID, statementOrder, Constraint.NaN);
		this(varsIncluded, conclusionIndex, propProbability, writeVarStarintIdx, constraintID, statementOrder, "");
	}
	
	public StatementConstraint(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, int writeVarStarintIdx, String name, int statementOrder, String controlDomID) {
		super(varsIncluded, conclusionIndex, propProbability, name);
		this.writeVarStartingIdx = writeVarStarintIdx;
//		this.setControlDomOrder(controlDomOrder);
		this.setControlDomID(controlDomID);
		if (this.haveControlDom()) {
			this.predIdx = this.varsIncluded.size() - 2;
		} else {
			this.predIdx = Constraint.NaN;
		}
		this.strucIdx = Constraint.NaN;
		this.nameIdx = Constraint.NaN;
		this.statementOrder = statementOrder;
	}
	
	@Override
	public List<String> getInvolvedPredIDs() {
		List<String> ids = new ArrayList<>();
		ids.addAll(super.getInvolvedPredIDs());
		ids.add(this.genStatID());
		return ids;
	}
	
	public void setStatementOrder(final int order) {
		this.statementOrder = order;
	}
	
	public int getStatementOrder() {
		return this.statementOrder;
	}
	
	protected String genStatID() {
		return StatementConstraint.statIDPre + this.statementOrder;
	}
	
	public static boolean isStatID(final String id) {
		return id.startsWith(StatementConstraint.statIDPre);
	}
	
	public static int extractStatOrderFromID(final String id) {
		return Integer.valueOf(id.replace(StatementConstraint.statIDPre, ""));
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
		final int stopIdx = this.haveControlDom() ? this.predIdx : this.varsIncluded.size() - 1;
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
	
	@Override
	public int getPredicateCount() {
		return this.haveControlDom() ? this.getVarCount() + 2 : this.getVarCount() + 1;
	}

}
