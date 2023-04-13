package microbat.probability.SPP.vectorization.vector;

import microbat.model.trace.TraceNode;

public class EnvironmentVector extends Vector {
	
	public static final int DIMENSION = 8;
	
	private final boolean[] vector = new boolean[EnvironmentVector.DIMENSION];
	
	public EnvironmentVector(final TraceNode node) {
		
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
