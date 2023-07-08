package microbat.debugpilot.propagation.BP.constraint;

import microbat.model.trace.TraceNode;

/**
 * StatementConstraintA6 is the constraint for special
 * case that the statement only have control dominator
 * 
 * If the control dominator is correct, then the
 * statement is correct.
 * 
 * @author David
 *
 */
public class StatementConstraintA6 extends StatementConstraint {

	/**
	 * Number of StatementConstraintA6 generated
	 */
	private static int count = 0;
	
	public StatementConstraintA6(TraceNode node, double propProbability) {
		super(node, propProbability, StatementConstraintA6.genID());
		
		if (Constraint.countReadVars(node) != 0 || Constraint.countWrittenVars(node) != 0) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " have read or written variable. Cannot construct Statement Constraint A6");
		}
		
		if (node.getControlDominator() == null) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " do not have control dominator. Cannot construct Statement Constraint A6");
		}
		
		this.setControlDomID(Constraint.extractControlDomVar(node.getControlDominator()).getVarID());
	}

	@Override
	protected double calProbability(int caseNo) {
		BitRepresentation binValue = this.filter(caseNo);
		double prob = -1;

		boolean correctControlDom = binValue.get(this.predIdx);
		if (correctControlDom) {
			 prob = binValue.get(this.conclusionIdx) ? this.propProbability : 1-this.propProbability;
		} else {
			prob = this.propProbability;
		}
		
		return prob;
	}
	
	@Override
	protected int defineWriteStartIdx(TraceNode node) {
		return 2;
	}
	
	@Override
	public String toString() {
		return "Stat Constraint A6 " + super.toString();
	}
	
	private static String genID() {
		return "SC6_" + StatementConstraintA6.count++;
	}
	
	public static void resetID() {
		StatementConstraintA6.count = 0;
	}



}
