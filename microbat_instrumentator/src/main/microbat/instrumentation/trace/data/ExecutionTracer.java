package microbat.instrumentation.trace.data;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.model.BreakPoint;
import microbat.model.trace.StepVariableRelationEntry;
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
import microbat.model.variable.VirtualVar;
import microbat.util.PrimitiveUtils;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.SignatureUtils;
import sav.common.core.utils.StringUtils;
import sav.strategies.dto.AppJavaClassPath;

public class ExecutionTracer implements IExecutionTracer {
	private static Map<Long, ExecutionTracer> rtStores;
	private static long mainThreadId = -1;
	
	public static AppJavaClassPath appJavaClassPath;
	
	//TODO this parameter should be controlled by user.
	public static int variableLayer = 2;
	static {
		rtStores = new HashMap<>();
	}
	private Trace trace;

//	private TraceNode currentNode;
//	private BreakPoint methodEntry;
	/*
	 * For exclusive case, invokeTrack will be recorded to collect data.
	 */
	private boolean exclusive;
//	private InvokingTrack invokeTrack = new EmptyInvokingTrack();
	private MethodCallStack methodCallStack;
	private Locker locker;

	public ExecutionTracer(long threadId) {
		locker = new Locker(threadId);
		methodCallStack = new MethodCallStack();
		trace = new Trace(appJavaClassPath);
	}

	private void buildDataRelation(Variable var, String rw){
		TraceNode currentNode = trace.getLatestNode();
		String order = trace.findDefiningNodeOrder(rw, currentNode, var.getVarID(), var.getAliasVarID());
		String varID = var.getVarID() + ":" + order;
		var.setVarID(varID);
		if(var.getAliasVarID()!=null){
			var.setAliasVarID(var.getAliasVarID()+":"+order);			
		}
		
		StepVariableRelationEntry entry = trace.getStepVariableTable().get(varID);
		if(entry == null){
			entry = new StepVariableRelationEntry(varID);
		}
		if(rw.equals(Variable.READ)){
			entry.addConsumer(currentNode);
		}
		else if(rw.equals(Variable.WRITTEN)){
			entry.addProducer(currentNode);
		}
		trace.getStepVariableTable().put(varID, entry);
	}
	
	private VarValue appendVarValue(Object value, Variable var, VarValue parent) {
		return appendVarValue(value, var, parent, variableLayer);
	}
	
