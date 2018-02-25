package microbat.instrumentation.instr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
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
import org.apache.bcel.generic.CodeExceptionGen;
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
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.POP;
import org.apache.bcel.generic.POP2;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.RETURN;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.SWAP;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.generic.Type;

import microbat.codeanalysis.bytecode.CFG;
import microbat.codeanalysis.bytecode.CFGConstructor;
import microbat.instrumentation.AgentConstants;
import microbat.instrumentation.AgentParams;
import microbat.instrumentation.filter.FilterChecker;
import microbat.instrumentation.instr.instruction.info.ArrayInstructionInfo;
import microbat.instrumentation.instr.instruction.info.EntryPoint;
import microbat.instrumentation.instr.instruction.info.FieldInstructionInfo;
import microbat.instrumentation.instr.instruction.info.LineInstructionInfo;
import microbat.instrumentation.instr.instruction.info.LocalVarInstructionInfo;
import microbat.instrumentation.instr.instruction.info.RWInstructionInfo;
import microbat.instrumentation.instr.instruction.info.UnknownLineInstructionInfo;
import microbat.instrumentation.runtime.IExecutionTracer;
import microbat.instrumentation.runtime.TraceUtils;
import sav.common.core.utils.StringUtils;

public class TraceInstrumenter extends AbstraceInstrumenter {
	private static final String TRACER_VAR_NAME = "$tracer"; // local var
	private static final String TEMP_VAR_NAME = "$tempVar"; // local var
	
	private BasicTypeSupporter basicTypeSupporter = new BasicTypeSupporter();
	private int tempVarIdx = 0;
	private EntryPoint entryPoint;

	public TraceInstrumenter(AgentParams params) {
		this.entryPoint = params.getEntryPoint();
	}

	public byte[] instrument(String classFName, byte[] classfileBuffer) throws Exception {
		String className = classFName.replace("/", ".");
		ClassParser cp = new ClassParser(new java.io.ByteArrayInputStream(classfileBuffer), classFName);
		JavaClass jc = cp.parse();
		// First, make sure we have to instrument this class:
		if (!jc.isClass()) {
			// could be an interface
			return null;
		}
		ClassGen classGen = new ClassGen(jc);
		ConstantPoolGen constPool = classGen.getConstantPool();
		JavaClass newJC = null;
		boolean entry = entryPoint == null ? false : className.equals(entryPoint.getClassName());
		boolean isAppClass = FilterChecker.isAppClass(classFName) || entry;
		for (Method method : jc.getMethods()) {
			if (method.isNative() || method.isAbstract() || method.getCode() == null) {
				continue; // Only instrument methods with code in them!
			}
			
			try {
				boolean changed = false;
				MethodGen methodGen = new MethodGen(method, classFName, constPool);
				boolean startTracing = false;
				if (entry && entryPoint.matchMethod(method.getName(), method.getSignature())) {
					startTracing = true;
				}
				changed = instrumentMethod(classGen, constPool, methodGen, method, isAppClass, startTracing);
				if (changed) {
					// All changes made, so finish off the method:
					InstructionList instructionList = methodGen.getInstructionList();
					instructionList.setPositions();
					methodGen.setMaxStack();
					methodGen.setMaxLocals();
					classGen.replaceMethod(method, methodGen.getMethod());
				}
				newJC = classGen.getJavaClass();
				newJC.setConstantPool(constPool.getFinalConstantPool());
			} catch (Exception e) {
				System.err.println(String.format("Error when run instrumentation: %s.%s", classFName, method.getName()));
				e.printStackTrace();
			}
		}
		if (newJC != null) {
			byte[] data = newJC.getBytes();
			return data;
		}
		return null;
	}

	private void dumpClass(JavaClass jc) {
		ClassGen cg = new ClassGen(jc);
		ConstantPoolGen cpg = cg.getConstantPool();
		System.out.println(jc.toString());
		for (Method meth : jc.getMethods()) {
			MethodGen mg = new MethodGen(meth, jc.getClassName(), cpg);
			System.out.println(mg.getName());
			System.out.println("==========================================");
			for (InstructionHandle ih : mg.getInstructionList()) {
				System.out.println(ih);
			}
		}
	}

