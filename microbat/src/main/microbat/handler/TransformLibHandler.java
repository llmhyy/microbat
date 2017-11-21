package microbat.handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

	private byte[] readBytes(InputStream inputStream) throws IOException {
		byte[] b = new byte[1024];
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		int c;
		while ((c = inputStream.read(b)) != -1) {
			os.write(b, 0, c);
		}
		return os.toByteArray();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String javaHomePath = Activator.getDefault().getPreferenceStore().getString(MicrobatPreference.JAVA7HOME_PATH);
		String workingDir = javaHomePath + File.separator + "jre" + File.separator + "lib";
		String jarFile = workingDir + File.separator + "rt.jar";

		File rtJarFile = new File(jarFile);
		if (rtJarFile.exists()) {
			String bakFilePath = workingDir + File.separator + "rt.bak.jar";
			File bakFile = new File(bakFilePath);
			try {
				if(bakFile.exists()){
					Files.copy(bakFile, rtJarFile);
				}
				else{
					Files.copy(rtJarFile, bakFile);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				JarFile jar = new JarFile(rtJarFile);
				Enumeration<? extends JarEntry> enumeration = jar.entries();
				List<String> writtenFiles = new ArrayList<>();
				while (enumeration.hasMoreElements()) {
					ZipEntry zipEntry = enumeration.nextElement();
					if (zipEntry.getName().endsWith(".class")) {
						String className = zipEntry.getName();
						if (className.equals("java/util/ArrayList.class")) {
							File toWriteFile = new File(workingDir, zipEntry.getName());

							if (!toWriteFile.exists()) {
								toWriteFile.getParentFile().mkdirs();
								toWriteFile.createNewFile();
							}
							
							writtenFiles.add(toWriteFile.getAbsolutePath());

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
							in0.close();

							instrument(toWriteFile, bytes, className);

						}
					}
				}
				jar.close();
				updateRTJar(writtenFiles, workingDir);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	private void updateRTJar(List<String> files, String workingDir) {
		for (String file : files) {
			List<String> command = new ArrayList<>();
			command.add("jar");
			command.add("uf");
			command.add("rt.jar");
			command.add(file);

			ProcessBuilder builder = new ProcessBuilder(command);
			builder.directory(new File(workingDir));
			try {
				Process process = builder.start();
				int errorCode = process.waitFor();
				if (errorCode != 0) {
					System.err.println("updating " + file + " to rt.jar fails.");
					
					String output = output(process.getErrorStream());
					System.out.println(output);
				}
				else{
					System.out.println("updating " + file + " to rt.jar successes.");
				}

				String output = output(process.getInputStream());
				System.out.println(output);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

	private static String output(InputStream inputStream) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + System.getProperty("line.separator"));
			}
		} finally {
			br.close();
		}
		return sb.toString();
	}

	private void instrument(File toWriteFile, byte[] bytes, String className) {
		InputStream stream = new ByteArrayInputStream(bytes);
		ClassParser parser = new ClassParser(stream, className);
		try {
			JavaClass clazz = parser.parse();
			ClassGen classGen = new ClassGen(clazz);
			for (int i = 0; i < classGen.getMethods().length; i++) {
				Method method = classGen.getMethodAt(i);
				MethodGen mGen = new MethodGen(method, clazz.getClassName(), classGen.getConstantPool());
				ConstantPoolGen constantPoolGen = mGen.getConstantPool();
				InstructionList instructionList = mGen.getInstructionList();

				LocalVariableGen lvGen = mGen.addLocalVariable(Activator.tempVariableName, Type.INT, instructionList.getStart(),
						instructionList.getEnd());
				int index = lvGen.getIndex();

				LocalVariableGen lvGen0 = mGen.addLocalVariable(Activator.tempVariableName + "0", Type.INT,
						instructionList.getStart(), instructionList.getEnd());
				int index0 = lvGen0.getIndex();

				List<InstructionHandle> arrayHandles = findArrayLoadInstruction(instructionList);

				Iterator<InstructionHandle> iterator = arrayHandles.iterator();
				while (iterator.hasNext()) {
					InstructionHandle arrayHandle = iterator.next();
					Instruction arrayIns = arrayHandle.getInstruction();
					if (arrayIns.getName().contains("load")) {
						StackInstruction stackIns = InstructionFactory.createDup(1);
						LocalVariableInstruction storeIns = InstructionFactory.createStore(Type.INT, index);

						InstructionHandle stackHandle = instructionList.append(arrayHandle.getPrev(), stackIns);
						instructionList.append(stackHandle, storeIns);
						
					} else if (arrayIns.getName().contains("store")) {
						String insName = arrayIns.getName();
						Type t = getType(insName);
						if (t != null) {
							lvGen0.setType(t);

							LocalVariableInstruction storeValueIns = InstructionFactory.createStore(t, index0);
							LocalVariableInstruction storeIndexIns = InstructionFactory.createStore(Type.INT, index);

							LocalVariableInstruction loadIndexIns = InstructionFactory.createLoad(Type.INT, index);
							LocalVariableInstruction loadValueIns = InstructionFactory.createLoad(t, index0);

							InstructionHandle handle = instructionList.append(arrayHandle.getPrev(), storeValueIns);
							handle = instructionList.append(handle, storeIndexIns);
							handle = instructionList.append(handle, loadIndexIns);
							handle = instructionList.append(handle, loadValueIns);
						}
					}
					InstructionHandle h = instructionList.getStart();
					h = instructionList.append(h, new GETSTATIC(constantPoolGen.addFieldref("java.lang.System",
							"out", "Ljava/io/PrintStream;")));
					h = instructionList.append(h, new
							LDC(constantPoolGen.addString("You are a real geek!")));
					h = instructionList.append(h, new INVOKEVIRTUAL(constantPoolGen.addMethodref("java.io.PrintStream",
									"println", "(Ljava/lang/String;)V")));
					instructionList.setPositions();

					System.currentTimeMillis();
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
			e.printStackTrace();
		}
	}

	private Type getType(String insName) {
		if (insName.equals("aastore")) {
			return Type.OBJECT;
		} else if (insName.equals("bastore")) {
			return Type.BYTE;
		} else if (insName.equals("castore")) {
			return Type.CHAR;
		} else if (insName.equals("dastore")) {
			return Type.DOUBLE;
		} else if (insName.equals("fastore")) {
			return Type.FLOAT;
		} else if (insName.equals("iastore")) {
			return Type.INT;
		} else if (insName.equals("lastore")) {
			return Type.LONG;
		} else if (insName.equals("sastore")) {
			return Type.SHORT;
		}
		return null;
	}

	private List<InstructionHandle> findArrayLoadInstruction(InstructionList instructionList) {
		List<InstructionHandle> handles = new ArrayList<>();
		for (InstructionHandle handle : instructionList.getInstructionHandles()) {
			if (handle.getInstruction() instanceof ArrayInstruction) {
				handles.add(handle);
			}
		}
		return handles;
	}

}
