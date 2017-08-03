package microbat.codeanalysis.bytecode;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

public class ReadFieldRetriever extends ASTNodeRetriever{
	String fullFieldName;
	
	public ReadFieldRetriever(CompilationUnit cu, int lineNumber, String varName){
		super(cu, lineNumber, varName);
	}
	
	public boolean visit(QualifiedName name){
		int linNum = cu.getLineNumber(name.getStartPosition());
		if(linNum == lineNumber){
			String qualifedName = name.getFullyQualifiedName();
			String namePart = name.getName().getIdentifier();
			
			if(namePart.equals(varName)){
				fullFieldName = qualifedName;
				return false;
			}
		}
		return true;
	}
	
	public boolean visit(SimpleName name){
		int linNum = cu.getLineNumber(name.getStartPosition());
		if(linNum == lineNumber){
			String namePart = name.getIdentifier();
			if(namePart.equals(varName)){
				fullFieldName = namePart;
				return false;
			}
		}
		return true;
	}
	
	public boolean visit(FieldAccess access){
		int linNum = cu.getLineNumber(access.getStartPosition());
		if(linNum == lineNumber){
			if(access.getName().getIdentifier().equals(fullFieldName)) {
				fullFieldName = access.toString();
				return false;				
			}
		}
		return true;
	}
}
