package microbat.instrumentation.cfgcoverage.runtime;

public class EmptyCoverageTracer implements ICoverageTracer {
	private static final EmptyCoverageTracer INSTANCE = new EmptyCoverageTracer();
	
	public static EmptyCoverageTracer getInstance() {
		return INSTANCE;
	}

	@Override
	public void _reachNode(String methodId, int nodeIdx) {
		
	}
	
	@Override
	public void enterMethod(String methodId, String paramTypeSignsCode, String paramNamesCode, Object[] params,
			boolean isEntryPoint, Object receiver) {
		
	}

	@Override
	public void _exitMethod(String methodId, boolean isEntryPoint) {
	}

	@Override
	public void _onIfACmp(Object value1, Object value2, String methodId, int nodeIdx) {
		
	}

	@Override
	public void _onIfICmp(int value1, int value2, String methodId, int nodeIdx) {
		
	}

	@Override
	public void _onIf(int value, boolean isNotIntCmpIf, String methodId, int nodeIdx) {
		
	}

	@Override
	public void _onIfNull(Object value, String methodId, int nodeIdx) {
	}

	@Override
	public void shutDown() {
		
	}

	@Override
	public void _onDcmp(double value1, double value2) {
		
	}

	@Override
	public void _onFcmp(float value1, float value2) {
		
	}

	@Override
	public void _onLcmp(long value1, long value2) {
		
	}

}