	private boolean instrumentMethod(ClassGen classGen, ConstantPoolGen constPool, MethodGen methodGen, Method method,
			boolean isAppClass, boolean startTracing) {
		tempVarIdx = 0;
		InstructionList insnList = methodGen.getInstructionList();
		List<LineInstructionInfo> lineInsnInfos = new ArrayList<>();

		CFGConstructor cfgConstructor = new CFGConstructor();
		CFG cfg = cfgConstructor.constructCFG(method.getCode());

		LineNumberTable lineNumberTable = method.getLineNumberTable();// methodGen.getLineNumberTable(constPool);
		Set<Integer> visitedLines = new HashSet<>();
		for (LineNumberGen lineGen : methodGen.getLineNumbers()) {
			if (!visitedLines.contains(lineGen.getSourceLine())) {
				String loc = StringUtils.dotJoin(classGen.getClassName(), method.getName(), lineGen.getSourceLine());
				lineInsnInfos.add(new LineInstructionInfo(loc, method.getLocalVariableTable(), constPool,
						lineNumberTable, lineGen, insnList, cfg, isAppClass));
				visitedLines.add(lineGen.getSourceLine());
			}
		}
		InstructionHandle startInsn = insnList.getStart();
		if (startInsn == null) {
			// empty method
			return false;
		}
		/* class does not include line number */
		if (visitedLines.isEmpty()) {
			String loc = classGen.getClassName() + "." + method.getName();
			lineInsnInfos.add(new UnknownLineInstructionInfo(loc, constPool, insnList, isAppClass));
		}
		int startLine = Integer.MAX_VALUE;
		int endLine = AgentConstants.UNKNOWN_LINE;
		for (int line : visitedLines) {
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

		for (LineInstructionInfo lineInfo : lineInsnInfos) {
			/* instrument RW instructions */
			List<RWInstructionInfo> rwInsns = lineInfo.getRWInstructions();
//			if (lineInfo.hasNoInstrumentation()) {
				injectCodeTracerHitLine(insnList, constPool, tracerVar, lineInfo.getLine(),
						lineInfo.getLineNumberInsn(), classNameVar, methodSigVar);
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
							updateExceptionTable(insnHandler, insnHandler.getPrev(), insnHandler.getNext());
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
				injectCodeTracerExit(exitInsHandle, insnList, constPool, tracerVar, line, classNameVar, methodSigVar);
			}

			lineInfo.dispose();
		}
		injectCodeInitTracer(methodGen, constPool, startLine, endLine, isAppClass, classNameVar,
				methodSigVar, startTracing, tracerVar);
		return true;
	}

	private void appendInstruction(InstructionList insnList, InstructionHandle insnHandler, InstructionList newInsns) {
		updateExceptionTable(insnHandler, insnHandler, newInsns.getEnd());
		insnList.append(insnHandler, newInsns);
	}

	private void injectCodeTracerExit(InstructionHandle exitInsHandle, InstructionList insnList, 
			ConstantPoolGen constPool, LocalVariableGen tracerVar, int line, LocalVariableGen classNameVar, LocalVariableGen methodSigVar) {
		InstructionList newInsns = new InstructionList();
		
		newInsns.append(new ALOAD(tracerVar.getIndex()));
		newInsns.append(new PUSH(constPool, line));
		newInsns.append(new ALOAD(classNameVar.getIndex()));
		newInsns.append(new ALOAD(methodSigVar.getIndex()));
		appendTracerMethodInvoke(newInsns, TracerMethods.HIT_METHOD_END, constPool);
		
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
		/* on stack: objectRef, arg1(*), arg2(*), ... */
		ReferenceType returnType = insn.getReferenceType(constPool);
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
			newInsns.append(new ALOAD(tracerVar.getIndex())); // tracer
		}
		newInsns.append(new PUSH(constPool, className)); 
		// ([objectRef], objectRef), invokeType

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
		/* after */
		if (isAppClass) {
			newInsns = new InstructionList();
			newInsns.append(new ALOAD(tracerVar.getIndex()));
			newInsns.append(new PUSH(constPool, line));
			newInsns.append(new ALOAD(classNameVar.getIndex()));
			newInsns.append(new ALOAD(methodSigVar.getIndex()));
			appendTracerMethodInvoke(newInsns, TracerMethods.AFTER_INVOKE, constPool);
			appendInstruction(insnList, insnHandler, newInsns);
			newInsns.dispose();
		}
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

