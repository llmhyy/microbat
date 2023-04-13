package microbat.probability.SPP.vectorization.vector;

import java.util.List;

import microbat.model.value.VarValue;

public class VariableVector extends Vector {
	
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
}
