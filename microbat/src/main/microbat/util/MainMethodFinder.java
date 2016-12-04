package microbat.util;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

public class MainMethodFinder extends ASTVisitor {
	private MethodDeclaration md;
	
	@SuppressWarnings("rawtypes")
	public boolean visit(MethodDeclaration md){
		String methodName = md.getName().getFullyQualifiedName();
		if(methodName.equals("main")){
			List list = md.modifiers();
			if(list != null && list.toString().equals("[public, static]")){
				String returnedType = md.getReturnType2().toString();
				if(returnedType.equals("void")){
					Object obj = md.parameters().get(0);
					SingleVariableDeclaration svd = (SingleVariableDeclaration)obj;
					Type type = svd.getType();
					
					if(type.toString().equals("String[]")){
						this.md = md;
					}
				}
			}
		}
		return false;
	}

	public MethodDeclaration getMd() {
		return md;
	}

	public void setMd(MethodDeclaration md) {
		this.md = md;
	}
}
