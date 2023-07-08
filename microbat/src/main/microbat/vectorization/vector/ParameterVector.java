package microbat.vectorization.vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ParameterVector extends Vector {

	protected static final String ARRAY_DES = "[";
	
	protected static final List<String> typeDescriptors_1 = ParameterVector.initTypeDescriptor_1();
	protected static final List<String> typeDescriptors_2 = ParameterVector.initPrimType_2();
	
	protected static final int LIB_OBJ_IDX = 8;
	protected static final int SELF_DEFINED_OBJ_IDX = 9;
	protected static final int ARRAY_OFFSET = 10;
	
	public ParameterVector() {}
	
	public ParameterVector(final int size) {
		super(size);
	}
	
	public ParameterVector(final float[] vector) {
		super(vector);
	}
	
	protected static List<String> initTypeDescriptor_1() {
		String[] descriptor = {"B", "C", "D", "F", "I", "J", "S", "Z"};	// Order is important
		List<String> list = Arrays.asList(descriptor);
		return list;
	}
	
	protected static List<String> initPrimType_2() { 
		// Order is important
		String[] types_array = {
			"java.lang.Byte",
			"java.lang.Character",
			"java.lang.Double",
			"java.lang.Float",
			"java.lang.Integer",
			"java.lang.Long",
			"java.lang.Short",
			"java.lang.Boolean",
		};
		List<String> types = Arrays.asList(types_array);
		return types;
	}
	
	protected static int getIdxOfPrimitiveType(String type) {
		int idx_1 = ParameterVector.typeDescriptors_1.indexOf(type);
		int idx_2 = ParameterVector.typeDescriptors_2.indexOf(type);
		return Math.max(idx_1, idx_2);
	}
	
	protected List<String> splitInputTypeDescriptor(final String descriptor) {
		List<String> types = new ArrayList<>();
		for (int idx=0; idx<descriptor.length(); idx++) {
			char character = descriptor.charAt(idx);
			if (character == 'L') {
				// Input type is a class eg: Ljava/lang/Object;
				// Capture the class name
				final int startIdx = idx;
				while (character != ';' && idx < descriptor.length()) {
					idx++;
					character = descriptor.charAt(idx);
				}
				String caturedStr = descriptor.substring(startIdx, idx+1);
				// Checking
				if (!(caturedStr.startsWith("L") && caturedStr.endsWith(";"))) {
					throw new RuntimeException("[InputParameterVector]: Wrong type caturing for object");
				}
				types.add(caturedStr);
			} else if (character == '[') {
				/*
				 * There are three types of array:
				 * 1. Array of primitive type eg: [I
				 * 2. High 
				 */
				// Input type is an array eg: [I
				// Note that it is also possible that it is an array of object [Ljava/lang/Object;
				// Note that is is also possible that it is a double or triple or more dimensional array
				
				// Handle the case for high dimensional array, we treat high dimension array same way as one dimension array
				int startIdx = idx;
				char nextChar = descriptor.charAt(idx);				
				while (nextChar == '[') {
					startIdx = idx;
					idx++;
					nextChar = descriptor.charAt(idx);
				}
				
				String caturedStr;
				if (nextChar == 'L') {
					while (character != ';' && idx<descriptor.length()) {
						idx++;
						character = descriptor.charAt(idx);
					}
					caturedStr = descriptor.substring(startIdx, idx+1);
					if (!(caturedStr.startsWith("[") && caturedStr.endsWith(";"))) {
						throw new RuntimeException("[InputParameterVector]: Wrong type capturiong for array of object");
					}
				} else {
					caturedStr = descriptor.substring(startIdx, idx+1);
				}
				types.add(caturedStr);
			} else {
				// Primitive type
				types.add(String.valueOf(character));
			}
		}
		return types;
	}
}
