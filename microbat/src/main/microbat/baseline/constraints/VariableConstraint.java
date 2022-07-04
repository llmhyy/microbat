package microbat.baseline.constraints;

import java.util.Collection;

import microbat.baseline.BitRepresentation;

/**
 * Variable constraint
 * If all other variable in the same constraint are correct, then
 * the remaining variable is likely to be correct
 * @author Siang Hwee
 *
 */
public class VariableConstraint extends Constraint {
	
	public VariableConstraint(BitRepresentation varsIncluded, Collection<Integer> conclusionIndexes, double propProbability) {
		super(varsIncluded, conclusionIndexes, propProbability, "VAR_CONSTRAINT");
	}
	
	public VariableConstraint(BitRepresentation varsIncluded, int conclusionIdx, double propProbability) {
		super(varsIncluded, conclusionIdx, propProbability, "VAR_CONSTRAINT");
	}

	@Override
	protected double calProbability(int caseNo) {
		BitRepresentation binValue = BitRepresentation.parse(caseNo, this.varsIncluded.size());
		binValue.and(this.varsIncluded);
		int numVarsIncluded = this.varsIncluded.getCardinality();
		int numTrue = binValue.getCardinality();
		int numFalse = numVarsIncluded - numTrue;
		if (numFalse <= conclusionIndexes.size() ) {
			for (Integer index : conclusionIndexes) {
				if (binValue.get(index))
					continue;
				// one of the conclusion index is false
				numFalse -= 1;
				if (numFalse == 0) {
					// all the false values are the written var
					// early termination
					return 1 - this.propProbability;
				}
			}
		}
		
		/*
		 *  if the number of vars that are false is more than the number of conclusion index
		 *  we know that at least one of the non-conclusion var is wrong and thus this statement
		 *  is veraciously true 
		 */
		return this.propProbability;
	}
	
	@Override
	public String toString() {
		return "Var Constraint " + super.toString();
	}
}
