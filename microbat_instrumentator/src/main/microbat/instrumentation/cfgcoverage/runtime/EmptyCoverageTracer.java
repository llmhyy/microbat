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
	public void _exitMethod(String methodId, boolean isEntryPoint) {
		// TODO Auto-generated method stub
	}

	@Override
	public void _onIfACmp(Object value1, Object value2, String methodId, int nodeIdx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void _onIfICmp(int value1, int value2, String methodId, int nodeIdx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void _onIf(int value, String methodId, int nodeIdx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void _onIfNull(Object value, String methodId, int nodeIdx) {
		// TODO Auto-generated method stub
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

}