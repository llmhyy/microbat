package microbat.codeanalysis.runtime.jpda.expr;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;

public class NameFinder extends ASTVisitor{
	public static final String PACKAGE = "package";
	public static final String TYPE = "type";
	
	String qualifiedTypeName;
	String type;
	
	CompilationUnit cu;
	String token;
	
	public NameFinder(CompilationUnit cu, String token){
		this.cu = cu;
		this.token = token;
	}
	
	public boolean visit(SimpleName name){
		int lineNumber = cu.getLineNumber(name.getStartPosition());
		if(lineNumber == ExpressionParser.lineNumber){
			String identifier = name.getIdentifier();
			if(identifier.equals(token)){
				IBinding binding = name.resolveBinding();
				if(binding instanceof ITypeBinding){
					type = TYPE;
					qualifiedTypeName = ((ITypeBinding)binding).getQualifiedName();
				}
				else if(binding instanceof IPackageBinding){
					type = PACKAGE;
				}
			}
		}
		
		return false;
	}
}
