package microbat.probability.BP.constraint;

import java.util.ArrayList;
import java.util.List;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.probability.BP.BeliefPropagation;

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
	
	/**
	 * Prefix of the statement constraint ID
	 */
	protected static final String statIDPre = "S_";
	
	/*
	 * Since we do not consider the structure constraint and
	 * the naming constraint, these two class members is not used
	 */
	protected int strucIdx;
	protected int nameIdx;
	
	/**
	 * Constructor 
	 * @param node Trace node
	 * @param propProbability Propagation probability
	 * @param constriantID Constraint ID
	 * @param writeVarStartIdx Index of bit which start to represent written variable
	 */
	public StatementConstraint(TraceNode node, double propProbability, String constriantID) {
		super(node, propProbability, constriantID);
		this.writeVarStartingIdx = this.defineWriteStartIdx(node);
		boolean haveControlDom = node.getControlDominator() != null;
		this.predIdx = haveControlDom ? this.bitRepresentation.size() - 2 : Constraint.NaN;
		this.strucIdx = Constraint.NaN;
		this.nameIdx = Constraint.NaN;
	}
	
	/**
	 * Constructor
	 * @param bitRepresentation Bit representation of constraint
	 * @param conclusionIdx Index of conclusion variable
	 * @param propProbability Propagation probability
	 * @param constraintID Constraint ID
	 * @param order Order of trace node that this constraint based on
	 * @param writeVarStartIdx Index of bit that start to represent written variable
	 * @param predIdx Index of control dominator
	 * @param strucIdx Index of structure predicate
	 * @param nameIdx Index of name predicate
	 */
	public StatementConstraint(BitRepresentation bitRepresentation, int conclusionIdx, double propProbability, 
			String constraintID, int order, int writeVarStartIdx, int predIdx, int strucIdx, int nameIdx) {
		super(bitRepresentation, conclusionIdx, propProbability, constraintID, order);
		this.writeVarStartingIdx = writeVarStartIdx;
		this.predIdx = predIdx;
		this.strucIdx = strucIdx;
		this.nameIdx = nameIdx;
	}
	
	/**
	 * Constructor
	 * @param bitRepresentation Bit representation of constraint
	 * @param conclusionIdx Index of conclusion variable
	 * @param propProbability Propagation probability
	 * @param constraintID Constraint ID
	 * @param order Order of trace node that this constraint based on
	 * @param writeVarStartIdx Index of bit that start to represent written variable
	 * @param predIdx Index of control dominator
	 */
	public StatementConstraint(BitRepresentation bitRepresentation, int conclusionIdx, double propProbability, String constraintID, int order, int writeVarStartIdx, int predIdx) {
		this(bitRepresentation, conclusionIdx, propProbability, constraintID, order, writeVarStartIdx, predIdx, Constraint.NaN, Constraint.NaN);
	}
	
	/**
	 * Deep Copy Constructor
	 * @param constraint Other constraint
	 */
	public StatementConstraint(StatementConstraint constraint) {
		super(constraint);
		this.writeVarStartingIdx = constraint.writeVarStartingIdx;
		this.predIdx = constraint.predIdx;
		this.strucIdx = constraint.strucIdx;
		this.nameIdx = constraint.nameIdx;
	}
	
	/**
	 * Define the index that start represent the written variable.
	 * 
	 * Since different kind of statement constraint have different
	 * way to define the starting index. So that it need to be
	 * implemented by child class.
	 * 
	 * @param node Target trace node
	 * @return Index that start represent the written variable
	 */
	abstract protected int defineWriteStartIdx(TraceNode node);
	
	@Override
	public List<String> getInvolvedPredIDs() {
		/**
		 * For statement encoder, we need to
		 * consider the statement predicate
		 */
		List<String> ids = new ArrayList<>();
		ids.addAll(super.getInvolvedPredIDs());
		ids.add(this.genStatID());
		return ids;
	}
	
	@Override
	protected int defineConclusionIdx(TraceNode node) {
		return Constraint.countPreds(node);
	}
	
	/**
	 * Generate statement constraint ID
	 * @return Statement constraint ID
	 */
	protected String genStatID() {
		return StatementConstraint.statIDPre + this.getOrder();
	}
	
	/**
	 * Check is the given ID a statement id
	 * @param id String to check
	 * @return True if the given ID is a statement ID
	 */
	public static boolean isStatID(final String id) {
		return id.startsWith(StatementConstraint.statIDPre);
	}
	
	/**
	 * Statement ID contain the node order information, this
	 * method will extract the node order from the ID
	 * 
	 * @param id Target ID
	 * @return Trace node order obtained from the ID
	 */
	public static int extractStatOrderFromID(final String id) {
		return Integer.valueOf(id.replace(StatementConstraint.statIDPre, ""));
	}
	
	/**
	 * Check is there are any wrong write variable in the given case number
	 * 
	 * If there are no written variable, then it will return False
	 * @param caseNo case number
	 * @return True if there are any wrong write variable. False otherwise.
	 */
	protected boolean checkWrongWriteVars(final int caseNo) {
		BitRepresentation binValue = this.filter(caseNo);
		
		boolean haveWrongWriteVar = false;
		// Note that the last index of write variable depends on control dominator exist or not
		final int stopIdx = this.haveControlDom() ? this.predIdx : this.bitRepresentation.size() - 1;
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
	 * 
	 * If there are no wrong read variable, then it will return false
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
		BitRepresentation binValue = BitRepresentation.parse(caseNo, this.bitRepresentation.size());
		binValue.and(this.bitRepresentation);
		return binValue;
	}
	
	@Override
	public int getPredicateCount() {
		/*
		 * For statement constraint, we need to
		 * consider the statement predicate 
		 */
		return super.getPredicateCount() + 1;
	}
	
	@Override
	protected BitRepresentation genBitRepresentation(TraceNode node) {
		final int totalLen = Constraint.countPreds(node)+1;
		BitRepresentation bitRepresentation = new BitRepresentation(totalLen);
		bitRepresentation.set(0, totalLen);
		return bitRepresentation;
	}

	/**
	 * Reset all the statement constraint count
	 */
	public static void resetID() {
		StatementConstraintA1.resetID();
		StatementConstraintA2.resetID();
		StatementConstraintA3.resetID();
		StatementConstraintA4.resetID();
		StatementConstraintA5.resetID();
	}
}
