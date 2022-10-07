package microbat.baseline.constraints;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * Prior constraint represent how likely the conclusion variable is correct
 * @author David
 */
public class PriorConstraint extends Constraint {
	
	private static int count = 0;
	
	/**
	 * Constructor
	 * @param node	Target node
	 * @param var	Target variable
	 * @param probProbability Propagation probability
	 */
	public PriorConstraint(VarValue var, double probProbability) {
		super(new TraceNode(null, null, -1, null, null), 0, probProbability, PriorConstraint.genID());
		this.addReadVarID(var.getVarID());
	}
	
//	public PriorConstraint(BitRepresentation varsIncluded, int conclusionIndex, double propProbability) {
//		super(varsIncluded, conclusionIndex, propProbability, PriorConstraint.genID());
//	}
//	
//	public PriorConstraint(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, String varID) {
//		super(varsIncluded, conclusionIndex, propProbability, PriorConstraint.genID());
//	}

	@Override
	protected double calProbability(int caseNo) {
		
		/*
		 * The probability directly depends on whether
		 * the conclusion predicate is true or false
		 * in the given case number
		 */
		
		BitRepresentation binValue = BitRepresentation.parse(caseNo, this.varsIncluded.size());
		binValue.and(this.varsIncluded);
		
		double prob = 0.0;
		for (int conclusionIdx : this.conclusionIndexes) {
			prob = binValue.get(conclusionIdx) ? this.propProbability : 1 - this.propProbability;
		}
		return prob;
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

	@Override
	protected BitRepresentation genBitRepresentation(TraceNode node) {
		// Trace node is unused
		BitRepresentation bitRepresentation = new BitRepresentation(1);
		bitRepresentation.set(0);
		return bitRepresentation;
	}
}
