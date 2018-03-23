package microbat.instrumentation.instr;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Type;

import microbat.instrumentation.runtime.IExecutionTracer;

public class ClassLoaderInstrumenter extends TraceInstrumenter {
	public static int ENTER_MARKER = 0;
	public static int EXIT_MARKER = 1;
	
	public ClassLoaderInstrumenter() {
		super();
	}

	@Override
	protected boolean instrumentMethod(ClassGen classGen, ConstantPoolGen constPool, MethodGen methodGen, Method method,
			boolean isAppClass, boolean isMainMethod) {
		if (!method.getName().equals("loadClass")) {
			return false;
		}
		
		InstructionList insnList = methodGen.getInstructionList();
		LocalVariableGen classNameVar = createLocalVariable(CLASS_NAME, methodGen, constPool);
		LocalVariableGen methodSigVar = createLocalVariable(METHOD_SIGNATURE, methodGen, constPool);
		LocalVariableGen tracerVar = methodGen.addLocalVariable(TRACER_VAR_NAME, Type.getType(IExecutionTracer.class),
				insnList.getStart(), insnList.getEnd());
		injectCodeTracerHitLine(insnList, constPool, tracerVar, ENTER_MARKER, insnList.getStart(), classNameVar,
				methodSigVar, false);
		List<InstructionHandle> returnInsns = extractReturnInstructions(insnList);
		for (InstructionHandle returnInsnHandler : returnInsns) {
			injectCodeTracerHitLine(insnList, constPool, tracerVar, EXIT_MARKER, returnInsnHandler, classNameVar,
					methodSigVar, false);
		}
		
		injectCodeInitTracer(methodGen, constPool, -1, -1, isAppClass, classNameVar,
				methodSigVar, isMainMethod, tracerVar);
		return true;
	}

	private List<InstructionHandle> extractReturnInstructions(InstructionList insnList) {
		List<InstructionHandle> returnInsns = new ArrayList<>();
		for (InstructionHandle insnHandler : insnList) {
			if (insnHandler.getInstruction() instanceof ReturnInstruction) {
				returnInsns.add(insnHandler);
			}
		}
		return returnInsns;
	}
	
	
	
}
