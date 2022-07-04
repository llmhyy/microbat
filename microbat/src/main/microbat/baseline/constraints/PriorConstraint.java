package microbat.baseline.constraints;

import microbat.baseline.BitRepresentation;

/**
 * Prior constraint represent how likely the conclusion variable is correct
 * @author David
 *
 */
public class PriorConstraint extends Constraint {

	public PriorConstraint(BitRepresentation varsIncluded, int conclusionIndex, double propProbability) {
		super(varsIncluded, conclusionIndex, propProbability, "PRIOR_CONSTRAINT");
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
}