	private void appendTracerMethodInvoke(InstructionList newInsns, TracerMethods method, ConstantPoolGen constPool) {
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
		if (insn.getIndex() == 0) {
			return null;
		}
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
		if (insnInfo.getInstructionHandler().getPrev().getInstruction() instanceof ACONST_NULL) {
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
		return methodGen.addLocalVariable(TEMP_VAR_NAME + (++tempVarIdx), type, insnHandler, insnHandler.getNext());
	}

	private void injectCodeTracerHitLine(InstructionList insnList, ConstantPoolGen constPool,
			LocalVariableGen tracerVar, int line, InstructionHandle lineNumberInsn, LocalVariableGen classNameVar, LocalVariableGen methodSigVar) {
		InstructionList newInsns = new InstructionList();
		newInsns.append(new ALOAD(tracerVar.getIndex()));
		newInsns.append(new PUSH(constPool, line));
		newInsns.append(new ALOAD(classNameVar.getIndex()));
		newInsns.append(new ALOAD(methodSigVar.getIndex()));
		appendTracerMethodInvoke(newInsns, TracerMethods.HIT_LINE, constPool);
		insertInsnHandler(insnList, newInsns, lineNumberInsn);
		newInsns.dispose();
	}

	private LocalVariableGen injectCodeInitTracer(MethodGen methodGen, ConstantPoolGen constPool, int methodStartLine,
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
		
		String methodString = methodGen.getMethod().toString();
		String args = methodString.substring(methodString.indexOf("(")+1, methodString.indexOf(")"));
		String[] argList = args.split(",");
		for(int i=0; i<argList.length; i++){
			argList[i] = argList[i].trim();
			argList[i] = argList[i].substring(argList[i].indexOf(" ")+1, argList[i].length());
		}
		newInsns.append(new PUSH(constPool, TraceUtils.encodeArgNames(argList)));
		newInsns.append(new PUSH(constPool, TraceUtils.encodeArgTypes(methodGen.getArgumentTypes())));
		// startTracing, className, String methodSig, int methodStartLine, argTypes
		
		/* init Object[] */
		LocalVariableGen argObjsVar = addTempVar(methodGen, new ArrayType(Type.OBJECT, 1), startInsn);
		newInsns.append(new PUSH(constPool, methodGen.getArgumentTypes().length));
		newInsns.append(new ANEWARRAY(constPool.addClass(Object.class.getName())));
		argObjsVar.setStart(newInsns.append(new ASTORE(argObjsVar.getIndex())));
		/* assign method argument values to Object[] */
		LocalVariableTable localVariableTable = methodGen.getLocalVariableTable(constPool);
		if (localVariableTable != null) {
			int varIdx = (Const.ACC_STATIC & methodGen.getAccessFlags()) != 0 ? 0 : 1;
			for (int i = 0; i < methodGen.getArgumentTypes().length; i++) {
				LocalVariable localVariable = localVariableTable.getLocalVariable(varIdx, 0);
				if (localVariable == null) {
					System.out.println("Warning: localVariable is empty, varIdx=" + varIdx);
					break;
				}
				newInsns.append(new ALOAD(argObjsVar.getIndex()));
				newInsns.append(new PUSH(constPool, i));
				Type argType = methodGen.getArgumentType(i);
				newInsns.append(InstructionFactory.createLoad(argType, localVariable.getIndex()));
				if (argType instanceof BasicType) {
					newInsns.append(
							new INVOKESTATIC(basicTypeSupporter.getValueOfMethodIdx((BasicType) argType, constPool)));
				}
				newInsns.append(new AASTORE());
				if (Type.DOUBLE.equals(argType) || Type.LONG.equals(argType)) {
					varIdx += 2;
				} else {
					varIdx ++;
				}
			}
		} else {
			System.out.println("Warning: localVariableTable is empty!");
		}
		newInsns.append(new ALOAD(argObjsVar.getIndex()));
		// className, String methodSig, int methodStartLine, methodEndLine, argNames, argTypes, argObjs
		
		appendTracerMethodInvoke(newInsns, TracerMethods.GET_TRACER, constPool);
		InstructionHandle tracerStartPos = newInsns.append(new ASTORE(tracerVar.getIndex()));
		tracerVar.setStart(tracerStartPos);
		insertInsnHandler(insnList, newInsns, startInsn);
		newInsns.dispose();
		return tracerVar;
	}

	public static void updateTargeters(InstructionHandle oldPos, InstructionHandle newPos) {
		InstructionTargeter[] itList = oldPos.getTargeters();
		if (itList != null) {
			for (InstructionTargeter it : itList) {
				if (!(it instanceof CodeExceptionGen)) {
					it.updateTarget(oldPos, newPos);
				}
			}
		}
	}

}
