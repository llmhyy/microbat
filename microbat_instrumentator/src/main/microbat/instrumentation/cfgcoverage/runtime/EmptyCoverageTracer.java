package microbat.instrumentation.cfgcoverage.runtime;

public class EmptyCoverageTracer implements ICoverageTracer {
	private static final EmptyCoverageTracer INSTANCE = new EmptyCoverageTracer();
	
	public static EmptyCoverageTracer getInstance() {
		return INSTANCE;
	}
}
