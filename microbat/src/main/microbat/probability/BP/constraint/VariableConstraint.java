package microbat.probability.BP.constraint;

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
	
	/**
	 * Constructor
	 * @param node	Target node
	 * @param propProbability Propagation probability
	 * @param constraintID Constraint ID
	 */
	public VariableConstraint(TraceNode node, double propProbability, String constraintID) {
		super(node, propProbability, constraintID);
	}
	
	/**
	 * Constructor
	 * @param bitRepresentation Bit representation of this constraint
	 * @param conclusionIdx Index of conclusion variable
	 * @param propProbability Propagation probability
	 * @param constraintID Constraint ID
	 * @param order Order of target node that this constraint based on
	 */
	public VariableConstraint(BitRepresentation bitRepresentation, int conclusionIdx, double propProbability, String constraintID, int order) {
		super(bitRepresentation, conclusionIdx, propProbability, constraintID, order);
	}
	
	/**
	 * Deep Copy Constructor
	 * @param constraint Other constraint
	 */
	public VariableConstraint(VariableConstraint constraint) {
		super(constraint);
	}
	
	@Override
	protected double calProbability(int caseNo) {
		
		/*
		 *  The constraint will only be invalid when
		 *  all the other variable is correct, but the
		 *  conclusion index is wrong. All the other
		 *  case mean that the constraint is valid
		 */
		
		BitRepresentation binValue = BitRepresentation.parse(caseNo, this.bitRepresentation.size());
		binValue.and(this.bitRepresentation);
		int numVarsIncluded = this.bitRepresentation.getCardinality();
		int numTrue = binValue.getCardinality();
		int numFalse = numVarsIncluded - numTrue;
		if (numFalse <= 1) {
			if (!binValue.get(this.conclusionIdx)) {
				numFalse -= 1;
				if (numFalse == 0) {
					return 1-this.propProbability;
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
