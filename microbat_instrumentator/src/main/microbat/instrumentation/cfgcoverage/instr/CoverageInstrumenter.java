package microbat.instrumentation.cfgcoverage.instr;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;

import microbat.instrumentation.AgentLogger;
import microbat.instrumentation.ClassGenUtils;
import microbat.instrumentation.cfgcoverage.CoverageAgentParams;
import microbat.instrumentation.filter.FilterChecker;
import microbat.instrumentation.instr.AbstractInstrumenter;

public class CoverageInstrumenter extends AbstractInstrumenter {
	private CoverageAgentParams agentParams;

	public CoverageInstrumenter(CoverageAgentParams agentParams) {
		this.agentParams = agentParams;
	}

	@Override
	protected byte[] instrument(String classFName, String className, JavaClass jc) {
		ClassGen classGen = new ClassGen(jc);
		ConstantPoolGen constPool = classGen.getConstantPool();
		JavaClass newJC = null;
		boolean isAppClass = FilterChecker.isAppClass(classFName);
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
								ClassGenUtils.getMethodFullName(classGen.getClassName(), method)));
					} else {
						// All changes made, so finish off the method:
						InstructionList instructionList = methodGen.getInstructionList();
						instructionList.setPositions();
						methodGen.setMaxStack();
						methodGen.setMaxLocals();
						classGen.replaceMethod(method, methodGen.getMethod());
					}
					newJC = classGen.getJavaClass();
					newJC.setConstantPool(constPool.getFinalConstantPool());
				}
			} catch (Exception e) {
				String message = e.getMessage();
				if (e.getMessage() != null && e.getMessage().contains("offset too large")) {
					message = "offset too large";
				}
				AgentLogger.info(String.format("Warning: %s [%s]",
						ClassGenUtils.getMethodFullName(classGen.getClassName(), method), message));
				AgentLogger.error(e);
			}
		}
		if (newJC != null) {
			byte[] data = newJC.getBytes();
			return data;
		}
		return null;
	}

	private boolean instrumentMethod(ClassGen classGen, ConstantPoolGen constPool, MethodGen methodGen, Method method,
			boolean isAppClass) {
		InstructionList insnList = methodGen.getInstructionList();
		InstructionHandle startInsn = insnList.getStart();
		if (startInsn == null) {
			// empty method
			return false;
		}
//		ProbeInstructionInfos probeInfo = ProbeInstructionInfos.buildProbeInstructions(insnList);
		
		return false;
	}

}
