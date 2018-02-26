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
import microbat.instrumentation.runtime.ExecutionTracer;

public class TestRunnerTranformer extends AbstractTransformer implements ClassFileTransformer {

	@Override
	protected byte[] doTransform(ClassLoader loader, String classFName, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if (ExecutionTracer.isShutdown()) {
			return null;
		}
		if ("microbat/evaluation/junit/MicroBatTestRunner".equals(classFName)) {
			try {
				byte[] data = instrument(classFName, classfileBuffer);
				return data;
			} catch (ClassFormatException | IOException e) {
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
			if ("$exitProgram".equals(method.getName())) {
				MethodGen methodGen = new MethodGen(method, classFName, constPool);
				InstructionList newInsns = new InstructionList();
				int varIdx = 1;
				LocalVariableTable localVariableTable = methodGen.getLocalVariableTable(constPool);
				LocalVariable localVariable = localVariableTable.getLocalVariable(varIdx, 0);
				Type argType = methodGen.getArgumentType(0);
				int index = constPool.addInterfaceMethodref(Agent.class.getName().replace(".", "/"), "_exitProgram",
						"(Ljava/lang/String;)V");
				
				newInsns.append(InstructionFactory.createLoad(argType, localVariable.getIndex()));
				newInsns.append(new INVOKESTATIC(index));
				InstructionList instructionList = methodGen.getInstructionList();
				InstructionHandle startInsn = instructionList.getStart();
				InstructionHandle pos = instructionList.insert(startInsn, newInsns);
				updateTargeters(startInsn, pos);
				instructionList.setPositions();
				methodGen.setMaxStack();
				methodGen.setMaxLocals();
				classGen.replaceMethod(method, methodGen.getMethod());
			} else if ("$testStarted".equals(method.getName())) {
				agentMethodIdx = constPool.addInterfaceMethodref(Agent.class.getName().replace(".", "/"), "_startTest",
						"(Ljava/lang/String;Ljava/lang/String;)V");
			} else if ("$testFinished".equals(method.getName())) {
				agentMethodIdx = constPool.addInterfaceMethodref(Agent.class.getName().replace(".", "/"), "_finishTest",
						"(Ljava/lang/String;Ljava/lang/String;)V");
			}
			if (agentMethodIdx >= 0) {
				MethodGen methodGen = new MethodGen(method, classFName, constPool);
				InstructionList newInsns = new InstructionList();
				int varIdx = 1;
				LocalVariableTable localVariableTable = methodGen.getLocalVariableTable(constPool);
				LocalVariable junitClassVar = localVariableTable.getLocalVariable(varIdx, 0);
				Type junitClassVarType = methodGen.getArgumentType(0);
				varIdx++;
				LocalVariable junitMethodVar = localVariableTable.getLocalVariable(varIdx, 0);
				Type junitMethodVarType = methodGen.getArgumentType(0);
				
				newInsns.append(InstructionFactory.createLoad(junitClassVarType, junitClassVar.getIndex()));
				newInsns.append(InstructionFactory.createLoad(junitMethodVarType, junitMethodVar.getIndex()));
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
			
		}
		newJC = classGen.getJavaClass();
		newJC.setConstantPool(constPool.getFinalConstantPool());
		return newJC.getBytes();
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
