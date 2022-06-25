package microbat.baseline.constraints;

import microbat.baseline.BitRepresentation;

public class StatementConstraint extends Constraint {
	
	protected int writeVarIdx;
	protected int predIdx;
	protected int strucIdx;
	protected int nameIdx;
	
	public StatementConstraint(BitRepresentation variablesIncluded, int conclusionIndex, double probability, ConstraintType type, int writeVarIdx) {
		super(variablesIncluded, conclusionIndex, probability, type);
		this.writeVarIdx = writeVarIdx;
		
		int size = variablesIncluded.size();
		this.nameIdx = size - 2;
		this.strucIdx = size - 3;
		this.predIdx = size - 4;
	}
	
	@Override
	protected double getProb(int bin) {
		BitRepresentation binValue = BitRepresentation.parse(bin, variablesIncluded.size());
		binValue.and(variablesIncluded);
		int numVarsIncluded = variablesIncluded.getCardinality();
		int numTrue = binValue.getCardinality();
		int numFalse = numVarsIncluded - numTrue;
		double prob = -1;
		
		// For statement constraint, we assume that there are only one conclusion index
		switch(this.constraintType) {
		case DEFINE:
			break;
		case NAME:
			/*
			 * For naming constraint, the only invalid case is that
			 * when the naming constraint is true but the conclusion
			 * is false.
			 */
			for (int conclusionIdx : this.conclusionIndexes) {
				if (numFalse == 1 && !binValue.get(conclusionIdx)) {
					prob = 1 - this.probability;
				} else {
					prob = this.probability;
				}
			}
			break;
		case PREDICATE:
			break;
		case PRIOR:
			for (int conclusionIdx : this.conclusionIndexes) {
				prob = binValue.get(conclusionIdx) ? this.probability : 1 - this.probability;
			}
			break;
		case PROG_STRUCTURE:
			/*
			 * For program statement constraint, the only invalid case is that
			 * when the structure is true but the conclusion is false.
			 */
			for (int conclusionIdx : this.conclusionIndexes) {
				if (numFalse == 1 && !binValue.get(conclusionIdx)) {
					prob = 1 - this.probability;
				} else {
					prob = this.probability;
				}
			}
			break;
		case USE:
			break;
		case VAR_TO_STAT_1:
			// For A1, the only invalid case is that all the predicate are correct but
			// the conclusion is wrong
			for (int conclusionIndex : this.conclusionIndexes) {
				if (numFalse == 1 && !binValue.get(conclusionIndex)) {
					prob = 1 - this.probability;
				} else {
					prob = this.probability;
				}
			}
			break;
		case VAR_TO_STAT_2:
			/*
			 * For A2, the invalid case is that, when there are write variable is wrong and
			 * at least one of the read variable is wrong, the statement is wrong. 
			 * All the other cases are valid.
			 */
			for (int conclusionIndex : this.conclusionIndexes) {
				boolean haveWrongWriteVar = false;
				for (int idx = this.writeVarIdx; idx < this.predIdx; idx++) {
					if (!binValue.get(idx)) {
						haveWrongWriteVar = true;
						break;
					}
				}
				
				boolean haveWrongReadVar = false;
				for (int idx = 0; idx < this.writeVarIdx; idx++) {
					if (!binValue.get(idx)) {
						haveWrongReadVar = true;
						break;
					}
				}
				
				if (haveWrongWriteVar && haveWrongReadVar) {
					if (binValue.get(conclusionIndex)) {
						prob = this.probability;
					} else {
						prob =1 - this.probability;
					}
				} else {
					prob = this.probability;
				}
			}
			break;
		case VAR_TO_STAT_3:
			/*
			 * For A3, the invalid case is that when there are at least one write variable
			 * is wrong and all the read variable is correct, the statement is still correct.
			 */
			for (int conclusionIdx : this.conclusionIndexes) {
				boolean haveWrongWriteVar = false;
				for (int idx = this.writeVarIdx; idx < this.predIdx; idx++) {
					if (!binValue.get(idx)) {
						haveWrongWriteVar = true;
						break;
					}
				}
				
				boolean haveWrongReadVar = false;
				for (int idx = 0; idx < this.writeVarIdx; idx++) {
					if (!binValue.get(idx)) {
						haveWrongReadVar = true;
						break;
					}
				}
				
				if (!haveWrongReadVar && haveWrongWriteVar) {
					if (binValue.get(conclusionIdx)) {
						prob =  1 - this.probability;
					} else {
						prob = this.probability;
					}
				} else {
					prob = this.probability;
				}
			}
			
			break;
		default:
			break;
		}
		
		return prob;
	}
}
