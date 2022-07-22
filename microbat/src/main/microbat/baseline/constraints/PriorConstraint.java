package microbat.baseline.constraints;

import java.util.ArrayList;
import java.util.List;

import microbat.baseline.BitRepresentation;

/**
 * Prior constraint represent how likely the conclusion variable is correct
 * @author David
 *
 */
public class PriorConstraint extends Constraint {
	
	private static int count = 0;

	public PriorConstraint(BitRepresentation varsIncluded, int conclusionIndex, double propProbability) {
		super(varsIncluded, conclusionIndex, propProbability, PriorConstraint.genID());
	}
	
	public PriorConstraint(BitRepresentation varsIncluded, int conclusionIndex, double propProbability, String varID) {
		super(varsIncluded, conclusionIndex, propProbability, PriorConstraint.genID());
	}

	
	@Override
	protected double calProbability(int caseNo) {
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
}
