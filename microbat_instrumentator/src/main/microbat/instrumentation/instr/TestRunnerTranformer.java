package microbat.instrumentation.instr;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import microbat.instrumentation.Agent;
import microbat.instrumentation.cfgcoverage.CoverageAgent;

/**
 * 
 * @author LLT
 *	
 * With the approach of using a TestRunner to execute the testcase, 
 * in order to trace the flow of the process of executing a testcase, we instrument the TestRunner to call 
 * our Agent at particular and provide some information for logging.
 * 
 * Methods for instrumentation in TestRunner must 
 * + start with a "$" and
 * + not be an abstract and have empty body
 *  
 * And Methods to be invoked in Agent must start with a "_"
 * 
 * SavJunitRunner/SavSimpleRunner/SavSocketTestRunner can be found at:
 * https://github.com/sunjun-group/Ziyuan/tree/learntest-nn/app/sav.junit.runner
 * 
 */
public class TestRunnerTranformer extends AbstractTransformer implements ClassFileTransformer {

	@Override
	protected byte[] doTransform(ClassLoader loader, String classFName, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if (!Agent.isInstrumentationActive()) {
			return null;
		}
		if ("microbat/evaluation/junit/MicroBatTestRunner".equals(classFName)
				|| "sav/junit/SavJunitRunner".equals(classFName)
				|| "sav/junit/SavSimpleRunner".equals(classFName)
				|| "sav/junit/SavSocketTestRunner".equals(classFName)) {
			try {
				byte[] data = instrument(classFName, classfileBuffer);
				return data;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private byte[] instrument(String classFName, byte[] classfileBuffer) throws ClassFormatException, IOException {
		ClassParser cp = new ClassParser(new java.io.ByteArrayInputStream(classfileBuffer), classFName);
		JavaClass jc = cp.parse();
		ClassGen classGen = new ClassGen(jc);
		ConstantPoolGen constPool = classGen.getConstantPool();
		JavaClass newJC = null;
		for (Method method : jc.getMethods()) {
			int agentMethodIdx = -1;
			int paramSize = -1;
			if ("$exitProgram".equals(method.getName())) {
				agentMethodIdx = constPool.addMethodref(Agent.class.getName().replace(".", "/"), "_exitProgram",
						"(Ljava/lang/String;)V");
				paramSize = 1;
			} else if ("$testStarted".equals(method.getName())) {
				agentMethodIdx = constPool.addMethodref(Agent.class.getName().replace(".", "/"), "_startTest",
						"(Ljava/lang/String;Ljava/lang/String;)V");
				paramSize = 2;
			} else if ("$testFinished".equals(method.getName())) {
				agentMethodIdx = constPool.addMethodref(Agent.class.getName().replace(".", "/"), "_finishTest",
						"(Ljava/lang/String;Ljava/lang/String;)V");
				paramSize = 2;
			} else if ("$exitTest".equals(method.getName())) {
				agentMethodIdx = constPool.addMethodref(Agent.class.getName().replace(".", "/"), "_exitTest",
						"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Long;)V");
				paramSize = 4;
			} else if ("$storeCoverage".equals(method.getName())) {
				agentMethodIdx = constPool.addMethodref(CoverageAgent.class.getName().replace(".", "/"), "_storeCoverage",
						"(Ljava/io/OutputStream;Ljava/lang/Boolean;)V");
				paramSize = 2;
			}
 			if (agentMethodIdx >= 0) {
 				instrumentDelegateMethod(method, classFName, constPool, agentMethodIdx, classGen, paramSize);
			}
			
		}
		newJC = classGen.getJavaClass();
		newJC.setConstantPool(constPool.getFinalConstantPool());
		return newJC.getBytes();
	}
	
	private void instrumentDelegateMethod(Method method, String classFName, ConstantPoolGen constPool,
			int agentMethodIdx, ClassGen classGen, int paramSize) {
		MethodGen methodGen = new MethodGen(method, classFName, constPool);
		InstructionList newInsns = new InstructionList();
		LocalVariableTable localVariableTable = methodGen.getLocalVariableTable(constPool);
		for (int paramIdx = 0; paramIdx < paramSize; paramIdx++) {
			// the first one is the class object, and parameter would be from the next one.
			LocalVariable localVar = localVariableTable.getLocalVariable(paramIdx + 1, 0); 
			Type varType = methodGen.getArgumentType(paramIdx);
			newInsns.append(InstructionFactory.createLoad(varType, localVar.getIndex()));
		}
		newInsns.append(new INVOKESTATIC(agentMethodIdx));
		InstructionList instructionList = methodGen.getInstructionList();
		InstructionHandle startInsn = instructionList.getStart();
		InstructionHandle pos = instructionList.insert(startInsn, newInsns);
		updateTargeters(startInsn, pos);
		instructionList.setPositions();
		methodGen.setMaxStack();
		methodGen.setMaxLocals();
		classGen.replaceMethod(method, methodGen.getMethod());
	}
	
	private void updateTargeters(InstructionHandle oldPos, InstructionHandle newPos) {
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
