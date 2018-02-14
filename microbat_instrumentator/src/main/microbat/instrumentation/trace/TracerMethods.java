package microbat.instrumentation.trace;

public enum TracerMethods {
	START_TRACING(false, "microbat/instrumentation/trace/data/ExecutionTracer", "_startTracing", "()V", 1),
	GET_TRACER(false, "microbat/instrumentation/trace/data/ExecutionTracer", "_getTracer", "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;[Ljava/lang/Object;)Lmicrobat/instrumentation/trace/data/IExecutionTracer;", 6),
	READ_LOCAL_VAR(true, "microbat/instrumentation/trace/data/IExecutionTracer", "_readLocalVar", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;IIIILjava/lang/String;Ljava/lang/String;)V", 10),
	WRITE_FIELD(true, "microbat/instrumentation/trace/data/IExecutionTracer", "_writeField", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", 8),
	READ_FIELD(true, "microbat/instrumentation/trace/data/IExecutionTracer", "_readField", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", 8),
	HIT_INVOKE_STATIC(true, "microbat/instrumentation/trace/data/IExecutionTracer", "_hitInvokeStatic", "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", 9),
	WRITE_LOCAL_VAR(true, "microbat/instrumentation/trace/data/IExecutionTracer", "_writeLocalVar", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;IIIILjava/lang/String;Ljava/lang/String;)V", 10),
	READ_ARRAY_ELEMENT_VAR(true, "microbat/instrumentation/trace/data/IExecutionTracer", "_readArrayElementVar", "(Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", 8),
	WRITE_ARRAY_ELEMENT_VAR(true, "microbat/instrumentation/trace/data/IExecutionTracer", "_writeArrayElementVar", "(Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", 8),
	WRITE_STATIC_FIELD(true, "microbat/instrumentation/trace/data/IExecutionTracer", "_writeStaticField", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", 8),
	READ_STATIC_FIELD(true, "microbat/instrumentation/trace/data/IExecutionTracer", "_readStaticField", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", 8),
	HIT_VOID_RETURN(true, "microbat/instrumentation/trace/data/IExecutionTracer", "_hitVoidReturn", "(ILjava/lang/String;Ljava/lang/String;)V", 4),
	HIT_INVOKE(true, "microbat/instrumentation/trace/data/IExecutionTracer", "_hitInvoke", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", 10),
	HIT_METHOD_END(true, "microbat/instrumentation/trace/data/IExecutionTracer", "_hitMethodEnd", "(I)V", 2),
	HIT_LINE(true, "microbat/instrumentation/trace/data/IExecutionTracer", "_hitLine", "(ILjava/lang/String;Ljava/lang/String;)V", 4),
	AFTER_INVOKE(true, "microbat/instrumentation/trace/data/IExecutionTracer", "_afterInvoke", "(Ljava/lang/String;I)V", 3),
	HIT_RETURN(true, "microbat/instrumentation/trace/data/IExecutionTracer", "_hitReturn", "(Ljava/lang/Object;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", 6)
	
	;
	private boolean interfaceMethod;
	private String declareClass;
	private String methodName;
	private String methodSign;
	private int argNo;

	private TracerMethods(boolean ifaceMethod, String declareClass, String methodName, String methodSign, int argNo) {
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
