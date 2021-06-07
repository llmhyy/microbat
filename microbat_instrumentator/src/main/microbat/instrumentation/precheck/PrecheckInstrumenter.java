package microbat.instrumentation.precheck;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;

import microbat.instrumentation.AgentLogger;
import microbat.instrumentation.AgentParams;
import microbat.instrumentation.instr.TraceInstrumenter;
import microbat.instrumentation.instr.instruction.info.LineInstructionInfo;
import microbat.instrumentation.utils.MicrobatUtils;

public class PrecheckInstrumenter extends TraceInstrumenter {
	private static final String MEASUREMENT_VAR_NAME = "$traceMs";
	private List<String> exceedLimitMethods = new ArrayList<>();
	
	public PrecheckInstrumenter(AgentParams params) {
		super(params);
	}

	private List<Method> instrumentedMethods = new ArrayList<>();
	@Override
	protected byte[] instrument(String classFName, String className, JavaClass jc) {
		instrumentedMethods.clear();
		byte[] data = super.instrument(classFName, className, jc);
		
		if (data != null) {
			calculateTraceInstrumentation(jc , classFName, new ArrayList<>(instrumentedMethods));
		}
		
		return data;
	}

	private void calculateTraceInstrumentation(JavaClass jc, String classFName, List<Method> methods) {
		ClassGen classGen = new ClassGen(jc);
		ConstantPoolGen constPool = classGen.getConstantPool();
		for (Method method : methods) {
			String classMethod = MicrobatUtils.getMicrobatMethodFullName(classGen.getClassName(), method);
			boolean changed = false;
			MethodGen methodGen = new MethodGen(method, classFName, constPool);
			try {
				changed = super.instrumentMethod(classGen, constPool, methodGen, method, true, false, false);
				methodGen.getMethod().toString(); // exception if exceeding limit.
				if (changed && doesBytecodeExceedLimit(methodGen)) {
					exceedLimitMethods.add(classMethod);
				}
			} catch (Exception e) {
				String message = e.getMessage();
				if (e.getMessage() != null && e.getMessage().contains("offset too large")) {
					message = "offset too large";
					exceedLimitMethods.add(classMethod);
					AgentLogger.info(String.format("Warning: %s [%s] - instrumentated bytecode exceeds limit", classMethod, message));
				} else {
					AgentLogger.info(String.format("Warning: %s [%s]", classMethod, message));
					AgentLogger.error(e);
				}
			}
		}
	}
	
	protected boolean instrumentMethod(ClassGen classGen, ConstantPoolGen constPool, MethodGen methodGen, Method method,
			boolean isAppClass, boolean isMainMethod, boolean isEntry) {
		InstructionList insnList = methodGen.getInstructionList();

		Set<Integer> visitedLines = new HashSet<>();
		List<LineNumberGen> lineInsnInfos = new ArrayList<>();
		for (LineNumberGen lineGen : methodGen.getLineNumbers()) {
			if (!visitedLines.contains(lineGen.getSourceLine())) {
				lineInsnInfos.add(lineGen);
				visitedLines.add(lineGen.getSourceLine());
			}
		}
		InstructionHandle startInsn = insnList.getStart();
		if (startInsn == null || visitedLines.isEmpty()) {
			// empty method
			return false;
		}
		
		LocalVariableGen classNameVar = createLocalVariable(CLASS_NAME, methodGen, constPool);
		LocalVariableGen methodSigVar = createLocalVariable(METHOD_SIGNATURE, methodGen, constPool);
		LocalVariableGen tracerVar = methodGen.addLocalVariable(MEASUREMENT_VAR_NAME,
				Type.getType(TraceMeasurement.class), insnList.getStart(), insnList.getEnd());
		int startLine = Integer.MAX_VALUE;
		for (LineNumberGen lineInfo : lineInsnInfos) {
			injectCodeTracerHitLine(insnList, constPool, tracerVar, lineInfo.getSourceLine(),
						lineInfo.getInstruction(), classNameVar, methodSigVar);
			for (InstructionHandle insn : LineInstructionInfo.getInstrInstructions(insnList,
					method.getLineNumberTable(), lineInfo.getSourceLine())) {
				injectCodeTracerHitLine(insnList, constPool, tracerVar, lineInfo.getSourceLine(), insn, classNameVar,
						methodSigVar);
				if (insn.getInstruction() instanceof InvokeInstruction) {
					InstructionList newInsns = getHitLineCode(constPool, tracerVar, lineInfo.getSourceLine(), classNameVar, methodSigVar);
					appendInstruction(insnList, newInsns, insn);
					newInsns.dispose();
				}
			}
			if (lineInfo.getSourceLine() < startLine) {
				startLine = lineInfo.getSourceLine();
			}
		}
		injectCodeInitMeasurement(methodGen, constPool, classNameVar, methodSigVar, tracerVar, startLine, isMainMethod);
		instrumentedMethods.add(method);
		return true;
	}

