package microbat.instrumentation.trace.data;

import org.apache.bcel.generic.Type;

import microbat.model.variable.Variable;
import microbat.util.PrimitiveUtils;

public class TraceUtils {
	private TraceUtils(){}
	
	public static String getObjectVarId(Object refValue) {
		if (refValue == null) {
			return "null";
		}
		return String.valueOf(getUniqueId(refValue));
	}

	public static long getUniqueId(Object refValue) {
		if (refValue == null) {
			return -1;
		}
		try {
			return System.identityHashCode(refValue);
		} catch (Throwable e) {
			return -1;
		}
	}

	public static String getFieldVarId(String parentVarId, String fieldName, String fieldType, Object fieldValue) {
		if (PrimitiveUtils.isPrimitive(fieldType)) {
			return Variable.concanateFieldVarID(parentVarId, fieldName);
		}
		return getObjectVarId(fieldValue);
	}
	
	public static String getLocalVarId(String className, int startLine, int endLine, 
			String varName, String varType, Object varValue) {
		if (PrimitiveUtils.isPrimitive(varType)) {
			return Variable.concanateLocalVarID(className, varName, startLine, endLine);
		}
		return getObjectVarId(varValue);
	}

	public static String getArrayElementVarId(String parentVarId, int index, String elementType, Object eleValue) {
		if (PrimitiveUtils.isPrimitive(elementType)) {
			return Variable.concanateArrayElementVarID(parentVarId, String.valueOf(index));
		}
		return getObjectVarId(eleValue);
	}
	
	private static final String ARG_TYPE_SEPARATOR = ":";
	
	public static String encodeArgNames(String[] argNames) {
		StringBuilder sb = new StringBuilder();
		int lastIdx = argNames.length - 1;
		for (int i = 0; i < argNames.length; i++) {
			sb.append(argNames[i]);
			if (i != lastIdx) {
				sb.append(ARG_TYPE_SEPARATOR);
			}
		}
		return sb.toString();
	}
	
	public static String encodeArgTypes(Type[] argTypes) {
		StringBuilder sb = new StringBuilder();
		int lastIdx = argTypes.length - 1;
		for (int i = 0; i < argTypes.length; i++) {
			sb.append(argTypes[i].getSignature());
			if (i != lastIdx) {
				sb.append(ARG_TYPE_SEPARATOR);
			}
		}
		return sb.toString();
	}
	
	public static String[] parseArgTypesOrNames(String code) {
		if (code == null || code.isEmpty()) {
			return new String[0];
		}
		return code.split(ARG_TYPE_SEPARATOR);
	}
}
