package microbat.instrumentation.trace.data;

import java.util.HashMap;
import java.util.Map;

import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.ArrayValue;
import microbat.model.value.PrimitiveValue;
import microbat.model.value.ReferenceValue;
import microbat.model.value.StringValue;
import microbat.model.value.VarValue;
import microbat.model.variable.ArrayElementVar;
import microbat.model.variable.FieldVar;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
import microbat.util.PrimitiveUtils;
import sav.common.core.utils.SignatureUtils;
import sav.common.core.utils.StringUtils;
import sav.strategies.dto.AppJavaClassPath;

public class ExecutionTracer {
	private static Map<Long, ExecutionTracer> rtStores;

	static {
		rtStores = new HashMap<>();
	}

	private Trace trace;

	private TraceNode currentNode;
	private BreakPoint methodEntry;
	private MethodCallStack methodCallStack;

	ExecutionTracer() {
		methodCallStack = new MethodCallStack();
		AppJavaClassPath appJavaClassPath = new AppJavaClassPath();
		trace = new Trace(appJavaClassPath);
	}

	/* TODO: Set aliasVarId*/
	private VarValue appendVarValue(Object fieldValue, Variable var, VarValue parent) {
		boolean isRoot = (parent == null);
		VarValue value = null;
		if (PrimitiveUtils.isString(var.getType())) {
			return new StringValue(getStringValue(fieldValue), isRoot, var);
		} else if (PrimitiveUtils.isPrimitiveType(var.getType())) {
			return new PrimitiveValue(getStringValue(fieldValue), isRoot, var);
		} else if(var.getType().endsWith("[]")) {
			/* array */
			ArrayValue arrVal = new ArrayValue(fieldValue == null, isRoot, var);
			arrVal.setComponentType(var.getType().substring(0, var.getType().length() - 2)); // 2 = "[]".length
			value = arrVal;
			/* TODO append children */
		} else {
			ReferenceValue refVal = new ReferenceValue(fieldValue == null, getUniqueId(fieldValue), isRoot, var);
			value = refVal;
			/* TODO append children */
		}
		if (parent != null) {
			parent.linkAchild(value);
		}
		return value;
	}
	
	private String getStringValue(Object obj) {
		return StringUtils.toString(obj, null);
	}

	private String getUniqueIdStr(Object refValue) {
		if (refValue == null) {
			return null;
		}
		return String.valueOf(getUniqueId(refValue));
	}

	private long getUniqueId(Object refValue) {
		if (refValue == null) {
			return -1;
		}
		return System.identityHashCode(refValue);
	}
	
	/* 
	 * Methods which are with prefix "_" are called in instrument code.
	 * =================================================================
	 * */
	public void _enterMethod(String className, String methodName) {
		methodEntry = new BreakPoint(className, null, methodName, -1);
		currentNode = null;
	}

	public void _exitMethod(int line) {
		currentNode = methodCallStack.safePop();
	}

	public void _hitInvoke(int line, Object invokeObj, String methodName) {
		_hitLine(line);
		methodCallStack.push(currentNode);
	}

	public void _hitLine(int line) {
		if (currentNode != null && currentNode.getBreakPoint().getLineNumber() == line) {
			return;
		}
		BreakPoint bkp = new BreakPoint(methodEntry.getClassCanonicalName(), null, methodEntry.getMethodName(), line);
		int order = trace.size() + 1;
		currentNode = new TraceNode(bkp, null, order, trace);
		trace.addTraceNode(currentNode);
	}
	
	/**
	 * @param refValue
	 * @param fieldValue
	 * @param fieldIdx
	 * @param fieldTypeSign
	 * @param line
	 */
	public void _writeField(Object refValue, Object fieldValue, int fieldIdx, String fieldName, String fieldTypeSign, int line) {
		_hitLine(line);
		Variable var = new FieldVar(false, fieldName, SignatureUtils.signatureToName(fieldTypeSign));
		var.setVarID(Variable.concanateFieldVarID(getUniqueIdStr(refValue), fieldName));
		VarValue value = appendVarValue(fieldValue, var, null);
		currentNode.addWrittenVariable(value);
	}
	
