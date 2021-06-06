package microbat.instrumentation.instr;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.PUSH;
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
			boolean isAppClass, boolean isMainMethod, boolean isEntry) {
		if (!method.getName().equals("loadClass")) {
			return false;
		}
		
		InstructionList insnList = methodGen.getInstructionList();
		InstructionHandle tryStart = insnList.getStart();
		InstructionHandle tryEnd = insnList.getEnd();
		
		LocalVariableGen classNameVar = createLocalVariable(CLASS_NAME, methodGen, constPool);
		LocalVariableGen methodSigVar = createLocalVariable(METHOD_SIGNATURE, methodGen, constPool);
		LocalVariableGen tracerVar = methodGen.addLocalVariable(TRACER_VAR_NAME, Type.getType(IExecutionTracer.class),
				insnList.getStart(), insnList.getEnd());
		injectCodeTracerHitLine(insnList, constPool, tracerVar, ENTER_MARKER, insnList.getStart(), classNameVar,
				methodSigVar, false, 0, 0);
		List<InstructionHandle> returnInsns = extractReturnInstructions(insnList);
		for (InstructionHandle returnInsnHandler : returnInsns) {
			injectCodeTracerHitLine(insnList, constPool, tracerVar, EXIT_MARKER, returnInsnHandler, classNameVar,
					methodSigVar, false, 0, 0);
		}
		
		injectCodeInitTracer(methodGen, constPool, -1, -1, isAppClass, classNameVar,
				methodSigVar, isMainMethod, tracerVar);
		
		injectTryCatch(methodGen, tryStart, tryEnd, constPool, tracerVar, classNameVar, methodSigVar);
		return true;
	}
	
	private void injectTryCatch(MethodGen methodGen, InstructionHandle tryStart, InstructionHandle tryEnd,
			ConstantPoolGen constPool, LocalVariableGen tracerVar, LocalVariableGen classNameVar,
			LocalVariableGen methodSigVar) {
		
		InstructionList list = methodGen.getInstructionList();
		LocalVariableGen exVar = methodGen.addLocalVariable("$ex", Type.THROWABLE, list.getStart(), list.getEnd());
		
		TracerMethods tracerMethod = TracerMethods.HIT_LINE;
		InstructionList newInsns = new InstructionList();
		newInsns.append(new ASTORE(exVar.getIndex()));
		newInsns.append(new ALOAD(tracerVar.getIndex()));
		newInsns.append(new PUSH(constPool, EXIT_MARKER));
		newInsns.append(new ALOAD(classNameVar.getIndex()));
		newInsns.append(new ALOAD(methodSigVar.getIndex()));
		appendTracerMethodInvoke(newInsns, tracerMethod, constPool);
		
		newInsns.append(new ALOAD(exVar.getIndex()));
		newInsns.append(new ATHROW());
		
		InstructionHandle exceptionHandler = methodGen.getInstructionList().append(newInsns);
		newInsns.dispose();	
		
		methodGen.addExceptionHandler(tryStart, tryEnd, exceptionHandler,  Type.THROWABLE);
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
