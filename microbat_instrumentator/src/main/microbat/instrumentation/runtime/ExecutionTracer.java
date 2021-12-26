package microbat.instrumentation.runtime;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

import microbat.codeanalysis.bytecode.ByteCodeParser;
import microbat.codeanalysis.bytecode.MethodFinderBySignature;
import microbat.instrumentation.Agent;
import microbat.instrumentation.AgentConstants;
import microbat.instrumentation.AgentLogger;
import microbat.instrumentation.filter.GlobalFilterChecker;
import microbat.model.BreakPoint;
import microbat.model.trace.HookedTrace;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.trace.VariableDefinitions;
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
import microbat.sql.IntervalRecorder;
import microbat.sql.TraceRecorder;
import microbat.util.PrimitiveUtils;
import sav.common.core.utils.SignatureUtils;
import sav.strategies.dto.AppJavaClassPath;

public class ExecutionTracer implements IExecutionTracer, ITracer {
	private static ExecutionTracerStore rtStore = new ExecutionTracerStore();

	public static AppJavaClassPath appJavaClassPath;
	public static int variableLayer = 2;
	public static int stepLimit = Integer.MAX_VALUE;
	public static int expectedSteps = Integer.MAX_VALUE;
//	private static int tolerantExpectedSteps = expectedSteps;
	public static boolean avoidProxyToString = false;

	private static IntervalRecorder intervalRecorder = null;
	private long threadId;

	private Trace trace;
	private String traceId;

	private MethodCallStack methodCallStack;
	private TrackingDelegate trackingDelegate;

	public static void setExpectedSteps(int expectedSteps) {
		if (expectedSteps != AgentConstants.UNSPECIFIED_INT_VALUE) {
			ExecutionTracer.expectedSteps = expectedSteps;
//			tolerantExpectedSteps = expectedSteps * 2;
		}
	}

	public static void setStepLimit(int stepLimit) {
		if (stepLimit != AgentConstants.UNSPECIFIED_INT_VALUE) {
			ExecutionTracer.stepLimit = stepLimit;
		}
	}
	
	public static void setRecorderFactory(final int threshold, TraceRecorder tr) {
		intervalRecorder = new IntervalRecorder(threshold, tr);
	}
	
	public ExecutionTracer(long threadId) {
		this.threadId = threadId;
		this.traceId = getUUID();
		trackingDelegate = new TrackingDelegate(threadId);
		methodCallStack = new MethodCallStack();
		if (intervalRecorder == null) {
			trace = new Trace(appJavaClassPath, traceId);
		} else {
			trace = new HookedTrace(traceId, appJavaClassPath, intervalRecorder);
		}
	}

	private String getUUID(){
        UUID uuid=UUID.randomUUID();
        String uuidStr=uuid.toString();
        return uuidStr;
	}
	

	// private void buildDataRelation(TraceNode currentNode, VarValue value, String
	// rw){
	// Variable var = value.getVariable();
	// if(currentNode==null){
	// return;
	// }
	//
	// String order = trace.findDefiningNodeOrder(rw, currentNode, var,
	// VariableDefinitions.USE_LAST);
	//
	// if(order.equals("0")){
	// if(var instanceof FieldVar || var instanceof ArrayElementVar){
	// if(!value.getParents().isEmpty()){
	// /**
	// * use the first defining step of the parent.
	// */
	// order = trace.findDefiningNodeOrder(rw, currentNode,
	// value.getParents().get(0).getVariable(), VariableDefinitions.USE_FIRST);
	// }
	//
	// }
	// }
	//
	// String varID = var.getVarID() + ":" + order;
	// var.setVarID(varID);
	// if(var.getAliasVarID()!=null){
	// var.setAliasVarID(var.getAliasVarID()+":"+order);
	// }
	//
	// StepVariableRelationEntry entry = trace.getStepVariableTable().get(varID);
	// if(entry == null){
	// entry = new StepVariableRelationEntry(varID);
	// if(!order.equals("0")){
	// TraceNode producer = trace.getTraceNode(Integer.valueOf(order));
	// entry.addProducer(producer);
	// trace.getStepVariableTable().put(varID, entry);
	// }
	// }
	// if(rw.equals(Variable.READ)){
	// entry.addConsumer(currentNode);
	// }
	// else if(rw.equals(Variable.WRITTEN)){
	// entry.addProducer(currentNode);
	// }
	// trace.getStepVariableTable().put(varID, entry);
	// }

