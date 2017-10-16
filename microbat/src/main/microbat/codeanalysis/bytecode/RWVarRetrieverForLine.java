package microbat.codeanalysis.bytecode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.SyntheticRepository;

import sav.strategies.dto.AppJavaClassPath;

public class RWVarRetrieverForLine{
	public static LineNumberVisitor0 parse(String className, int lineNumber, int offset, AppJavaClassPath appClassPath){
		String originalSystemClassPath = System.getProperty("java.class.path");
		String[] paths = originalSystemClassPath.split(File.pathSeparator);
		List<String> pathList = new ArrayList<>();
		for(String path: paths){
			pathList.add(path);
		}
		
		StringBuffer buffer = new StringBuffer(originalSystemClassPath);
		for(String classPath: appClassPath.getClasspaths()){
			if(!pathList.contains(classPath)){
				buffer.append(File.pathSeparator + classPath);				
			}
		}
		buffer.append(File.pathSeparator);
		String jdkPath = appClassPath.getJavaHome() + File.separator + "jre" + 
				File.separator + "lib" + File.separator + "rt.jar";
		buffer.append(jdkPath);
		System.setProperty("java.class.path", buffer.toString());
		String s = System.getProperty("java.class.path");
		
//		if(!isOptimizeByteCodeCompilation){
//			Repository.clearCache();				
//			ClassPath0 classPath = new ClassPath0(s);
//			Repository.setRepository(SyntheticRepository.getInstance(classPath));
//		}
		
		ClassPath0 classPath = new ClassPath0(s);
		Repository.setRepository(SyntheticRepository.getInstance(classPath));
		
		LineNumberVisitor0 visitor = null;
		JavaClass clazz;
		try {
			clazz = Repository.lookupClass(className);
			visitor = new LineNumberVisitor0(lineNumber, className, offset, appClassPath);
			clazz.accept(new DescendingVisitor(clazz, visitor));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		System.setProperty("java.class.path", originalSystemClassPath);
		return visitor;
	}
}
