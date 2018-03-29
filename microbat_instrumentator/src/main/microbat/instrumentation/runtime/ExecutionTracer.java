package microbat.instrumentation.runtime;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

import microbat.codeanalysis.bytecode.ByteCodeParser;
import microbat.codeanalysis.bytecode.MethodFinderBySignature;
import microbat.instrumentation.Agent;
import microbat.instrumentation.AgentLogger;
import microbat.instrumentation.filter.FilterChecker;
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
import sav.strategies.dto.AppJavaClassPath;

public class ExecutionTracer implements IExecutionTracer {
	private static Map<Long, ExecutionTracer> rtStores;
	private static long mainThreadId = -1;
	
	public static AppJavaClassPath appJavaClassPath;
	public static int variableLayer = 2;
	public static int stepLimit = Integer.MAX_VALUE;
	public static int expectedSteps = Integer.MAX_VALUE;
	
	static {
		rtStores = new HashMap<>();
	}
	private Trace trace;

	private MethodCallStack methodCallStack;
	private Locker locker;

	public ExecutionTracer(long threadId) {
		locker = new Locker(threadId);
		methodCallStack = new MethodCallStack();
		trace = new Trace(appJavaClassPath);
	}

	private void buildDataRelation(VarValue value, String rw){
		Variable var = value.getVariable();
		TraceNode currentNode = trace.getLatestNode();
		if(currentNode==null){
			return;
		}
		
		String order = trace.findDefiningNodeOrder(rw, currentNode, var.getVarID(), var.getAliasVarID());
		
		if(order.equals("0")){
			if(var instanceof FieldVar || var instanceof ArrayElementVar){
				if(!value.getParents().isEmpty()){
					String parentID = value.getParents().get(0).getVarID();
					order = trace.findDefiningNodeOrder(rw, currentNode, parentID, parentID);					
				}
				
			}
		}
		
		String varID = var.getVarID() + ":" + order;
		var.setVarID(varID);
		if(var.getAliasVarID()!=null){
			var.setAliasVarID(var.getAliasVarID()+":"+order);			
		}
		
		StepVariableRelationEntry entry = trace.getStepVariableTable().get(varID);
		if(entry == null){
			entry = new StepVariableRelationEntry(varID);
			if(!order.equals("0")){
				TraceNode producer = trace.getTraceNode(Integer.valueOf(order));
				entry.addProducer(producer);
				trace.getStepVariableTable().put(varID, entry);
			}
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
		if (retrieveLayer <= 0) {
			return null;
		}
		retrieveLayer--;
		
		boolean isRoot = (parent == null);
		VarValue varValue = null;
		if (PrimitiveUtils.isString(var.getType())) {
			varValue = new StringValue(getStringValue(value, null), isRoot, var);
		} else if (PrimitiveUtils.isPrimitive(var.getType())) {
			varValue = new PrimitiveValue(getStringValue(value, null), isRoot, var);
		} else if(var.getType().endsWith("[]")) {
			/* array */
			ArrayValue arrVal = new ArrayValue(value == null, isRoot, var);
			arrVal.setComponentType(var.getType().substring(0, var.getType().length() - 2)); // 2 = "[]".length
			varValue = arrVal;
//			varValue.setStringValue(getStringValue(value, arrVal.getComponentType()));
			varValue.setStringValue(getStringValue(value, null));
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
//			varValue.setStringValue(getStringValue(value, var.getType()));
			varValue.setStringValue(getStringValue(value, null));
			if (value != null) {
				Class<?> objClass = value.getClass();
				boolean needParseFields = HeuristicIgnoringFieldRule.isNeedParsingFields(objClass);
				if (needParseFields) {
					List<Field> allFields = CollectionUtils.toArrayList(objClass.getDeclaredFields());
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
									fieldVar.setVarID(TraceUtils.getFieldVarId(var.getVarID(), field.getName(), fieldTypeStr, fieldValue));
									appendVarValue(fieldValue, fieldVar, refVal, retrieveLayer);
								}
							}
						} catch (Exception e) {
							handleException(e);
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

	private String getStringValue(Object obj, String type) {
		try {
			if (obj == null) {
				return "null";
			}
			
			return obj.toString();
//			String className = obj.getClass().getName();
//			
//			if (FilterChecker.isAppClazz(className)
//					|| ((type != null) && FilterChecker.isAppClazz(type))) {
//				return className;
//			}
//			if (Map.class.isAssignableFrom(obj.getClass())) {
//				Map<?, ?> map = (Map<?, ?>) obj;
//				if (!map.isEmpty()) {
//					if (FilterChecker.isAppClazz(map.keySet().iterator().next().getClass().getName())) {
//						return className + ", size=" + map.size();
//					} else if (!map.values().isEmpty()){
//						Object value = map.values().iterator().next();
//						if (value != null && FilterChecker.isAppClazz(value.getClass().getName())) {
//							return className + ", size=" + map.size();
//						}
//					}
//				}
//			}
//			return obj.toString();
		} catch (Throwable t) {
			return null;
		}
	}

	/* 
	 * Methods with prefix "_" are called in instrument code.
	 * =================================================================
	 * */
	public void enterMethod(String className, String methodSignature, int methodStartLine, int methodEndLine, 
			String paramTypeSignsCode, String paramNamesCode, Object[] params) {
		locker.lock();
		TraceNode latestNode = trace.getLatestNode();
		if(latestNode!=null){
			int varScopeStart = methodStartLine;
			int varScopeEnd = methodEndLine;
			
			String[] parameterTypes = TraceUtils.parseArgTypesOrNames(paramTypeSignsCode);
			String[] parameterNames = paramNamesCode.split(":");
			if(parameterNames.length != 0){
				int adjust = adjustVariableStartScope(methodSignature, className);
				varScopeStart = (adjust > 0) ? adjust : varScopeStart;
			}
			
			for(int i=0; i<parameterTypes.length; i++){
				String pType = parameterTypes[i];
				String parameterType = SignatureUtils.signatureToName(pType);
				String varName = parameterNames[i];
				
				Variable var = new LocalVar(varName, parameterType, className, methodStartLine);
				
				String varID = Variable.concanateLocalVarID(className, varName, varScopeStart, varScopeEnd);
				var.setVarID(varID);
				VarValue value = appendVarValue(params[i], var, null);
				addRWriteValue(value, true);
			}
		}
		
		boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
		if (!exclusive) {
			if(latestNode!=null){
				methodCallStack.push(latestNode);
			}
			_hitLine(methodStartLine, className, methodSignature);
		} else {
			locker.unLock();
			return;
		}
		locker.unLock();
	}

	private int adjustVariableStartScope(String fullSign, String className) {
		String shortSign = fullSign.substring(fullSign.indexOf("#")+1, fullSign.length());
		MethodFinderBySignature finder = new MethodFinderBySignature(shortSign);
		ByteCodeParser.parse(className, finder, appJavaClassPath);
		Method method = finder.getMethod();
		
		LocalVariableTable table = method.getLocalVariableTable();
		if(table!=null){
			int start = -1;
			for(LocalVariable v: table.getLocalVariableTable()){
				int line = method.getCode().getLineNumberTable().getSourceLine(v.getStartPC());
				if(start==-1){
					start = line;
				}
				else{
					if(start > line){
						start = line;
					}
				}
			}
			return start;
		}
		
		return -1;
	}
	
	public void exitMethod(int line, String className, String methodSignature) {
		locker.lock();
		boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
		if(!exclusive){
			methodCallStack.safePop();			
		}
		locker.unLock();
	}

	@Override
	public void _hitInvoke(Object invokeObj, String invokeTypeSign, String methodSig, Object[] params,
			String paramTypeSignsCode, String returnTypeSign, int line, String residingClassName, String residingMethodSignature) {
		locker.lock();
		try {
			_hitLine(line, residingClassName, residingMethodSignature);
			TraceNode latestNode = trace.getLatestNode();
			if (latestNode != null) {
				latestNode.setInvokingMethod(methodSig);
				initInvokingDetail(invokeObj, invokeTypeSign, methodSig, params, paramTypeSignsCode, residingClassName,
						latestNode);
			}
		} catch (Throwable t) {
			handleException(t);
		}
		
		locker.unLock();
	}

	private void initInvokingDetail(Object invokeObj, String invokeTypeSign, String methodSig, Object[] params,
			String paramTypeSignsCode, String residingClassName, TraceNode latestNode) {
		boolean exclusive = FilterChecker.isExclusive(invokeTypeSign, methodSig);
		if (exclusive && latestNode.getBreakPoint().getClassCanonicalName().equals(residingClassName)) {
			InvokingDetail invokeDetail = latestNode.getInvokingDetail();
			if (invokeDetail == null) {
				invokeDetail = new InvokingDetail(latestNode);
				latestNode.setInvokingDetail(invokeDetail);
			}
			invokeDetail.initRelevantVars(invokeObj, params, paramTypeSignsCode);
		}
	}
	
	@Override
	public void _hitInvokeStatic(String invokeTypeSign, String methodSig, Object[] params,
			String paramTypeSignsCode, String returnTypeSign, int line, String className, String methodSignature) {
		locker.lock();
		try {
			_hitLine(line, className, methodSignature);
			TraceNode latestNode = trace.getLatestNode();
			if (latestNode != null) {
				latestNode.setInvokingMethod(methodSig);
				initInvokingDetail(null, invokeTypeSign, methodSignature, params, paramTypeSignsCode, className,
						latestNode);
			}
		} catch (Throwable t) {
			handleException(t);
		}
		locker.unLock();
	}
	
	@Override
	public void _hitMethodEnd(int line, String className, String methodSignature){
		locker.lock();
		try {
			exitMethod(line, className, methodSignature);
		} catch (Throwable t) {
			handleException(t);
		}
		locker.unLock();
	}
	
	/**
	 * Instrument for: Application Classes only.
	 */
	@Override
	public void _afterInvoke(int line, String residingClassName, String residingMethodSignature, boolean needRevisiting) {
		locker.lock();
		try {
			boolean exclusive = FilterChecker.isExclusive(residingClassName, residingMethodSignature);
			if (!exclusive) {
				TraceNode latestNode = trace.getLatestNode();
				if (latestNode != null) {
					latestNode.setInvokingDetail(null);
				}
				if (needRevisiting) {
					_hitLine(line, residingClassName, residingMethodSignature);
				}
			}
		} catch (Throwable t) {
			handleException(t);
		}
		locker.unLock();
	}
	
	/**
	 * @param line
	 * @param returnObj
	 * @param returnGeneralTypeSign (if type is object type -> this will be display of object type, not specific name 
	 */
	@Override
	public void _hitReturn(Object returnObj, String returnGeneralTypeSign, int line, String className, String methodSignature) {
		locker.lock();
		try {
			_hitLine(line, className, methodSignature);
			String returnGeneralType = SignatureUtils.signatureToName(returnGeneralTypeSign);
			Variable returnVar = new VirtualVar(methodSignature, returnGeneralType);
			String varID = VirtualVar.VIRTUAL_PREFIX + methodSignature;
			returnVar.setVarID(varID);
			String aliasID = TraceUtils.getObjectVarId(returnObj, returnGeneralTypeSign);
			returnVar.setAliasVarID(aliasID);
			VarValue returnVal = appendVarValue(returnObj, returnVar, null);
			if (returnVal != null) {
				TraceNode latestNode = trace.getLatestNode();
				if(latestNode!=null){
					latestNode.addReturnVariable(returnVal);					
				}
			}
		} catch (Throwable t) {
			handleException(t);
		}
		
		locker.unLock();
	}

	private void handleException(Throwable t) {
		if (t.getMessage() != null) {
			AgentLogger.info("ExecutionTracer error: " + t.getMessage());
		}
		AgentLogger.error(t);
	}
	
	@Override
	public void _hitVoidReturn(int line, String className, String methodSignature) {
		try {
			_hitLine(line, className, methodSignature);
		} catch (Throwable t) {
			handleException(t);
		}
	}

	@Override
	public void _hitLine(int line, String className, String methodSignature) {
		boolean isLocked = locker.isLock();
		locker.lock();
		try {
			boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
			if (exclusive) {
				locker.unLock(isLocked);
				return;
			}
			TraceNode latestNode = trace.getLatestNode();
			if (latestNode != null && latestNode.getBreakPoint().getClassCanonicalName().equals(className)
					&& latestNode.getBreakPoint().getLineNumber() == line) {
				locker.unLock(isLocked);
				return;
			}
			
			int order = trace.size() + 1;
			if (order > stepLimit) {
				shutdown();
				Agent._exitProgram("fail;Trace is over long!");
			}
//			if (order > expectedSteps) {
//				shutdown();
//				Agent._exitProgram("fail;Trace size exceeds expected_steps!");
//			}
			
			BreakPoint bkp = new BreakPoint(className, methodSignature, line);
			TraceNode currentNode = new TraceNode(bkp, null, order, trace); // leave programState empty.
			trace.addTraceNode(currentNode);
			AgentLogger.printProgress(order, expectedSteps);
			if(!methodCallStack.isEmpty()){
				TraceNode caller = methodCallStack.peek();
				caller.addInvocationChild(currentNode);
				currentNode.setInvocationParent(caller);			
			}
		} catch (Throwable t) {
			handleException(t);
		}
		
		locker.unLock(isLocked);
	}

	@Override
	public void _hitExeptionTarget(int line, String className, String methodSignature) {
		locker.lock();
		try {
			_hitLine(line, className, methodSignature);
			TraceNode latestNode = trace.getLatestNode();
			latestNode.setException(true);
			boolean invocationLayerChanged = this.methodCallStack.
					popForException(methodSignature, appJavaClassPath);
			
			if(invocationLayerChanged){
				TraceNode caller = null;
				if(!this.methodCallStack.isEmpty()){
					caller = this.methodCallStack.peek();
				}
				
				TraceNode olderCaller = latestNode.getInvocationParent();
				if(olderCaller!=null){
					olderCaller.getInvocationChildren().remove(latestNode);
					latestNode.setInvocationParent(caller);
				}
			}
		} catch (Throwable t) {
			handleException(t);
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
		locker.lock();
		try {
			_hitLine(line, className, methodSignature); 
			boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
			if (exclusive) {
				TraceNode latestNode = trace.getLatestNode();
				if (latestNode != null && latestNode.getInvokingDetail() != null) {
					InvokingDetail invokingDetail = latestNode.getInvokingDetail();
					boolean relevant = invokingDetail.updateRelevantVar(refValue, fieldValue, fieldType);
					if (!relevant) {
						locker.unLock();
						return;
					}
				} else {
					locker.unLock();
					return;
				}
			}
			String parentVarId = TraceUtils.getObjectVarId(refValue, refValue.getClass().getName());
			String fieldVarId = TraceUtils.getFieldVarId(parentVarId, fieldName, fieldType, fieldValue);
			Variable var = new FieldVar(false, fieldName, fieldType, refValue.getClass().getName());
			var.setVarID(fieldVarId);
			if(!PrimitiveUtils.isPrimitive(fieldType)){
				String aliasID = TraceUtils.getObjectVarId(fieldValue, fieldType);
				var.setAliasVarID(aliasID);				
			}
			
			VarValue value = appendVarValue(fieldValue, var, null);
			
			Variable parentVariable = new FieldVar(false, "unknown", refValue.getClass().getName(), "unknown");
			parentVariable.setVarID(parentVarId);
			ReferenceValue parentValue = new ReferenceValue(false, false, parentVariable);
			value.addParent(parentValue);
			
			addRWriteValue(value, true);
		} catch (Throwable t) {
			handleException(t);
		}
		locker.unLock();
	}

	private void addRWriteValue(VarValue value, boolean isWrittenVar) {
		if (value == null) {
			return;
		}
		TraceNode currentNode = trace.getLatestNode();
		if (currentNode == null) {
			return;
		}
		if (isWrittenVar) {
			currentNode.addWrittenVariable(value);
			buildDataRelation(value, Variable.WRITTEN);
		} else {
			currentNode.addReadVariable(value);
			buildDataRelation(value, Variable.READ);
		}
	}
	
	/**
	 * @param fieldValue
	 * @param refType
	 * @param fieldName
	 * @param fieldTypeSign
	 * @param line
	 */
	@Override
	public void _writeStaticField(Object fieldValue, String refType, String fieldName, String fieldType, int line,
			String className, String methodSignature) {
		locker.lock();
		try {
//			boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
//			if (exclusive) {
//				return;
//			}
			_hitLine(line, className, methodSignature);
			Variable var = new FieldVar(false, fieldName, fieldType, refType);
			var.setVarID(Variable.concanateFieldVarID(refType, fieldName));
			if(!PrimitiveUtils.isPrimitive(fieldType)){
				String aliasVarID = TraceUtils.getObjectVarId(fieldValue, fieldType);
				var.setAliasVarID(aliasVarID);				
			}
			VarValue value = appendVarValue(fieldValue, var, null);
			addRWriteValue(value, true);
		} catch (Throwable t) {
			handleException(t);
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
	public void _readField(Object refValue, Object fieldValue, String fieldName, String fieldType, int line,
			String className, String methodSignature) {
		locker.lock();
		try {
			boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
			if (exclusive) {
				TraceNode latestNode = trace.getLatestNode();
				boolean relevant = false;
				if (latestNode != null && latestNode.getInvokingDetail() != null) {
					InvokingDetail invokingDetail = latestNode.getInvokingDetail();
					relevant = invokingDetail.updateRelevantVar(refValue, fieldValue, fieldType);
				}
				if (!relevant) {
					locker.unLock();
					return;
				}
			}
			_hitLine(line, className, methodSignature);
			String parentVarId = TraceUtils.getObjectVarId(refValue, refValue.getClass().getName());
			String fieldVarId = TraceUtils.getFieldVarId(parentVarId, fieldName, fieldType, fieldValue);
			//		invokeTrack.updateRelevant(parentVarId, fieldVarId);
			//		if (exclusive) {
			//			return;
			//		}
			Variable var = new FieldVar(false, fieldName, fieldType, refValue.getClass().getName());
			var.setVarID(fieldVarId);
			String aliasID = TraceUtils.getObjectVarId(fieldValue, fieldType);
			var.setAliasVarID(aliasID);
			
			VarValue value = appendVarValue(fieldValue, var, null);
			
			Variable parentVariable = new FieldVar(false, "unknown", refValue.getClass().getName(), "unknown");
			parentVariable.setVarID(parentVarId);
			ReferenceValue parentValue = new ReferenceValue(false, false, parentVariable);
			value.addParent(parentValue);
			
			addRWriteValue(value, false);
		} catch (Throwable t) {
			handleException(t);
		}
		locker.unLock();
	}

	@Override
	public void _readStaticField(Object fieldValue, String refType, String fieldName, String fieldType, int line,
			String className, String methodSignature) {
		locker.lock();
		try {
//			boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
//			if (exclusive) {
//				locker.unLock();
//				return;
//			}
			_hitLine(line, className, methodSignature);
			Variable var = new FieldVar(true, fieldName, fieldType, refType);
			var.setVarID(Variable.concanateFieldVarID(refType, fieldName));
			
			String aliasID = TraceUtils.getObjectVarId(fieldValue, fieldType);
			var.setAliasVarID(aliasID);
			
			VarValue value = appendVarValue(fieldValue, var, null);
			addRWriteValue(value, false);
		} catch (Throwable t) {
			handleException(t);
		}
		locker.unLock();
	}
	
	/**
	 * Instrument for: Application Classes only.
	 */
	@Override
	public void _writeLocalVar(Object varValue, String varName, String varType, int line, int bcLocalVarIdx,
			int varScopeStartLine, int varScopeEndLine, String className, String methodSignature) {
		locker.lock();
		try {
//			boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
//			if (exclusive) {
//				locker.unLock();
//				return;
//			}
			_hitLine(line, className, methodSignature);
			Variable var = new LocalVar(varName, varType, className, line);
			
//			String varID = TraceUtils.getLocalVarId(className, varScopeStartLine, varScopeEndLine, varName, varType, varValue);
			String varID = Variable.concanateLocalVarID(className, varName, varScopeStartLine, varScopeEndLine);
			var.setVarID(varID);
			if(!PrimitiveUtils.isPrimitive(varType)){
				String aliasID = TraceUtils.getObjectVarId(varValue, varType);
				var.setAliasVarID(aliasID);				
			}
			
			VarValue value = appendVarValue(varValue, var, null);
			addRWriteValue(value, true);
		} catch (Throwable t) {
			handleException(t);
		}
		locker.unLock();
	}
	
	/**
	 * Instrument for: Application Classes only.
	 */
	@Override
	public void _readLocalVar(Object varValue, String varName, String varType, int line, int bcLocalVarIdx,
			int varScopeStartLine, int varScopeEndLine, String className, String methodSignature) {
		locker.lock();
		try {
//			boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
//			if (exclusive) {
//				locker.unLock();
//				return;
//			}
			_hitLine(line, className, methodSignature);
			Variable var = new LocalVar(varName, varType, className, line);
			
			String varID = TraceUtils.getLocalVarId(className, varScopeStartLine, varScopeEndLine, varName, varType, varValue);				
			var.setVarID(varID);
			String aliasVarID = TraceUtils.getObjectVarId(varValue, varType);
			var.setAliasVarID(aliasVarID);
			
			VarValue value = appendVarValue(varValue, var, null);
			addRWriteValue(value, false);
			
			
//			TraceNode currentNode = trace.getLatestNode();
//			String order = trace.findDefiningNodeOrder(Variable.READ, currentNode, var.getVarID(), var.getAliasVarID());
//			if(value instanceof ReferenceValue && order.equals("0")){
//				if(isParameter(varScopeStartLine, varScopeEndLine, className)){
//					
//					TraceNode invocationParent = currentNode.getInvocationParent();
//					if(invocationParent!=null){
//						String simpleVarID = Variable.truncateSimpleID(varID);
//						varID = simpleVarID + ":" + invocationParent.getOrder();
//						value.setVarID(varID);
//						
//						if(!invocationParent.getWrittenVariables().contains(value)){
//							invocationParent.addWrittenVariable(value);							
//						}
//						if(!currentNode.getReadVariables().contains(value)){
//							currentNode.addReadVariable(value);							
//						}
//						
//						StepVariableRelationEntry entry = new StepVariableRelationEntry(varID);
//						entry.addProducer(invocationParent);
//						entry.addConsumer(currentNode);
//						trace.getStepVariableTable().put(varID, entry);
//						
//						
//					}
//				}
//			}
//			else{
//				addRWriteValue(value, false);
//			}
			
		} catch (Throwable t) {
			handleException(t);
		}
		locker.unLock();
	}
	
	private boolean isParameter(int varScopeStartLine, int varScopeEndLine, String className) {
		BreakPoint point = trace.getLatestNode().getBreakPoint();
		String fullSign = point.getMethodSign();
		String shortSign = fullSign.substring(fullSign.indexOf("#")+1, fullSign.length());
		MethodFinderBySignature finder = new MethodFinderBySignature(shortSign);
		ByteCodeParser.parse(className, finder, appJavaClassPath);
		Method method = finder.getMethod();
		
		if(method!=null && method.getCode()!=null){
			InstructionList list = new InstructionList(method.getCode().getCode());
			InstructionHandle start = list.getStart();
			InstructionHandle end = list.getEnd();
			
			int startLine = method.getLineNumberTable().getSourceLine(start.getPosition());
			int endLine = method.getLineNumberTable().getSourceLine(end.getPosition());
			
			if(varScopeStartLine==startLine && varScopeEndLine==endLine){
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Instrument for: Application Classes only.
	 */
	@Override
	public void _iincLocalVar(Object varValue, Object varValueAfter, String varName, String varType, int line, int bcLocalVarIdx,
			int varScopeStartLine, int varScopeEndLine, String className, String methodSignature) {
		locker.lock();
		try {
//			boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
//			if (exclusive) {
//				locker.unLock();
//				return;
//			}
			_hitLine(line, className, methodSignature);
			Variable var = new LocalVar(varName, varType, className, line);
			String varID = TraceUtils.getLocalVarId(className, varScopeStartLine, varScopeEndLine, varName, varType, varValue);
			var.setVarID(varID);
			
			Variable varBefore = var.clone();
			VarValue value = appendVarValue(varValue, varBefore, null);
			addRWriteValue(value, false); // add read var
			
			Variable varAfter = var.clone();
			VarValue writtenValue = appendVarValue(varValueAfter, varAfter, null);
			addRWriteValue(writtenValue, true); // add written var
		} catch (Throwable t) {
			handleException(t);
		}
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
		locker.lock();
		try {
			boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
			if (exclusive) {
				TraceNode latestNode = trace.getLatestNode();
				boolean relevant = false;
				if (latestNode != null && latestNode.getInvokingDetail() != null) {
					InvokingDetail invokingDetail = latestNode.getInvokingDetail();
					relevant = invokingDetail.updateRelevantVar(arrayRef, eleValue, elementType);
				}
				if (!relevant) {
					locker.unLock();
					return;
				}
			}
			_hitLine(line, className, methodSignature);
			VarValue value = addArrayElementVarValue(arrayRef, index, eleValue, elementType, line, false);
			
			Variable parentVariable = new FieldVar(false, "unknown", arrayRef.getClass().getName(), "unknown");
			String parentVarId = TraceUtils.getObjectVarId(arrayRef, arrayRef.getClass().getName());
			parentVariable.setVarID(parentVarId);
			ReferenceValue parentValue = new ReferenceValue(false, false, parentVariable);
			value.addParent(parentValue);
			
			addRWriteValue(value, false);
		} catch (Throwable t) {
			handleException(t);
		}
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
		locker.lock();
		try {
			boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
			if (exclusive) {
				TraceNode latestNode = trace.getLatestNode();
				boolean relevant = false;
				if (latestNode != null && latestNode.getInvokingDetail() != null) {
					InvokingDetail invokingDetail = latestNode.getInvokingDetail();
					relevant = invokingDetail.updateRelevantVar(arrayRef, eleValue, elementType);
				} 
				if (!relevant) {
					locker.unLock();
					return;
				}
			}
			_hitLine(line, className, methodSignature);
			VarValue value = addArrayElementVarValue(arrayRef, index, eleValue, elementType, line, true);
			
			Variable parentVariable = new FieldVar(false, "unknown", arrayRef.getClass().getName(), "unknown");
			String parentVarId = TraceUtils.getObjectVarId(arrayRef, arrayRef.getClass().getName());
			parentVariable.setVarID(parentVarId);
			ReferenceValue parentValue = new ReferenceValue(false, false, parentVariable);
			value.addParent(parentValue);
			
			addRWriteValue(value, true);
		} catch (Throwable t) {
			handleException(t);
		}
		locker.unLock();
	}
	
	private VarValue addArrayElementVarValue(Object arrayRef, int index, Object eleValue, String elementType, int line,
			boolean write) {
		String id = new StringBuilder(TraceUtils.getObjectVarId(arrayRef, elementType+"[]")).append("[").append(index).append("]").toString();
		String name = id;
		Variable var = new ArrayElementVar(name, elementType, id);
		var.setVarID(id);
		
		if(!PrimitiveUtils.isPrimitive(elementType)){
			String aliasID = TraceUtils.getObjectVarId(eleValue, elementType);
			var.setAliasVarID(aliasID);			
		}
		
		VarValue value = appendVarValue(eleValue, var, null);
		return value;
	}
	
	private static LockedThreads lockedThreads = new LockedThreads();
	private static final Locker gLocker = new Locker();
	
	public synchronized static IExecutionTracer _getTracer(boolean isAppClass, String className, String methodSig,
			int methodStartLine, int methodEndLine, String paramNamesCode, String paramTypeSignsCode, Object[] params) {
		if (gLocker.isLock()) {
			if (state == TracingState.TEST_STARTED && isAppClass) {
				state = TracingState.RECORDING;
				// entry point
				gLocker.unLock();
			} else {
				return EmptyExecutionTracer.getInstance();
			}
		}
		gLocker.lock();
		if (ClassLoader.class.getName().equals(className)) {
			gLocker.unLock();
			return ClassLoaderHandler.getInstance();
		}
		long threadId = Thread.currentThread().getId();
		if (lockedThreads.contains(threadId) || ((mainThreadId >= 0 && mainThreadId != threadId))) {
			gLocker.unLock();
			return EmptyExecutionTracer.getInstance();
		}
		if (mainThreadId < 0) {
			mainThreadId = threadId;
		}
		if (threadId != mainThreadId) {
			// ignore other thread.
			gLocker.unLock();
			return EmptyExecutionTracer.getInstance();
		}
		ExecutionTracer tracer = getTracer(threadId);
		if (tracer.locker.isLock()) {
			gLocker.unLock();
			return EmptyExecutionTracer.getInstance();
		}
		tracer.enterMethod(className, methodSig, methodStartLine, methodEndLine, paramTypeSignsCode, paramNamesCode, params);
		gLocker.unLock();
		return tracer;
	}
	
	private static ExecutionTracer getTracer(long threadId) {
		boolean locked = gLocker.isLock();
		gLocker.lock();
		ExecutionTracer store = rtStores.get(threadId);
		if (store == null) {
			store = new ExecutionTracer(threadId);
			rtStores.put(threadId, store);
		}
		if (!locked) {
			gLocker.unLock();
		}
		return store;
	}
	
	public static boolean glock() {
		boolean isLocked = gLocker.isLock();
		gLocker.lock();
		return isLocked;
	}
	
	public static void gUnlock(boolean isLocked) {
		gLocker.unLock(isLocked);
	}
	
	public static Map<Long, ExecutionTracer> getRtStores() {
		return rtStores;
	}
	
	public static IExecutionTracer getMainThreadStore() {
		return getTracer(mainThreadId);
	}
	
	public static IExecutionTracer getCurrentThreadStore() {
		boolean locked = gLocker.isLock();
		gLocker.lock();
		long threadId = Thread.currentThread().getId();
		IExecutionTracer store = rtStores.get(threadId);
		if (store == null) {
			store = EmptyExecutionTracer.getInstance();
		}
		if (!locked) {
			gLocker.unLock();
		}
		return store;
	}
	
	private static TracingState state = TracingState.INIT;
	public static void shutdown() {
		gLocker.lock();
		state = TracingState.SHUTDOWN;
	}
	
	public static void _start() {
		state = TracingState.TEST_STARTED;
	}
	
	public static boolean isShutdown() {
		return state == TracingState.SHUTDOWN;
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
		
		public void unLock(boolean preserveLock) {
			if (!preserveLock) {
				unLock();
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

	@Override
	public boolean lock() {
		boolean isLock = locker.isLock();
		locker.lock();
		return isLock;
	}

	@Override
	public void unLock() {
		locker.unLock();
	}
	
}
