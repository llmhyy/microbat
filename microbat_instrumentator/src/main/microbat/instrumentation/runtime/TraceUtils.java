package microbat.instrumentation.runtime;

import org.apache.bcel.generic.Type;

import microbat.model.variable.Variable;

public class TraceUtils {
	private TraceUtils(){}
	
	public static String getObjectVarId(Object refValue, String type) {
		if (refValue == null) {
			return null;
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
		return Variable.concanateFieldVarID(parentVarId, fieldName);
	}
	
//	public static String getLocalVarId(String className, int startLine, int endLine, 
//			String varName, String varType, Object varValue) {
//		return Variable.concanateLocalVarID(className, varName, startLine, endLine);
//	}

	public static String getArrayElementVarId(String parentVarId, int index, String elementType, Object eleValue) {
		return Variable.concanateArrayElementVarID(parentVarId, String.valueOf(index));
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
