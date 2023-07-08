package microbat.debugpilot.propagation.BP.constraint;

import microbat.model.trace.TraceNode;

/**
 * Statement constraint A3
 * If the control dominator is correct, when all the read variable is correct, but the write variable is wrong, 
 * then the trace node is likely to be wrong
 * 
 * Condition: Node should have both read and written variables.
 * @author David
 *
 */
public class StatementConstraintA3 extends StatementConstraint {
	
	/**
	 * Number of StatementConstraintA3 generated
	 */
	private static int count = 0;
	
	/**
	 * Constructor
	 * @param node Target trace node
	 * @param propProbability Propagation probability
	 */
	public StatementConstraintA3(TraceNode node, double propProbability) {
		super(node, propProbability, StatementConstraintA3.genID());
		if (Constraint.countReadVars(node) == 0 || Constraint.countWrittenVars(node) == 0) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " do not have both read and written variable. Cannot construct Statement Constraint A3");
		}
		this.setVarsID(node);
	}
	
	/**
	 * Constructor
	 * @param varsIncluded Bit representation of this constraint
	 * @param conclusionIdx Index of conclusion variable
	 * @param propProbability Propagation probability
	 * @param constraintID Constraint ID
	 * @param order Order of trace node that this constraint based on
	 * @param writeVarStartIdx Index of bit that start to represent written variable
	 * @param predIdx Index of control dominator
	 */
	public StatementConstraintA3(BitRepresentation varsIncluded, int conclusionIdx, double propProbability, int order, int writeVarStarintIdx, int predIdx) {
		super(varsIncluded, conclusionIdx, propProbability, StatementConstraintA3.genID(), order, writeVarStarintIdx, predIdx);
	}
	
	/**
	 * Deep Copy Constructor
	 * @param constraint Other constraint
	 */
	public StatementConstraintA3(StatementConstraintA3 constraint) {
		super(constraint);
	}
	
	@Override
	protected double calProbability(int caseNo) {
		BitRepresentation binValue = this.filter(caseNo);
		double prob = -1;
		
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
			if (binValue.get(this.conclusionIdx)) {
				prob =  1 - this.propProbability;
			} else {
				prob = this.propProbability;
			}
		} else {
			prob = this.propProbability;
		}
		
		return prob;
	}
	
	@Override
	protected int defineWriteStartIdx(TraceNode node) {
		return Constraint.countReadVars(node);
	}
	
	@Override
	public String toString() {
		return "Stat constraint A3 " + super.toString();
	}
	
	private static String genID() {
		return "SC3_" + StatementConstraintA3.count++;
	}
	
	public static void resetID() {
		StatementConstraintA3.count = 0;
	}


}
