/**
 * 
 */
package microbat.instrumentation.instr;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;

import microbat.instrumentation.Agent;
import microbat.instrumentation.AgentLogger;

/**
 * @author LLT
 * This instrumenter is to instrument Thread class
 * Thread.start() is to be instrumented to call Agent._onStartThread to determine whether the 
 * application is multi-thread or not.
 */
public class ThreadInstrumenter extends AbstractInstrumenter {
	
	
	@Override
	protected byte[] instrument(String classFName, String className, JavaClass jc) {
		ClassGen classGen = new ClassGen(jc);
		ConstantPoolGen constPool = classGen.getConstantPool();
		JavaClass newJC = null;
		Method startMethod = null;
		for (Method method : jc.getMethods()) {
			if ("start".equals(method.getName())) {
				startMethod = method;
				break;
			}
		}
		if (startMethod == null) {
			AgentLogger.debug("Warning: cannot find start() method in class Thread!");
			return null;
		}
		try {
			MethodGen methodGen = new MethodGen(startMethod, classFName, constPool);
			boolean changed = instrumentStartMethod(classGen, constPool, methodGen, startMethod);
			if (changed) {
				// All changes made, so finish off the method:
				InstructionList instructionList = methodGen.getInstructionList();
				instructionList.setPositions();
				methodGen.setMaxStack();
				methodGen.setMaxLocals();
				classGen.replaceMethod(startMethod, methodGen.getMethod());
			}
			newJC = classGen.getJavaClass();
			newJC.setConstantPool(constPool.getFinalConstantPool());
		} catch(Exception e) {
			AgentLogger.debug(String.format("Error when running instrumentation: Thread.start()"));
			AgentLogger.error(e);
		}
		
		if (newJC != null) {
			byte[] data = newJC.getBytes();
			return data;
		}
		return null;
	}

	private boolean instrumentStartMethod(ClassGen classGen, ConstantPoolGen constPool, MethodGen methodGen,
			Method startMethod) {
		InstructionList newInsns = new InstructionList();
		int index = constPool.addInterfaceMethodref(Agent.class.getName().replace(".", "/"), "_onStartThread", "()V");
		newInsns.append(new INVOKESTATIC(index));
		InstructionList instructionList = methodGen.getInstructionList();
		InstructionHandle startInsn = instructionList.getStart();
		insertInsnHandler(instructionList, newInsns, startInsn);
		return true;
	}

	@Override
	protected boolean instrumentMethod(ClassGen classGen, ConstantPoolGen constPool, MethodGen methodGen, Method method,
			boolean isAppClass, boolean isMainMethod, boolean isEntry) {
		// TODO Auto-generated method stub
		return false;
	}

	
}
