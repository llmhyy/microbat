package microbat.codeanalysis.ast;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import microbat.model.SourceScope;
import microbat.util.JavaUtil;

public class SourceScopeParser {
	
	class ScopeVisitor extends ASTVisitor{
		CompilationUnit cu;
		SourceScope scope;
		int conditionLineNumber;
		
		public ScopeVisitor(CompilationUnit cu, int lineNumber){
			this.cu = cu;
			this.conditionLineNumber = lineNumber;
		}
		
		public boolean visit(DoStatement statement){
			int line = cu.getLineNumber(statement.getExpression().getStartPosition());
			if(line == conditionLineNumber){
				setScope(statement, true);
				return false;
			}
			
			return true;
		}
		
		public boolean visit(EnhancedForStatement statement){
			int line = cu.getLineNumber(statement.getStartPosition());
			if(line == conditionLineNumber){
				setScope(statement, true);
				return false;
			}
			return true;
		}
		
		public boolean visit(ForStatement statement){
			int line = cu.getLineNumber(statement.getStartPosition());
			if(line == conditionLineNumber){
				setScope(statement, true);
				return false;
			}
			return true;
		}
		
		public boolean visit(IfStatement statement){
			int line = cu.getLineNumber(statement.getExpression().getStartPosition());
			if(line == conditionLineNumber){
				setScope(statement, false);
				return false;
			}
			return true;
		}
		
		public boolean visit(SwitchStatement statement){
			int line = cu.getLineNumber(statement.getExpression().getStartPosition());
			if(line == conditionLineNumber){
				setScope(statement, false);
				return false;
			}
			return true;
		}
		
		public boolean visit(WhileStatement statement){
			int line = cu.getLineNumber(statement.getExpression().getStartPosition());
			if(line == conditionLineNumber){
				setScope(statement, true);
				return false;
			}
			return true;
		}
		
		private void setScope(Statement statement, boolean isLoop){
			String className = JavaUtil.getFullNameOfCompilationUnit(cu);
			int start = cu.getLineNumber(statement.getStartPosition()); 
			int end = cu.getLineNumber(statement.getStartPosition()+statement.getLength()); 
			scope = new SourceScope(className, start, end, isLoop);
			
			JumpStatementFinder jumpStatementFinder = new JumpStatementFinder();
			statement.accept(jumpStatementFinder);
			scope.setHasJumpStatement(jumpStatementFinder.hasJumpStatement);
			
		}
	}
	
	class JumpStatementFinder extends ASTVisitor{
		boolean hasJumpStatement = false;
		
		public boolean visit(BreakStatement statement){
			hasJumpStatement = true;
			return false;
		}
		
		public boolean visit(ContinueStatement statement){
			hasJumpStatement = true;
			return false;
		}
		
		public boolean visit(ReturnStatement statement){
			hasJumpStatement = true;
			return false;
		}
		
		public boolean visit(ThrowStatement statement){
			hasJumpStatement = true;
			return false;
		}
	}
	
	public SourceScope parseScope(CompilationUnit cu, int conditionLineNumber){
		
		ScopeVisitor scopeVisitor = new ScopeVisitor(cu, conditionLineNumber);
		cu.accept(scopeVisitor);
		
		SourceScope scope = scopeVisitor.scope;
		return scope;
	}
}
