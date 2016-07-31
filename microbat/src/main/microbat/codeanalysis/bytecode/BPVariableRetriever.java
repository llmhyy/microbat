package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.JavaClass;

import microbat.model.BreakPoint;
import sav.strategies.dto.AppJavaClassPath;

public class BPVariableRetriever {
	
	private List<BreakPoint> executingStatements = new ArrayList<>();
	
	public BPVariableRetriever(List<BreakPoint> executingStatements) {
		super();
		this.setExecutingStatements(executingStatements);
	}

	public List<BreakPoint> parsingBreakPoints(AppJavaClassPath appClassPath, boolean isForEvaluation) throws Exception {
		
		String systemClassPath = System.getProperty("java.class.path");
		StringBuffer buffer = new StringBuffer(systemClassPath);
		for(String classPath: appClassPath.getClasspaths()){
			buffer.append(";" + classPath);
		}
		System.setProperty("java.class.path", buffer.toString());
		systemClassPath = System.getProperty("java.class.path");
		
		Map<String, List<BreakPoint>> class2PointMap = summarize(executingStatements);
		for(String className: class2PointMap.keySet()){
			/** current evaluation does not change line number, so we can keep the cache to speed up the progress */
			if(!isForEvaluation){
				Repository.clearCache();				
			}
			JavaClass clazz = Repository.lookupClass(className);
			LineNumberVisitor visitor = new LineNumberVisitor(class2PointMap.get(className));
			clazz.accept(new DescendingVisitor(clazz, visitor));
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
