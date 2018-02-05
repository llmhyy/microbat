package microbat.instrumentation.trace.data;

import microbat.instrumentation.Excludes;

public class FilterChecker {

	public static boolean isExclusive(String className, String methodName) {
		for (String exclude : Excludes.defaultLibExcludes) {
			if (className.startsWith(exclude)) {
				return true;
			}
		}
		return false;
	}

	public static void setup() {
		// TODO Auto-generated method stub
		
	}

	public static boolean isTransformable(String className) {
		if (className.startsWith("microbat.instrumentation.testdata")) {
			return true;
		}
		if (className.startsWith("microbat.")) {
			return false;
		}
		return true;
	}

}
