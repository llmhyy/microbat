package microbat.instrumentation.cfgcoverage.instr;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.DLOAD;
import org.apache.bcel.generic.DSTORE;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.DUP2;
import org.apache.bcel.generic.DUP_X1;
import org.apache.bcel.generic.DUP_X2;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LLOAD;
import org.apache.bcel.generic.LSTORE;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.POP;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;

import microbat.instrumentation.AgentLogger;
import microbat.instrumentation.cfgcoverage.CoverageAgentParams;
import microbat.instrumentation.cfgcoverage.CoverageAgentParams.CoverageCollectionType;
import microbat.instrumentation.cfgcoverage.InstrumentationUtils;
import microbat.instrumentation.filter.GlobalFilterChecker;
import microbat.instrumentation.instr.AbstractInstrumenter;
import microbat.instrumentation.runtime.TraceUtils;
import microbat.instrumentation.utils.MicrobatUtils;
import sav.common.core.SavRtException;

public class CoverageInstrumenter extends AbstractInstrumenter {
	private static final String TRACER_VAR_NAME = "$tracer";
	private static final String METHOD_ID_VAR_NAME = "$methodId";
	private static final String TEMP_VAR_NAME = "$tempVar"; // local var
	private int tempVarIdx = 0;
	private CoverageAgentParams agentParams;
	private String entryPoint; // methodId

	public CoverageInstrumenter(CoverageAgentParams agentParams) {
		this.agentParams = agentParams;
	}
	
