package microbat.debugpilot.propagation.BP.constraint;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * Prior constraint represent how likely the conclusion variable is correct
 * @author David
 */
public class PriorConstraint extends Constraint {
	
	/**
	 * Number of PriorConstraint generated
	 */
	private static int count = 0;
	
	/**
	 * Constructor
	 * @param node	Target node
	 * @param var	Target variable
	 * @param probProbability Propagation probability
	 */
	public PriorConstraint(VarValue var, double probProbability) {
		super(new TraceNode(null, null, -1, null, null), probProbability, PriorConstraint.genID());
		this.addReadVarID(var.getVarID());
	}
	
	/**
	 * Constructor
	 * @param propProbability Propagation probability
	 */
	public PriorConstraint(double propProbability) {
		super(new BitRepresentation(1), 0, propProbability, PriorConstraint.genID(), -1);
	}
	
	/**
	 * Deep Copy Constructor
	 * @param constraint Other constraint
	 */
	public PriorConstraint(PriorConstraint constraint) {
		super(constraint);
	}

	@Override
	protected double calProbability(int caseNo) {
		
		/*
		 * The probability directly depends on whether
		 * the conclusion predicate is true or false
		 * in the given case number
		 */
		
		BitRepresentation binValue = BitRepresentation.parse(caseNo, this.bitRepresentation.size());
		binValue.and(this.bitRepresentation);
		return binValue.get(this.conclusionIdx) ? this.propProbability : 1-this.propProbability;
	}
	
	@Override
	protected BitRepresentation genBitRepresentation(TraceNode node) {
		// Trace node is unused
		BitRepresentation bitRepresentation = new BitRepresentation(1);
		bitRepresentation.set(0);
		return bitRepresentation;
	}

	@Override
	protected int defineConclusionIdx(TraceNode node) {
		// Trace node is unused
		return 0;
	}	
	
	@Override
	public String toString() {
		return "Prior Constraint " + super.toString();
	}
	
	private static String genID() {
		return "PC_" + PriorConstraint.count++;
	}
	
	public static void resetID() {
		PriorConstraint.count = 0;
	}


}
