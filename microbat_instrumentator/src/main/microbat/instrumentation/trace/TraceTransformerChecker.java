package microbat.instrumentation.trace;

import java.util.Set;

public class TraceTransformerChecker {
	private Set<String> imports;
	private Set<String> excludeLibs;
	
	public static enum CheckerResult {
		EXCLUDE,
		RETURN_INSTRUMENTATION,
		NORMAL_INSTRUMENTATION
	}

	public CheckerResult check(String className) {
		return CheckerResult.NORMAL_INSTRUMENTATION;
	}
}