	/* TODO: Set aliasVarId*/
	private VarValue appendVarValue(Object value, Variable var, VarValue parent, int retrieveLayer) {
		if (retrieveLayer < 0) {
			return null;
		}
		retrieveLayer--;
		
		boolean isRoot = (parent == null);
		VarValue varValue = null;
		if (PrimitiveUtils.isString(var.getType())) {
			varValue = new StringValue(getStringValue(value), isRoot, var);
		} else if (PrimitiveUtils.isPrimitiveType(var.getType())) {
			varValue = new PrimitiveValue(getStringValue(value), isRoot, var);
		} else if(var.getType().endsWith("[]")) {
			/* array */
			ArrayValue arrVal = new ArrayValue(value == null, isRoot, var);
			arrVal.setComponentType(var.getType().substring(0, var.getType().length() - 2)); // 2 = "[]".length
			varValue = arrVal;
			varValue.setStringValue(getStringValue(value));
			if (value == null) {
				arrVal.setNull(true);
			} else {
				int length = Array.getLength(value);
				for (int i = 0; i < length; i++) {
					String parentSimpleID = Variable.truncateSimpleID(var.getVarID());
					String aliasVarID = Variable.concanateArrayElementVarID(parentSimpleID, String.valueOf(i));
					String varName = String.valueOf(i);
					ArrayElementVar varElement = new ArrayElementVar(varName, arrVal.getComponentType(), aliasVarID);
					Object elementValue = Array.get(value, i);
					appendVarValue(elementValue, varElement, arrVal, retrieveLayer);
				}
			}
		} else {
			ReferenceValue refVal = new ReferenceValue(value == null, TraceUtils.getUniqueId(value), isRoot, var);
			varValue = refVal;
			varValue.setStringValue(getStringValue(value));
			if (value != null) {
				Class<?> objClass = value.getClass();
				boolean needParseFields = HeuristicIgnoringFieldRule.isNeedParsingFields(objClass);
				if (needParseFields) {
					List<Field> allFields = CollectionUtils.toArrayList(objClass.getDeclaredFields());
					Collections.sort(allFields, new Comparator<Field>() {
						@Override
						public int compare(Field o1, Field o2) {
							return o1.getName().compareTo(o2.getName());
						}
					});
					for (Field field : allFields) {
						field.setAccessible(true);
						try {
							Object fieldValue = field.get(value);
							Class<?> fieldType = field.getType();
							String fieldTypeStr = fieldType.getName();
							if (fieldType.isArray()) {
								fieldTypeStr = SignatureUtils.signatureToName(fieldTypeStr);
							}
							if(fieldType.isEnum()){
								if(fieldTypeStr.equals(var.getType())){
									continue;
								}
							}
							boolean isIgnore = HeuristicIgnoringFieldRule.isForIgnore(objClass, field);
							if(!isIgnore){
								if(fieldValue != null){
									FieldVar fieldVar = new FieldVar(Modifier.isStatic(field.getModifiers()),
											field.getName(), fieldTypeStr, field.getDeclaringClass().getName());
									appendVarValue(fieldValue, fieldVar, refVal, retrieveLayer);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		if (parent != null) {
			parent.linkAchild(varValue);
		}
		return varValue;
	}
	
	private String getStringValue(Object obj) {
		return StringUtils.toString(obj, null);
	}

	/* 
	 * Methods which are with prefix "_" are called in instrument code.
	 * =================================================================
	 * */
	public void enterMethod(String className, String methodSignature, int methodStartLine) {
		locker.lock();
		exclusive = FilterChecker.isExclusive(className, methodSignature);
		// TODO-INSTR: declaringCompilationUnitName would not be correct here.
//		currentNode = null;
		
		TraceNode latestNode = trace.getLatestNode();
		if(latestNode!=null){
			methodCallStack.push(latestNode, exclusive);
		}
		
		locker.unLock();
	}

	public void exitMethod(int line) {
		locker.lock();
		OnWorkingMethod onWorkingMethod = methodCallStack.safePop();
		if (onWorkingMethod != null) {
			this.exclusive = onWorkingMethod.isExclusive();
		}
		locker.unLock();
	}

	@Override
	public void _hitInvoke(Object invokeObj, String invokeTypeSign, String methodSig, Object[] params,
			String paramTypeSignsCode, String returnTypeSign, int line, String residingClassName, String residingMethodSignature) {
		_hitLine(line, residingClassName, residingMethodSignature);
		locker.lock();
//		InvokingDetail invokeDetail = new InvokingDetail(invokeObj, invokeTypeSign, methodSig, params,
//				TraceUtils.parseArgTypes(paramTypeSignsCode));
		
		TraceNode latestNode = trace.getLatestNode();
		latestNode.setInvokingMethod(methodSig);
//		latestNode.setInvokingDetail(invokeDetail);
		
		locker.unLock();
	}
	
	@Override
	public void _hitInvokeStatic(String invokeTypeSign, String methodName, Object[] params,
			String paramTypeSignsCode, String returnTypeSign, int line, String className, String methodSignature) {
		_hitLine(line, className, methodSignature);
		locker.lock();
		TraceNode latestNode = trace.getLatestNode();
		latestNode.setInvokingMethod(methodName+paramTypeSignsCode);
		locker.unLock();
	}
	
	@Override
	public void _hitMethodEnd(int line){
//		System.out.println(currentNode);
		exitMethod(line);
	}
	
	@Override
	public void _afterInvoke(String loc, int line) {
		locker.lock();
//		if (methodEntry.getClassCanonicalName().equals(loc) && methodEntry.getLineNumber() == line) {
//			/* exit success */
//		} else {
//			exitMethod(-1);
//		}
		locker.unLock();
	}
	
	/**
	 * @param line
	 * @param returnObj
	 * @param returnGeneralTypeSign (if type is object type -> this will be display of object type, not specific name 
	 */
	@Override
	public void _hitReturn(Object returnObj, String returnGeneralTypeSign, int line, String className, String methodSignature) {
		_hitLine(line, className, methodSignature);
		locker.lock();
		String returnGeneralType = SignatureUtils.signatureToName(returnGeneralTypeSign);
		Variable returnVar = new VirtualVar(methodSignature, returnGeneralType);
		String varID = VirtualVar.VIRTUAL_PREFIX + methodSignature;
		returnVar.setVarID(varID);
		VarValue returnVal = appendVarValue(returnObj, returnVar, null);
		TraceNode latestNode = trace.getLatestNode();
		latestNode.addReturnVariable(returnVal);
		
		locker.unLock();
	}
	
	@Override
	public void _hitVoidReturn(int line, String className, String methodSignature) {
		_hitLine(line, className, methodSignature);
	}

	@Override
	public void _hitLine(int line, String className, String methodSignature) {
		if (exclusive) {
			return;
		}
		locker.lock();
		TraceNode latestNode = trace.getLatestNode();
		if (latestNode != null && latestNode.getBreakPoint().getLineNumber() == line) {
			locker.unLock();
			return;
		}
		
		BreakPoint bkp = new BreakPoint(className, methodSignature, line);
		int order = trace.size() + 1;
		TraceNode currentNode = new TraceNode(bkp, null, order, trace); // leave programState empty.
		trace.addTraceNode(currentNode);
		
		if(!methodCallStack.isEmpty()){
			OnWorkingMethod owm = methodCallStack.peek();
			TraceNode caller = owm.getCurrentNode();
			caller.addInvocationChild(currentNode);
			currentNode.setInvocationParent(caller);			
		}
		
		locker.unLock();
	}
	
	/**
	 * @param refValue
	 * @param fieldValue
	 * @param fieldType
	 * @param line
	 */
	@Override
	public void _writeField(Object refValue, Object fieldValue, String fieldName, String fieldType, int line,
			String className, String methodSignature) {
		_hitLine(line, className, methodSignature);
		locker.lock();
		String parentVarId = TraceUtils.getObjectVarId(refValue);
		String fieldVarId = Variable.concanateFieldVarID(parentVarId, fieldName);
//		boolean invokeRelevant = invokeTrack.updateRelevant(parentVarId, fieldVarId);
//		if (exclusive && !invokeRelevant) {
//			locker.unLock();
//			return;
//		}
		Variable var = new FieldVar(false, fieldName, fieldType, fieldType);
		var.setVarID(fieldVarId);
		VarValue value = appendVarValue(fieldValue, var, null);
		addRWriteValue(value, true);
//		if (invokeRelevant) {
//			invokeTrack.addWrittenValue(value);
//		}
		locker.unLock();
	}

	private void addRWriteValue(VarValue value, boolean isWrittenVar) {
		TraceNode currentNode = trace.getLatestNode();
		if (currentNode == null) {
			return;
		}
		if (isWrittenVar) {
			currentNode.addWrittenVariable(value);
			buildDataRelation(value.getVariable(), Variable.WRITTEN);
		} else {
			currentNode.addReadVariable(value);
			buildDataRelation(value.getVariable(), Variable.READ);
		}
	}
	
	/**
	 * @param fieldValue
	 * @param refType
	 * @param fieldName
	 * @param fieldTypeSign
	 * @param line
	 * TODO LLT: handle relevant?
	 */
	@Override
	public void _writeStaticField(Object fieldValue, String refType, String fieldName, String fieldType, int line, String className, String methodSignature) {
		if (exclusive) {
			return;
		}
		_hitLine(line, className, methodSignature);
		locker.lock();
		Variable var = new FieldVar(false, fieldName, fieldType, fieldType);
		var.setVarID(Variable.concanateFieldVarID(refType, fieldName));
		VarValue value = appendVarValue(fieldValue, var, null);
		addRWriteValue(value, true);
		locker.unLock();
	}

	/**
	 * @param refValue
	 * @param fieldValue
	 * @param fieldType
	 * @param line
	 */
	@Override
	public void _readField(Object refValue, Object fieldValue, String fieldName, String fieldType, int line, String className, String methodSignature) {
		_hitLine(line, className, methodSignature);
		locker.lock();
		String parentVarId = TraceUtils.getObjectVarId(refValue);
		String fieldVarId = TraceUtils.getFieldVarId(parentVarId, fieldName, fieldType, fieldValue);
//		invokeTrack.updateRelevant(parentVarId, fieldVarId);
//		if (exclusive) {
//			return;
//		}
		Variable var = new FieldVar(false, fieldName, fieldType, fieldType);
		var.setVarID(fieldVarId);
		VarValue value = appendVarValue(fieldValue, var, null);
		addRWriteValue(value, false);
		locker.unLock();
	}

	/**
	 * @param fieldValue
	 * @param refType
	 * @param fieldName
	 * @param fieldType
	 * @param line
	 */
	@Override
	public void _readStaticField(Object fieldValue, String refType, String fieldName, String fieldType, int line,
			String className, String methodSignature) {
		if (exclusive) {
			return;
		}
		_hitLine(line, className, methodSignature);
		locker.lock();
		Variable var = new FieldVar(true, fieldName, fieldType, fieldType);
		var.setVarID(Variable.concanateFieldVarID(refType, fieldName));
		VarValue value = appendVarValue(fieldValue, var, null);
		addRWriteValue(value, false);
		locker.unLock();
	}
	
	/**
	 * @param varValue
	 * @param varName
	 * @param varType
	 * @param line
	 * @param bcLocalVarIdx
	 */
	@Override
	public void _writeLocalVar(Object varValue, String varName, String varType, int line, int bcLocalVarIdx,
			int varScopeStartLine, int varScopeEndLine, String className, String methodSignature) {
		if (exclusive) {
			return;
		}
		_hitLine(line, className, methodSignature);
		locker.lock();
		Variable var = new LocalVar(varName, varType, className, line);
		String varID = TraceUtils.getLocalVarId(className, varScopeStartLine, varScopeEndLine, varName, varType, varValue);
		var.setVarID(varID);
		VarValue value = appendVarValue(varValue, var, null);
		addRWriteValue(value, true);
		locker.unLock();
	}
	
	/**
	 * @param value
	 * @param varName
	 * @param varType
	 * @param line
	 * @param bcLocalVarIdx
	 */
	@Override
	public void _readLocalVar(Object varValue, String varName, String varType, int line, int bcLocalVarIdx,
			int varScopeStartLine, int varScopeEndLine, String className, String methodSignature) {
		_hitLine(line, className, methodSignature);
		locker.lock();
		Variable var = new LocalVar(varName, varType, className, line);
		String varID = TraceUtils.getLocalVarId(className, varScopeStartLine, varScopeEndLine, varName, varType, varValue);
		var.setVarID(varID);
		VarValue value = appendVarValue(varValue, var, null);
		addRWriteValue(value, false);
		locker.unLock();
	}
	
	/**
	 * @param arrayRef
	 * @param index
	 * @param eleValue
	 * @param elementType
	 * @param line
	 */
	@Override
	public void _readArrayElementVar(Object arrayRef, int index, Object eleValue, String elementType, int line, String className, String methodSignature) {
		_hitLine(line, className, methodSignature);
		locker.lock();
//		String parentVarId = TraceUtils.getObjectVarId(arrayRef);
//		String arrEleVarId = TraceUtils.getArrayElementVarId(parentVarId, index, elementType, eleValue);
//		invokeTrack.updateRelevant(parentVarId, arrEleVarId);
//		if (exclusive) {
//			locker.unLock();
//			return;
//		}
		addArrayElementVarValue(arrayRef, index, eleValue, elementType, line, false);
		locker.unLock();
	}
	
	/**
	 * 
	 * @param arrayRef
	 * @param index
	 * @param eleValue
	 * @param elementType
	 * @param line
	 */
	@Override
	public void _writeArrayElementVar(Object arrayRef, int index, Object eleValue, String elementType, int line, String className, String methodSignature) {
		_hitLine(line, className, methodSignature);
		locker.lock();
//		String parentVarId = TraceUtils.getObjectVarId(arrayRef);
//		String arrEleVarId = TraceUtils.getArrayElementVarId(parentVarId, index, elementType, eleValue);
//		boolean involeRelevant = invokeTrack.updateRelevant(parentVarId, arrEleVarId);
//		if (exclusive && !involeRelevant) {
//			locker.unLock();
//			return;
//		}
		VarValue value = addArrayElementVarValue(arrayRef, index, eleValue, elementType, line, true);
//		invokeTrack.addWrittenValue(value);
		locker.unLock();
	}
	
	private VarValue addArrayElementVarValue(Object arrayRef, int index, Object eleValue, String elementType, int line,
			boolean write) {
		String id = new StringBuilder(TraceUtils.getObjectVarId(arrayRef)).append("[").append(index).append("]").toString();
		String name = id;
		Variable var = new ArrayElementVar(name, elementType, id);
		var.setVarID(id);
		VarValue value = appendVarValue(eleValue, var, null);
		addRWriteValue(value, write);
		return value;
	}
	
	private static LockedThreads lockedThreads = new LockedThreads();
	private static final Locker gLocker = new Locker();
	
	public synchronized static IExecutionTracer _getTracer(boolean startTracing, String className, String methodSig,
			int methodStartLine, String paramTypeSignsCode, Object[] params) {
		if (gLocker.isLock() && !startTracing) {
			return EmptyExecutionTracer.getInstance();
		}
		gLocker.lock();
		long threadId = Thread.currentThread().getId();
		if (lockedThreads.contains(threadId)) {
			gLocker.unLock();
			return EmptyExecutionTracer.getInstance();
		}
		if (mainThreadId < 0) {
			mainThreadId = threadId;
		}
		ExecutionTracer tracer = getTracer(threadId);
		if (tracer.locker.isLock()) {
			gLocker.unLock();
			return EmptyExecutionTracer.getInstance();
		}
//		String name = className.replace("/", ".");
		tracer.enterMethod(className, methodSig, methodStartLine);
		gLocker.unLock();
		return tracer;
	}

	private static ExecutionTracer getTracer(long threadId) {
		ExecutionTracer store = rtStores.get(threadId);
		if (store == null) {
			store = new ExecutionTracer(threadId);
			rtStores.put(threadId, store);
		}
		return store;
	}
	
	public static Map<Long, ExecutionTracer> getRtStores() {
		return rtStores;
	}
	
	public synchronized static IExecutionTracer getMainThreadStore() {
		return getTracer(mainThreadId);
	}
	
	private static boolean shutdown;
	public static void shutdown() {
		gLocker.lock();
		shutdown = true;
	}
	
	public static boolean isShutdown() {
		return shutdown;
	}
	
	public Trace getTrace() {
		return trace;
	}
	
	private static class Locker {
		boolean tracing;
		long threadId;
		
		public Locker() {
			// global locker
			this.threadId = -1;
			lock();
		}
		
		public Locker(long threadId) {
			this.threadId = threadId;
		}

		public void lock() {
			if (!tracing) {
				lockedThreads.add(threadId);
				tracing = true;
			}
		}
		
		public void unLock() {
			tracing = false;
			lockedThreads.remove(threadId);
		}
		
		public boolean isLock() {
			return tracing;
		}
	}
}
