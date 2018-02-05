package microbat.instrumentation.trace.data;

import java.util.ArrayList;
import java.util.List;

import microbat.instrumentation.Excludes;

public class FilterChecker implements IFilterChecker {
	private static final IFilterChecker checker = new FilterChecker();
	private static List<String> excludeList;
	
	public static boolean isExclusive(String className, String methodName) {
		return checker.checkExclusive(className, methodName);
	}
	
	public static boolean isTransformable(String className) {
		return checker.checkTransformable(className);
	}

	public boolean checkExclusive(String className, String methodName) {
		for (String exclude : excludeList) {
			if (className.startsWith(exclude)) {
				return true;
			}
		}
		return false;
	}

	public static void setup() {
		checker.startup();
	}

	public void startup() {
		excludeList = new ArrayList<>(Excludes.defaultLibExcludes.length);
		for (String exclude : Excludes.defaultLibExcludes) {
			excludeList.add(exclude.replace(".", "/"));
		}
	}

	public boolean checkTransformable(String className) {
		if (className.startsWith("microbat/instrumentation/trace/testdata")) {//
			return true;
		}
		if (className.startsWith("microbat/") || className.startsWith("sav/")) {
			return false;
		}
		return true;
	}

	
}
