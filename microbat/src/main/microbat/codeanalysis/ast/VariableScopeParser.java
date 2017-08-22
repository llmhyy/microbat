package microbat.codeanalysis.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

import microbat.util.JavaUtil;
import sav.strategies.dto.AppJavaClassPath;

public class VariableScopeParser {

	private List<LocalVariableScope> variableScopeList = new ArrayList<>();
	
	public void parseLocalVariableScopes(List<String> interestingClasses, AppJavaClassPath appPath){
		System.currentTimeMillis();
		for(String qualifiedName: interestingClasses){
			CompilationUnit cu = JavaUtil.findCompilationUnitInProject(qualifiedName, appPath);
			parseLocalVariables(cu);
			
		}
	}

	private void parseLocalVariables(CompilationUnit cu) {
		cu.accept(new ASTVisitor() {
			public boolean visit(VariableDeclarationFragment fragment){
				SimpleName name = fragment.getName();
				ASTNode scope = findLeastContainingBlock(name);
				if(scope != null){
					LocalVariableScope lvs = new LocalVariableScope(name.getIdentifier(), scope);
					if(!variableScopeList.contains(lvs)){
						variableScopeList.add(lvs);						
					}
				}
				return true;
			}
			
			public boolean visit(SingleVariableDeclaration svd){
				SimpleName name = svd.getName();
				ASTNode scope = findLeastContainingBlock(name);
				if(scope != null){
					LocalVariableScope lvs = new LocalVariableScope(name.getIdentifier(), scope);
					if(!variableScopeList.contains(lvs)){
						variableScopeList.add(lvs);						
					}			
				}
				
				return false;
			}
			
			
		});
		
	}

	private ASTNode findLeastContainingBlock(ASTNode node){
		ASTNode parentNode = node.getParent();
		while(!(parentNode instanceof Block) && 
				!(parentNode instanceof MethodDeclaration) &&
				!(parentNode instanceof ForStatement) &&
				!(parentNode instanceof DoStatement) &&
				!(parentNode instanceof EnhancedForStatement) &&
				!(parentNode instanceof IfStatement) &&
				!(parentNode instanceof SwitchCase) &&
				!(parentNode instanceof TryStatement) &&
				!(parentNode instanceof WhileStatement)){
			parentNode = parentNode.getParent();
			if(parentNode == null){
				break;
			}
		}
		
		return parentNode;
	}
	
	public List<LocalVariableScope> getVariableScopeList() {
		return variableScopeList;
	}
	
//	public LocalVariableScope parseScope(BreakPoint breakPoint, LocalVar localVar){
//		final CompilationUnit cu = JavaUtil.findCompilationUnitInProject(breakPoint.getClassCanonicalName());
//		final String varName = localVar.getName();
//		final int lineNumber = breakPoint.getLineNo();
//		
//		cu.accept(new ASTVisitor(){
//			public boolean visit(SimpleName name){
//				int lin = cu.getLineNumber(name.getStartPosition());
//				if(lin == lineNumber && varName.equals(name.getIdentifier())){
//					if(name.resolveBinding() instanceof IVariableBinding){
//						ASTNode scope = findLeastContainingBlock(name);
//						if(scope != null){
//							LocalVariableScope lvs = new LocalVariableScope(name.getIdentifier(), scope);
//							variableScopeList.add(lvs);
//						}
//					}
//				}
//				
//				return false;
//			}
//		});
//		
//		/**
//		 * Analyzing the byte code could let us know that a "this" local variable is implicitly read or written, however,
//		 * we may not explicitly find a "this" in AST.
//		 */
//		if(this.variableScopeList.isEmpty() && varName.equals("this")){
//			return null;
//		}
//		else{
//			LocalVariableScope lvScope = this.variableScopeList.get(0);
//			return lvScope;			
//		}
//	}

	public LocalVariableScope parseMethodScope(String typeName, final int lineNumber, 
			final String variableName, AppJavaClassPath appPath) {
		final CompilationUnit cu = JavaUtil.findCompilationUnitInProject(typeName, appPath);
		
		if(cu == null){
			System.currentTimeMillis();
		}
		
		cu.accept(new ASTVisitor(){
			public boolean visit(MethodDeclaration md){
				
				int startLine = cu.getLineNumber(md.getStartPosition());
				int endLine = cu.getLineNumber(md.getStartPosition()+md.getLength());
				if(startLine <= lineNumber && endLine >= lineNumber){
					LocalVariableScope lvs = new LocalVariableScope(variableName, md);
					variableScopeList.add(lvs);
					
					return true;
				}
				
				return false;
			}
		});
		
		if(!variableScopeList.isEmpty()) {
			LocalVariableScope minScope = this.variableScopeList.get(0);
			for(LocalVariableScope scope: this.variableScopeList){
				if(scope.getStartLine()>minScope.getStartLine() && scope.getEndLine()<minScope.getEndLine()){
					minScope=scope;
				}
			}
			return minScope;			
		}
		else {
			return null;
		}
	}
}
