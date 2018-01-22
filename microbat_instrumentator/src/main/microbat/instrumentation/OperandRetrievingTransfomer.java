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
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Type;

public class OperandRetrievingTransfomer implements ClassFileTransformer {

	public static String tempVariableName = "microbat_return_value";
	
	private boolean isExcluded(String className){
		for(String excl: Excludes.defaultLibExcludes){
			String prefix = excl.replace("*", "");
			if(className.startsWith(prefix)){
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		System.out.println(className);
		if(isExcluded(className)){
			return classfileBuffer;
		}
		
		
		InputStream stream = new ByteArrayInputStream(classfileBuffer);
		ClassParser parser = new ClassParser(stream, className);
		try {
			JavaClass clazz = parser.parse();
			ClassGen classGen = new ClassGen(clazz);
			for (int i = 0; i < classGen.getMethods().length; i++) {
				Method method = classGen.getMethodAt(i);
				
				Type returnType = method.getReturnType();
				
				if(returnType instanceof BasicType){
					BasicType bType = (BasicType)returnType;
					if(bType.getType()==BasicType.VOID.getType()){
						continue;
					}
				}
				
				MethodGen mGen = new MethodGen(method, clazz.getClassName(), classGen.getConstantPool());
				InstructionList instructionList = mGen.getInstructionList();
//				System.out.println(instructionList);
				
				LocalVariableGen lvGen = mGen.addLocalVariable(tempVariableName, returnType, instructionList.getStart(),
						instructionList.getEnd());
				int index = lvGen.getIndex();
				
				List<InstructionHandle> returnHandles = findReturnInstruction(instructionList);
				
				Iterator<InstructionHandle> iterator = returnHandles.iterator();
				while (iterator.hasNext()) {
					InstructionHandle returnHandle = iterator.next();
					LocalVariableInstruction storeIndexIns = InstructionFactory.createStore(returnType, index);
					LocalVariableInstruction loadIndexIns = InstructionFactory.createLoad(returnType, index);
					
					InstructionHandle handle = instructionList.append(returnHandle.getPrev(), storeIndexIns);
					handle = instructionList.append(handle, loadIndexIns);
				}
				
		        instructionList.setPositions();
		        
//		        System.out.println(instructionList);
				
		        mGen.setMaxLocals();
				mGen.setMaxStack();
				
				Method newMethod = mGen.getMethod();
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

	private List<InstructionHandle> findReturnInstruction(InstructionList instructionList) {
		List<InstructionHandle> handles = new ArrayList<>();
		for(InstructionHandle handle: instructionList.getInstructionHandles()){
			if(handle.getInstruction() instanceof ReturnInstruction){
				handles.add(handle);
			}
		}
		return handles;
	}

}
