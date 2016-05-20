package microbat.codeanalysis.ast;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodDeclarationFinder extends ASTVisitor{
	
	private CompilationUnit cu;
	private int linNumber;
	
	private MethodDeclaration method;
	private int methodStartLine;
	private int methodEndLine;
	
	public MethodDeclarationFinder(CompilationUnit cu, int linNumber){
		this.cu = cu;
		this.linNumber = linNumber;
	}
	
	public boolean visit(MethodDeclaration md){
		int startLine = cu.getLineNumber(md.getStartPosition());
		int endLine = cu.getLineNumber(md.getStartPosition()+md.getLength());
		
		if(startLine<=linNumber && linNumber<=endLine){
			if(this.getMethod() == null){
				this.setMethod(md);
				this.methodStartLine = startLine;
				this.methodEndLine = endLine;
			}
			else{
				if(startLine>=this.methodStartLine && endLine<=this.methodEndLine){
					this.setMethod(md);
					this.methodStartLine = startLine;
					this.methodEndLine = endLine;
				}
			}
		}
		
		return true;
	}

	public MethodDeclaration getMethod() {
		return method;
	}

	public void setMethod(MethodDeclaration method) {
		this.method = method;
	}
}
