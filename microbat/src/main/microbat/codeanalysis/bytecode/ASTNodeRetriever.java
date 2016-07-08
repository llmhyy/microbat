package microbat.codeanalysis.bytecode;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class ASTNodeRetriever extends ASTVisitor{
	CompilationUnit cu;
	int lineNumber;
	String varName;
	
	public ASTNodeRetriever(CompilationUnit cu, int lineNumber, String varName){
		this.cu = cu;
		this.lineNumber = lineNumber;
		this.varName = varName;
	}
}
