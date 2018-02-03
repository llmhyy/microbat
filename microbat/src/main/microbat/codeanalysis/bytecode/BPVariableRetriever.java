package microbat.codeanalysis.bytecode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.SyntheticRepository;

import microbat.model.BreakPoint;
import sav.strategies.dto.AppJavaClassPath;

public class BPVariableRetriever {
	
	private List<BreakPoint> executingStatements = new ArrayList<>();
	
	public BPVariableRetriever(List<BreakPoint> executingStatements) {
		super();
		this.setExecutingStatements(executingStatements);
	}

	public List<BreakPoint> parsingBreakPoints(AppJavaClassPath appClassPath, boolean isOptimizeByteCodeCompilation) throws Exception {
		
		String originalSystemClassPath = System.getProperty("java.class.path");
		String[] paths = originalSystemClassPath.split(File.pathSeparator);
		
		try{
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
			System.setProperty("java.class.path", buffer.toString());
			String s = System.getProperty("java.class.path");
			
			/** when evaluation does not change line number, so we can keep the cache to speed up the progress */
			if(!isOptimizeByteCodeCompilation){
				Repository.clearCache();				
				ClassPath0 classPath = new ClassPath0(s);
				Repository.setRepository(SyntheticRepository.getInstance(classPath));
			}
			
			Map<String, List<BreakPoint>> class2PointMap = summarize(executingStatements);
			for(String className: class2PointMap.keySet()){
				JavaClass clazz = Repository.lookupClass(className);
				LineNumberVisitor visitor = new LineNumberVisitor(class2PointMap.get(className), appClassPath);
				clazz.accept(new DescendingVisitor(clazz, visitor));
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			System.setProperty("java.class.path", originalSystemClassPath);			
		}
		
		return executingStatements;
	}
	
	private Map<String, List<BreakPoint>> summarize(List<BreakPoint> executingStatements){
		Map<String, List<BreakPoint>> map = new HashMap<>();
		for(BreakPoint breakpoint: executingStatements){
			String className = breakpoint.getClassCanonicalName();
			List<BreakPoint> points = map.get(className);
			if(points == null){
				points = new ArrayList<>();
			}
			points.add(breakpoint);
			map.put(className, points);
		}
		
		return map;
	}

	public List<BreakPoint> getExecutingStatements() {
		return executingStatements;
	}

	public void setExecutingStatements(List<BreakPoint> executingStatements) {
		this.executingStatements = executingStatements;
	}
}
