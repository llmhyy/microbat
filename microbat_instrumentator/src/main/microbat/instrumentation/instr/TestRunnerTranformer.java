package microbat.instrumentation.instr;

import java.io.FileOutputStream;
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
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import microbat.instrumentation.Agent;
import microbat.instrumentation.runtime.ExecutionTracer;
import sav.common.core.utils.FileUtils;

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
				store(data, classFName.replace("/", "."));
				return data;
			} catch (ClassFormatException | IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private void store(byte[] data, String className) {
		String filePath = "E:/lyly/Projects/inst_src/test/" + className.substring(className.lastIndexOf(".") + 1)
				+ ".class";
		System.out.println("dump instrumented class to file: " + filePath);
		FileUtils.getFileCreateIfNotExist(filePath);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filePath);
			out.write(data);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private byte[] instrument(String classFName, byte[] classfileBuffer) throws ClassFormatException, IOException {
		ClassParser cp = new ClassParser(new java.io.ByteArrayInputStream(classfileBuffer), classFName);
		JavaClass jc = cp.parse();
		ClassGen classGen = new ClassGen(jc);
		ConstantPoolGen constPool = classGen.getConstantPool();
		JavaClass newJC = null;
		for (Method method : jc.getMethods()) {
			if ("$setProgramMessage".equals(method.getName())) {
				MethodGen methodGen = new MethodGen(method, classFName, constPool);
				InstructionList newInsns = new InstructionList();
				int varIdx = 1;
				LocalVariableTable localVariableTable = methodGen.getLocalVariableTable(constPool);
				LocalVariable localVariable = localVariableTable.getLocalVariable(varIdx, 0);
				Type argType = methodGen.getArgumentType(0);
				int index = constPool.addInterfaceMethodref(Agent.class.getName().replace(".", "/"), "setProgramMsg",
						"(Ljava/lang/String;)V");
				
				newInsns.append(InstructionFactory.createLoad(argType, localVariable.getIndex()));
				newInsns.append(new INVOKESTATIC(index));
				InstructionList instructionList = methodGen.getInstructionList();
				InstructionHandle startInsn = instructionList.getStart();
				InstructionHandle pos = instructionList.insert(startInsn, newInsns);
				TraceInstrumenter.updateTargeters(startInsn, pos);
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

}
