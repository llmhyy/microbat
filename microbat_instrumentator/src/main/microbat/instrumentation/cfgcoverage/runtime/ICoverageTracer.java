package microbat.instrumentation.cfgcoverage.runtime;

public interface ICoverageTracer {

	void _reachNode(String methodId, int nodeIdx);

	void enterMethod(String methodId, String paramTypeSignsCode, String paramNamesCode, Object[] params, boolean isEntryPoint, Object receiver);

	void _exitMethod(String methodId, boolean isEntryPoint);

	void _onIfACmp(Object value1, Object value2, String methodId, int nodeIdx);

	void _onIfICmp(int value1, int value2, String methodId, int nodeIdx);

	void _onIf(int value, boolean isNotIntCmpIf, String methodId, int nodeIdx);

	void _onIfNull(Object value, String methodId, int nodeIdx);

	void shutDown();

	void _onDcmp(double value1, double value2);

	void _onFcmp(float value1, float value2);

	void _onLcmp(long value1, long value2);

}
