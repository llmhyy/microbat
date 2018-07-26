package microbat.instrumentation.cfgcoverage.runtime;

public class EmptyCoverageTracer implements ICoverageTracer {
	private static final EmptyCoverageTracer INSTANCE = new EmptyCoverageTracer();
	
	public static EmptyCoverageTracer getInstance() {
		return INSTANCE;
	}

	@Override
	public void _reachNode(String methodId, int nodeIdx) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void enterMethod(String methodId, String paramTypeSignsCode, String paramNamesCode, Object[] params,
			boolean isEntryPoint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void _exitMethod(String methodId) {
		// TODO Auto-generated method stub
		
	}

}
