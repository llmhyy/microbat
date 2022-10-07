package microbat.baseline.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * Variable constraint
 * @author Siang Hwee, David
 *
 */
public abstract class VariableConstraint extends Constraint {
//	
//	private static int count = 0;
	
	public VariableConstraint(TraceNode node, int conclusionIdx, double propProbability, String constraintID) {
		super(node, conclusionIdx, propProbability, constraintID);
	}

	
//	public VariableConstraint(TraceNode node, VarValue conclusionVar, double propProbability) {
//		super(node, conclusionVar, propProbability, VariableConstraint.genID());
//	}
//	
//	public VariableConstraint(BitRepresentation varsIncluded, Collection<Integer> conclusionIndexes, double propProbability) {
//		super(varsIncluded, conclusionIndexes, propProbability, VariableConstraint.genID());
//	}
//	
//	public VariableConstraint(BitRepresentation varsIncluded, int conclusionIdx, double propProbability) {
//		super(varsIncluded, conclusionIdx, propProbability, VariableConstraint.genID());
//	}
	
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

	public static void resetID() {
		VariableConstraintA1.resetID();
		VariableConstraintA2.resetID();
		VariableConstraintA3.resetID();
	}
}
