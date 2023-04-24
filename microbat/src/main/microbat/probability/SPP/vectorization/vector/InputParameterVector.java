package microbat.probability.SPP.vectorization.vector;

import java.util.Arrays;
import java.util.List;

import microbat.model.trace.TraceNode;

public class InputParameterVector extends Vector {

	/*
	 * 	B	byte	signed byte
		C	char	Unicode character code point in the Basic Multilingual Plane, encoded with UTF-16
		D	double	double-precision floating-point value
		F	float	single-precision floating-point value
		I	int	integer
		J	long	long integer
		L ClassName ;	reference	an instance of class ClassName
		S	short	signed short
		Z	boolean	true or false
		[	reference	one array dimension
	 */
	
	public static int DIMENSION = 11;
	
	private static final String ARRAY_DES = "[";
	
	private static final List<String> typeDescriptors = InputParameterVector.initTypeDescriptor_1();

	
	public InputParameterVector() {
		super(new float[InputParameterVector.DIMENSION]);
		Arrays.fill(this.vector, 0.0f);
	}
	
	public InputParameterVector(final String typeDescriptor) {
		super(new float[InputParameterVector.DIMENSION]);
		Arrays.fill(this.vector, 0.0f);
	}
	
	public static InputParameterVector[] constructVectors(final String typeDescriptors, final int vectorCount) {
		InputParameterVector[] vectors = new InputParameterVector[vectorCount];
		for (int idx=0; idx<vectorCount; idx++) {
			vectors[idx] = new InputParameterVector();
		}
		return vectors;
	}

	
	private static List<String> initTypeDescriptor_1() {
		String[] descriptor = {"B", "C", "D", "F", "I", "J", "L", "S", "Z"};
		List<String> list = Arrays.asList(descriptor);
		return list;
	}
	
}
