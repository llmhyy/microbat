package microbat.handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

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
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.google.common.io.Files;

import microbat.Activator;
import microbat.preference.MicrobatPreference;

public class TransformLibHandler extends AbstractHandler {

	public static String tempVariableName = "t_t_t";
	
	private byte[] readBytes(InputStream inputStream) throws IOException {
	    byte[] b = new byte[1024];
	    ByteArrayOutputStream os = new ByteArrayOutputStream();
	    int c;
	    while ((c = inputStream.read(b)) != -1) {
	      os.write(b, 0, c);
	    }
	    return os.toByteArray();
	  }
	
	@SuppressWarnings("resource")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String javaHomePath = Activator.getDefault().getPreferenceStore().getString(MicrobatPreference.JAVA7HOME_PATH);
		String destFile = javaHomePath + File.separator + "jre" + File.separator + "lib";
		String jarFile = destFile + File.separator + "rt.jar";
		
		File rtJarFile = new File(jarFile);
		if(rtJarFile.exists()){
			try {
				JarFile jar = new JarFile(rtJarFile);
				Enumeration<? extends JarEntry> enumeration = jar.entries();
				while(enumeration.hasMoreElements()){
					ZipEntry zipEntry = enumeration.nextElement();
					if(zipEntry.getName().endsWith(".class")){
						String className = zipEntry.getName();
						if(className.equals("java/util/ArrayList.class")){
							File toWriteFile = new File(destFile, zipEntry.getName());
							
							if(!toWriteFile.exists()){
								toWriteFile.getParentFile().mkdirs();
								toWriteFile.createNewFile();
							}
							
							InputStream in = new BufferedInputStream(jar.getInputStream(zipEntry));
			                OutputStream out = new BufferedOutputStream(new FileOutputStream(toWriteFile));
			                byte[] buffer = new byte[2048];
			                for (;;) {
			                    int nBytes = in.read(buffer);
			                    if (nBytes <= 0) {
			                        break;
			                    }
			                    out.write(buffer, 0, nBytes);
			                }
			                out.flush();
			                out.close();
			                in.close();
							
			                InputStream in0 = new FileInputStream(toWriteFile);
			                byte[] bytes = readBytes(in0);
			                
			                instrument(toWriteFile, bytes, className);
			                
						}
					}
				}
				 
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			
		}
		
		return null;
	}

	private void instrument(File toWriteFile, byte[] bytes, String className) {
		InputStream stream = new ByteArrayInputStream(bytes);
		ClassParser parser = new ClassParser(stream, className);
		try {
			JavaClass clazz = parser.parse();			
			ClassGen classGen = new ClassGen(clazz);
			for(int i=0; i<classGen.getMethods().length; i++){
				Method method = classGen.getMethodAt(i);
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
						StackInstruction stackIns = InstructionFactory.createDup(1);
						LocalVariableInstruction storeIns = InstructionFactory.createStore(Type.INT, index);
						
						InstructionHandle stackHandle = instructionList.append(arrayHandle.getPrev(), stackIns);
						instructionList.append(stackHandle, storeIns);
						instructionList.setPositions();
						
						System.currentTimeMillis();
//				        instructionList.append(new GETSTATIC(constantPoolGen.addFieldref("java.lang.System", 
//				        		"out", "Ljava/io/PrintStream;")));
//				        instructionList.append(new LDC(constantPoolGen.addString("You are a real geek!")));
//				        instructionList.append(new INVOKEVIRTUAL(constantPoolGen.addMethodref("java.io.PrintStream", "println", "(Ljava/lang/String;)V")));
					}
					else if(arrayIns.getName().contains("store")){
						//TODO
					}
				}
				
				System.out.println("instrument method " + method.getName());
				
				mGen.setMaxLocals();
				mGen.setMaxStack();
				Method newMethod = mGen.getMethod();
				classGen.setMethodAt(newMethod, i);
			}
			
			JavaClass cl = classGen.getJavaClass();
			cl.setMethods(classGen.getMethods());
			byte[] newBytes = cl.getBytes();
			Files.write(newBytes, toWriteFile);
		} catch (ClassFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
