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
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.StackInstruction;
import org.apache.bcel.generic.Type;

public class OperandRetrievingTransfomer implements ClassFileTransformer {

	public static String tempVariableName = "t_t_t";
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if(className.startsWith("java/util"))
			System.out.println("[Interesting] enter " + className);
//		else
//			System.out.println("enter " + className);
//		System.out.println("class file buffer: " + new String(classfileBuffer));
		
		if(className.contains("LibTest")){
			System.out.println("enter " + className);
		}
		
		if(className.length()>0){
			return classfileBuffer;
		}
		
		if(className.startsWith("java/util/")){
			InputStream stream = new ByteArrayInputStream(classfileBuffer);
			ClassParser parser = new ClassParser(stream, className);
			try {
				JavaClass clazz = parser.parse();
				ClassGen classGen = new ClassGen(clazz);
				for(Method method: classGen.getMethods()){
//					System.out.println("enter " + method.getName());
					MethodGen mGen = new MethodGen(method, clazz.getClassName(), classGen.getConstantPool());
					ConstantPoolGen constantPoolGen = mGen.getConstantPool();
					InstructionList instructionList = mGen.getInstructionList();
					
					
					LocalVariableGen lvGen = mGen.addLocalVariable(tempVariableName, Type.INT, 
							instructionList.getStart(), instructionList.getEnd());
					int index = lvGen.getIndex();
					
					List<InstructionHandle> arrayHandles = findArrayLoadInstruction(instructionList); 
					
					Iterator<InstructionHandle> iterator = arrayHandles.iterator();
					while(iterator.hasNext()){
						InstructionHandle arrayHandle = iterator.next();
						Instruction arrayIns = arrayHandle.getInstruction();
						if(arrayIns.getName().contains("load")){
							StackInstruction sIns = InstructionFactory.createDup(1);
							LocalVariableInstruction storeIns = InstructionFactory.createStore(Type.INT, index);
//							LocalVariableInstruction loadIns = InstructionFactory.createLoad(Type.INT, index);
							
							InstructionHandle handle = instructionList.append(arrayHandle.getPrev(), sIns);
							instructionList.append(handle, storeIns);
							
							//Get the reference to static field out in class java.lang.System.
					        instructionList.append(new GETSTATIC(constantPoolGen.addFieldref("java.lang.System", 
					        		"out", "Ljava/io/PrintStream;")));
					        
					        //Load the constant
					        instructionList.append(new LDC(constantPoolGen.addString("You are a real geek!")));
					        
					        //Invoke the method.
					        instructionList.append(new INVOKEVIRTUAL(constantPoolGen.addMethodref("java.io.PrintStream", "println", "(Ljava/lang/String;)V")));
					        
					        System.out.println("insert print in " + method.getName() + "()");
						}
						else if(arrayIns.getName().contains("store")){
							//TODO
						}
					}
					
					
					
					mGen.setMaxLocals();
					mGen.setMaxStack();
				}
				
				return classGen.getJavaClass().getBytes();
			} catch (ClassFormatException | IOException e) {
				e.printStackTrace();
			}
			
		}
		
		return classfileBuffer;
	}

	private List<InstructionHandle> findArrayLoadInstruction(InstructionList instructionList) {
		List<InstructionHandle> handles = new ArrayList<>();
		for(InstructionHandle handle: instructionList.getInstructionHandles()){
			if(handle.getInstruction() instanceof ArrayInstruction){
				handles.add(handle);
			}
		}
		return handles;
	}

}
