package microbat.baseline.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import microbat.baseline.BitRepresentation;
import microbat.model.trace.TraceNode;

/**
 * Variable constraint
 * @author Siang Hwee, David
 *
 */
public class VariableConstraint extends Constraint {
	
	private static int count = 0;
	
	public VariableConstraint(BitRepresentation varsIncluded, Collection<Integer> conclusionIndexes, double propProbability) {
		super(varsIncluded, conclusionIndexes, propProbability, VariableConstraint.genID());
	}
	
	public VariableConstraint(BitRepresentation varsIncluded, int conclusionIdx, double propProbability) {
		super(varsIncluded, conclusionIdx, propProbability, VariableConstraint.genID());
	}
	
	@Override
	protected double calProbability(int caseNo) {
		
		/*
		 *  The constraint will only be invalid when
		 *  all the other variable is correct, but the
		 *  conclusion index is wrong. All the other
		 *  case mean that the constraint is valid
		 */
		
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
	
	private static String genID() {
		return "VC_" + VariableConstraint.count++;
	}
}
