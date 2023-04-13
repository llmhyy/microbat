package microbat.probability.SPP.vectorization.vector;

import java.util.List;

import microbat.model.value.VarValue;

public class VariableVector extends Vector {
	
	public static final int DIMENSION = 25;
	
	private final boolean[] vector = new boolean[VariableVector.DIMENSION];
	
	public VariableVector() {
		
	}
	
	public VariableVector(final VarValue var) {
		
	}
	
	public static VariableVector[] constructVarVectors(final List<VarValue> vars, final int vectorCount) {
		VariableVector[] varVectors = new VariableVector[vectorCount];
		for (int idx=0; idx<vectorCount; idx++) {
			if (idx >= vars.size()) {
				varVectors[idx] = new VariableVector();
			} else {
				final VarValue var = vars.get(idx);
				varVectors[idx] = new VariableVector(var);
			}
		}
		return varVectors;
	}
	
	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(this.vector[0] ? "1" : "0");
		for (int idx=1; idx<this.vector.length; ++idx) {
			strBuilder.append(",");
			strBuilder.append(this.vector[idx] ? "1" : "0");
		}
		return strBuilder.toString();
	}
}
