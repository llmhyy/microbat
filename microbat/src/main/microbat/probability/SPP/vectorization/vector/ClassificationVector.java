package microbat.probability.SPP.vectorization.vector;

import java.util.Arrays;

import microbat.model.trace.TraceNode;

public class ClassificationVector extends Vector {
	
	public static final int DIMENSION = 6;
	
	public ClassificationVector(final TraceNode node) {
		super(new float[ClassificationVector.DIMENSION]);
		Arrays.fill(this.vector, 0.0f);
	}

}
