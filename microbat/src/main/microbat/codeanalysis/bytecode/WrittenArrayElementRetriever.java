package microbat.codeanalysis.bytecode;

import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;

/**
 * TODO 
 * it is possible that two array elements are written in the same line. In this implementation, I do
 * not handle such case. An improvement is required in the future. 
 * @author "linyun"
 *
 */
public class WrittenArrayElementRetriever extends ASTNodeRetriever{
	String typeName;
	String arrayElementName;
	
	public WrittenArrayElementRetriever(CompilationUnit cu, int lineNumber, String typeName){
		super(cu, lineNumber, "");
		this.typeName = typeName;
	}
	
	public boolean visit(Assignment assignment){
		int linNum = cu.getLineNumber(assignment.getStartPosition());
		if(linNum == lineNumber){
			Expression expr = assignment.getLeftHandSide();
			
			if(expr instanceof ArrayAccess){
				ArrayAccess access = (ArrayAccess)expr;
				Expression arrayExp = access.getArray();
				if(arrayExp instanceof Name){
					Name name = (Name)arrayExp;
					ITypeBinding typeBinding = name.resolveTypeBinding();
					if(typeBinding != null){
						if(typeBinding.isArray()){
							String arrayType = typeBinding.getElementType().getName();
							if(arrayType.equals(typeName)){
								arrayElementName = access.toString();
								return false;
							}
						}						
					}
					else{
						arrayElementName = access.toString();
						return false;
					}
				}
			}
			
		}
		return true;
	}
	
}
