package microbat.instrumentation.cfgcoverage;

import microbat.model.ClassLocation;

public class CoverageAgentUtils {

	public static String getMethodId(String className, String methodName, int startline) {
		if (startline < 0) {
			return String.format("%s.%s", className, methodName);
		}
		return String.format("%s.%s.%d", className, methodName, startline);
	}

	public static ClassLocation getMethodId(String targetMethod) {
		String[] frags = targetMethod.split(".");
		return new ClassLocation(frags[0], frags[1], Integer.valueOf(frags[2]));
	}

	public static String getMethodWithSignature(String methodName, String signature) {
		return String.format("%s%s", methodName, signature);
	}
}
