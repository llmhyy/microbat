package microbat.instrumentation.cfgcoverage;

import org.apache.bcel.classfile.Method;

import microbat.model.ClassLocation;

public class InstrumentationUtils {

	public static String getMethodId(String className, String methodNameWthSignature) {
		return String.format("%s.%s", className, methodNameWthSignature);
	}
	
	public static String getMethodId(String className, Method method) {
		return String.format("%s.%s", className, getMethodWithSignature(method.getName(), method.getSignature()));
	}
	
	public static ClassLocation getClassLocation(String className, String methodNameWthSignature) {
		return new ClassLocation(className, methodNameWthSignature, -1);
	}

	public static ClassLocation getClassLocation(String targetMethod) {
		int idx = targetMethod.lastIndexOf(".");
		if (idx < 0) {
			throw new IllegalArgumentException(
					"Cannot extract method from string, expect [classname].[method], get "
							+ targetMethod);
		}
		String lastFrag = targetMethod.substring(idx + 1);
		if (lastFrag.contains("(")) {
			return new ClassLocation(targetMethod.substring(0, idx),  lastFrag, -1);
		} else {
			int lineNo = Integer.valueOf(lastFrag);
			int methodNameIdx = targetMethod.substring(0, idx).lastIndexOf(".");
			return new ClassLocation(targetMethod.substring(0, methodNameIdx), targetMethod.substring(methodNameIdx + 1, idx), lineNo);
		}
	}

	public static String toTargetMethodId(ClassLocation targetMethod) {
		if (targetMethod.getLineNumber() >= 0) {
			return String.format("%s.%s.%d", targetMethod.getClassCanonicalName(), targetMethod.getMethodSign(), 
					targetMethod.getLineNumber());
		} else {
			return String.format("%s.%s", targetMethod.getClassCanonicalName(), targetMethod.getMethodSign());
		}
	}
	
	public static String getMethodWithSignature(String methodName, String signature) {
		return String.format("%s%s", methodName, signature);
	}
}
