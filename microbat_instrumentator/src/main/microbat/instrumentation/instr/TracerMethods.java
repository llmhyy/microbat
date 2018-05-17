package microbat.instrumentation.instr;

public enum TracerMethods {
	AFTER_INVOKE(true, "microbat/instrumentation/runtime/IExecutionTracer", "_afterInvoke", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Z)V", 8),
	GET_TRACER(false, "microbat/instrumentation/runtime/ExecutionTracer", "_getTracer", "(ZLjava/lang/String;Ljava/lang/String;IILjava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Lmicrobat/instrumentation/runtime/IExecutionTracer;", 9),
	HIT_EXEPTION_TARGET(true, "microbat/instrumentation/runtime/IExecutionTracer", "_hitExeptionTarget", "(ILjava/lang/String;Ljava/lang/String;)V", 4),
	HIT_INVOKE(true, "microbat/instrumentation/runtime/IExecutionTracer", "_hitInvoke", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", 10),
	HIT_INVOKE_STATIC(true, "microbat/instrumentation/runtime/IExecutionTracer", "_hitInvokeStatic", "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", 9),
	HIT_LINE(true, "microbat/instrumentation/runtime/IExecutionTracer", "_hitLine", "(ILjava/lang/String;Ljava/lang/String;II)V", 6),
	HIT_METHOD_END(true, "microbat/instrumentation/runtime/IExecutionTracer", "_hitMethodEnd", "(ILjava/lang/String;Ljava/lang/String;)V", 4),
	HIT_RETURN(true, "microbat/instrumentation/runtime/IExecutionTracer", "_hitReturn", "(Ljava/lang/Object;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", 6),
	HIT_VOID_RETURN(true, "microbat/instrumentation/runtime/IExecutionTracer", "_hitVoidReturn", "(ILjava/lang/String;Ljava/lang/String;)V", 4),
	IINC_LOCAL_VAR(true, "microbat/instrumentation/runtime/IExecutionTracer", "_iincLocalVar", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;IIIILjava/lang/String;Ljava/lang/String;)V", 11),
	READ_ARRAY_ELEMENT_VAR(true, "microbat/instrumentation/runtime/IExecutionTracer", "_readArrayElementVar", "(Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", 8),
	READ_FIELD(true, "microbat/instrumentation/runtime/IExecutionTracer", "_readField", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", 8),
	READ_LOCAL_VAR(true, "microbat/instrumentation/runtime/IExecutionTracer", "_readLocalVar", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;IIIILjava/lang/String;Ljava/lang/String;)V", 10),
	READ_STATIC_FIELD(true, "microbat/instrumentation/runtime/IExecutionTracer", "_readStaticField", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", 8),
	START(false, "microbat/instrumentation/runtime/ExecutionTracer", "_start", "()V", 1),
	WRITE_ARRAY_ELEMENT_VAR(true, "microbat/instrumentation/runtime/IExecutionTracer", "_writeArrayElementVar", "(Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", 8),
	WRITE_FIELD(true, "microbat/instrumentation/runtime/IExecutionTracer", "_writeField", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", 8),
	WRITE_LOCAL_VAR(true, "microbat/instrumentation/runtime/IExecutionTracer", "_writeLocalVar", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;IIIILjava/lang/String;Ljava/lang/String;)V", 10),
	WRITE_STATIC_FIELD(true, "microbat/instrumentation/runtime/IExecutionTracer", "_writeStaticField", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", 8),

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
