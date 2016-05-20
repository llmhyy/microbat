package microbat.codeanalysis.runtime.jpda.expr;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.QualifiedName;

public class QualifiedTypeNameFinder extends ASTVisitor {
	
	CompilationUnit cu;
	String name;
	
	String qualifiedTypeName;
	
	public QualifiedTypeNameFinder(CompilationUnit cu, String name){
		this.name = name;
		this.cu = cu;
	}
	
	public boolean visit(QualifiedName name){
		int lineNum = cu.getLineNumber(name.getStartPosition());
		if(lineNum == ExpressionParser.lineNumber){
			IBinding binding = name.resolveBinding();
			if(binding instanceof ITypeBinding){
				ITypeBinding tBind = (ITypeBinding)binding;
				String qualifedTypeName = tBind.getQualifiedName();
				
				if(this.name.startsWith(qualifedTypeName)){
					this.qualifiedTypeName = qualifedTypeName;
				}
			}
		}
		
		return true;
	}
}
