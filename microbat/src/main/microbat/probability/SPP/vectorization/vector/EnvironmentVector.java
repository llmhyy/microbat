package microbat.probability.SPP.vectorization.vector;

import java.util.Arrays;

import microbat.model.trace.TraceNode;

public class EnvironmentVector extends Vector {
	
	public static final int DIMENSION = 8;
	
	private final boolean[] vector = new boolean[EnvironmentVector.DIMENSION];
	
	public EnvironmentVector() {
		super(EnvironmentVector.DIMENSION);
		Arrays.fill(this.vector, false);
	}
	
	public EnvironmentVector(final TraceNode node) {
		super(EnvironmentVector.DIMENSION);
	}

}