	/**
	 * @param fieldValue
	 * @param refType
	 * @param fieldName
	 * @param fieldTypeSign
	 * @param line
	 */
	public void _writeStaticField(Object fieldValue, String refType, String fieldName, String fieldTypeSign, int line) {
		_hitLine(line);
		Variable var = new FieldVar(false, fieldName, SignatureUtils.signatureToName(fieldTypeSign));
		var.setVarID(Variable.concanateFieldVarID(refType, fieldName));
		VarValue value = appendVarValue(fieldValue, var, null);
		currentNode.addWrittenVariable(value);
	}

	/**
	 * @param refValue
	 * @param fieldValue
	 * @param fieldIdx
	 * @param fieldTypeSign
	 * @param line
	 */
	public void _readField(Object refValue, Object fieldValue, int fieldIdx, String fieldName, String fieldTypeSign, int line) {
		_hitLine(line);
		Variable var = new FieldVar(false, fieldName, SignatureUtils.signatureToName(fieldTypeSign));
		var.setVarID(Variable.concanateFieldVarID(getUniqueIdStr(refValue), fieldName));
		VarValue value = appendVarValue(fieldValue, var, null);
		currentNode.addReadVariable(value);
	}

	/**
	 * @param fieldValue
	 * @param refType
	 * @param fieldName
	 * @param fieldTypeSign
	 * @param line
	 */
	public void _readStaticField(Object fieldValue, String refType, String fieldName, String fieldTypeSign, int line) {
		_hitLine(line);
		Variable var = new FieldVar(true, fieldName, SignatureUtils.signatureToName(fieldTypeSign));
		var.setVarID(Variable.concanateFieldVarID(refType, fieldName));
		VarValue value = appendVarValue(fieldValue, var, null);
		currentNode.addReadVariable(value);
	}
	
	/**
	 * @param varValue
	 * @param varName
	 * @param varType
	 * @param line
	 * @param bcLocalVarIdx
	 */
	public void _writeLocalVar(Object varValue, String varName, String varType, int line, int bcLocalVarIdx,
			int varScopeStartLine, int varScopeEndLine) {
		_hitLine(line);
		Variable var = new LocalVar(varName, varType, methodEntry.getClassCanonicalName(), line);
		var.setVarID(Variable.concanateLocalVarID(methodEntry.getClassCanonicalName(), varName, varScopeStartLine,
				varScopeEndLine));
		VarValue value = appendVarValue(varValue, var, null);
		currentNode.addWrittenVariable(value);
	}
	
	/**
	 * @param value
	 * @param varName
	 * @param varType
	 * @param line
	 * @param bcLocalVarIdx
	 */
	public void _readLocalVar(Object varValue, String varName, String varType, int line, int bcLocalVarIdx,
			int varScopeStartLine, int varScopeEndLine) {
		_hitLine(line);
		Variable var = new LocalVar(varName, varType, methodEntry.getClassCanonicalName(), line);
		var.setVarID(Variable.concanateLocalVarID(methodEntry.getClassCanonicalName(), varName, varScopeStartLine,
				varScopeEndLine));
		VarValue value = appendVarValue(varValue, var, null);
		currentNode.addReadVariable(value);
	}
	
	public void _readArrayElementVar(Object arrayRef, int index, Object eleValue, String elementType, int line) {
		_hitLine(line);
		addArrayElementVarValue(arrayRef, index, eleValue, elementType, line, false);
	}
	
	public void _writeArrayElementVar(Object arrayRef, int index, Object eleValue, String elementType, int line) {
		_hitLine(line);
		addArrayElementVarValue(arrayRef, index, eleValue, elementType, line, true);
	}
	
	private void addArrayElementVarValue(Object arrayRef, int index, Object eleValue, String elementType, int line, boolean write) {
		String name = new StringBuilder(getUniqueIdStr(arrayRef)).append("[").append(index).append("]").toString();
		String eleType = SignatureUtils.signatureToName(arrayRef.getClass().getName());
		Variable var = new ArrayElementVar(name, eleType, null);
		VarValue value = appendVarValue(eleValue, var, null);
		if (write) {
			currentNode.addWrittenVariable(value);
		} else {
			currentNode.addReadVariable(value);
		}
	}
	
	public void tryTracer(Object refValue, Object fieldValue) {
		// TODO LLT: do nothing, JUST FOR TEST. TO REMOVE.
	}

	public synchronized static ExecutionTracer _getTracer() {
		long threadId = Thread.currentThread().getId();
		ExecutionTracer store = rtStores.get(threadId);
		if (store == null) {
			store = new ExecutionTracer();
			rtStores.put(threadId, store);
		}
		return store;
	}
}
