package microbat.probability.SPP.vectorization.vector;

import java.util.Arrays;

public class OutputParameterVector extends Vector {

	public static int DIMENSION = InputParameterVector.DIMENSION+1;
	
	public OutputParameterVector() {
		super(new float[OutputParameterVector.DIMENSION]);
		Arrays.fill(this.vector, 0.0f);
	}
	
	public OutputParameterVector(final String typeDescriptor) {
		super(new float[OutputParameterVector.DIMENSION]);
		Arrays.fill(this.vector, 0.0f);
	}
}
