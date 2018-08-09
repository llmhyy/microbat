package microbat.instrumentation.cfgcoverage.instr;

public enum CoverageTracerMethods {
	EXIT_METHOD(true, "microbat/instrumentation/cfgcoverage/runtime/ICoverageTracer", "_exitMethod", "(Ljava/lang/String;Z)V", 3),
	GET_TRACER(false, "microbat/instrumentation/cfgcoverage/runtime/CoverageTracer", "_getTracer", "(Ljava/lang/String;ZLjava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Lmicrobat/instrumentation/cfgcoverage/runtime/ICoverageTracer;", 6),
	ON_IF(true, "microbat/instrumentation/cfgcoverage/runtime/ICoverageTracer", "_onIf", "(ILjava/lang/String;I)V", 4),
	ON_IF_A_CMP(true, "microbat/instrumentation/cfgcoverage/runtime/ICoverageTracer", "_onIfACmp", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;I)V", 5),
	ON_IF_I_CMP(true, "microbat/instrumentation/cfgcoverage/runtime/ICoverageTracer", "_onIfICmp", "(IILjava/lang/String;I)V", 5),
	ON_IF_NULL(true, "microbat/instrumentation/cfgcoverage/runtime/ICoverageTracer", "_onIfNull", "(Ljava/lang/Object;Ljava/lang/String;I)V", 4),
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
