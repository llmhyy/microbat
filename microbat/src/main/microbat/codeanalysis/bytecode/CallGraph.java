package microbat.codeanalysis.bytecode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.util.Repository;

import microbat.codeanalysis.runtime.Executor;
import microbat.model.BreakPoint;
import microbat.util.MicroBatUtil;
import sav.strategies.dto.AppJavaClassPath;

public class CallGraph {
	private AppJavaClassPath appPath;
	private List<String> includeLibraries = new ArrayList<>();

	private Map<String, MethodNode> methodMaps = new HashMap<>();

	public CallGraph(AppJavaClassPath appPath, List<String> includeLibraries) {
		this.appPath = appPath;
		this.includeLibraries = includeLibraries;
	}

	public MethodNode findOrCreateMethodNode(BreakPoint location) {
		String methodSign = location.getMethodSign();
		if (methodSign != null) {

			MethodNode node = methodMaps.get(methodSign);
			if (node == null) {
				Method method = findByteCodeMethod(location);
				if (method != null) {
					node = new MethodNode(appPath.getClassLoader(), methodSign, method);
					methodMaps.put(methodSign, node);

					appendCallGraphRootAt(node);
				}
			}

			return node;
		}

		return null;
	}
	
	private boolean isValidClass(String className){
		for(String include: includeLibraries){
			String includeStr = include.replace("\\", "");
			includeStr = includeStr.replace("*", "");
			
			if(className.contains(includeStr)){
				return true;
			}
		}
		
		String[] excludePrefix = Executor.getLibExcludes();
		
		for(String prefix: excludePrefix){
			String pref = prefix;
			if(prefix.contains("*")){
				pref = prefix.replace("*", "");
			}
			
			if(className.contains(pref)){
				return false;
			}
		}
		
		return true;
	}

	private void appendCallGraphRootAt(MethodNode callerNode) {
		Method method = callerNode.getMethod();
		if (method.getCode() == null) {
			return;
		}

		ConstantPoolGen cpGen = new ConstantPoolGen(method.getConstantPool());

		InstructionList insList = new InstructionList(method.getCode().getCode());
		for (InstructionHandle handle : insList.getInstructionHandles()) {
			Instruction ins = handle.getInstruction();
			if (ins instanceof InvokeInstruction) {
				InvokeInstruction invokeIns = (InvokeInstruction) ins;

				String methodSignature = invokeIns.getMethodName(cpGen) + invokeIns.getSignature(cpGen);
				String className = invokeIns.getClassName(cpGen);

				if (!className.contains("[") && isValidClass(className)) {
					ByteCodeMethodFinder finder = new MethodFinderBySignature(methodSignature);
					ByteCodeParser.parse(className, finder, appPath);

					JavaClass clazz = finder.javaClass;
					
					if(clazz==null){
						continue;
					}
					
					List<MethodAndClass> mcList = new ArrayList<>();
					if(clazz.isInterface()){
						List<String> implementation = findImplementations(clazz);
						for(String className0: implementation){
							finder = new MethodFinderBySignature(methodSignature);
							ByteCodeParser.parse(className0, finder, appPath);
							
							if(finder.method!=null){
								mcList.add(new MethodAndClass(finder.method, className0));
							}
						}
					}
					else{
						Method calleeMethod = finder.getMethod();
						if(calleeMethod!=null){
							mcList.add(new MethodAndClass(calleeMethod, className));
						}
						else{
							MethodAndClass mc = checkSuperClass((MethodFinderBySignature) finder);
							if(mc!=null){
								mcList.add(mc);								
							}
						}
					}

					for (MethodAndClass mc: mcList) {
						String calleeSignature = mc.clazz + "#" + mc.method.getName() + mc.method.getSignature();
						MethodNode calleeNode = methodMaps.get(calleeSignature);
						if (calleeNode==null) {
							calleeNode = new MethodNode(appPath.getClassLoader(), calleeSignature, mc.method);
							methodMaps.put(calleeSignature, calleeNode);
							appendCallGraphRootAt(calleeNode);
						}
						
						callerNode.addCallee(handle, calleeNode);
						calleeNode.addCaller(callerNode, handle);
						
					}

				}

			}
		}
	}

	private Map<String, List<String>> implementationMap = new HashMap<>();
	
