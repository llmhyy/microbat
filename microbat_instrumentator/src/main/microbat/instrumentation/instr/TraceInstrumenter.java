package microbat.instrumentation.instr;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.AALOAD;
import org.apache.bcel.generic.AASTORE;
import org.apache.bcel.generic.ACONST_NULL;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.DUP2;
import org.apache.bcel.generic.DUP2_X1;
import org.apache.bcel.generic.DUP2_X2;
import org.apache.bcel.generic.DUP_X1;
import org.apache.bcel.generic.DUP_X2;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.POP;
import org.apache.bcel.generic.POP2;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.RETURN;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.SWAP;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.generic.Type;

import microbat.instrumentation.Agent;
import microbat.instrumentation.AgentConstants;
import microbat.instrumentation.AgentLogger;
import microbat.instrumentation.AgentParams;
import microbat.instrumentation.filter.GlobalFilterChecker;
import microbat.instrumentation.filter.UserFilters;
import microbat.instrumentation.instr.instruction.info.ArrayInstructionInfo;
import microbat.instrumentation.instr.instruction.info.EntryPoint;
import microbat.instrumentation.instr.instruction.info.FieldInstructionInfo;
import microbat.instrumentation.instr.instruction.info.LineInstructionInfo;
import microbat.instrumentation.instr.instruction.info.LocalVarInstructionInfo;
import microbat.instrumentation.instr.instruction.info.RWInstructionInfo;
import microbat.instrumentation.runtime.IExecutionTracer;
import microbat.instrumentation.runtime.TraceUtils;
import microbat.instrumentation.utils.MicrobatUtils;

public class TraceInstrumenter extends AbstractInstrumenter {
	protected static final String TRACER_VAR_NAME = "$tracer"; // local var
	private static final String TEMP_VAR_NAME = "$tempVar"; // local var
	
	private int tempVarIdx = 0;
	private EntryPoint entryPoint;
	private Set<String> requireSplittingMethods = Collections.emptySet();
	private UserFilters userFilters;
	
	TraceInstrumenter() {
	}
	
	public TraceInstrumenter(AgentParams params) {
		this.entryPoint = params.getEntryPoint();
		if (params.isRequireMethodSplit()) {
			this.requireSplittingMethods = params.getOverlongMethods();
		}
		this.userFilters = params.getUserFilters();
	}

	@Override
	protected byte[] instrument(String classFName, String className, JavaClass jc) {
		ClassGen classGen = new ClassGen(jc);
		ConstantPoolGen constPool = classGen.getConstantPool();
		JavaClass newJC = null;
		boolean entry = entryPoint == null ? false : className.equals(entryPoint.getClassName());
		boolean isAppClass = GlobalFilterChecker.isAppClass(classFName) || entry;
		if (!userFilters.isInstrumentable(className)) {
			return null;
		}
		for (Method method : jc.getMethods()) {
			if (method.isNative() || method.isAbstract() || method.getCode() == null) {
				continue; // Only instrument methods with code in them!
			}
			try {
				MethodGen methodGen = new MethodGen(method, classFName, constPool);
				boolean isMainMethod = false;
				if (entry && entryPoint.matchMethod(method.getName(), method.getSignature())) {
					isMainMethod = true;
				}
				
				boolean isEntry = false;
				if(method.getName().equals("run") && isThread(jc)) {
					isEntry = true;
				}
				
				
				GeneratedMethods generatedMethods = runMethodInstrumentation(classGen, constPool, methodGen, 
						method, isAppClass, isMainMethod, isEntry);
				if (generatedMethods != null) {
					if (doesBytecodeExceedLimit(generatedMethods)) {
						AgentLogger.info(String.format("Warning: %s exceeds bytecode limit!",
								MicrobatUtils.getMicrobatMethodFullName(classGen.getClassName(), method)));
					} else {
						for (MethodGen newMethod : generatedMethods.getExtractedMethods()) {
							newMethod.setMaxStack();
							newMethod.setMaxLocals();
							classGen.addMethod(newMethod.getMethod());
						}
						methodGen = generatedMethods.getRootMethod();
						// All changes made, so finish off the method:
						InstructionList instructionList = methodGen.getInstructionList();
						instructionList.setPositions();
						methodGen.setMaxStack();
						methodGen.setMaxLocals();
						classGen.replaceMethod(method, methodGen.getMethod());
					}
				}
				newJC = classGen.getJavaClass();
				newJC.setConstantPool(constPool.getFinalConstantPool());
			} catch (Exception e) {
				String message = e.getMessage();
				if (e.getMessage() != null && e.getMessage().contains("offset too large")) {
					message = "offset too large";
				}
				AgentLogger.info(String.format("Warning: %s [%s]",
						MicrobatUtils.getMicrobatMethodFullName(classGen.getClassName(), method), message));
				AgentLogger.error(e);
			}
		}
		if (newJC != null) {
			byte[] data = newJC.getBytes();
			return data;
		}
		return null;
	}
	
