package microbat.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import microbat.Activator;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.model.variable.Variable;
import microbat.preference.MicrobatPreference;
import sav.common.core.SavRtException;
import sav.common.core.utils.FileUtils;
import sav.common.core.utils.StringUtils;
import sav.strategies.dto.AppJavaClassPath;


public class MicroBatUtil {
	private MicroBatUtil(){}
	
	public static String getProjectPath(String projectName){
		IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject iProject = myWorkspaceRoot.getProject(projectName);
		
		String projectPath = iProject.getLocationURI().getPath();
//		projectPath = projectPath.substring(1, projectPath.length());
		projectPath = projectPath.replace("/", File.separator);
		
		return projectPath;
	}
	
	public static AppJavaClassPath constructClassPaths(){
		return constructClassPaths(Settings.projectName);
	}
	
	public static AppJavaClassPath constructClassPaths(String projectName){
		AppJavaClassPath appClassPath = new AppJavaClassPath();
		
		String projectPath = getProjectPath(projectName);
		IJavaProject javaProject = getJavaProject(projectName);
		
		/**
		 * setting depended jars into classpath
		 */
//		String workspacePath = projectPath.substring(0, projectPath.indexOf(File.separator+Settings.projectName));
//		String workspacePath = projectPath.substring(0, projectPath.lastIndexOf(File.separator)); 
		appClassPath.setClassLoader(IProjectUtils.getPrjClassLoader(javaProject));
		appClassPath.addClasspath(projectPath);
		
		for(IClasspathEntry classpathEntry: javaProject.readRawClasspath()){
			if(classpathEntry.getEntryKind()==IClasspathEntry.CPE_LIBRARY){
				String path = classpathEntry.getPath().toOSString();
				String newPath = path.substring(path.indexOf(File.separator, 1));
				newPath = projectPath + newPath;
				appClassPath.addClasspath(newPath);
				appClassPath.addExternalLibPath(newPath);
			}
			else if (classpathEntry.getEntryKind()==IClasspathEntry.CPE_VARIABLE) {
				String varName= classpathEntry.getPath().segment(0);
				IPath path0 = JavaCore.getClasspathVariable(varName);
				if(path0!=null) {
					IPath newPath0 = path0.append(classpathEntry.getPath().removeFirstSegments(1));
					String path = newPath0.toOSString();
					appClassPath.addClasspath(path);
					appClassPath.addExternalLibPath(path);
				}
			}
			else if(classpathEntry.getEntryKind()==IClasspathEntry.CPE_SOURCE) {
//			    String newPath = path.substring(path.indexOf(File.separator, 1));
				/**
				 * means that it is a default output folder
				 */
				String path = null;
				if(classpathEntry.getOutputLocation() == null) {
					IPath iPath = classpathEntry.getPath();
					path = iPath.toOSString();
				}
				path = classpathEntry.getOutputLocation().toOSString();
				
			    String newPath = path.substring(path.indexOf(File.separator, 1));
			    newPath = projectPath + newPath;
			    if(!appClassPath.getClasspaths().contains(newPath)) {
			     appClassPath.addClasspath(newPath);     
			    }
			    System.currentTimeMillis();
			}
		}
		
		setSystemJars(appClassPath);
		
		/**
		 * setting output folder
		 */
		String outputFolder = "";
		try {
			for(String seg: javaProject.getOutputLocation().segments()){
				if(!seg.equals(projectName)){
					outputFolder += seg + File.separator;
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		if(outputFolder.length() == 0){
			outputFolder = "bin";
		}
		
		
		String outputPath = projectPath + File.separator + outputFolder; 
		
		String javaHome = getJavaHome(javaProject);
		appClassPath.setJavaHome(javaHome);
		appClassPath.addClasspath(outputPath);
		appClassPath.setWorkingDirectory(projectPath);
		appClassPath.setLaunchClass(Settings.lanuchClass);
		return appClassPath;
		
	}

	/** for centralization */
	public static void setSystemJars(AppJavaClassPath appClassPath) {
		/**
		 * setting junit lib into classpath
		 */
		String dropinsDir = IResourceUtils.getDropinsDir();
		String junitDir = dropinsDir + File.separator + "junit_lib";
		
		
		String junitPath = junitDir + File.separator + "junit.jar";
		String hamcrestCorePath = junitDir + File.separator + "org.hamcrest.core.jar";
		appClassPath.addClasspath(junitPath);
		appClassPath.addClasspath(hamcrestCorePath);
		
		String testRunnerDir = junitDir + File.separator + "testrunner.jar";
		appClassPath.addClasspath(testRunnerDir);
		
		/**
		 * setting bcel lib (for instrumentation) into classpath
		 */
		String bcelDir = junitDir + File.separator + "bcel-6.0.jar";
		appClassPath.addClasspath(bcelDir);
		String javassitDir = junitDir + File.separator + "javassist.jar";
		appClassPath.addClasspath(javassitDir);
		
		/**
		 * setting java agent lib 
		 */
		String agentLib = junitDir + File.separator + "instrumentator.jar";
		appClassPath.setAgentLib(agentLib);
	}

	public static IJavaProject getJavaProject(String projectName) {
		IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject iProject = myWorkspaceRoot.getProject(projectName);
		IJavaProject javaProject = JavaCore.create(iProject);
		return javaProject;
	}
	
	public static List<String> getSourceFolders(String projectName) {
		IJavaProject javaProject = getJavaProject(projectName);
		List<String> srcFolders = new ArrayList<>();
		for(IClasspathEntry classpathEntry: javaProject.readRawClasspath()){
			if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				srcFolders.add(IResourceUtils.getAbsolutePathOsStr(classpathEntry.getPath()));
			}
		}
		return srcFolders;
	}
	
	public static String getSourceFolder(String cName, String projectName) {
		ICompilationUnit unit = JavaUtil.findICompilationUnitInProject(cName, projectName);
		IPath uri = unit.getResource().getFullPath();
		String sourceFolderPath = IResourceUtils.getAbsolutePathOsStr(uri);
		cName = cName.substring(0, cName.lastIndexOf(".")).replace(".", File.separator);
		sourceFolderPath = sourceFolderPath.substring(0, sourceFolderPath.indexOf(cName) - 1);
		return sourceFolderPath;
	}
	
	public static IPackageFragmentRoot getRtPackageFragmentRoot() {
		JarFile jar = null;
		try {
			jar = new JarFile(getRtJarPathInDefinedJavaHome());
			Enumeration<? extends JarEntry> enumeration = jar.entries();
			int i = 0;
			while (enumeration.hasMoreElements()) {
				JarEntry jarEntry = enumeration.nextElement();
				System.out.println(jarEntry.toString());
				i++;
			}
			System.out.println("total: " + i);
		} catch(IOException ex) {
			System.out.println(ex.getMessage());
		} finally {
			if (jar != null) {
				try {
					jar.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}
	
	public static String getRtJarPathInDefinedJavaHome() {
		String javaHomePath = getDefinedJavaHomeFolder();
		String rtJarPath = StringUtils.join(File.separator, javaHomePath, "jre", "lib", "rt.jar");
		if (!new File(rtJarPath).exists()) {
			return lookupRtJar(javaHomePath);
		}
		return rtJarPath;
	}

	public static String getDefinedJavaHomeFolder() {
		String javaHomePath = Activator.getDefault().getPreferenceStore().getString(MicrobatPreference.JAVA7HOME_PATH);
		if (javaHomePath.endsWith(File.separator)) {
			javaHomePath = javaHomePath.substring(0, javaHomePath.length() - 1);
		}
		return javaHomePath;
	}
	
	private static String getJavaHome(IJavaProject javaProject) {
		String javaHome = Activator.getDefault().getPreferenceStore().getString(MicrobatPreference.JAVA7HOME_PATH);
		if (StringUtils.isEmpty(javaHome)) {
			javaHome = IProjectUtils.getJavaHome(javaProject);
		}
		return javaHome;
	}
	
	private static String lookupRtJar(String javaHome) {
		String rtJarPath = FileUtils.lookupFile(javaHome, "rt.jar");
		if (rtJarPath == null) {
			throw new SavRtException("Cannot find rt.jar in javaHome " + javaHome);
		}
		return rtJarPath;
	}
	
	public static List<String> extractExcludeFiles(String parentDirectory, List<String> libJars) {
		List<String> excludes = new ArrayList<>();
		for(String libJar: libJars) {
			File file = new File(libJar);
			
			if(file.toString().contains("junit")) {
				continue;
			}
			
			try {
				@SuppressWarnings("resource")
				JarFile jarFile = new JarFile(file);
				Enumeration<JarEntry> enumeration = jarFile.entries();
				while(enumeration.hasMoreElements()) {
					JarEntry entry = enumeration.nextElement();
					List<String> subExcludes = findSubExcludes(parentDirectory, entry);
					excludes.addAll(subExcludes);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		return excludes;
	}

	private static List<String> findSubExcludes(String parentDirectory, JarEntry entry) {
		List<String> subExcludes = new ArrayList<>();
		
		String classFilePath = entry.getName();
		if (classFilePath.endsWith(".class")) {
			classFilePath = classFilePath.substring(0, classFilePath.indexOf(".class"));
			String className = classFilePath.replace(File.separatorChar, '.');
			subExcludes.add(className);
		}
		
		return subExcludes;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T asT(Object obj) {
		return (T) obj;
	}
	
	public static String combineTraceNodeExpression(String className, int lineNumber){
		String exp = className + " line:" + lineNumber;
		return exp;
	}
	
	/**
	 * For string1: a b c d
	 *     string2: a f c d
	 * The result is a c d
	 * @param nodeList1
	 * @param nodeList2
	 * @param comparator
	 * @return
	 */
	public static Object[] generateCommonNodeList(Object[] nodeList1,
			Object[] nodeList2) {
		int[][] commonLengthTable = buildLeveshteinTable(nodeList1, nodeList2);

		int commonLength = commonLengthTable[nodeList1.length][nodeList2.length];
		Object[] commonList = new Object[commonLength];

		for (int k = commonLength - 1, i = nodeList1.length, j = nodeList2.length; (i > 0 && j > 0);) {
			if (nodeList1[i - 1].equals(nodeList2[j - 1])) {
				commonList[k] = nodeList1[i - 1];
				k--;
				i--;
				j--;
			} else {
				if (commonLengthTable[i - 1][j] >= commonLengthTable[i][j - 1])
					i--;
				else
					j--;
			}
		}

		return commonList;
	}
	
	public static int[][] buildLeveshteinTable(Object[] nodeList1, Object[] nodeList2){
		int[][] commonLengthTable = new int[nodeList1.length + 1][nodeList2.length + 1];
		for (int i = 0; i < nodeList1.length + 1; i++)
			commonLengthTable[i][0] = 0;
		for (int j = 0; j < nodeList2.length + 1; j++)
			commonLengthTable[0][j] = 0;

		for (int i = 1; i < nodeList1.length + 1; i++)
			for (int j = 1; j < nodeList2.length + 1; j++) {
				if (nodeList1[i - 1].equals(nodeList2[j - 1]))
					commonLengthTable[i][j] = commonLengthTable[i - 1][j - 1] + 1;
				else {
					commonLengthTable[i][j] = (commonLengthTable[i - 1][j] >= commonLengthTable[i][j - 1]) ? commonLengthTable[i - 1][j]
							: commonLengthTable[i][j - 1];
				}

			}
		
		return commonLengthTable;
	}

	/**
	 * This method is used for solving
	 * @param retrievedChildren
	 * @param currentNode
	 * @param trace
	 */
	public static void assignWrittenIdentifier(List<VarValue> retrievedChildren, TraceNode currentNode) {
		for(VarValue var: retrievedChildren){
			String simpleVarID = var.getVarID();
			boolean isFound = false;
			
			TraceNode node = currentNode.getStepInPrevious();
			stop:
			while(node != null){
				for(VarValue writtenVar: node.getWrittenVariables()){
					String varID = writtenVar.getVarID();
					String prefix = Variable.truncateSimpleID(varID);
					
					if(prefix.equals(simpleVarID)){
						var.setVarID(varID);
						isFound = true;
						break stop;
					}
				}
				
				node = node.getStepInPrevious();
			}
			
			if(!isFound){
				String varID = simpleVarID + ":0";
				var.setVarID(varID);				
			}
		}
	}
	
//	/**
//	 * @param variable1
//	 * @param variable2
//	 * @return
//	 */
//	public static boolean isTheSameVariable(InterestedVariable v1, InterestedVariable v2) {
//		VarValue var1 = v1.getVariable();
//		VarValue var2 = v2.getVariable();
//		
//		if(var1 instanceof ReferenceValue && var2 instanceof ReferenceValue){
//			ReferenceValue rv1 = (ReferenceValue)var1;
//			ReferenceValue rv2 = (ReferenceValue)var2;
//			if(rv1.getReferenceID() == rv2.getReferenceID()){
//				return true;
//			}
//		}
//		/**
//		 * Otherwise, it means var1 and var2 should be primitive variable, and they are either
//		 * local variable or field.
//		 */
//		else if(!(var1 instanceof ReferenceValue) && !(var2 instanceof ReferenceValue)){
//			if(var1.getVarName().equals(var2.getVarName())){
//				
//				if(var1.isLocalVariable() && var2.isLocalVariable()){
//					boolean isEqualRange = isEqualRange(var1, v1, var2, v2);
//					return isEqualRange;
//				}
//				else if(var1.isField() && var2.isField()){
//					ReferenceValue parent1 = (ReferenceValue)var1.getParents().get(0);
//					ReferenceValue parent2 = (ReferenceValue)var2.getParents().get(0);
//					
//					if(parent1.getReferenceID() == parent2.getReferenceID()){
//						return true;
//					}
//				}
//				
//			}
//		}
//		
//		return false;
//	}
//
//
//	private static boolean isEqualRange(VarValue var1, InterestedVariable v1, VarValue var2, InterestedVariable v2) {
//		String varID = var1.getVariablePath();
//		for(LocalVariableScope lvs: Settings.localVariableScopes.getVariableScopes()){
//			if(varID.equals(lvs.getVariableName())){
//				boolean isRootVar1InScope = lvs.getStartLine() <= v1.getLineNumber() && lvs.getEndLine() >= v1.getLineNumber();
//				boolean isRootVar2InScope = lvs.getStartLine() <= v2.getLineNumber() && lvs.getEndLine() >= v2.getLineNumber();
//				
//				return isRootVar1InScope && isRootVar2InScope;
//			}
//		}
//		
//		return false;
//	}
	
	public static String getTraceFolder() {
		return new StringBuilder(IResourceUtils.getEclipseRootDir()).append(File.separator).append("trace")
				.toString();
	}
	
	public static String generateTraceDir(String projectName, String bugID) {
		String traceFolder = sav.common.core.utils.FileUtils.getFilePath(MicroBatUtil.getTraceFolder(), projectName,
				bugID);
		sav.common.core.utils.FileUtils.createFolder(traceFolder);
		return traceFolder;
	}
}
