package microbat.codeanalysis.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodInvocationFinder extends ASTVisitor {
	
	private List<MethodInvocation> invocations = new ArrayList<>();
	
	private CompilationUnit cu;
	private int linNumber;
	
	public MethodInvocationFinder(CompilationUnit cu, int linNumber){
		this.cu = cu;
		this.linNumber = linNumber;
	}
	
	public boolean visit(MethodDeclaration md){
		int startLine = cu.getLineNumber(md.getStartPosition());
		int endLine = cu.getLineNumber(md.getStartPosition()+md.getLength());
		
		if(startLine<=linNumber && linNumber<=endLine){
			return true;
		}
		else{
			return false;
		}
	}
	
	public boolean visit(MethodInvocation invocation){
		int linNum = cu.getLineNumber(invocation.getStartPosition());
		
		if(linNum == linNumber){
			getInvocations().add(invocation);
		}
		
		return true;
	}

	public List<MethodInvocation> getInvocations() {
		return invocations;
	}

	public void setInvocations(List<MethodInvocation> invocations) {
		this.invocations = invocations;
	}
}
