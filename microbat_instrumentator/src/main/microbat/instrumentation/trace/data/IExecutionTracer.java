package microbat.instrumentation.trace.data;

public interface IExecutionTracer {

	void _hitInvoke(Object invokeObj, String invokeTypeSign, String methodName, Object[] params,
			String paramTypeSignsCode, String returnTypeSign, int line);

	void _hitInvokeStatic(String invokeTypeSign, String methodName, Object[] params, String paramTypeSignsCode,
			String returnTypeSign, int line);

	void _hitReturn(Object returnObj, String returnGeneralType, int line);

	void _hitVoidReturn(int line);

	void _hitLine(int line);

	void _writeField(Object refValue, Object fieldValue, String fieldName, String fieldTypeSign, int line);

	void _writeStaticField(Object fieldValue, String refType, String fieldName, String fieldType, int line);

	void _readField(Object refValue, Object fieldValue, String fieldName, String fieldType, int line);

	void _readStaticField(Object fieldValue, String refType, String fieldName, String fieldTypeSign, int line);

	void _writeLocalVar(Object varValue, String varName, String varType, int line, int bcLocalVarIdx,
			int varScopeStartLine, int varScopeEndLine);

	void _readLocalVar(Object varValue, String varName, String varType, int line, int bcLocalVarIdx,
			int varScopeStartLine, int varScopeEndLine);

	void _readArrayElementVar(Object arrayRef, int index, Object eleValue, String elementType, int line);

	void _writeArrayElementVar(Object arrayRef, int index, Object eleValue, String elementType, int line);

}
