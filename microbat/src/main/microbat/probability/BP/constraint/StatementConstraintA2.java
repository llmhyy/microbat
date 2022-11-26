package microbat.probability.BP.constraint;

import microbat.model.trace.TraceNode;

/**
 * Statement constraint A2
 * If the control dominator is correct, but at least one write variable and read variable is wrong
 * at the same time, then the trace node is still likely to be correct
 * 
 * Condition: The given node should have both read and written variable
 * @author David
 *
 */
public class StatementConstraintA2 extends StatementConstraint {
	
	/**
	 * Number of StatementConstraintA2 generated
	 */
	private static int count = 0;
	
	/**
	 * Constructor
	 * @param node Trace node
	 * @param propProbability Propagation probability
	 */
	public StatementConstraintA2(TraceNode node, double propProbability) {
		super(node, propProbability, StatementConstraintA2.genID());
		if (Constraint.countReadVars(node)==0 || Constraint.countWrittenVars(node) == 0) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " do not have both read and written variable. Cannot construct Statement Constraint A2");
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
	public StatementConstraintA2(BitRepresentation varsIncluded, int conclusionIdx, double propProbability, int order, int writeVarStarintIdx, int predIdx) {
		super(varsIncluded, conclusionIdx, propProbability, StatementConstraintA2.genID(), order, writeVarStarintIdx, predIdx);
	}
	
	/**
	 * Deep Copy Constructor
	 * @param constraint Other constraint
	 */
	public StatementConstraintA2(StatementConstraintA2 constraint) {
		super(constraint);
	}
	
	@Override
	protected double calProbability(int caseNo) {
		
		BitRepresentation binValue = this.filter(caseNo);
		double prob = -1;
		
		boolean haveWrongWriteVar = this.checkWrongWriteVars(caseNo);
		boolean haveWrongReadVar = this.checkWrongReadVars(caseNo);
		
		/*
		 * For A2, the invalid case is that, when the control dominator is correct and 
		 * there are write variable is wrong and at least one of the read variable is wrong, the statement is wrong. 
		 * All the other cases are valid.
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
		
		if (haveWrongWriteVar && haveWrongReadVar) {
			if (binValue.get(this.conclusionIdx)) {
				prob = this.propProbability;
			} else {
				prob = 1 - this.propProbability;
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
		return "Stat constraint A2 " + super.toString();
	}
	
	private static String genID() {
		return "SC2_" + StatementConstraintA2.count++;
	}
	
	public static void resetID() {
		StatementConstraintA2.count = 0;
	}


}
