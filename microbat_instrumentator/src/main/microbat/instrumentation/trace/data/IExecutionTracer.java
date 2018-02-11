package microbat.instrumentation.trace.data;

public interface IExecutionTracer {

	public void _hitInvoke(Object invokeObj, String invokeTypeSign, String methodName, Object[] params,
			String paramTypeSignsCode, String returnTypeSign, int line);

	public void _hitInvokeStatic(String invokeTypeSign, String methodName, Object[] params, String paramTypeSignsCode,
			String returnTypeSign, int line);

	public void _hitReturn(Object returnObj, String returnGeneralType, int line);

	public void _hitVoidReturn(int line);

	public void _hitLine(int line);

	public void _writeField(Object refValue, Object fieldValue, String fieldName, String fieldTypeSign, int line);

	public void _writeStaticField(Object fieldValue, String refType, String fieldName, String fieldType, int line);

	public void _readField(Object refValue, Object fieldValue, String fieldName, String fieldType, int line);

	public void _readStaticField(Object fieldValue, String refType, String fieldName, String fieldTypeSign, int line);

	public void _writeLocalVar(Object varValue, String varName, String varType, int line, int bcLocalVarIdx,
			int varScopeStartLine, int varScopeEndLine);

	public void _readLocalVar(Object varValue, String varName, String varType, int line, int bcLocalVarIdx,
			int varScopeStartLine, int varScopeEndLine);

	public void _readArrayElementVar(Object arrayRef, int index, Object eleValue, String elementType, int line);

	public void _writeArrayElementVar(Object arrayRef, int index, Object eleValue, String elementType, int line);

	public void _afterInvoke(String loc, int line);

	public void _hitMethodEnd(int line);
	
}
