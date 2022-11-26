package microbat.probability.BP.constraint;

import java.util.List;

import microbat.model.trace.TraceNode;

/**
 * Statement constraint A1
 * 
 * If all the involved predicate,
 * then the trace node will be correct.
 * 
 * Condition:
 * The node should have both read and written variables.
 * 
 * @author David
 *
 */
public class StatementConstraintA1 extends StatementConstraint {
	
	/**
	 * Number of StatementConstraintA1 generated
	 */
	private static int count = 0;
	
	/**
	 * Constructor
	 * @param node Trace node
	 * @param propProbability Propagation probability
	 */
	public StatementConstraintA1(TraceNode node, double propProbability) {
		super(node, propProbability, StatementConstraintA1.genID());
		
		if (Constraint.countReadVars(node) == 0 || Constraint.countWrittenVars(node) == 0) {
			throw new WrongConstraintConditionException("Node: " + node.getOrder() + " do not have both read and written variable. Cannot construct Statement Constraint A1");
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
	public StatementConstraintA1(BitRepresentation varsIncluded, int conclusionIdx, double propProbability, int order, int writeVarStarintIdx, int predIdx) {
		super(varsIncluded, conclusionIdx, propProbability, StatementConstraintA1.genID(), order, writeVarStarintIdx, predIdx);
	}
	
	/**
	 * Deep Copy Constructor
	 * @param constraint Other constraint
	 */
	public StatementConstraintA1(StatementConstraintA1 constraint) {
		super(constraint);
	}
	
	@Override
	protected double calProbability(int caseNo) {
		BitRepresentation binValue = this.filter(caseNo);
		double prob = -1;
		
		int numVarsIncluded = this.bitRepresentation.getCardinality();
		int numTrue = binValue.getCardinality();
		int numFalse = numVarsIncluded - numTrue;
		
		/*
		 * For A1, the only invalid case is that all the
		 * predicates are correct but the conclusion is
		 * wrong
		 */

		if (numFalse == 1 && !binValue.get(this.conclusionIdx)) {
			prob = 1 - this.propProbability;
		} else {
			prob = this.propProbability;
		}
		
		return prob;
	}
	
	@Override
	protected int defineWriteStartIdx(TraceNode node) {
		final int readLen = Constraint.countReadVars(node);
		final int writeLen = Constraint.countWrittenVars(node);
		
		if (readLen == 0 && writeLen == 0) {
			return Constraint.countPreds(node) + 1;
		} else {
			return readLen;
		}
	}
	
	@Override
	public String toString() {
		return "Stat Constraint A1 " + super.toString();
	}
	
	private static String genID() {
		return "SC1_" + StatementConstraintA1.count++;
	}
	
	public static void resetID() {
		StatementConstraintA1.count = 0;
	}


}