	private void injectCodeTracerHitLine(InstructionList insnList, ConstantPoolGen constPool,
			LocalVariableGen tracerVar, int sourceLine, InstructionHandle lineNumberInsn, LocalVariableGen classNameVar,
			LocalVariableGen methodSigVar) {
		InstructionList newInsns = getHitLineCode(constPool, tracerVar, sourceLine, classNameVar, methodSigVar);
		insertInsnHandler(insnList, newInsns, lineNumberInsn);
		newInsns.dispose();
	}

	private InstructionList getHitLineCode(ConstantPoolGen constPool, LocalVariableGen tracerVar, int sourceLine,
			LocalVariableGen classNameVar, LocalVariableGen methodSigVar) {
		InstructionList newInsns = new InstructionList();
		newInsns.append(new ALOAD(tracerVar.getIndex()));
		newInsns.append(new PUSH(constPool, sourceLine));
		newInsns.append(new ALOAD(classNameVar.getIndex()));
		newInsns.append(new ALOAD(methodSigVar.getIndex()));
		appendTracerMethodInvoke(newInsns, MeasurementMethods.HIT_LINE, constPool, false);
		return newInsns;
	}

	private LocalVariableGen injectCodeInitMeasurement(MethodGen methodGen, ConstantPoolGen constPool,
			LocalVariableGen classNameVar, LocalVariableGen methodSigVar, LocalVariableGen tracerVar, int startLine,
			boolean startTracing) {
		InstructionList insnList = methodGen.getInstructionList();
		InstructionHandle startInsn = insnList.getStart();
		if (startInsn == null) {
			return null;
		}
		
		InstructionList newInsns = new InstructionList();
		if (startTracing) {
			appendTracerMethodInvoke(newInsns, MeasurementMethods.START, constPool, true);
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
		newInsns.append(new ALOAD(classNameVar.getIndex())); // startTracing, className
		newInsns.append(new ALOAD(methodSigVar.getIndex())); // startTracing, className, String methodSig
		newInsns.append(new PUSH(constPool, startLine));	
		appendTracerMethodInvoke(newInsns, MeasurementMethods.GET_TRACER, constPool, true);
		InstructionHandle tracerStartPos = newInsns.append(new ASTORE(tracerVar.getIndex()));
		tracerVar.setStart(tracerStartPos);
		insertInsnHandler(insnList, newInsns, startInsn);
		newInsns.dispose();
		return tracerVar;
	}
	
	private void appendTracerMethodInvoke(InstructionList newInsns, MeasurementMethods method,
			ConstantPoolGen constPool, boolean isStatic) {
		if (isStatic) {
			int index = constPool.addMethodref(method.getDeclareClass(), method.getMethodName(),
					method.getMethodSign());
			newInsns.append(new INVOKESTATIC(index));

		} else {
			int index = constPool.addInterfaceMethodref(method.getDeclareClass(), method.getMethodName(),
					method.getMethodSign());
			newInsns.append(new INVOKEVIRTUAL(index));
		}
	}
	
	public List<String> getExceedLimitMethods() {
		return exceedLimitMethods;
	}
	
	private enum MeasurementMethods {
		GET_TRACER("microbat/instrumentation/precheck/TraceMeasurement", "_getTracer", "(Ljava/lang/String;Ljava/lang/String;I)Lmicrobat/instrumentation/precheck/TraceMeasurement;"),
		HIT_LINE("microbat/instrumentation/precheck/TraceMeasurement", "_hitLine", "(ILjava/lang/String;Ljava/lang/String;)V"),
		START("microbat/instrumentation/precheck/TraceMeasurement", "_start", "()V")

	
		;
		private String declareClass;
		private String methodName;
		private String methodSign;

		private MeasurementMethods(String declareClass, String methodName, String methodSign) {
			this.declareClass = declareClass;
			this.methodName = methodName;
			this.methodSign = methodSign;
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
	}
}
