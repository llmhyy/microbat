package microbat.baseline.constraints;

import microbat.model.trace.TraceNode;

public class StatementConstraintA6 extends StatementConstraint {

	private static int count = 0;
	
	public StatementConstraintA6(TraceNode node, double propProbability) {
		super(node, propProbability, StatementConstraintA6.genID(), 2);
		
		if (Constraint.countReadVars(node) != 0 || Constraint.countWrittenVars(node) != 0) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " have read or written variable. Cannot construct Statement Constraint A6");
		}
		
		if (node.getControlDominator() == null) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " do not have control dominator. Cannot construct Statement Constraint A6");
		}
		
		this.setControlDomID(StatementConstraint.getControlDomVar(node.getControlDominator()).getVarID());
	}

	@Override
	protected double calProbability(int caseNo) {
		BitRepresentation binValue = this.filter(caseNo);
		double prob = -1;
		for (int conclusionIndex : this.conclusionIndexes) {
			boolean correctControlDom = binValue.get(this.predIdx);
			if (correctControlDom) {
				 prob = binValue.get(conclusionIndex) ? this.propProbability : 1-this.propProbability;
			} else {
				prob = this.propProbability;
			}
		}
		return prob;
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