	@Override
	protected byte[] instrument(String classFName, String className, JavaClass jc) {
		if (!MethodInstructionsInfo.hasClassInInstrumentationList(className)) {
			return null;
		}
		
		boolean isAppClass = GlobalFilterChecker.isAppClass(classFName);
		ClassGen classGen = new ClassGen(jc);
		ConstantPoolGen constPool = classGen.getConstantPool();
		JavaClass newJC = null;
		
		for (Method method : jc.getMethods()) {
			if (method.isNative() || method.isAbstract() || method.getCode() == null) {
				continue; // Only instrument methods with code in them!
			}
			try {
				MethodGen methodGen = new MethodGen(method, classFName, constPool);
				boolean change = instrumentMethod(classGen, constPool, methodGen, method, isAppClass);
				if (change) {
					if (doesBytecodeExceedLimit(methodGen)) {
						AgentLogger.info(String.format("Warning: %s exceeds bytecode limit!",
								MicrobatUtils.getMicrobatMethodFullName(classGen.getClassName(), method)));
					} else {
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

	protected boolean instrumentMethod(ClassGen classGen, ConstantPoolGen constPool, MethodGen methodGen, Method method,
			boolean isAppClass) {
		InstructionList insnList = methodGen.getInstructionList();
		InstructionHandle startInsn = insnList.getStart();
		if (startInsn == null) {
			// empty method
			return false;
		}
		MethodInstructionsInfo instmInsns = MethodInstructionsInfo.getInstrumentationInstructions(insnList, method,
				classGen.getClassName());
		
		if (instmInsns == null) {
			return false;
		}
		
		tempVarIdx = 0;
		String methodId = InstrumentationUtils.getMethodId(classGen.getClassName(), method);
		LocalVariableGen methodIdVar = createLocalVariable(METHOD_ID_VAR_NAME, methodGen, constPool);
		LocalVariableGen tracerVar = createLocalVariable(TRACER_VAR_NAME, methodGen, constPool);
		for (InstructionInfo instnInfo : instmInsns.getNodeInsns()) {
			injectCodeTracerReachNode(methodIdVar, instnInfo, tracerVar, constPool, insnList);
		}
		if (agentParams.collectConditionVariation()) {
			for (InstructionInfo cmpInsnInfo : instmInsns.getNotIntCmpInIfInsns()) {
				injectCodeTracerOnNotIntCmp(methodIdVar, tracerVar, cmpInsnInfo, constPool, insnList, methodGen);
			}
			for (InstructionInfo condInsnInfo : instmInsns.getConditionInsns()) {
				injectCodeTracerOnIf(methodIdVar, tracerVar, condInsnInfo, constPool, insnList);
			}
		}
		for (InstructionHandle exitInsn : instmInsns.getExitInsns()) {
			injectCodeTracerExitMethod(methodIdVar, tracerVar, constPool, insnList, exitInsn, methodId);
		}
		injectCodeInitTracer(methodGen, constPool, tracerVar, methodIdVar, methodId);
		return true;
	}
	
	private void injectCodeTracerOnNotIntCmp(LocalVariableGen methodIdVar, LocalVariableGen tracerVar, InstructionInfo instnInfo,
			ConstantPoolGen constPool, InstructionList insnList, MethodGen methodGen) {
		InstructionList newInsns = new InstructionList();
		short opcode = instnInfo.getInsnHandler().getInstruction().getOpcode();
		switch (opcode) {
		case Const.DCMPG:
		case Const.DCMPL:
			LocalVariableGen d1Var = methodGen.addLocalVariable(nextTempVarName(), new ArrayType(Type.DOUBLE, 1),
					instnInfo.getInsnHandler().getPrev(), instnInfo.getInsnHandler().getNext());
			LocalVariableGen d2Var = methodGen.addLocalVariable(nextTempVarName(), new ArrayType(Type.DOUBLE, 1),
					instnInfo.getInsnHandler().getPrev(), instnInfo.getInsnHandler().getNext());
			newInsns.append(new DSTORE(d2Var.getIndex()));
			newInsns.append(new DSTORE(d1Var.getIndex()));
			newInsns.append(new ALOAD(tracerVar.getIndex()));
			newInsns.append(new DLOAD(d1Var.getIndex()));
			newInsns.append(new DLOAD(d2Var.getIndex()));
			appendTracerMethodInvoke(newInsns, CoverageTracerMethods.ON_DCMP, constPool);
			newInsns.append(new DLOAD(d1Var.getIndex()));
			newInsns.append(new DLOAD(d2Var.getIndex()));
			insertInsnHandler(insnList, newInsns, instnInfo.getInsnHandler());
			newInsns.dispose();
			break;
		case Const.LCMP:
			LocalVariableGen l1Var = methodGen.addLocalVariable(nextTempVarName(), new ArrayType(Type.LONG, 1),
					instnInfo.getInsnHandler().getPrev(), instnInfo.getInsnHandler().getNext());
			LocalVariableGen l2Var = methodGen.addLocalVariable(nextTempVarName(), new ArrayType(Type.LONG, 1),
					instnInfo.getInsnHandler().getPrev(), instnInfo.getInsnHandler().getNext());
			newInsns.append(new LSTORE(l2Var.getIndex()));
			newInsns.append(new LSTORE(l1Var.getIndex()));
			newInsns.append(new ALOAD(tracerVar.getIndex()));
			newInsns.append(new LLOAD(l1Var.getIndex()));
			newInsns.append(new LLOAD(l2Var.getIndex()));
			appendTracerMethodInvoke(newInsns, CoverageTracerMethods.ON_LCMP, constPool);
			newInsns.append(new LLOAD(l1Var.getIndex()));
			newInsns.append(new LLOAD(l2Var.getIndex()));
			insertInsnHandler(insnList, newInsns, instnInfo.getInsnHandler());
			newInsns.dispose();
			break;
		case Const.FCMPG:
		case Const.FCMPL:
			/* duplicate */
			newInsns.append(new DUP2()); // value1, value2, value1, value2
			newInsns.append(new ALOAD(tracerVar.getIndex())); // value1, value2, value1, value2, $tracer
			newInsns.append(new DUP_X2()); // value1, value2, $tracer, value1, value2, $tracer
			newInsns.append(new POP()); // value1, value2, $tracer, value1, value2
			appendTracerMethodInvoke(newInsns, CoverageTracerMethods.ON_FCMP, constPool); // value1, value2
			insertInsnHandler(insnList, newInsns, instnInfo.getInsnHandler());
			newInsns.dispose();
			break;
		}
	}
	
	private void injectCodeTracerOnIf(LocalVariableGen methodIdVar, LocalVariableGen tracerVar,
			InstructionInfo instnInfo, ConstantPoolGen constPool, InstructionList insnList) {
		InstructionList newInsns = new InstructionList();
		CoverageTracerMethods tracerMethod = null;
		short opcode = instnInfo.getInsnHandler().getInstruction().getOpcode();
		switch (opcode) {
		case Const.IF_ACMPEQ:
		case Const.IF_ACMPNE:
			tracerMethod = CoverageTracerMethods.ON_IF_A_CMP;
			/* value1, value2 */
			newInsns.append(new DUP2());
			newInsns.append(new ALOAD(tracerVar.getIndex())); // value1, value2, $tracer
			newInsns.append(new DUP_X2()); // $tracer, value1, value2, $tracer
			newInsns.append(new POP()); // $tracer, value1, value2
			/**/
			break;
		case Const.IF_ICMPEQ:
		case Const.IF_ICMPNE:
		case Const.IF_ICMPLT:
		case Const.IF_ICMPGE:
		case Const.IF_ICMPGT:
		case Const.IF_ICMPLE:
			tracerMethod = CoverageTracerMethods.ON_IF_I_CMP;
			/* value1, value2 */
			newInsns.append(new DUP2());
			newInsns.append(new ALOAD(tracerVar.getIndex())); // value1, value2, $tracer
			newInsns.append(new DUP_X2()); // $tracer, value1, value2, $tracer
			newInsns.append(new POP()); // $tracer, value1, value2
			/**/
			break;
		case Const.IFEQ:
		case Const.IFNE:
		case Const.IFLT:
		case Const.IFGE:
		case Const.IFGT:
		case Const.IFLE:
			tracerMethod = CoverageTracerMethods.ON_IF;
			/* value */
			newInsns.append(new DUP());
			newInsns.append(new ALOAD(tracerVar.getIndex())); // value, $tracer
			newInsns.append(new DUP_X1()); // $tracer, value, $tracer
			newInsns.append(new POP()); // $tracer, value
			newInsns.append(new PUSH(constPool, instnInfo.isNotIntCmpIf()));
			/**/
			break;
		case Const.IFNONNULL:
		case Const.IFNULL:
			tracerMethod = CoverageTracerMethods.ON_IF_NULL;
			/* value */
			newInsns.append(new DUP());
			newInsns.append(new ALOAD(tracerVar.getIndex())); // value, $tracer
			newInsns.append(new DUP_X1()); // $tracer, value, $tracer
			newInsns.append(new POP()); // $tracer, value
			/**/
			break;
		default:
			throw new SavRtException("Missing the case: " + opcode);
		}
		newInsns.append(new ALOAD(methodIdVar.getIndex())); // $tracer, [value..], methodId
		newInsns.append(new PUSH(constPool, instnInfo.getInsnIdx())); // $tracer, [value..], methodId, nodeIdx
		appendTracerMethodInvoke(newInsns, tracerMethod, constPool);
		insertInsnHandler(insnList, newInsns, instnInfo.getInsnHandler());
		newInsns.dispose();
	}

	private void injectCodeTracerReachNode(LocalVariableGen methodIdVar, InstructionInfo instnInfo, LocalVariableGen tracerVar,
			ConstantPoolGen constPool, InstructionList insnList) {
		CoverageTracerMethods method = CoverageTracerMethods.REACH_NODE;
		InstructionList newInsns = new InstructionList();
		newInsns.append(new ALOAD(tracerVar.getIndex()));
		newInsns.append(new ALOAD(methodIdVar.getIndex()));
		newInsns.append(new PUSH(constPool, instnInfo.getInsnIdx()));
		appendTracerMethodInvoke(newInsns, method, constPool);
		insertInsnHandler(insnList, newInsns, instnInfo.getInsnHandler());
		newInsns.dispose();
	}

	private LocalVariableGen injectCodeInitTracer(MethodGen methodGen, ConstantPoolGen constPool,
			LocalVariableGen tracerVar, LocalVariableGen methodIdVar, String methodId) {
		InstructionList insnList = methodGen.getInstructionList();
		InstructionHandle startInsn = insnList.getStart();
		if (startInsn == null) {
			return null;
		}
		boolean isEntryPoint = this.entryPoint.equals(methodId);
		InstructionList newInsns = new InstructionList();
		/* store methodId */
		newInsns.append(new PUSH(constPool, methodId));
		newInsns.append(new ASTORE(methodIdVar.getIndex()));
		
		if (agentParams.getCoverageType() == CoverageCollectionType.BRANCH_COVERAGE) {
			newInsns.append(new ALOAD(methodIdVar.getIndex()));// methodId
			appendTracerMethodInvoke(newInsns, CoverageTracerMethods.BRANCH_COVERAGE_GET_TRACER, constPool);
		} else {
			/* invoke _getTracer */
			newInsns.append(new ALOAD(methodIdVar.getIndex()));// methodId
			newInsns.append(new PUSH(constPool, isEntryPoint)); // isEntryPoint
			newInsns.append(new PUSH(constPool, TraceUtils.encodeArgNames(getArgumentNames(methodGen)))); // paramNamesCode
			newInsns.append(new PUSH(constPool, TraceUtils.encodeArgTypes(methodGen.getArgumentTypes()))); // paramTypeSignsCode
			
			LocalVariableGen argObjsVar = createMethodParamTypesObjectArrayVar(methodGen, constPool, startInsn, newInsns, nextTempVarName()); 
			newInsns.append(new ALOAD(argObjsVar.getIndex())); // params
			newInsns.append(new ALOAD(0));
			appendTracerMethodInvoke(newInsns, CoverageTracerMethods.GET_TRACER, constPool);
		}
		InstructionHandle tracerStartPos = newInsns.append(new ASTORE(tracerVar.getIndex()));
		tracerVar.setStart(tracerStartPos);
		
		insnList.insert(startInsn, newInsns);
		newInsns.dispose();
		return tracerVar;
	}
	
	private void injectCodeTracerExitMethod(LocalVariableGen methodIdVar, LocalVariableGen tracerVar,
			ConstantPoolGen constPool, InstructionList insnList, InstructionHandle exitInsn, String methodId) {
		boolean isEntryPoint = this.entryPoint.equals(methodId);
		InstructionList newInsns = new InstructionList();
		newInsns.append(new ALOAD(tracerVar.getIndex()));
		newInsns.append(new ALOAD(methodIdVar.getIndex()));
		newInsns.append(new PUSH(constPool, isEntryPoint));
		appendTracerMethodInvoke(newInsns, CoverageTracerMethods.EXIT_METHOD, constPool);
		
		insertInsnHandler(insnList, newInsns, exitInsn);
		insnList.toString();
		newInsns.dispose();
	}
	
	protected void appendTracerMethodInvoke(InstructionList newInsns, CoverageTracerMethods method, ConstantPoolGen constPool) {
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

	private String nextTempVarName() {
		return TEMP_VAR_NAME + (++tempVarIdx);
	}
	
	public void setEntryPoint(String entryPoint) {
		this.entryPoint = entryPoint;
	}

	@Override
	protected boolean instrumentMethod(ClassGen classGen, ConstantPoolGen constPool, MethodGen methodGen, Method method,
			boolean isAppClass, boolean isMainMethod, boolean isEntry) {
		// TODO Auto-generated method stub
		return false;
	}
}
