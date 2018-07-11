package microbat.instrumentation.cfgcoverage.instr;

public enum CoverageTracerMethods {
	ENTER_METHOD(true, "microbat/instrumentation/cfgcoverage/runtime/ICoverageTracer", "_enterMethod", "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V", 4),
	EXIT_METHOD(true, "microbat/instrumentation/cfgcoverage/runtime/ICoverageTracer", "_exitMethod", "(Ljava/lang/String;)V", 2),
	GET_TRACER(false, "microbat/instrumentation/cfgcoverage/runtime/CoverageTracer", "_getTracer", "(Ljava/lang/String;ZLjava/lang/String;[Ljava/lang/Object;)Lmicrobat/instrumentation/cfgcoverage/runtime/ICoverageTracer;", 5),
	REACH_NODE(true, "microbat/instrumentation/cfgcoverage/runtime/ICoverageTracer", "_reachNode", "(Ljava/lang/String;I)V", 3);

	private boolean interfaceMethod;
	private String declareClass;
	private String methodName;
	private String methodSign;
	private int argNo;

	private CoverageTracerMethods(boolean ifaceMethod, String declareClass, String methodName, String methodSign, int argNo) {
		this.interfaceMethod = ifaceMethod;
		this.declareClass = declareClass;
		this.methodName = methodName;
		this.methodSign = methodSign;
		this.argNo = argNo;
	}

	public String getDeclareClass() {
		return declareClass;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getMethodSign() {
		return methodSign;
	}

	public int getArgNo() {
		return argNo;
	}

	public boolean isInterfaceMethod() {
		return interfaceMethod;
	}
}