	private VarValue appendVarValue(Object value, Variable var, VarValue parent) {
		return appendVarValue(value, var, parent, variableLayer);
	}

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
		} else if (var.getType().endsWith("[]")) {
			/* array */
			ArrayValue arrVal = new ArrayValue(value == null, isRoot, var);
			arrVal.setComponentType(var.getType().substring(0, var.getType().length() - 2)); // 2 = "[]".length
			varValue = arrVal;
			// varValue.setStringValue(getStringValue(value, arrVal.getComponentType()));
			varValue.setStringValue(getStringValue(value, var.getType()));
			if (value == null) {
				arrVal.setNull(true);
			} else {
				int length = Array.getLength(value);
				arrVal.ensureChildrenSize(length);
				for (int i = 0; i < length; i++) {
					String parentSimpleID = Variable.truncateSimpleID(var.getVarID());
					String arrayElementID = Variable.concanateArrayElementVarID(parentSimpleID, String.valueOf(i));
					String varName = arrayElementID;
					ArrayElementVar varElement = new ArrayElementVar(varName, arrVal.getComponentType(), arrayElementID);
					Object elementValue = Array.get(value, i);
					if (HeuristicIgnoringFieldRule.isHashMapTableType(arrVal.getComponentType())) {
						appendVarValue(elementValue, varElement, arrVal, retrieveLayer + 1);
					} else {
						appendVarValue(elementValue, varElement, arrVal, retrieveLayer);
					}
				}
			}
		} else {
			ReferenceValue refVal = new ReferenceValue(value == null, TraceUtils.getUniqueId(value), isRoot, var);
			varValue = refVal;
			// varValue.setStringValue(getStringValue(value, var.getType()));
			varValue.setStringValue(getStringValue(value, null));
			if (value != null) {
				Class<?> objClass = value.getClass();
				var.setRtType(objClass.getName());
				boolean needParseFields = HeuristicIgnoringFieldRule.isNeedParsingFields(objClass);
				boolean isCollectionOrHashMap = HeuristicIgnoringFieldRule.isCollectionClass(objClass)
						|| HeuristicIgnoringFieldRule.isHashMapClass(objClass);
				if (needParseFields) {
					List<Field> validFields = HeuristicIgnoringFieldRule.getValidFields(objClass, value);
					for (Field field : validFields) {
						field.setAccessible(true);
						try {
							Object fieldValue = field.get(value);
							Class<?> fieldType = field.getType();
							String fieldTypeStr = fieldType.getName();
							if (fieldType.isArray()) {
								fieldTypeStr = SignatureUtils.signatureToName(fieldTypeStr);
							}
							if (fieldType.isEnum()) {
								if (fieldTypeStr.equals(var.getType())) {
									continue;
								}
							}
							if (fieldValue != null) {
								FieldVar fieldVar = new FieldVar(Modifier.isStatic(field.getModifiers()), field.getName(), fieldTypeStr,
										field.getDeclaringClass().getName());
								fieldVar.setVarID(TraceUtils.getFieldVarId(var.getVarID(), field.getName(), fieldTypeStr, fieldValue));
								if (isCollectionOrHashMap
										&& HeuristicIgnoringFieldRule.isCollectionOrMapElement(var.getRuntimeType(), field.getName())) {
									appendVarValue(fieldValue, fieldVar, refVal, retrieveLayer + 1);
								} else {
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

	private static Set<Class<?>> stringValueBlackList = new HashSet<>();
	
	private String getStringValue(final Object obj, String type) {
		try {
			if (obj == null) {
				return "null";
			}

			String simpleType = null;
			if (type != null && type.contains("[]")) {
				simpleType = type.substring(0, type.indexOf("[]"));
			}

			if (simpleType != null) {
				if (simpleType.equals("char")) {
					char[] charArray = (char[]) obj;
					return String.valueOf(charArray);
				}
			}

			// if (FilterChecker.isCustomizedToStringClass(obj.getClass().getName())) {
			// java.lang.reflect.Method toStringMethod = null;
			// for (java.lang.reflect.Method method : obj.getClass().getDeclaredMethods()) {
			// if (method.getName().equals(TraceInstrumenter.NEW_TO_STRING_METHOD)) {
			// toStringMethod = method;
			// break;
			// }
			// }
			// if (toStringMethod != null) {
			// return (String) toStringMethod.invoke(obj);
			// }
			// }

			if (avoidProxyToString && isProxyClass(obj.getClass())) {
				return obj.getClass().getName();
			}

			if(stringValueBlackList.contains(obj.getClass())) {
				return "$unknown (estimated as too cost to have its value)";
			}
			
			long t1 = System.currentTimeMillis();
			String value = String.valueOf(obj);// obj.toString();
			long t2 = System.currentTimeMillis();
			if(t2-t1 > 500) {
				stringValueBlackList.add(obj.getClass());
			}
			
			return value;
		} catch (Throwable t) {
			return null;
		}
	}

	private boolean isProxyClass(Class<? extends Object> clazz) {
		if (Proxy.isProxyClass(clazz)) {
			return true;
		}
		/* to detect proxy enhancered by CGLIB */
		int enhancerTagIdx = clazz.getName().indexOf("$$");
		if (enhancerTagIdx > 0 && clazz.getName().lastIndexOf("$$", enhancerTagIdx + 1) > 0) {
			return true;
		}
		return false;
	}

	/*
	 * Methods with prefix "_" are called in instrument code.
	 * =================================================================
	 */
	public void enterMethod(String className, String methodSignature, int methodStartLine, int methodEndLine,
			String paramTypeSignsCode, String paramNamesCode, Object[] params) {
		trackingDelegate.untrack();
		TraceNode caller = trace.getLatestNode();
		if (caller != null && caller.getMethodSign().contains("<clinit>")) {
			caller = caller.getInvocationParent();
		}

		if (caller != null) {
			int varScopeStart = methodStartLine;
			int varScopeEnd = methodEndLine;

			String[] parameterTypes = TraceUtils.parseArgTypesOrNames(paramTypeSignsCode);
			String[] parameterNames = paramNamesCode.split(":");
			if (parameterNames.length != 0) {
				int adjust = adjustVariableStartScope(methodSignature, className);
				varScopeStart = (adjust > 0) ? adjust : varScopeStart;
			}

			for (int i = 0; i < parameterTypes.length; i++) {
				String pType = parameterTypes[i];
				String parameterType = SignatureUtils.signatureToName(pType);
				String varName = parameterNames[i];

				Variable var = new LocalVar(varName, parameterType, className, methodStartLine);

				String varID = Variable.concanateLocalVarID(className, varName, varScopeStart, varScopeEnd,
						caller.getInvocationLevel() + 1);
				var.setVarID(varID);
				if (!PrimitiveUtils.isPrimitive(pType)) {
					String aliasID = TraceUtils.getObjectVarId(params[i], pType);
					var.setAliasVarID(aliasID);
				}

				VarValue value = appendVarValue(params[i], var, null);
				if(value instanceof PrimitiveValue && !(value instanceof StringValue)) {
					addRWriteValue(caller, value, true);					
				}
			}
		}

		boolean exclusive = GlobalFilterChecker.isExclusive(className, methodSignature);
		if (!exclusive) {
			if (caller != null) {
				methodCallStack.push(caller);
			}
			hitLine(methodStartLine, className, methodSignature);
		} else {
			trackingDelegate.track();
			return;
		}
		trackingDelegate.track();
	}

	private static Map<String, Integer> adjustVarMap = new HashMap<>();

	private int adjustVariableStartScope(String fullSign, String className) {
		Integer value = adjustVarMap.get(fullSign);
		if (value != null) {
			return value;
		}
		String shortSign = fullSign.substring(fullSign.indexOf("#") + 1, fullSign.length());
		MethodFinderBySignature finder = new MethodFinderBySignature(shortSign);
		ByteCodeParser.parse(className, finder, appJavaClassPath);
		Method method = finder.getMethod();

		if(method == null) {
			System.currentTimeMillis();
		}
		
		LocalVariableTable table = method.getLocalVariableTable();
		int start = -1;
		if (table != null) {
			for (LocalVariable v : table.getLocalVariableTable()) {
				int line = method.getCode().getLineNumberTable().getSourceLine(v.getStartPC());
				if (start == -1) {
					start = line;
				} else {
					if (start > line) {
						start = line;
					}
				}
			}
		}
		adjustVarMap.put(fullSign, start);
		Repository.clearCache();
		return start;
	}

	public void exitMethod(int line, String className, String methodSignature) {
		trackingDelegate.untrack();
		boolean exclusive = GlobalFilterChecker.isExclusive(className, methodSignature);
		if (!exclusive) {
			methodCallStack.safePop();
		}
		trackingDelegate.track();
	}

	@Override
	public void _hitInvoke(Object invokeObj, String invokeTypeSign, String methodSig, Object[] params,
			String paramTypeSignsCode, String returnTypeSign, int line, String residingClassName,
			String residingMethodSignature) {
		trackingDelegate.untrack();
		try {
			hitLine(line, residingClassName, residingMethodSignature);
			TraceNode latestNode = trace.getLatestNode();
			if (latestNode != null) {
				latestNode.setInvokingMethod(methodSig);
				initInvokingDetail(invokeObj, invokeTypeSign, methodSig, params, paramTypeSignsCode, residingClassName,
						latestNode);

				if (methodSig.contains("clone()")) {

					String type = SignatureUtils.signatureToName(invokeTypeSign);
					Variable var = new LocalVar("$tmp", type, null, -1);
					VarValue value = new ReferenceValue(false, false, var);

					String varID = TraceUtils.getObjectVarId(invokeObj, invokeTypeSign);
					value.setVarID(varID);

					appendVarValue(invokeObj, var, value, 2);

					if (!value.getChildren().isEmpty()) {
						VarValue parent = value.getChildren().get(0);
						addRWriteValue(latestNode, parent.getChildren(), false);
					}
				}
			}
		} catch (Throwable t) {
			handleException(t);
		}

		trackingDelegate.track();
	}

	private void initInvokingDetail(Object invokeObj, String invokeTypeSign, String methodSig, Object[] params,
			String paramTypeSignsCode, String residingClassName, TraceNode latestNode) {
		boolean exclusive = GlobalFilterChecker.isExclusive(invokeTypeSign, methodSig);
		if (exclusive && latestNode.getBreakPoint().getClassCanonicalName().equals(residingClassName)) {
			InvokingDetail invokeDetail = latestNode.getInvokingDetail();
			if (invokeDetail == null) {
				invokeDetail = new InvokingDetail(latestNode);
				latestNode.setInvokingDetail(invokeDetail);
			}
			invokeDetail.initRelevantVars(invokeObj, params, paramTypeSignsCode);
		}
	}

	public void buildReadRelationForArrayCopy(Object array, int startPosition, int length, int line) {
		Variable sourceParentVariable = new FieldVar(false, "unknown", array.getClass().getName(), "unknown");
		String sourceParentVarId = TraceUtils.getObjectVarId(array, array.getClass().getName());
		sourceParentVariable.setVarID(sourceParentVarId);
		ReferenceValue sourceParentValue = new ReferenceValue(false, false, sourceParentVariable);
		for (int i = 0; i < length; i++) {
			int k = startPosition + i;
			Object elementValue = Array.get(array, k);
			String elementType = "Object";
			if(elementValue != null) {
				elementType = elementValue.getClass().getName();
			}
			VarValue value = addArrayElementVarValue(array, k, elementValue, elementType, line);
			value.addParent(sourceParentValue);
			addRWriteValue(trace.getLatestNode(), value, false);
		}
	}

	public void buildWriteRelationForArrayCopy(Object targetArray, int startPosition, Object sourceArray, int srcStartPos,
			int length, int line) {
		Variable targetParentVariable = new FieldVar(false, "unknown", targetArray.getClass().getName(), "unknown");
		String targetParentVarId = TraceUtils.getObjectVarId(targetArray, targetArray.getClass().getName());
		targetParentVariable.setVarID(targetParentVarId);
		ReferenceValue targetParentValue = new ReferenceValue(false, false, targetParentVariable);
		for (int i = 0; i < length; i++) {
			int k = srcStartPos + i;
			Object elementValue = Array.get(sourceArray, k);
			String elementType = "Object";
			if(elementValue != null) {
				elementType = elementValue.getClass().getName();
			}
			int index = startPosition + i;
			VarValue value = addArrayElementVarValue(targetArray, index, elementValue, elementType, line);
			value.addParent(targetParentValue);
			addRWriteValue(trace.getLatestNode(), value, true);
		}
	}

	@Override
	public void _hitInvokeStatic(String invokeTypeSign, String methodSig, Object[] params, String paramTypeSignsCode,
			String returnTypeSign, int line, String className, String residingMethodSignature) {
		trackingDelegate.untrack();
		try {
			hitLine(line, className, residingMethodSignature);

			if (methodSig.equals("java.lang.System#arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V")) {
				Object sourceArray = params[0];
				int sourcePosition = (Integer) params[1];
				Object targetArray = params[2];
				int targetPosition = (Integer) params[3];
				int length = (Integer) params[4];

				buildReadRelationForArrayCopy(sourceArray, sourcePosition, length, line);
				buildWriteRelationForArrayCopy(targetArray, targetPosition, sourceArray, sourcePosition, length, line);
			}

			TraceNode latestNode = trace.getLatestNode();
			if (latestNode != null) {
				latestNode.setInvokingMethod(methodSig);
				initInvokingDetail(null, invokeTypeSign, methodSig, params, paramTypeSignsCode, className, latestNode);
			}
		} catch (Throwable t) {
			handleException(t);
		}
		trackingDelegate.track();
	}

	@Override
	public void _hitMethodEnd(int line, String className, String methodSignature) {
		trackingDelegate.untrack();
		try {
			exitMethod(line, className, methodSignature);
		} catch (Throwable t) {
			handleException(t);
		}
		trackingDelegate.track();
	}

	/**
	 * Instrument for: Application Classes only.
	 */
	@Override
	public void _afterInvoke(Object returnedValue, Object invokeObj, String invokeMethodSig, int line,
			String residingClassName, String residingMethodSignature, boolean needRevisiting) {
		trackingDelegate.untrack();
		try {
			boolean exclusive = GlobalFilterChecker.isExclusive(residingClassName, residingMethodSignature);
			if (!exclusive) {
				hitLine(line, residingClassName, residingMethodSignature);
				TraceNode latestNode = trace.getLatestNode();
				if (latestNode != null) {
					latestNode.setInvokingDetail(null);
					// TraceNode invokingMatchNode = findInvokingMatchNode(latestNode,
					// invokeMethodSig);
					// if(invokingMatchNode!=null){
					// invokingMatchNode.setInvokingMatchNode(latestNode);
					// latestNode.setInvokingMatchNode(invokingMatchNode);
					// }
				}

				if (returnedValue != null && invokeMethodSig.contains("clone()")) {
					String returnTypeSign = returnedValue.getClass().getName();
					String type = SignatureUtils.signatureToName(returnTypeSign);
					Variable var = new LocalVar("$tmp", type, null, -1);
					VarValue value = new ReferenceValue(false, false, var);

					String varID = TraceUtils.getObjectVarId(returnedValue, returnTypeSign);
					value.setVarID(varID);

					appendVarValue(returnedValue, var, value, 2);

					if (!value.getChildren().isEmpty()) {
						VarValue parent = value.getChildren().get(0);
						addRWriteValue(latestNode, parent.getChildren(), true);
					}
				}
			}
		} catch (Throwable t) {
			handleException(t);
		}
		trackingDelegate.track();
	}

	private TraceNode findInvokingMatchNode(TraceNode latestNode, String invokingMethodSig) {
		List<TraceNode> candidates = new ArrayList<>();
		if (latestNode.getInvocationParent() != null) {
			candidates = latestNode.getInvocationParent().getInvocationChildren();
		} else {
			candidates = trace.getTopMethodLevelNodes();
		}

		for (int i = candidates.size() - 1; i >= 0; i--) {
			TraceNode prevOver = candidates.get(i);
			if (prevOver.getOrder() != latestNode.getOrder()) {
				String prevOverInvocation = prevOver.getInvokingMethod();
				if (prevOverInvocation != null && prevOverInvocation.equals(invokingMethodSig)) {

					if (prevOver.getInvokingMatchNode() == null) {
						return prevOver;
					}
				}
			}
		}
		System.currentTimeMillis();
		return null;
	}

	/**
	 * @param line
	 * @param returnObj
	 * @param returnGeneralTypeSign (if type is object type -> this will be display
	 *                              of object type, not specific name
	 */
	@Override
	public void _hitReturn(Object returnObj, String returnGeneralTypeSign, int line, String className,
			String methodSignature) {
		trackingDelegate.untrack();
		try {
			hitLine(line, className, methodSignature);
			String returnGeneralType = SignatureUtils.signatureToName(returnGeneralTypeSign);
			Variable returnVar = new VirtualVar(methodSignature, returnGeneralType);

			String varID = VirtualVar.VIRTUAL_PREFIX + methodSignature;
			returnVar.setVarID(varID);

			String aliasID = TraceUtils.getObjectVarId(returnObj, returnGeneralTypeSign);
			returnVar.setAliasVarID(aliasID);
			VarValue returnVal = appendVarValue(returnObj, returnVar, null);
			if (returnVal != null) {
				TraceNode latestNode = trace.getLatestNode();
				if (latestNode != null) {
					String definingOrder = trace.findDefiningNodeOrder(Variable.WRITTEN, latestNode, returnVar,
							VariableDefinitions.USE_LAST);
					returnVar.setVarID(returnVar.getVarID() + ":" + definingOrder);
					returnVar.setAliasVarID(returnVar.getAliasVarID() + ":" + definingOrder);

					latestNode.addReturnVariable(returnVal);
				}
			}
		} catch (Throwable t) {
			handleException(t);
		}

		trackingDelegate.track();
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
			hitLine(line, className, methodSignature);
		} catch (Throwable t) {
			handleException(t);
		}
	}

	public void hitLine(int line, String className, String methodSignature) {
		_hitLine(line, className, methodSignature, -1, -1);
	}

	@Override
	public void _hitLine(int line, String className, String methodSignature, int numOfReadVars, int numOfWrittenVars) {
		boolean isLocked = trackingDelegate.isUntrack();
		trackingDelegate.untrack();
		try {
			boolean exclusive = GlobalFilterChecker.isExclusive(className, methodSignature);
			if (exclusive) {
				trackingDelegate.track(isLocked);
				return;
			}
			TraceNode latestNode = trace.getLatestNode();
			if (latestNode != null && latestNode.getBreakPoint().getClassCanonicalName().equals(className)
					&& latestNode.getBreakPoint().getLineNumber() == line) {
				trackingDelegate.track(isLocked);
				return;
			}

			int order = trace.getOrder();
			// TODO: remove step limit check?
			if (order > stepLimit) {
				shutdown();
				Agent._exitProgram("fail;Trace is over long!");
			}
//			if (order > tolerantExpectedSteps) {
//				shutdown();
//				Agent._exitProgram("fail;Trace size exceeds expected_steps!");
//			}

			BreakPoint bkp = new BreakPoint(className, methodSignature, line);
			long timestamp = System.currentTimeMillis();
			TraceNode currentNode = new TraceNode(bkp, null, order, trace, numOfReadVars, numOfWrittenVars, timestamp);

			trace.addTraceNode(currentNode);
			AgentLogger.printProgress(order);
			if (!methodCallStack.isEmpty()) {
				TraceNode caller = methodCallStack.peek();
				caller.addInvocationChild(currentNode);
				currentNode.setInvocationParent(caller);
			}
		} catch (Throwable t) {
			handleException(t);
		}

		trackingDelegate.track(isLocked);
	}

	@Override
	public void _hitExeptionTarget(int line, String className, String methodSignature) {
		trackingDelegate.untrack();
		try {
			hitLine(line, className, methodSignature);
			TraceNode latestNode = trace.getLatestNode();
			if(latestNode == null) return;
			latestNode.setException(true);
			boolean invocationLayerChanged = this.methodCallStack.popForException(methodSignature, appJavaClassPath);

			if (invocationLayerChanged) {
				TraceNode caller = null;
				if (!this.methodCallStack.isEmpty()) {
					caller = this.methodCallStack.peek();
				}

				TraceNode olderCaller = latestNode.getInvocationParent();
				if (olderCaller != null) {
					olderCaller.getInvocationChildren().remove(latestNode);
					latestNode.setInvocationParent(caller);
				}
			}
		} catch (Throwable t) {
			handleException(t);
		}
		trackingDelegate.track();
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
		trackingDelegate.untrack();
		try {
			hitLine(line, className, methodSignature);
			boolean exclusive = GlobalFilterChecker.isExclusive(className, methodSignature);
			TraceNode latestNode = trace.getLatestNode();
			if (exclusive) {
				if (latestNode != null && latestNode.getInvokingDetail() != null) {
					InvokingDetail invokingDetail = latestNode.getInvokingDetail();
					boolean relevant = invokingDetail.updateRelevantVar(refValue, fieldValue, fieldType);
					if (!relevant) {
						trackingDelegate.track();
						return;
					}
				} else {
					trackingDelegate.track();
					return;
				}
			}
			String parentVarId = TraceUtils.getObjectVarId(refValue, refValue.getClass().getName());
			String fieldVarId = TraceUtils.getFieldVarId(parentVarId, fieldName, fieldType, fieldValue);
			Variable var = new FieldVar(false, fieldName, fieldType, refValue.getClass().getName());
			var.setVarID(fieldVarId);
			if (!PrimitiveUtils.isPrimitive(fieldType)) {
				String aliasID = TraceUtils.getObjectVarId(fieldValue, fieldType);
				var.setAliasVarID(aliasID);
			}

			VarValue value = appendVarValue(fieldValue, var, null);

			Variable parentVariable = new FieldVar(false, "unknown", refValue.getClass().getName(), "unknown");
			parentVariable.setVarID(parentVarId);
			ReferenceValue parentValue = new ReferenceValue(false, false, parentVariable);
			value.addParent(parentValue);

			addRWriteValue(latestNode, value, true);
		} catch (Throwable t) {
			handleException(t);
		}
		trackingDelegate.track();
	}

	private void addRWriteValue(TraceNode currentNode, List<VarValue> value, boolean isWrittenVar) {
		ArrayList<VarValue> values;
		if (isWrittenVar) {
			values = (ArrayList<VarValue>) currentNode.getWrittenVariables();
		} else {
			values = (ArrayList<VarValue>) currentNode.getReadVariables();
		}
		if (value.size() > values.size()) {
			values.ensureCapacity(values.size() + value.size());
		}
		for (VarValue child : value) {
			addSingleRWriteValue(currentNode, child, false);
		}
	}

	private void addRWriteValue(TraceNode currentNode, VarValue value, boolean isWrittenVar) {
		if (value == null || currentNode == null) {
			return;
		}
		addSingleRWriteValue(currentNode, value, isWrittenVar);
	}

	private void addSingleRWriteValue(TraceNode currentNode, VarValue value, boolean isWrittenVar) {
		if (isWrittenVar) {
			currentNode.addWrittenVariable(value);
			// buildDataRelation(currentNode, value, Variable.WRITTEN);
		} else {
			currentNode.addReadVariable(value);
			// buildDataRelation(currentNode, value, Variable.READ);
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
		trackingDelegate.untrack();
		try {
			// boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
			// if (exclusive) {
			// return;
			// }
			hitLine(line, className, methodSignature);
			Variable var = new FieldVar(false, fieldName, fieldType, refType);
			var.setVarID(Variable.concanateFieldVarID(refType, fieldName));
			if (!PrimitiveUtils.isPrimitive(fieldType)) {
				String aliasVarID = TraceUtils.getObjectVarId(fieldValue, fieldType);
				var.setAliasVarID(aliasVarID);
			}
			VarValue value = appendVarValue(fieldValue, var, null);
			addRWriteValue(trace.getLatestNode(), value, true);
		} catch (Throwable t) {
			handleException(t);
		}
		trackingDelegate.track();
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
		trackingDelegate.untrack();
		try {
			boolean exclusive = GlobalFilterChecker.isExclusive(className, methodSignature);
			if (exclusive) {
				TraceNode latestNode = trace.getLatestNode();
				boolean relevant = false;
				if (latestNode != null && latestNode.getInvokingDetail() != null) {
					InvokingDetail invokingDetail = latestNode.getInvokingDetail();
					relevant = invokingDetail.updateRelevantVar(refValue, fieldValue, fieldType);
				}
				if (!relevant) {
					trackingDelegate.track();
					return;
				}
			}
			hitLine(line, className, methodSignature);
			String parentVarId = TraceUtils.getObjectVarId(refValue, refValue.getClass().getName());
			String fieldVarId = TraceUtils.getFieldVarId(parentVarId, fieldName, fieldType, fieldValue);
			// invokeTrack.updateRelevant(parentVarId, fieldVarId);
			// if (exclusive) {
			// return;
			// }
			Variable var = new FieldVar(false, fieldName, fieldType, refValue.getClass().getName());
			var.setVarID(fieldVarId);
			String aliasID = TraceUtils.getObjectVarId(fieldValue, fieldType);
			var.setAliasVarID(aliasID);

			VarValue value = appendVarValue(fieldValue, var, null);

			Variable parentVariable = new FieldVar(false, "unknown", refValue.getClass().getName(), "unknown");
			parentVariable.setVarID(parentVarId);
			ReferenceValue parentValue = new ReferenceValue(false, false, parentVariable);
			value.addParent(parentValue);

			addRWriteValue(trace.getLatestNode(), value, false);
			addHeuristicVarChildren(trace.getLatestNode(), value, false);
		} catch (Throwable t) {
			handleException(t);
		}
		trackingDelegate.track();
	}

	@Override
	public void _readStaticField(Object fieldValue, String refType, String fieldName, String fieldType, int line,
			String className, String methodSignature) {
		trackingDelegate.untrack();
		try {
			// boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
			// if (exclusive) {
			// locker.unLock();
			// return;
			// }
			hitLine(line, className, methodSignature);
			Variable var = new FieldVar(true, fieldName, fieldType, refType);
			var.setVarID(Variable.concanateFieldVarID(refType, fieldName));

			String aliasID = TraceUtils.getObjectVarId(fieldValue, fieldType);
			var.setAliasVarID(aliasID);

			VarValue value = appendVarValue(fieldValue, var, null);
			addRWriteValue(trace.getLatestNode(), value, false);
			addHeuristicVarChildren(trace.getLatestNode(), value, false);

		} catch (Throwable t) {
			handleException(t);
		}
		trackingDelegate.track();
	}

	/**
	 * Instrument for: Application Classes only. the filter is in
	 * LineInstructionInfo.extractRWInstructions()
	 */
	@Override
	public void _writeLocalVar(Object varValue, String varName, String varType, int line, int bcLocalVarIdx,
			int varScopeStartLine, int varScopeEndLine, String className, String methodSignature) {
		trackingDelegate.untrack();
		try {
			// boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
			// if (exclusive) {
			// locker.unLock();
			// return;
			// }
			hitLine(line, className, methodSignature);
			Variable var = new LocalVar(varName, varType, className, line);

			TraceNode latestNode = trace.getLatestNode();
			String varID = Variable.concanateLocalVarID(className, varName, varScopeStartLine, varScopeEndLine,
					latestNode.getInvocationLevel());
			var.setVarID(varID);
			if (!PrimitiveUtils.isPrimitive(varType)) {
				String aliasID = TraceUtils.getObjectVarId(varValue, varType);
				var.setAliasVarID(aliasID);
			}

			VarValue value = appendVarValue(varValue, var, null);
			addRWriteValue(trace.getLatestNode(), value, true);
		} catch (Throwable t) {
			handleException(t);
		}
		trackingDelegate.track();
	}

	/**
	 * Instrument for: Application Classes only.
	 */
	@Override
	public void _readLocalVar(Object varValue, String varName, String varType, int line, int bcLocalVarIdx,
			int varScopeStartLine, int varScopeEndLine, String className, String methodSignature) {
		trackingDelegate.untrack();
		try {
			// boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
			// if (exclusive) {
			// locker.unLock();
			// return;
			// }
			hitLine(line, className, methodSignature);
			TraceNode latestNode = trace.getLatestNode();
			Variable var = new LocalVar(varName, varType, className, line);

			String varID = Variable.concanateLocalVarID(className, varName, varScopeStartLine, varScopeEndLine,
					latestNode.getInvocationLevel());
			// String varID = TraceUtils.getLocalVarId(className, varScopeStartLine,
			// varScopeEndLine, varName, varType, varValue);
			var.setVarID(varID);
			String aliasVarID = TraceUtils.getObjectVarId(varValue, varType);
			var.setAliasVarID(aliasVarID);

			VarValue value = appendVarValue(varValue, var, null);
			addRWriteValue(trace.getLatestNode(), value, false);
			// System.currentTimeMillis();
			addHeuristicVarChildren(trace.getLatestNode(), value, false);

			// TraceNode currentNode = trace.getLatestNode();
			// String order = trace.findDefiningNodeOrder(Variable.READ, currentNode,
			// var.getVarID(), var.getAliasVarID());
			// if(value instanceof ReferenceValue && order.equals("0")){
			// if(isParameter(varScopeStartLine, varScopeEndLine, className)){
			//
			// TraceNode invocationParent = currentNode.getInvocationParent();
			// if(invocationParent!=null){
			// String simpleVarID = Variable.truncateSimpleID(varID);
			// varID = simpleVarID + ":" + invocationParent.getOrder();
			// value.setVarID(varID);
			//
			// if(!invocationParent.getWrittenVariables().contains(value)){
			// invocationParent.addWrittenVariable(value);
			// }
			// if(!currentNode.getReadVariables().contains(value)){
			// currentNode.addReadVariable(value);
			// }
			//
			// StepVariableRelationEntry entry = new StepVariableRelationEntry(varID);
			// entry.addProducer(invocationParent);
			// entry.addConsumer(currentNode);
			// trace.getStepVariableTable().put(varID, entry);
			//
			//
			// }
			// }
			// }
			// else{
			// addRWriteValue(value, false);
			// }

		} catch (Throwable t) {
			handleException(t);
		}
		trackingDelegate.track();
	}

	private boolean isParameter(int varScopeStartLine, int varScopeEndLine, String className) {
		BreakPoint point = trace.getLatestNode().getBreakPoint();
		String fullSign = point.getMethodSign();
		String shortSign = fullSign.substring(fullSign.indexOf("#") + 1, fullSign.length());
		MethodFinderBySignature finder = new MethodFinderBySignature(shortSign);
		ByteCodeParser.parse(className, finder, appJavaClassPath);
		Method method = finder.getMethod();

		if (method != null && method.getCode() != null) {
			InstructionList list = new InstructionList(method.getCode().getCode());
			InstructionHandle start = list.getStart();
			InstructionHandle end = list.getEnd();

			int startLine = method.getLineNumberTable().getSourceLine(start.getPosition());
			int endLine = method.getLineNumberTable().getSourceLine(end.getPosition());

			if (varScopeStartLine == startLine && varScopeEndLine == endLine) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Instrument for: Application Classes only.
	 */
	@Override
	public void _iincLocalVar(Object varValue, Object varValueAfter, String varName, String varType, int line,
			int bcLocalVarIdx, int varScopeStartLine, int varScopeEndLine, String className, String methodSignature) {
		trackingDelegate.untrack();
		try {
			// boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
			// if (exclusive) {
			// locker.unLock();
			// return;
			// }
			hitLine(line, className, methodSignature);

			TraceNode latestNode = trace.getLatestNode();
			Variable var = new LocalVar(varName, varType, className, line);
			String varID = Variable.concanateLocalVarID(className, varName, varScopeStartLine, varScopeEndLine,
					latestNode.getInvocationLevel());
			// String varID = TraceUtils.getLocalVarId(className, varScopeStartLine,
			// varScopeEndLine, varName, varType, varValue);
			var.setVarID(varID);

			Variable varBefore = var.clone();
			VarValue value = appendVarValue(varValue, varBefore, null);
			addRWriteValue(trace.getLatestNode(), value, false); // add read var

			Variable varAfter = var.clone();
			VarValue writtenValue = appendVarValue(varValueAfter, varAfter, null);
			addRWriteValue(trace.getLatestNode(), writtenValue, true); // add written var
		} catch (Throwable t) {
			handleException(t);
		}
		trackingDelegate.track();
	}

	/**
	 * @param arrayRef
	 * @param index
	 * @param eleValue
	 * @param elementType
	 * @param line
	 */
	@Override
	public void _readArrayElementVar(Object arrayRef, int index, Object eleValue, String elementType, int line,
			String className, String methodSignature) {
		trackingDelegate.untrack();
		try {
			boolean exclusive = GlobalFilterChecker.isExclusive(className, methodSignature);
			if (exclusive) {
				TraceNode latestNode = trace.getLatestNode();
				boolean relevant = false;
				if (latestNode != null && latestNode.getInvokingDetail() != null) {
					InvokingDetail invokingDetail = latestNode.getInvokingDetail();
					relevant = invokingDetail.updateRelevantVar(arrayRef, eleValue, elementType);
				}
				if (!relevant) {
					trackingDelegate.track();
					return;
				}
			}
			hitLine(line, className, methodSignature);
			VarValue value = addArrayElementVarValue(arrayRef, index, eleValue, elementType, line);

			Variable parentVariable = new FieldVar(false, "unknown", arrayRef.getClass().getName(), "unknown");
			String parentVarId = TraceUtils.getObjectVarId(arrayRef, arrayRef.getClass().getName());
			parentVariable.setVarID(parentVarId);
			ReferenceValue parentValue = new ReferenceValue(false, false, parentVariable);
			value.addParent(parentValue);

			addRWriteValue(trace.getLatestNode(), value, false);
			addHeuristicVarChildren(trace.getLatestNode(), value, false);

		} catch (Throwable t) {
			handleException(t);
		}
		trackingDelegate.track();
	}

	private void addHeuristicVarChildren(TraceNode latestNode, VarValue value, boolean isWritten) {
		// if (ArrayList.class.getName().equals(value.getRuntimeType())) {
		// for (VarValue child : value.getChildren()) {
		// if ("elementData".equals(child.getVarName())) {
		// addHeuristicVarChildren(latestNode, child, isWritten);
		// return;
		// }
		// }
		// } else if (HashMap.class.getName().equals(value.getRuntimeType())) {
		// for (VarValue child : value.getChildren()) {
		// if ("table".equals(child.getVarName())) {
		// addHeuristicVarChildren(latestNode, child, isWritten);
		// return;
		// }
		// }
		// }
		// else if (Stack.class.getName().equals(value.getRuntimeType())) {
		// /**
		// * TODO for Xuezhi extend stack content here.
		// */
		// for (VarValue child : value.getChildren()) {
		// if ("elementData".equals(child.getVarName())) {
		// addHeuristicVarChildren(latestNode, child, isWritten);
		// return;
		// }
		// }
		// }
		//
		// else if (value instanceof ArrayValue) {
		// Collection<VarValue> nodeReadVars = trace.getLatestNode().getReadVariables();
		// if (value.getChildren().size() > nodeReadVars.size()) {
		// ((ArrayList<?>)nodeReadVars).ensureCapacity(nodeReadVars.size() +
		// value.getChildren().size());
		// }
		// for (VarValue child : value.getChildren()) {
		// if (child.getVariable() instanceof ArrayElementVar) {
		// if (HeuristicIgnoringFieldRule.isHashMapTableType(child.getRuntimeType())) {
		// for (VarValue nodeAttr : child.getChildren()) {
		// addRWriteValue(trace.getLatestNode(), nodeAttr, false);
		// }
		// } else {
		// addRWriteValue(trace.getLatestNode(), child, false);
		// }
		// }
		// }
		// }

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
	public void _writeArrayElementVar(Object arrayRef, int index, Object eleValue, String elementType, int line,
			String className, String methodSignature) {
		trackingDelegate.untrack();
		try {
			boolean exclusive = GlobalFilterChecker.isExclusive(className, methodSignature);
			if (exclusive) {
				TraceNode latestNode = trace.getLatestNode();
				boolean relevant = false;
				if (latestNode != null && latestNode.getInvokingDetail() != null) {
					InvokingDetail invokingDetail = latestNode.getInvokingDetail();
					relevant = invokingDetail.updateRelevantVar(arrayRef, eleValue, elementType);
				}
				if (!relevant) {
					trackingDelegate.track();
					return;
				}
			}
			hitLine(line, className, methodSignature);
			VarValue value = addArrayElementVarValue(arrayRef, index, eleValue, elementType, line);

			Variable parentVariable = new FieldVar(false, "unknown", arrayRef.getClass().getName(), "unknown");
			String parentVarId = TraceUtils.getObjectVarId(arrayRef, arrayRef.getClass().getName());
			parentVariable.setVarID(parentVarId);
			ReferenceValue parentValue = new ReferenceValue(false, false, parentVariable);
			value.addParent(parentValue);

			addRWriteValue(trace.getLatestNode(), value, true);
		} catch (Throwable t) {
			handleException(t);
		}
		trackingDelegate.track();
	}

	private VarValue addArrayElementVarValue(Object arrayRef, int index, Object eleValue, String elementType, int line) {
		String id = new StringBuilder(TraceUtils.getObjectVarId(arrayRef, elementType + "[]")).append("[").append(index)
				.append("]").toString();
		String name = id;
		Variable var = new ArrayElementVar(name, elementType, id);
		var.setVarID(id);

		if (!PrimitiveUtils.isPrimitive(elementType)) {
			String aliasID = TraceUtils.getObjectVarId(eleValue, elementType);
			var.setAliasVarID(aliasID);
		}

		VarValue value = appendVarValue(eleValue, var, null);
		return value;
	}

	/**
	 * BE VERY CAREFUL WHEN MODIFYING THIS FUNCTION! TO AVOID CREATING A LOOP, DO
	 * KEEP THIS ATMOST SIMPLE, AVOID INVOKE ANY EXTERNAL LIBRARY FUNCTION, EVEN JDK
	 * INSIDE THIS BLOCK OF CODE AND ITS INVOKED METHODS.! (ONLY
	 * Thread.currentThread().getId() is exceptional used) IF NEED TO USE A LIST,MAP
	 * -> USE AN ARRAY INSTEAD!
	 */
	public synchronized static IExecutionTracer _getTracer(boolean isAppClass, String className, String methodSig,
			int methodStartLine, int methodEndLine, String paramNamesCode, String paramTypeSignsCode, Object[] params) {
		try {
			if (state == TracingState.TEST_STARTED && isAppClass) {
				state = TracingState.RECORDING;
				rtStore.setMainThreadId(Thread.currentThread().getId());
			}
			if (state != TracingState.RECORDING) {
				return EmptyExecutionTracer.getInstance();
			}
			long threadId = Thread.currentThread().getId();
			if (lockedThreads.isUntracking(threadId)) {
				return EmptyExecutionTracer.getInstance();
			}
			lockedThreads.untrack(threadId);
			// FIXME -mutithread LINYUN [1]
			/*
			 * LLT: the corresponding tracer for a thread will be load by threadId,
			 * currently we always return null if not main thread.
			 */
			ExecutionTracer tracer = rtStore.get(threadId);
			if (tracer == null) {
				tracer = rtStore.get(threadId);
				// lockedThreads.remove(threadId);
				// return EmptyExecutionTracer.getInstance();
			}
			tracer.enterMethod(className, methodSig, methodStartLine, methodEndLine, paramTypeSignsCode, paramNamesCode,
					params);
			lockedThreads.track(threadId);
			return tracer;
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

	public static IExecutionTracer getMainThreadStore() {
		return rtStore.getMainThreadTracer();
	}

	public static List<IExecutionTracer> getAllThreadStore() {
		return rtStore.getAllThreadTracer();
	}

	public static synchronized IExecutionTracer getCurrentThreadStore() {
		synchronized (rtStore) {
			long threadId = Thread.currentThread().getId();
			// String threadName = Thread.currentThread().getName();
			if (lockedThreads.isUntracking(threadId)) {
				return EmptyExecutionTracer.getInstance();
			}
			IExecutionTracer tracer = rtStore.get(threadId);
			// store.setThreadName(threadName);

			if (tracer == null) {
				tracer = EmptyExecutionTracer.getInstance();
			}
			return tracer;
		}
	}
	
	public static List<Long> stoppedThreads = new ArrayList<Long>();
	
	public static synchronized void stopRecordingCurrendThread() {
		synchronized (rtStore) {
			long threadId = Thread.currentThread().getId();
			lockedThreads.untrack(threadId);
			stoppedThreads.add(threadId);
		}
	}

	private static TracingState state = TracingState.INIT;

	public static void shutdown() {
		state = TracingState.SHUTDOWN;
	}

	public static void dispose() {
		adjustVarMap = new HashMap<>();
		lockedThreads = new LockedThreads();
		HeuristicIgnoringFieldRule.clearCache();
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

	private static volatile LockedThreads lockedThreads = new LockedThreads();

	static class TrackingDelegate {
//		boolean tracing;
		long threadId;

		public TrackingDelegate(long threadId) {
			this.threadId = threadId;
		}

		public void untrack() {
//			if (!tracing) {
//				lockedThreads.untrack(threadId);
//				tracing = true;
//			}
			lockedThreads.untrack(threadId);
		}

		public void track(boolean preserveLock) {
			if (!preserveLock) {
				track();
			}
		}

		public void track() {
//			tracing = false;
			lockedThreads.track(threadId);
		}

		public boolean isUntrack() {
			return lockedThreads.isUntracking(threadId);
		}
	}

	@Override
	public boolean lock() {
		boolean isLock = trackingDelegate.isUntrack();
		if (!isLock) {
			trackingDelegate.untrack();
		}
		return isLock;
	}

	public boolean isLock() {
		return trackingDelegate.isUntrack();
	}

	@Override
	public void unLock() {
		trackingDelegate.track();
	}

	public long getThreadId() {
		return threadId;
	}

	@Override
	public void setThreadName(String threadName) {
		this.trace.setThreadName(threadName);
	}

	public String getThreadName() {
		return this.trace.getThreadName();
	}


}