	private List<JavaClass> allClasses = new ArrayList<>();
	private List<String> findImplementations(JavaClass clazz) {
		
		List<String> implementations = implementationMap.get(clazz.getClassName());
		if(implementations!=null){
			return implementations;
		}
		
		
		implementations = new ArrayList<>();
		
		if(allClasses.isEmpty()){
			Repository repo = clazz.getRepository();
			
			String javaHomePath = MicroBatUtil.getDefinedJavaHomeFolder();
			String workingDir = javaHomePath + File.separator + "jre" + File.separator + "lib";
			String jarFile = workingDir + File.separator + "rt.jar";
			
			List<String> classPaths = new ArrayList<>(appPath.getClasspaths());
			classPaths.add(jarFile);
			
			for(String classPath: appPath.getClasspaths()){
				if(classPath.endsWith(".jar")){
					searchInJar(clazz, repo, jarFile);
				}
				else{
					searchInDirectory(classPath, classPath, clazz, repo);
				}
			}
		}
		else{
			for(JavaClass jCl: allClasses){
				try {
					if(jCl.implementationOf(clazz)){
						implementations.add(jCl.getClassName());
					}
				} catch (ClassNotFoundException e) {
					//e.printStackTrace();
				}
			}
		}
		
		implementationMap.put(clazz.getClassName(), implementations);
		
		return implementations;
	}

	private void searchInJar(JavaClass clazz, Repository repo, String jarFile) {
		File rtJarFile = new File(jarFile);
		if (rtJarFile.exists()) {
			JarFile jar;
			try {
				jar = new JarFile(rtJarFile);
				Enumeration<? extends JarEntry> enumeration = jar.entries();
				while (enumeration.hasMoreElements()) {
					ZipEntry zipEntry = enumeration.nextElement();
					String zipName = zipEntry.getName();
					if (zipName.endsWith(".class")) {
						try {
							String classFile = zipName.replace("/", ".");
							classFile = classFile.replace(".class", "");
							
							if(isValidClass(classFile)){
								JavaClass jCl = repo.loadClass(classFile);
								if(jCl != null){
									allClasses.add(jCl);								
								}
							}
							
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
	}

	private void searchInDirectory(String basePath, String classPath, JavaClass clazz, Repository repo) {
		File file = new File(classPath);
		if(file.isDirectory()){
			for(String subFile: file.list()){
				String f = file.getAbsolutePath() + File.separator + subFile;
				searchInDirectory(basePath, f, clazz, repo);
			}
		}
		else if(file.isFile()){
			String path = file.getPath();
			if(path.endsWith(".class")){
				try {
					if(!path.startsWith(File.separator)){
						path = File.separator + path;
					}
					
					String classFile = path.replace(basePath, "");
					classFile = classFile.replace(".class", "");
					classFile = classFile.replace(File.separator, ".");
					
					if(isValidClass(classFile)){
						JavaClass jCl = repo.loadClass(classFile);
						if(jCl.implementationOf(clazz) && jCl.getClassName().equals(classFile)){
							allClasses.add(jCl);
						}
					}
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	class MethodAndClass {
		Method method;
		String clazz;

		public MethodAndClass(Method method, String clazz) {
			super();
			this.method = method;
			this.clazz = clazz;
		}

	}

	private MethodAndClass checkSuperClass(MethodFinderBySignature finder) {
		JavaClass clazz = finder.javaClass;
		while (clazz != null) {
			try {
				clazz = clazz.getSuperClass();

				//TODO possibly, it is an abstract method of an abstract class
				if(clazz==null){
					return null;
				}
				
				ByteCodeParser.parse(clazz.getClassName(), finder, appPath);
				Method method = finder.getMethod();
				if (method != null) {
					return new MethodAndClass(method, clazz.getClassName());
				}

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				break;
			}
		}

		return null;
	}
	
	private Map<String, Method> locationMethodMap = new HashMap<>();

	public Method findByteCodeMethod(BreakPoint point) {
		String className = point.getClassCanonicalName();
		int lineNumber = point.getLineNumber();

		String locationID = className + "$" + lineNumber;
		Method method = locationMethodMap.get(locationID);
		if (method == null) {
			ByteCodeMethodFinder finder = new MethodFinderByLine(point);
			ByteCodeParser.parse(className, finder, appPath);
			method = finder.getMethod();
			locationMethodMap.put(locationID, method);
		}

		return method;
	}

	public Map<String, MethodNode> getMethodMaps() {
		return methodMaps;
	}

	public void setMethodMaps(Map<String, MethodNode> methodMaps) {
		this.methodMaps = methodMaps;
	}

	public List<String> getIncludeLibraries() {
		return includeLibraries;
	}

	public void setIncludeLibraries(List<String> includeLibraries) {
		this.includeLibraries = includeLibraries;
	}

}
