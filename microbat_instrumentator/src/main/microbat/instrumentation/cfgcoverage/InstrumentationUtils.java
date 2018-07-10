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

	public static ClassLocation getMethodId(String targetMethod) {
		String[] frags = targetMethod.split(".");
		return new ClassLocation(frags[0], frags[1], Integer.valueOf(frags[2]));
	}

	public static String getMethodWithSignature(String methodName, String signature) {
		return String.format("%s%s", methodName, signature);
	}
}
