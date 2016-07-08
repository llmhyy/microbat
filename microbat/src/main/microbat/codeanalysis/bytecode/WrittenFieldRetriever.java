package microbat.codeanalysis.bytecode;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

public class WrittenFieldRetriever extends ASTNodeRetriever{
	String fullFieldName;
	
	public WrittenFieldRetriever(CompilationUnit cu, int lineNumber, String varName){
		super(cu, lineNumber, varName);
	}
	
	public boolean visit(Assignment assignment){
		int linNum = cu.getLineNumber(assignment.getStartPosition());
		if(linNum == lineNumber){
			Expression expr = assignment.getLeftHandSide();
			
			if(expr instanceof QualifiedName){
				QualifiedName qName = (QualifiedName)expr;
				fullFieldName = qName.getFullyQualifiedName();
			}
			else if(expr instanceof SimpleName){
				SimpleName sName = (SimpleName)expr;
				fullFieldName = sName.getFullyQualifiedName();
			}
			else if(expr instanceof FieldAccess){
				FieldAccess access = (FieldAccess)expr;
				fullFieldName = access.toString();
			}
			
			return false;
		}
		return true;
	}
	
	public boolean visit(FieldDeclaration fd){
		int linNum = cu.getLineNumber(fd.getStartPosition());
		if(linNum == lineNumber){
			fullFieldName = varName;
			return false;
		}
		return true;
	}
}
