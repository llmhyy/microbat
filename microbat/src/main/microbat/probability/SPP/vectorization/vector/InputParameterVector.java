package microbat.probability.SPP.vectorization.vector;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class InputParameterVector extends ParameterVector {

	/*
	 * 	20-dim: histogram of input type
	 * 		8-dim for primitive type
	 * 		1-dim for library object
	 * 		1-dim for self-defined object
	 * 		10-dim array version for the above feature
	 * 
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
	
	public static int DIMENSION = 20;
	
	public InputParameterVector() {
		super(InputParameterVector.DIMENSION);
	}
	
	public InputParameterVector(final float[] vector) {
		super(vector);
	}
	
	public InputParameterVector(final String typeDescriptor) {
		super(InputParameterVector.DIMENSION);
		List<String> types = this.splitInputTypeDescriptor(typeDescriptor);
		for (String type : types) {
			if (type.startsWith("[L")) {
				// Array of object
				type = type.substring(2, type.length()-1);
				int idx = LibraryClassDetector.isLibClass(type) ? ParameterVector.LIB_OBJ_IDX : ParameterVector.SELF_DEFINED_OBJ_IDX;
				this.set(ParameterVector.ARRAY_OFFSET+idx);
			} else if (type.startsWith("L")) {
				// Object
				type = type.substring(1, type.length()-1);
				// Check is the object library object
				int idx = LibraryClassDetector.isLibClass(type) ? ParameterVector.LIB_OBJ_IDX : ParameterVector.SELF_DEFINED_OBJ_IDX;
				this.set(idx);
			} else if (type.startsWith("[")){
				// Array of primitive type
				type = type.substring(1, type.length());
				int idx = ParameterVector.getIdxOfPrimitiveType(type);
				if (idx < 0) {
					throw new RuntimeException("[InputParameterVector] Cannot get type idx: " + typeDescriptor);
				}
				this.set(ParameterVector.ARRAY_OFFSET+idx);
			} else {
				// Primitive type
				int idx = ParameterVector.getIdxOfPrimitiveType(type);
				if (idx < 0) {
					throw new RuntimeException("[InputParameterVector] Cannot get type idx: " + typeDescriptor);
				}
				this.set(idx);
			}
		}
	}
	
	@Override
	public void set(final int idx) {
		this.vector[idx]++;
	}
}