	private boolean isThread(JavaClass jc) {
		try {
			for(JavaClass interf: jc.getAllInterfaces()) {
				if(interf.getClassName().equals("java.lang.Runnable")) {
					return true;
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			for(JavaClass interf: jc.getSuperClasses()) {
				if(interf.getClassName().equals("java.lang.Thread")) {
					return true;
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	private boolean doesBytecodeExceedLimit(GeneratedMethods generatedMethods) {
		boolean excessive = doesBytecodeExceedLimit(generatedMethods.getRootMethod());
		for (MethodGen addedMethod : generatedMethods.getExtractedMethods()) {
			excessive |= doesBytecodeExceedLimit(addedMethod);
		}
		return excessive;
	}

	private GeneratedMethods runMethodInstrumentation(ClassGen classGen, ConstantPoolGen constPool, MethodGen methodGen, Method method,
			boolean isAppClass, boolean isMainMethod, boolean isEntry) {
		String methodFullName = MicrobatUtils.getMicrobatMethodFullName(classGen.getClassName(), method);
		boolean changed = instrumentMethod(classGen, constPool, methodGen, method, isAppClass, isMainMethod, isEntry);
		if (requireSplittingMethods.contains(methodFullName)) {
			MethodSplitter methodSplitter = new MethodSplitter(classGen, constPool);
			return methodSplitter.splitMethod(methodGen);
		}
		if (changed) {
			return new GeneratedMethods(methodGen);
		}
		return null;
	}
	
	/**
	 * 
	 * isEntry means either main() or run() (e.g, thread)
	 * 
	 * @return whether method is changed
	 */
	protected boolean instrumentMethod(ClassGen classGen, ConstantPoolGen constPool, MethodGen methodGen, Method method,
			boolean isAppClass, boolean isMainMethod, boolean isEntry) {
		if (!userFilters.isInstrumentable(classGen.getClassName(), method, methodGen.getLineNumbers())) {
			return false;
		}
		tempVarIdx = 0;
		InstructionList insnList = methodGen.getInstructionList();
		InstructionHandle startInsn = insnList.getStart();
		if (startInsn == null) {
			// empty method
			return false;
		}
		
		/* fill up missing variables in localVariableTable */
		LocalVariableSupporter.fillUpVariableTable(methodGen, method, constPool);
		
		List<LineInstructionInfo> lineInsnInfos = LineInstructionInfo.buildLineInstructionInfos(classGen, constPool,
				methodGen, method, isAppClass, insnList);
		int startLine = Integer.MAX_VALUE;
		int endLine = AgentConstants.UNKNOWN_LINE;
		for (LineInstructionInfo lineInfo : lineInsnInfos) {
			int line = lineInfo.getLine();
			if (line < startLine) {
				startLine = line;
			}
			if (line > endLine) {
				endLine = line;
			}
		}
		if (startLine == Integer.MAX_VALUE) {
			startLine = AgentConstants.UNKNOWN_LINE;
		}
		LocalVariableGen classNameVar = createLocalVariable(CLASS_NAME, methodGen, constPool);
		LocalVariableGen methodSigVar = createLocalVariable(METHOD_SIGNATURE, methodGen, constPool);
		LocalVariableGen tracerVar = methodGen.addLocalVariable(TRACER_VAR_NAME, Type.getType(IExecutionTracer.class),
				insnList.getStart(), insnList.getEnd());
		
		userFilters.filter(lineInsnInfos, classGen.getClassName(), method);
		for (LineInstructionInfo lineInfo : lineInsnInfos) {
			/* instrument RW instructions */
			List<RWInstructionInfo> rwInsns = lineInfo.getRWInstructions();
//			if (lineInfo.hasNoInstrumentation()) {
			injectCodeTracerHitLine(insnList, constPool, tracerVar, lineInfo.getLine(), lineInfo.getLineNumberInsn(),
					classNameVar, methodSigVar, lineInfo.hasExceptionTarget(), lineInfo.getReadWriteInsnTotal(false),
					lineInfo.getReadWriteInsnTotal(true));
//			}
			for (RWInstructionInfo rwInsnInfo : rwInsns) {
				InstructionList newInsns = null;
				if (rwInsnInfo instanceof FieldInstructionInfo) {
					newInsns = getInjectCodeTracerRWriteField(constPool, tracerVar, (FieldInstructionInfo) rwInsnInfo, classNameVar, methodSigVar);
				} else if (rwInsnInfo instanceof ArrayInstructionInfo) {
					newInsns = getInjectCodeTracerRWriteArray(methodGen, constPool, tracerVar,
							(ArrayInstructionInfo) rwInsnInfo, classNameVar, methodSigVar);
				} else if (rwInsnInfo instanceof LocalVarInstructionInfo) {
					if (rwInsnInfo.getInstruction() instanceof IINC) {
						newInsns = getInjectCodeTracerIINC(constPool, tracerVar,
								(LocalVarInstructionInfo) rwInsnInfo, classNameVar, methodSigVar);
					} else {
						newInsns = getInjectCodeTracerRWLocalVar(constPool, tracerVar,
								(LocalVarInstructionInfo) rwInsnInfo, classNameVar, methodSigVar);
					}
				}
				if ((newInsns != null) && (newInsns.getLength() > 0)) {
					InstructionHandle insnHandler = rwInsnInfo.getInstructionHandler();
					if (rwInsnInfo.isStoreInstruction()) {
						insertInsnHandler(insnList, newInsns, insnHandler);
						newInsns.dispose();
					} else {
						insertInsnHandler(insnList, newInsns, insnHandler);
						try {
							updateTarget(insnHandler, insnHandler.getPrev(), insnHandler.getNext());
							insnList.delete(insnHandler);
						} catch (TargetLostException e) {
							e.printStackTrace();
						}
						newInsns.dispose();
					}
				}
			}
			int line = lineInfo.getLine();
			/* instrument Invocation instructions */
			InstructionFactory instructionFactory = new InstructionFactory(classGen, constPool);
			for (InstructionHandle insn : lineInfo.getInvokeInstructions()) {
				injectCodeTracerInvokeMethod(methodGen, insnList, constPool, instructionFactory, tracerVar, insn, line,
						classNameVar, methodSigVar, isAppClass);
			}
			/* instrument Return instructions */
			for (InstructionHandle insn : lineInfo.getReturnInsns()) {
				injectCodeTracerReturn(insnList, constPool, tracerVar, insn, line, classNameVar, methodSigVar);
			}

			/**
			 * instrument exit instructions
			 */
			for (InstructionHandle exitInsHandle : lineInfo.getExitInsns()) {
				injectCodeTracerExit(exitInsHandle, insnList, constPool, tracerVar, line, classNameVar, methodSigVar, isMainMethod, isEntry);
			}

			lineInfo.dispose();
		}
		injectCodeInitTracer(methodGen, constPool, startLine, endLine, isAppClass, classNameVar,
				methodSigVar, isMainMethod, tracerVar);
		return true;
	}

	private void injectCodeTracerExit(InstructionHandle exitInsHandle, InstructionList insnList, 
			ConstantPoolGen constPool, LocalVariableGen tracerVar, int line, LocalVariableGen classNameVar, 
			LocalVariableGen methodSigVar, boolean isMainMethod, boolean isEntry) {
		InstructionList newInsns = new InstructionList();
		
		newInsns.append(new ALOAD(tracerVar.getIndex()));
		newInsns.append(new PUSH(constPool, line));
		newInsns.append(new ALOAD(classNameVar.getIndex()));
		newInsns.append(new ALOAD(methodSigVar.getIndex()));
		appendTracerMethodInvoke(newInsns, TracerMethods.HIT_METHOD_END, constPool);
		
		if (isMainMethod || isEntry) {
			int index = constPool.addInterfaceMethodref(Agent.class.getName().replace(".", "/"), "_exitProgram",
					"(Ljava/lang/String;)V");
			newInsns.append(new ALOAD(classNameVar.getIndex()));
			newInsns.append(new ALOAD(methodSigVar.getIndex()));
			newInsns.append(new INVOKESTATIC(index));
		}
		
		insertInsnHandler(insnList, newInsns, exitInsHandle);
		newInsns.dispose();
	}

	private void injectCodeTracerReturn(InstructionList insnList, ConstantPoolGen constPool, LocalVariableGen tracerVar,
			InstructionHandle insnHandler, int line, LocalVariableGen classNameVar, LocalVariableGen methodSigVar) {
		InstructionList newInsns = new InstructionList();
		ReturnInstruction insn = (ReturnInstruction) insnHandler.getInstruction();
		if (insn instanceof RETURN) {
			newInsns.append(new ALOAD(tracerVar.getIndex()));
			newInsns.append(new PUSH(constPool, line));
			newInsns.append(new ALOAD(classNameVar.getIndex()));
			newInsns.append(new ALOAD(methodSigVar.getIndex()));
			appendTracerMethodInvoke(newInsns, TracerMethods.HIT_VOID_RETURN, constPool);
		} else {
			Type type = insn.getType();
			if (insnHandler.getPrev().getInstruction() instanceof ACONST_NULL) {
				newInsns.append(new ALOAD(tracerVar.getIndex())); // val, tracer
				newInsns.append(new ACONST_NULL());
			} else {
				/* on stack: value */
				if (type.getSize() == 1) {
					newInsns.append(new DUP()); // val, val
					newInsns.append(new ALOAD(tracerVar.getIndex())); // val, val, tracer
					newInsns.append(new SWAP()); // val, tracer, val
				} else {
					newInsns.append(new DUP2()); // val*, val*
					newInsns.append(new ALOAD(tracerVar.getIndex())); // val*, val*, tracer
					newInsns.append(new DUP_X2()); // val*, tracer, val*, tracer
					newInsns.append(new POP()); // val*, tracer, val*
				}
				
				if (type instanceof BasicType) {
					newInsns.append(
							new INVOKESTATIC(basicTypeSupporter.getValueOfMethodIdx((BasicType) type, constPool)));
				}
			}
			newInsns.append(new PUSH(constPool, type.getSignature())); 
			// val*, tracer, val*, returnGeneralType
			
			newInsns.append(new PUSH(constPool, line)); 
			// val*, tracer, val*, returnGeneralType, line
			
			newInsns.append(new ALOAD(classNameVar.getIndex()));
			newInsns.append(new ALOAD(methodSigVar.getIndex()));
			appendTracerMethodInvoke(newInsns, TracerMethods.HIT_RETURN, constPool);
			// val
		}
		insertInsnHandler(insnList, newInsns, insnHandler);
		newInsns.dispose();
	}

	private void injectCodeTracerInvokeMethod(MethodGen methodGen, InstructionList insnList, ConstantPoolGen constPool,
			InstructionFactory instructionFactory, LocalVariableGen tracerVar, InstructionHandle insnHandler,
			int line, LocalVariableGen classNameVar, LocalVariableGen methodSigVar, boolean isAppClass) {
		InvokeInstruction insn = (InvokeInstruction) insnHandler.getInstruction();
		String className = insn.getClassName(constPool);

		if (insn instanceof INVOKESPECIAL && "java.lang.Object".equals(className)
				&& methodGen.getName().equals("<init>")) {
			return;
		}

		InstructionList newInsns = new InstructionList();
		TracerMethods tracerMethod = TracerMethods.HIT_INVOKE;
		boolean isInvokeStatic = insn instanceof INVOKESTATIC;
		if (isInvokeStatic) {
			tracerMethod = TracerMethods.HIT_INVOKE_STATIC;
		}
		/* on stack: (objectRef)+, arg1(*), arg2(*), ... */
		Type returnType = insn.getReturnType(constPool);//getReferenceType(constPool);
		Type[] argTypes = insn.getArgumentTypes(constPool);
		/*
		 * add tempVar to keep args. Object[] temp = Object[] {arg1(*), arg2(*), ...}
		 */
		LocalVariableGen argObjsVar = addTempVar(methodGen, new ArrayType(Type.OBJECT, 1), insnHandler);
		newInsns.append(new PUSH(constPool, argTypes.length));
		newInsns.append(new ANEWARRAY(constPool.addClass(Object.class.getName())));
		argObjsVar.setStart(newInsns.append(new ASTORE(argObjsVar.getIndex())));
		/* store args */
		for (int i = (argTypes.length - 1); i >= 0; i--) {
			newInsns.append(new ALOAD(argObjsVar.getIndex())); 
			// [objectRef, arg1, arg2, ...] argn, tempVar
			
			Type argType = argTypes[i];
			/* swap */
			if (argType.getSize() == 1) {
				newInsns.append(new SWAP()); // tempVar, argn
				newInsns.append(new PUSH(constPool, i)); // tempVar, argn, idx
				newInsns.append(new SWAP()); // tempVar, idx, argn
			} else {
				// argn*, tempVar
				/* swap */
				newInsns.append(new DUP_X2()); // tempVar, argn*, tempVar
				newInsns.append(new POP()); // tempVar, argn*
				newInsns.append(new PUSH(constPool, i)); // tempVar, argn*, idx
				newInsns.append(new DUP_X2()); // tempVar, idx, argn*, idx
				newInsns.append(new POP()); // tempVar, idx, argn*
			}
			if (argType instanceof BasicType) {
				newInsns.append(
						new INVOKESTATIC(basicTypeSupporter.getValueOfMethodIdx((BasicType) argType, constPool)));
			}
			newInsns.append(new AASTORE());
		}
		if (!isInvokeStatic) {
			/* duplicate objectRef */
			newInsns.append(new DUP()); // objectRef, objectRef
			newInsns.append(new ALOAD(tracerVar.getIndex()));
			newInsns.append(new SWAP()); // objectRef, tracer, objectRef
		} else {
			/* empty stack */
			newInsns.append(new ALOAD(tracerVar.getIndex())); // tracer
		}
		newInsns.append(new PUSH(constPool, className)); 
		// ([objectRef], objectRef),  

		String sig = insn.getSignature(constPool);
		String methodName = insn.getMethodName(constPool);
		String mSig = className + "#" + methodName + sig;
		int stringIndex = constPool.lookupString(mSig);
		if (stringIndex == -1) {
			stringIndex = constPool.addString(mSig);
		}
		newInsns.append(new PUSH(constPool, mSig)); 
		// ([objectRef], objectRef), invokeType, methodName
		
		newInsns.append(new ALOAD(argObjsVar.getIndex())); 
		// ([objectRef], objectRef), invokeType, methodName, params
		
		newInsns.append(new PUSH(constPool, TraceUtils.encodeArgTypes(argTypes))); 
		// (objectRef), invokeType, methodName, params, paramTypesCode
		
		newInsns.append(new PUSH(constPool, returnType.getSignature())); 
		// (objectRef), invokeType, methodName, params, paramTypesCode, returnTypeSign
		
		newInsns.append(new PUSH(constPool, line));
		newInsns.append(new ALOAD(classNameVar.getIndex()));
		newInsns.append(new ALOAD(methodSigVar.getIndex()));
		appendTracerMethodInvoke(newInsns, tracerMethod, constPool);
		
		/* on stack: (objectRef) */
		// duplicate objectRef to use in afterInvoke
		if (isAppClass && !isInvokeStatic) {
			newInsns.append(new DUP()); // objectRef, objectRef
		}
		
		/* restore arg values */
		for (int i = 0; i < argTypes.length; i++) {
			Type argType = argTypes[i];
			newInsns.append(new ALOAD(argObjsVar.getIndex())); // load argObjs[]
			newInsns.append(new PUSH(constPool, i)); // arg_idx
			newInsns.append(new AALOAD()); // -> argObjs[arg_idx]
			if (argType instanceof BasicType) {
				newInsns.append(instructionFactory
						.createCheckCast(basicTypeSupporter.getCorrespondingPrimitiveType((BasicType) argType)));
				newInsns.append(new INVOKEVIRTUAL(
						basicTypeSupporter.getToPrimitiveValueMethodIdx((BasicType) argType, constPool)));
			}
		}
		
		insertInsnHandler(insnList, newInsns, insnHandler);
		newInsns.dispose();
		/* after_invoke */
		if (isAppClass) {
			injectCodeAfterInvoke(insnList, constPool, tracerVar, insnHandler, line, classNameVar, methodSigVar,
					isInvokeStatic, returnType, mSig);
		}
	}

	private void injectCodeAfterInvoke(InstructionList insnList, ConstantPoolGen constPool, LocalVariableGen tracerVar,
			InstructionHandle insnHandler, int line, LocalVariableGen classNameVar, LocalVariableGen methodSigVar,
			boolean isInvokeStatic, Type returnType, String mSig) {
		InstructionList newInsns;
		/* on stack: [objectRef]/[], returnValue */
		boolean revisit = !Type.VOID.equals(returnType); 
//					&& ((insnHandler.getNext() == null)
//					|| !(insnHandler.getNext().getInstruction() instanceof POP));
		newInsns = new InstructionList();
		
		if (isInvokeStatic) {
			if (Type.VOID.equals(returnType)) {
				/* on stack: [empty] */
				newInsns.append(new ALOAD(tracerVar.getIndex())); // $tracer
				newInsns.append(new ACONST_NULL()); // $tracer, returnValue
			} else if (returnType.getSize() == 1) {
				/* on stack: returnValue */
				newInsns.append(new DUP()); // returnValue, returnValue
				newInsns.append(new ALOAD(tracerVar.getIndex())); // returnValue, returnValue, $tracer
				newInsns.append(new SWAP()); // returnValue, $tracer, returnValue
			} else { // 2
				/* on stack: returnValue* */
				newInsns.append(new DUP2()); // returnValue*, returnValue*
				newInsns.append(new ALOAD(tracerVar.getIndex())); // returnValue*, returnValue*, $tracer
				newInsns.append(new DUP_X2()); // returnValue*, $tracer, returnValue*, $tracer
				newInsns.append(new POP()); // returnValue*, $tracer, returnValue*
			}
			basicTypeSupporter.appendObjectConvertInstruction(returnType, newInsns, constPool);
			newInsns.append(new ACONST_NULL()); // (returnValue(*)), $tracer, returnValue*, objectRef
			/* no redundant obj on stack */
		} else {
			if (Type.VOID.equals(returnType)) {
				// objectRef
				newInsns.append(new ALOAD(tracerVar.getIndex())); // objectRef, $tracer
				newInsns.append(new SWAP()); // $tracer, objectRef
				newInsns.append(new ACONST_NULL()); // $tracer, objectRef, returnValue_null
				newInsns.append(new SWAP()); // $tracer, returnValue_null, objectRef
				// do nothing
			} else if (returnType.getSize() == 1) {
				// objectRef, returnValue
				newInsns.append(new DUP()); // objectRef, returnValue, returnValue
				newInsns.append(new DUP_X2()); // returnValue, objectRef, returnValue, returnValue
				newInsns.append(new POP()); // returnValue, objectRef, returnValue
				newInsns.append(new ALOAD(tracerVar.getIndex())); // [returnValue], objectRef, returnValue, $trace
				newInsns.append(new DUP_X2()); // [returnValue], $trace, objectRef, returnValue, $trace
				newInsns.append(new POP()); // [returnValue], $trace, objectRef, returnValue
				basicTypeSupporter.appendObjectConvertInstruction(returnType, newInsns, constPool);
				newInsns.append(new SWAP()); // [returnValue], $trace, returnValue, objectRef
				// returnValue, objectRef
			} else { // 2
				/* on stack: objectRef, returnValue* */
				newInsns.append(new DUP2_X1()); // returnValue*, objectRef, returnValue*
				basicTypeSupporter.appendObjectConvertInstruction(returnType, newInsns, constPool); // returnValue*, objectRef, returnValue
				newInsns.append(new ALOAD(tracerVar.getIndex())); // [returnValue*], objectRef, returnValue, $trace
				newInsns.append(new DUP_X2()); // [returnValue*], $trace, objectRef, returnValue, $trace
				newInsns.append(new POP()); // [returnValue*], $trace, objectRef, returnValue
				newInsns.append(new SWAP()); // [returnValue*], $trace, returnValue, objectRef
			}
			// $tracer, returnValue, objectRef
		}
		newInsns.append(new PUSH(constPool, mSig));
		newInsns.append(new PUSH(constPool, line));
		newInsns.append(new ALOAD(classNameVar.getIndex()));
		newInsns.append(new ALOAD(methodSigVar.getIndex()));
		newInsns.append(new PUSH(constPool, revisit));
		appendTracerMethodInvoke(newInsns, TracerMethods.AFTER_INVOKE, constPool);
		appendInstruction(insnList, newInsns, insnHandler);
		newInsns.dispose();
	}

	private InstructionList getInjectCodeTracerRWriteField(ConstantPoolGen constPool, LocalVariableGen tracerVar,
			FieldInstructionInfo info, LocalVariableGen classNameVar, LocalVariableGen methodSigVar) {
		FieldInstruction insn = (FieldInstruction) info.getInstruction();
		if (insn instanceof PUTFIELD) {
			return getInjectCodePutField(constPool, tracerVar, info, classNameVar, methodSigVar);
		} else if (insn instanceof PUTSTATIC) {
			return getInjectCodePutStatic(constPool, tracerVar, info, classNameVar, methodSigVar);
		} else if (insn instanceof GETFIELD) {
			return getInjectCodeGetField(constPool, tracerVar, info, classNameVar, methodSigVar);
		} else if (insn instanceof GETSTATIC) {
			return getInjectCodeGetStatic(constPool, tracerVar, info, classNameVar, methodSigVar);
		}
		return null;
	}

	private InstructionList getInjectCodePutField(ConstantPoolGen constPool, LocalVariableGen tracerVar,
			FieldInstructionInfo info, LocalVariableGen classNameVar, LocalVariableGen methodSigVar) {
		InstructionList newInsns = new InstructionList();
		/*
		 * on stack: obj, value
		 */
		if (info.isNextToAconstNull()) {
			newInsns.append(new POP()); // obj
			newInsns.append(new DUP()); // obj, obj
			newInsns.append(new ALOAD(tracerVar.getIndex())); // obj, obj, tracer
			newInsns.append(new SWAP()); // obj, tracer, obj
			newInsns.append(new ACONST_NULL()); // obj, tracer, obj, null
		} else if (info.isComputationalType1()) {
			newInsns.append(new DUP2()); // Duplicates object and value: 
			// [obj, val], obj, val
			
			newInsns.append(new ALOAD(tracerVar.getIndex())); 
			// [obj, val], obj, val, tracer
			
			newInsns.append(new DUP_X2()); 
			// [obj, val], tracer, obj, val, tracer
			
			newInsns.append(new POP()); 
			// [obj, val], tracer, obj, val
			
		} else { 
			// obj, val*
			
			newInsns.append(new DUP2_X1()); // val*, obj, val*
			newInsns.append(new POP2());  // val*, obj
			newInsns.append(new DUP_X2()); // obj, val*, obj
			newInsns.append(new DUP_X2()); // obj, obj, val*, obj
			newInsns.append(new POP()); // obj, obj, val*
			newInsns.append(new DUP2_X1()); // obj, val*, obj, val*

			/* swap obj, var* */
			newInsns.append(new DUP2_X1()); // [obj, val*], val*, obj, val*
			newInsns.append(new POP2()); // [obj, val*], val*, obj

			/* push tracer */
			newInsns.append(new ALOAD(tracerVar.getIndex())); // [obj, val*], val*, obj, tracer
			/* bring tracer to the bottom */
			newInsns.append(new DUP2_X2()); // [obj, val*], obj, tracer, val*, obj, tracer
			newInsns.append(new POP()); // [obj, val*], obj, tracer, val*, obj
			
			/* swap obj, var* */
			newInsns.append(new DUP_X2()); // [obj, val*], obj, tracer, obj, val*, obj
			newInsns.append(new POP()); // [obj, val*], obj, tracer, obj, val*
		}
		Type fieldType = info.getFieldBcType();
		if (fieldType instanceof BasicType) {
			newInsns.append(new INVOKESTATIC(basicTypeSupporter.getValueOfMethodIdx((BasicType) fieldType, constPool)));
		}
		newInsns.append(new PUSH(constPool, info.getFieldName())); 
		// [obj || (obj, val) || (obj, val*, obj)], tracer, obj, val, fieldName
		
		newInsns.append(new PUSH(constPool, info.getFieldType())); 
		// [obj || (obj, val) || (obj, val*, obj)],tracer, obj, val, fieldName, fieldTypeSignature
		
		newInsns.append(new PUSH(constPool, info.getLine())); 
		// [obj || (obj, val) || (obj, val*, obj)], tracer, obj, val, fieldName, fieldTypeSignature, line
		
		newInsns.append(new ALOAD(classNameVar.getIndex()));
		newInsns.append(new ALOAD(methodSigVar.getIndex()));
		appendTracerMethodInvoke(newInsns, TracerMethods.WRITE_FIELD, constPool); 
		// record -> [obj || (obj, val) || (obj, val*, obj)]
		
		if (info.isNextToAconstNull()) {
			newInsns.append(new ACONST_NULL());
		} else if (info.isComputationalType2()) {
			newInsns.append(new POP());
		}
		return newInsns;
	}

	/**
	 * ex: ldc "strABC" (java.lang.String) putstatic
	 * microbat/instrumentation/trace/testdata/RefVar.staticStr:java.lang.String
	 * @param methodSigVar 
	 * @param classNameVar 
	 */
	private InstructionList getInjectCodePutStatic(ConstantPoolGen constPool, LocalVariableGen tracerVar,
			FieldInstructionInfo info, LocalVariableGen classNameVar, LocalVariableGen methodSigVar) {
		InstructionList newInsns = new InstructionList();
		if (info.isNextToAconstNull()) {
			newInsns.append(new ALOAD(tracerVar.getIndex())); // tracer
			newInsns.append(new ACONST_NULL()); // tracer, val
		} else if (info.getFieldStackSize() == 1) {
			newInsns.append(new DUP()); // val
			newInsns.append(new ALOAD(tracerVar.getIndex())); // val, tracer
			newInsns.append(new SWAP()); // tracer, val
		} else {
			newInsns.append(new DUP2()); // val*
			newInsns.append(new ALOAD(tracerVar.getIndex())); // val*, tracer
			newInsns.append(new DUP_X2()); // tracer, val*, tracer
			newInsns.append(new POP()); // tracer, val*
		}
		Type fieldType = info.getFieldBcType();
		if (fieldType instanceof BasicType) {
			newInsns.append(new INVOKESTATIC(basicTypeSupporter.getValueOfMethodIdx((BasicType) fieldType, constPool)));
		}
		newInsns.append(new PUSH(constPool, info.getRefType())); 
		// tracer, val*, refType
		newInsns.append(new PUSH(constPool, info.getFieldName())); 
		// tracer, val*, refType, fieldName
		newInsns.append(new PUSH(constPool, info.getFieldType())); 
		// tracer, val*, refType, fieldName, fieldType
		newInsns.append(new PUSH(constPool, info.getLine())); 
		// tracer, val*, refType, fieldName, fieldType, line
		newInsns.append(new ALOAD(classNameVar.getIndex()));
		newInsns.append(new ALOAD(methodSigVar.getIndex()));
		appendTracerMethodInvoke(newInsns, TracerMethods.WRITE_STATIC_FIELD, constPool);
		return newInsns;
	}

	protected void appendTracerMethodInvoke(InstructionList newInsns, TracerMethods method, ConstantPoolGen constPool) {
		if (method.isInterfaceMethod()) {
			int index = constPool.addInterfaceMethodref(method.getDeclareClass(), method.getMethodName(),
					method.getMethodSign());
			newInsns.append(new INVOKEINTERFACE(index, method.getArgNo()));
		} else {
			int index = constPool.addMethodref(method.getDeclareClass(), method.getMethodName(),
					method.getMethodSign());
			newInsns.append(new INVOKESTATIC(index));
		}
	}

	private InstructionList getInjectCodeGetField(ConstantPoolGen constPool, LocalVariableGen tracerVar,
			FieldInstructionInfo info, LocalVariableGen classNameVar, LocalVariableGen methodSigVar) {
		InstructionList newInsns = new InstructionList();

		// stack: obj (refValue)
		GETFIELD insn = (GETFIELD) info.getInstruction();
		newInsns.append(new DUP()); // obj, obj
		newInsns.append(new GETFIELD(insn.getIndex())); // obj, val (*)
		if (info.isComputationalType1()) {
			newInsns.append(new DUP_X1()); // [val], obj, val
			newInsns.append(new ALOAD(tracerVar.getIndex())); 
			// [val], obj, val, tracer
			
			newInsns.append(new DUP_X2()); // [val], tracer, obj, val, tracer
			newInsns.append(new POP()); // [val], tracer, obj, val
		} else {
			// obj, val* 
			
			newInsns.append(new DUP2_X1()); // [val*], obj, val*
			/* swap */
			newInsns.append(new DUP2_X1()); // [val*], val*, obj, val*
			newInsns.append(new POP2()); // [val*], val*, obj

			/* push tracer */
			newInsns.append(new ALOAD(tracerVar.getIndex())); // [val*], val*, obj, tracer
			/* bring tracer to the bottom */
			newInsns.append(new DUP2_X2()); // [val*], obj, tracer, val*, obj, tracer
			newInsns.append(new POP()); // [val*], obj, tracer, val*, obj
			/* swap obj, var* */
			newInsns.append(new DUP_X2()); // [val*], obj, tracer, obj, val*, obj
			newInsns.append(new POP()); // [val*, obj], tracer, obj, val*
		}
		Type fieldType = info.getFieldBcType();
		if (fieldType instanceof BasicType) {
			newInsns.append(new INVOKESTATIC(basicTypeSupporter.getValueOfMethodIdx((BasicType) fieldType, constPool)));
		}
		newInsns.append(new PUSH(constPool, info.getFieldName())); // [val*, obj], tracer, obj, val, fieldName
		newInsns.append(new PUSH(constPool, info.getFieldType())); // [val*, obj], tracer, obj, val, fieldName, fieldTypeSignature
		newInsns.append(new PUSH(constPool, info.getLine())); // [val*, obj], tracer, obj, val, fieldName, fieldTypeSignature, line
		newInsns.append(new ALOAD(classNameVar.getIndex()));
		newInsns.append(new ALOAD(methodSigVar.getIndex()));
		appendTracerMethodInvoke(newInsns, TracerMethods.READ_FIELD, constPool); 
		// record -> [val] or [val*, obj]
		if (info.isComputationalType2()) {
			newInsns.append(new POP());
		}
		return newInsns;
	}

	private InstructionList getInjectCodeGetStatic(ConstantPoolGen constPool, LocalVariableGen tracerVar,
			FieldInstructionInfo info, LocalVariableGen classNameVar, LocalVariableGen methodSigVar) {
		InstructionList newInsns = new InstructionList();
		GETSTATIC insn = (GETSTATIC) info.getInstruction();
		newInsns.append(insn); // val
		/* duplicate field value */
		if (info.isComputationalType1()) {
			newInsns.append(new DUP()); // [val], val
			newInsns.append(new ALOAD(tracerVar.getIndex())); // [val], val,
																// tracer
			newInsns.append(new SWAP()); // [val], tracer, val
		} else {
			newInsns.append(new DUP2()); // val*, val*
			newInsns.append(new ALOAD(tracerVar.getIndex())); // [val*], val*, tracer
			/* swap */
			newInsns.append(new DUP_X2()); // [val*], tracer, val*, tracer
			newInsns.append(new POP()); // [val*], tracer, val*
		}
		Type fieldType = info.getFieldBcType();
		if (fieldType instanceof BasicType) {
			newInsns.append(new INVOKESTATIC(basicTypeSupporter.getValueOfMethodIdx((BasicType) fieldType, constPool)));
		}
		newInsns.append(new PUSH(constPool, info.getRefType())); 
		// tracer, val*, refType
		newInsns.append(new PUSH(constPool, info.getFieldName()));
		// tracer, val*, refType, fieldName
		newInsns.append(new PUSH(constPool, info.getFieldType())); 
		// tracer, val*, refType, fieldName, fieldType
		newInsns.append(new PUSH(constPool, info.getLine())); 
		// tracer, val*, refType, fieldName, fieldType, line
		newInsns.append(new ALOAD(classNameVar.getIndex()));
		newInsns.append(new ALOAD(methodSigVar.getIndex()));
		appendTracerMethodInvoke(newInsns, TracerMethods.READ_STATIC_FIELD, constPool);
		return newInsns;
	}

	private InstructionList getInjectCodeTracerRWLocalVar(ConstantPoolGen constPool, LocalVariableGen tracerVar,
			LocalVarInstructionInfo insnInfo, LocalVariableGen classNameVar, LocalVariableGen methodSigVar) {
		LocalVariableInstruction insn = insnInfo.getInstruction();
		// ignore reference to self
//		if (insn.getIndex() == 0) {
//			return null;
//		}
		InstructionList newInsns = new InstructionList();
		Type type = insn.getType(constPool);
		/*
		 * for load instruction, we need to execute the load instruction first
		 * to get the value of local variable, then onward, the logic would be
		 * the same for both case, load & store
		 */
		TracerMethods tracerMethod = TracerMethods.WRITE_LOCAL_VAR;
		if (!insnInfo.isStoreInstruction()) {
			newInsns.append(insn.copy()); // value
				tracerMethod = TracerMethods.READ_LOCAL_VAR; // value
		} 
		/* invoke tracer */
		if (insnInfo.isStoreInstruction() && insnInfo.getInstructionHandler().getPrev().getInstruction() instanceof ACONST_NULL) {
			newInsns.append(new ALOAD(tracerVar.getIndex())); // value, $tracer
			newInsns.append(new ACONST_NULL());
		} else {
			if (insnInfo.isComputationalType1()) {
				newInsns.append(new DUP()); // [value], value
				newInsns.append(new ALOAD(tracerVar.getIndex())); // [value], value, $tracer
				newInsns.append(new SWAP()); //  [value], $tracer, value
			} else { // stack size = 2
				newInsns.append(new DUP2()); // [value*], value* 
				newInsns.append(new ALOAD(tracerVar.getIndex())); // [value*], value*, $tracer
				newInsns.append(new DUP_X2()); // [value*], $tracer, value*, $tracer
				newInsns.append(new POP()); // [value*], $tracer, value*
			}
		}
		if (type instanceof BasicType) {
			newInsns.append(new INVOKESTATIC(basicTypeSupporter.getValueOfMethodIdx((BasicType) type, constPool)));
		}
		
		newInsns.append(new PUSH(constPool, insnInfo.getVarName())); 
		// [value(*)], $tracer, value, varName
		
		newInsns.append(new PUSH(constPool, insnInfo.getVarType())); 
		// [value(*)], $tracer, value, varName, varType
		
		newInsns.append(new PUSH(constPool, insnInfo.getLine())); 
		// [value(*)], $tracer, value, varName, line
		
		newInsns.append(new PUSH(constPool, insn.getIndex())); 
		// [value(*)], $tracer, value, varName, bcLocalVarIdx
		
		newInsns.append(new PUSH(constPool, insnInfo.getVarScopeStartLine())); 
		// [value(*)], $tracer, value, varName, bcLocalVarIdx, varScopeStartLine
		
		newInsns.append(new PUSH(constPool, insnInfo.getVarScopeEndLine())); 
		// [value(*)], $tracer, value, varName, bcLocalVarIdx, varScopeStartLine, varScopeEndLine
		
		newInsns.append(new ALOAD(classNameVar.getIndex()));
		newInsns.append(new ALOAD(methodSigVar.getIndex()));
		appendTracerMethodInvoke(newInsns, tracerMethod, constPool);
		return newInsns;
	}
	
	private InstructionList getInjectCodeTracerIINC(ConstantPoolGen constPool, LocalVariableGen tracerVar,
			LocalVarInstructionInfo insnInfo, LocalVariableGen classNameVar, LocalVariableGen methodSigVar) {
		IINC insn = (IINC) insnInfo.getInstruction();
		// ignore reference to self
		if (insn.getIndex() == 0) {
			return null;
		}
		InstructionList newInsns = new InstructionList();
		Type type = insn.getType(constPool);
		
		/* tracer */
		newInsns.append(new ALOAD(tracerVar.getIndex()));
		/* load current value */
		newInsns.append(InstructionFactory.createLoad(type, insn.getIndex())); // $tracer, value
		newInsns.append(new INVOKESTATIC(basicTypeSupporter.getValueOfMethodIdx((BasicType) type, constPool)));
		
		/* iinc */
		newInsns.append(insn.copy()); // $tracer, value
		
		/* load valueAfter */
		newInsns.append(InstructionFactory.createLoad(type, insn.getIndex())); // $tracer, value, valueAfter
		newInsns.append(new INVOKESTATIC(basicTypeSupporter.getValueOfMethodIdx((BasicType) type, constPool)));

		newInsns.append(new PUSH(constPool, insnInfo.getVarName())); 
		// $tracer, value, valueAfter, varName
		
		newInsns.append(new PUSH(constPool, insnInfo.getVarType())); 
		// $tracer, value, valueAfter, varName, varType
		
		newInsns.append(new PUSH(constPool, insnInfo.getLine())); 
		// $tracer, value, valueAfter, varName, line
		
		newInsns.append(new PUSH(constPool, insn.getIndex())); 
		// $tracer, value, valueAfter, varName, bcLocalVarIdx
		
		newInsns.append(new PUSH(constPool, insnInfo.getVarScopeStartLine())); 
		// $tracer, value, valueAfter, varName, bcLocalVarIdx, varScopeStartLine
		
		newInsns.append(new PUSH(constPool, insnInfo.getVarScopeEndLine())); 
		// $tracer, value, valueAfter, varName, bcLocalVarIdx, varScopeStartLine, varScopeEndLine
		
		newInsns.append(new ALOAD(classNameVar.getIndex()));
		newInsns.append(new ALOAD(methodSigVar.getIndex()));
		appendTracerMethodInvoke(newInsns, TracerMethods.IINC_LOCAL_VAR, constPool);
		
		return newInsns;
	}

	private InstructionList getInjectCodeTracerRWriteArray(MethodGen methodGen, ConstantPoolGen constPool,
			LocalVariableGen tracerVar, ArrayInstructionInfo info, LocalVariableGen classNameVar, LocalVariableGen methodSigVar) {
		InstructionHandle insnHandler = info.getInstructionHandler();
		ArrayInstruction insn = info.getInstruction();
		InstructionList newInsns = new InstructionList();

		LocalVariableGen arrEleTempVar = addTempVar(methodGen, info.getElementType(), insnHandler);
		InstructionHandle tempVarStartPos;
		TracerMethods tracerMethod = null;
		if (info.isStoreInstruction()) {
			tracerMethod = TracerMethods.WRITE_ARRAY_ELEMENT_VAR;
			// arrRef, idx, val
			tempVarStartPos = newInsns
					.append(InstructionFactory.createStore(info.getElementType(), arrEleTempVar.getIndex())); // arrRef,
																												// idx
			newInsns.append(new DUP2()); // [arrRef, idx], arrRef, idx
			/* in waiting list: [arrRef, idx] */
		} else {
			tracerMethod = TracerMethods.READ_ARRAY_ELEMENT_VAR;
			// arrRef, idx
			newInsns.append(new DUP2()); // [arrRef, idx], arrRef, idx
			newInsns.append(insn.copy()); // arrRef, idx, val
			tempVarStartPos = newInsns
					.append(InstructionFactory.createStore(info.getElementType(), arrEleTempVar.getIndex())); // arrRef,
																												// idx
			/* waiting list (empty): [] */
		}
		arrEleTempVar.setStart(tempVarStartPos);
		/* working on active list: arrRef, idx */
		newInsns.append(new ALOAD(tracerVar.getIndex())); // arrRef, idx, tracer
		newInsns.append(new DUP_X2()); // tracer, arrRef, idx, tracer
		newInsns.append(new POP()); // tracer, arrRef, idx
		newInsns.append(InstructionFactory.createLoad(info.getElementType(), arrEleTempVar.getIndex())); 
		// tracer, arrRef, idx, val
		
		if (info.getElementType() instanceof BasicType) {
			newInsns.append(
					new INVOKESTATIC(basicTypeSupporter.getValueOfMethodIdx((BasicType) info.getElementType(), constPool)));
		}
		newInsns.append(new PUSH(constPool, info.getVarType())); 
		// tracer, arrRef, idx, val, eleType
		
		newInsns.append(new PUSH(constPool, info.getLine())); 
		// tracer, arrRef, idx, val, eleType, line
		
		newInsns.append(new ALOAD(classNameVar.getIndex()));
		newInsns.append(new ALOAD(methodSigVar.getIndex()));
		appendTracerMethodInvoke(newInsns, tracerMethod, constPool);
		/* restore element value for use */
		newInsns.append(InstructionFactory.createLoad(info.getElementType(), arrEleTempVar.getIndex())); // val
		/*
		 * at this point : For Store Instruction case: arrRef, idx, val For Load
		 * Instruction case: val
		 */
		return newInsns;
	}

	private LocalVariableGen addTempVar(MethodGen methodGen, Type type, InstructionHandle insnHandler) {
		return methodGen.addLocalVariable(nextTempVarName(), type, insnHandler, insnHandler.getNext());
	}

	protected void injectCodeTracerHitLine(InstructionList insnList, ConstantPoolGen constPool,
			LocalVariableGen tracerVar, int line, InstructionHandle lineNumberInsn, LocalVariableGen classNameVar,
			LocalVariableGen methodSigVar, boolean isExceptionTarget, int readVars, int writtenVars) {
		TracerMethods tracerMethod = isExceptionTarget ? TracerMethods.HIT_EXEPTION_TARGET : TracerMethods.HIT_LINE;
		InstructionList newInsns = new InstructionList();
		newInsns.append(new ALOAD(tracerVar.getIndex()));
		newInsns.append(new PUSH(constPool, line));
		newInsns.append(new ALOAD(classNameVar.getIndex()));
		newInsns.append(new ALOAD(methodSigVar.getIndex()));
		if (!isExceptionTarget) {
			newInsns.append(new PUSH(constPool, readVars));
			newInsns.append(new PUSH(constPool, writtenVars));
		}
		appendTracerMethodInvoke(newInsns, tracerMethod, constPool);
		insertInsnHandler(insnList, newInsns, lineNumberInsn);
		newInsns.dispose();
	}

	protected LocalVariableGen injectCodeInitTracer(MethodGen methodGen, ConstantPoolGen constPool, int methodStartLine,
			int methodEndLine, boolean isAppClass, LocalVariableGen classNameVar, LocalVariableGen methodSigVar,
			boolean startTracing, LocalVariableGen tracerVar) {
		InstructionList insnList = methodGen.getInstructionList();
		InstructionHandle startInsn = insnList.getStart();
		if (startInsn == null) {
			return null;
		}
		
		InstructionList newInsns = new InstructionList();
		if (startTracing) {
			appendTracerMethodInvoke(newInsns, TracerMethods.START, constPool);
		}
		/* store classNameVar */
		String className = methodGen.getClassName();
		className = className.replace("/", ".");
		newInsns.append(new PUSH(constPool, className));
		newInsns.append(new ASTORE(classNameVar.getIndex()));
		
		/* store methodSignVar */
		String sig = methodGen.getSignature();
		String methodName = methodGen.getName();
		String mSig = className + "#" + methodName + sig;
		newInsns.append(new PUSH(constPool, mSig));
		newInsns.append(new ASTORE(methodSigVar.getIndex())); 
		
		/* invoke _getTracer()  */
		newInsns.append(new PUSH(constPool, isAppClass)); // startTracing
		newInsns.append(new ALOAD(classNameVar.getIndex())); // startTracing, className
		newInsns.append(new ALOAD(methodSigVar.getIndex())); // startTracing, className, String methodSig
		newInsns.append(new PUSH(constPool, methodStartLine));	// startTracing, className, String methodSig, int methodStartLine	
		newInsns.append(new PUSH(constPool, methodEndLine)); // startTracing, className, String methodSig, int methodStartLine, methodEndLine
		
		String[] argList = getArgumentNames(methodGen);
		newInsns.append(new PUSH(constPool, TraceUtils.encodeArgNames(argList)));
		newInsns.append(new PUSH(constPool, TraceUtils.encodeArgTypes(methodGen.getArgumentTypes())));
		// startTracing, className, String methodSig, int methodStartLine, argTypes
		
		LocalVariableGen argObjsVar = createMethodParamTypesObjectArrayVar(methodGen, constPool, startInsn, newInsns, nextTempVarName());
		newInsns.append(new ALOAD(argObjsVar.getIndex()));
		// className, String methodSig, int methodStartLine, methodEndLine, argNames, argTypes, argObjs
		
		appendTracerMethodInvoke(newInsns, TracerMethods.GET_TRACER, constPool);
		InstructionHandle tracerStartPos = newInsns.append(new ASTORE(tracerVar.getIndex()));
		tracerVar.setStart(tracerStartPos);
		
		//insertInsnHandler(insnList, newInsns, startInsn);
		insnList.insert(startInsn, newInsns);
		newInsns.dispose();
		return tracerVar;
	}

	private String nextTempVarName() {
		return TEMP_VAR_NAME + (++tempVarIdx);
	}

	
}
