package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.List;

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

	public List<BreakPoint> parsingBreakPoints(AppJavaClassPath appClassPath) throws Exception {
		
		String systemClassPath = System.getProperty("java.class.path");
		StringBuffer buffer = new StringBuffer(systemClassPath);
		for(String classPath: appClassPath.getClasspaths()){
			buffer.append(";" + classPath);
		}
		System.setProperty("java.class.path", buffer.toString());
		systemClassPath = System.getProperty("java.class.path");
		
		for(BreakPoint breakpoint: executingStatements){
			JavaClass clazz = Repository.lookupClass(breakpoint.getClassCanonicalName());
			LineNumberVisitor visitor = new LineNumberVisitor(breakpoint);
			clazz.accept(new DescendingVisitor(clazz, visitor));
		}
		
		return executingStatements;
	}

	public List<BreakPoint> getExecutingStatements() {
		return executingStatements;
	}

	public void setExecutingStatements(List<BreakPoint> executingStatements) {
		this.executingStatements = executingStatements;
	}
}
