package microbat.probability.SPP.vectorization.vector;

import microbat.model.trace.TraceNode;

public class FunctionVector extends Vector {
	
	public static final int DIMENSION = 44;
	public final boolean[] vector = new boolean[FunctionVector.DIMENSION];
	
	public FunctionVector () {
		
	}
	
	public FunctionVector (final TraceNode node) {
		
	}
	
	public static FunctionVector[] constructFuncVectors(final TraceNode node, final int vectorCount) {
		FunctionVector[] funcVectors = new FunctionVector[vectorCount];
		for (int idx=0; idx<vectorCount; idx++) {
			
		}
		return funcVectors;
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
