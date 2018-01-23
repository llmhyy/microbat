package microbat.instrumentation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IFNULL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Type;

import microbat.variableswitch.MicrobatSwitch;

public class OperandRetrievingTransfomer implements ClassFileTransformer {

//	public static String tempVariableName = "microbat_return_value";
	public static String returnVariableValue = "microbat_return_value";
	public static String returnVariableType = "microbat_return_type";

	private boolean isExcluded(String className) {
		for (String excl : Excludes.defaultLibExcludes) {
			String prefix = excl.replace("*", "");
			if (className.startsWith(prefix)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
//		 System.out.println(className);
		// System.out.println("heeloo");

		// if(!classNameSet.contains(className)){
		// classNameSet.add(className);
		// }
		// else{
		// System.out.println("additional transform happens: " + className);
		// }

		// return classfileBuffer;

		if (isExcluded(className)) {
			return classfileBuffer;
		}
		
		if(!className.equals("org/Test")){
			return classfileBuffer;
		}

		InputStream stream = new ByteArrayInputStream(classfileBuffer);
		ClassParser parser = new ClassParser(stream, className);
		try {
			JavaClass clazz = parser.parse();
			ClassGen classGen = new ClassGen(clazz);
			for (int i = 0; i < classGen.getMethods().length; i++) {
				Method method = classGen.getMethodAt(i);


				MethodGen mGen = new MethodGen(method, clazz.getClassName(), classGen.getConstantPool());
				InstructionList instructionList = mGen.getInstructionList();
				// System.out.println(instructionList);

				instrumentForInvocationInstructions(mGen, instructionList);
				instrumentForReturnInstructions(mGen, instructionList);

				mGen.setMaxLocals();
				mGen.setMaxStack();

				Method newMethod = mGen.getMethod();
//				System.out.println(newMethod.getCode());
//				System.out.println(newMethod.getConstantPool());
				classGen.setMethodAt(newMethod, i);
			}

			JavaClass cl = classGen.getJavaClass();
			cl.setMethods(classGen.getMethods());
			byte[] newBytes = cl.getBytes();

			return newBytes;
		} catch (ClassFormatException | IOException e) {
			e.printStackTrace();
		}

		return classfileBuffer;
	}

	private void instrumentForInvocationInstructions(MethodGen mGen, InstructionList instructionList) {
		
		List<InstructionHandle> invocationList = findInvocationInstruction(instructionList);
		Iterator<InstructionHandle> iterator = invocationList.iterator();
		
		ConstantPoolGen constantPoolGen = mGen.getConstantPool();
		int classIndex = constantPoolGen.lookupClass(MicrobatSwitch.class.getName());
		if(classIndex==-1){
			classIndex = constantPoolGen.addClass(MicrobatSwitch.class.getName());
		}
		
		int fieldTypeIndex = constantPoolGen.lookupFieldref(MicrobatSwitch.class.getName(), "type", "Ljava/lang/String;");
		if(fieldTypeIndex==-1){
			fieldTypeIndex = constantPoolGen.addFieldref(MicrobatSwitch.class.getName(), "type", "Ljava/lang/String;");
		}
		
		int fieldValueIndex = constantPoolGen.lookupFieldref(MicrobatSwitch.class.getName(), "value", "Ljava/lang/String;");
		if(fieldValueIndex==-1){
			fieldValueIndex = constantPoolGen.addFieldref(MicrobatSwitch.class.getName(), "value", "Ljava/lang/String;");
		}
		
		
		LocalVariableGen rnGen = mGen.addLocalVariable(returnVariableType, Type.STRING, instructionList.getStart(),
				instructionList.getEnd());
		int localVarTypeIndex = rnGen.getIndex();
		LocalVariableGen rtGen = mGen.addLocalVariable(returnVariableValue, Type.STRING, instructionList.getStart(),
				instructionList.getEnd());
		int localVarValueIndex = rtGen.getIndex();
		
		while (iterator.hasNext()) {
			InstructionHandle invocation = iterator.next();
			
			InstructionHandle handle = instructionList.append(invocation.getNext(), new GETSTATIC(fieldTypeIndex));
			LocalVariableInstruction storeIndexIns = InstructionFactory.createStore(Type.STRING, localVarTypeIndex);
			handle = instructionList.append(handle, storeIndexIns);
			
			handle = instructionList.append(handle, new GETSTATIC(fieldValueIndex));
			LocalVariableInstruction storeIndexIns0 = InstructionFactory.createStore(Type.STRING, localVarValueIndex);
			handle = instructionList.append(handle, storeIndexIns0);
			
//			handle = instructionList.append(handle, new GETSTATIC(
//					constantPoolGen.addFieldref("java.lang.System", "out", "Ljava/io/PrintStream;")));
//			handle = instructionList.append(handle, new ALOAD(localVarValueIndex));
//			handle = instructionList.append(handle,
//					new INVOKEVIRTUAL(constantPoolGen.addMethodref("java.io.PrintStream", "println", "(Ljava/lang/String;)V")));
		}
		
		instructionList.setPositions();
	}

	private void instrumentForReturnInstructions(MethodGen mGen, InstructionList instructionList) {
		Type returnType = mGen.getMethod().getReturnType();

		if (returnType instanceof BasicType) {
			BasicType bType = (BasicType) returnType;
			if (bType.getType() == BasicType.VOID.getType()) {
				return;
			}
		}
		
		ConstantPoolGen constantPoolGen = mGen.getConstantPool();
		int classIndex = constantPoolGen.lookupClass(MicrobatSwitch.class.getName());
		if(classIndex==-1){
			classIndex = constantPoolGen.addClass(MicrobatSwitch.class.getName());
		}
		
		int fieldTypeIndex = constantPoolGen.lookupFieldref(MicrobatSwitch.class.getName(), "type", "Ljava/lang/String;");
		if(fieldTypeIndex==-1){
			fieldTypeIndex = constantPoolGen.addFieldref(MicrobatSwitch.class.getName(), "type", "Ljava/lang/String;");
		}
		
		int fieldValueIndex = constantPoolGen.lookupFieldref(MicrobatSwitch.class.getName(), "value", "Ljava/lang/String;");
		if(fieldValueIndex==-1){
			fieldValueIndex = constantPoolGen.addFieldref(MicrobatSwitch.class.getName(), "value", "Ljava/lang/String;");
		}
		
		int valueOfMethod = constantPoolGen.lookupMethodref("java/lang/String", "valueOf", "("+returnType.getSignature()+")"+"Ljava/lang/String;");
		if(valueOfMethod==-1){
			valueOfMethod = constantPoolGen.addMethodref(
					"java/lang/String", "valueOf", "("+returnType.getSignature()+")"+"Ljava/lang/String;");
		}
		
		int nullString = constantPoolGen.lookupString("null");
		if(nullString==-1){
			nullString = constantPoolGen.addString("null");
		}
		
		int returnTypeString = constantPoolGen.lookupString(returnType.toString());
		if(returnTypeString==-1){
			returnTypeString = constantPoolGen.addString(returnType.toString());
		}
		
		LocalVariableGen lvGen = mGen.addLocalVariable("microbat_tmp", returnType, instructionList.getStart(),
				instructionList.getEnd());
		int index = lvGen.getIndex();

		List<InstructionHandle> returnHandles = findReturnInstruction(instructionList);

		Iterator<InstructionHandle> iterator = returnHandles.iterator();
		while (iterator.hasNext()) {
			InstructionHandle returnHandle = iterator.next();

			InstructionHandle handle = instructionList.append(returnHandle.getPrev(), new LDC(returnTypeString));
			handle = instructionList.append(handle, new PUTSTATIC(fieldTypeIndex));
			
			LocalVariableInstruction storeIndexIns = InstructionFactory.createStore(returnType, index);
			handle = instructionList.append(handle, storeIndexIns);
			LocalVariableInstruction loadIndexIns = InstructionFactory.createLoad(returnType, index);
			handle = instructionList.append(handle, loadIndexIns);
			
			handle = instructionList.append(handle, loadIndexIns);
			if(returnType instanceof BasicType){
				handle = instructionList.append(handle, new INVOKESTATIC(valueOfMethod));
				handle = instructionList.append(handle, new PUTSTATIC(fieldValueIndex));
			}
			else{
				InstructionHandle ifHandle = instructionList.append(handle, new IFNULL(null));
				handle = instructionList.append(ifHandle, loadIndexIns);
				handle = instructionList.append(handle, new INVOKESTATIC(valueOfMethod));
				handle = instructionList.append(handle, new PUTSTATIC(fieldValueIndex));
				handle = instructionList.append(handle, new GOTO(returnHandle));
				InstructionHandle nullStringhandle = instructionList.append(handle, new LDC(nullString));
				((BranchHandle)ifHandle).setTarget(nullStringhandle);
				handle = instructionList.append(nullStringhandle, new PUTSTATIC(fieldValueIndex));
			}
			
			
//			handle = instructionList.append(handle, new GETSTATIC(
//					constantPoolGen.addFieldref("java.lang.System", "out", "Ljava/io/PrintStream;")));
//			handle = instructionList.append(handle, new GETSTATIC(fieldValueIndex));
//			handle = instructionList.append(handle,
//					new INVOKEVIRTUAL(constantPoolGen.addMethodref("java.io.PrintStream", "println", "(Ljava/lang/String;)V")));
		
//			int fieldRef = constantPoolGen.addFieldref("microbat.variableswitch.MicrobatSwitch", "value", "Ljava/lang/String;");
//			InstructionHandle handle = instructionList.append(returnHandle.getPrev(), new GETSTATIC(
//					constantPoolGen.addFieldref("java.lang.System", "out", "Ljava/io/PrintStream;")));
//			handle = instructionList.append(handle, new GETSTATIC(fieldRef));
//			handle = instructionList.append(handle,
//					new INVOKEVIRTUAL(constantPoolGen.addMethodref("java.io.PrintStream", "println", "(Ljava/lang/String;)V")));
		}

		instructionList.setPositions();
		
	}

	private List<InstructionHandle> findReturnInstruction(InstructionList instructionList) {
		List<InstructionHandle> handles = new ArrayList<>();
		for (InstructionHandle handle : instructionList.getInstructionHandles()) {
			if (handle.getInstruction() instanceof ReturnInstruction) {
				handles.add(handle);
			}
		}
		return handles;
	}

	
	private List<InstructionHandle> findInvocationInstruction(InstructionList instructionList) {
		List<InstructionHandle> handles = new ArrayList<>();
		for (InstructionHandle handle : instructionList.getInstructionHandles()) {
			if (handle.getInstruction() instanceof InvokeInstruction) {
				handles.add(handle);
			}
		}
		return handles;
	}
}
