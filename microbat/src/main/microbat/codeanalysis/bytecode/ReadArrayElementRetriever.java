package microbat.codeanalysis.bytecode;

import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;

/**
 * TODO 
 * A rigorous implementation. I just find the first array access in a given source code line which has
 * the specific type. A more precise implementation is left in the future.
 * @author "linyun"
 *
 */
public class ReadArrayElementRetriever extends ASTNodeRetriever{
	String typeName;
	String arrayElementName;
	
	public ReadArrayElementRetriever(CompilationUnit cu, int lineNumber, String typeName){
		super(cu, lineNumber, "");
		this.typeName = typeName;
	}
	
	public boolean visit(ArrayAccess access){
		int linNum = cu.getLineNumber(access.getStartPosition());
		if(linNum == lineNumber){
			Expression arrayExp = access.getArray();
			if(arrayExp instanceof Name){
				Name name = (Name)arrayExp;
				ITypeBinding typeBinding = name.resolveTypeBinding();
				if(typeBinding.isArray()){
					String arrayType = typeBinding.getElementType().getName();
					if(arrayType.equals(typeName)){
						arrayElementName = access.toString();
						return false;
					}
				}
			}
		}
		return true;
	}
	
}