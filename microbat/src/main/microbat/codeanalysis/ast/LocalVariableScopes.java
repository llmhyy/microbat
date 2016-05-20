package microbat.codeanalysis.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;

public class LocalVariableScopes {
	private List<LocalVariableScope> variableScopes = new ArrayList<>();

	public List<LocalVariableScope> getVariableScopes() {
		return variableScopes;
	}

	public void setVariableScopes(List<LocalVariableScope> variableScopes) {
		this.variableScopes = variableScopes;
	}
	
	public LocalVariableScope findScope(String variableName, int appearedLineNum, String fullQualifiedTypeName){
		LocalVariableScope candScope = null;
		
		if(appearedLineNum == 60){
			System.currentTimeMillis();
		}
		
		for(LocalVariableScope scope: variableScopes){
			AbstractTypeDeclaration td = (AbstractTypeDeclaration) scope.getCompilationUnit().types().get(0);
			String typeName = td.resolveBinding().getQualifiedName();
			
			if(typeName.equals(fullQualifiedTypeName) && scope.getVariableName().equals(variableName) &&
					appearedLineNum >= scope.getStartLine() && appearedLineNum <= scope.getEndLine()){
				
				if(candScope == null){
					candScope = scope;					
				}
				else{
					if(candScope.getStartLine()<=scope.getStartLine() && candScope.getEndLine()>=scope.getEndLine()){
						candScope = scope;
					}
				}
			}
		}
		
		return candScope;
	}

	public void clear() {
		this.variableScopes.clear();
		
	}
}
