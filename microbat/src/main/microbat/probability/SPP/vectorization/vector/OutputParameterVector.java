package microbat.probability.SPP.vectorization.vector;

import java.util.Arrays;
import java.util.List;

public class OutputParameterVector extends ParameterVector {

	public static int DIMENSION = InputParameterVector.DIMENSION+1;
	private static int VOID_IDX = OutputParameterVector.DIMENSION-1;
	
	public OutputParameterVector() {
		super(OutputParameterVector.DIMENSION);
	}
	
	public OutputParameterVector(final String typeDescriptor) {
		super(OutputParameterVector.DIMENSION);
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
			} else if (type.equals("V")) {
				this.set(OutputParameterVector.VOID_IDX);
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
}
