package microbat.codeanalysis.ast;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.WhileStatement;


public class LoopHeadParser extends ASTVisitor{
	CompilationUnit cu;
	int conditionLineNumber;
	
	private boolean isLoop = false;
	
	public LoopHeadParser(CompilationUnit cu, int lineNumber){
		this.cu = cu;
		this.conditionLineNumber = lineNumber;
	}
	
	public boolean visit(DoStatement statement){
		int line = cu.getLineNumber(statement.getExpression().getStartPosition());
		if(line == conditionLineNumber){
			isLoop = true;
			return false;
		}
		
		return true;
	}
	
	public boolean visit(EnhancedForStatement statement){
		int line = cu.getLineNumber(statement.getStartPosition());
		if(line == conditionLineNumber){
			isLoop = true;
			return false;
		}
		return true;
	}
	
	public boolean visit(ForStatement statement){
		int line = cu.getLineNumber(statement.getExpression().getStartPosition());
		if(line == conditionLineNumber){
			isLoop = true;
			return false;
		}
		return true;
	}
	
	public boolean visit(WhileStatement statement){
		int line = cu.getLineNumber(statement.getExpression().getStartPosition());
		if(line == conditionLineNumber){
			isLoop = true;
			return false;
		}
		return true;
	}

	public boolean isLoop() {
		return isLoop;
	}

	public void setLoop(boolean isLoop) {
		this.isLoop = isLoop;
	}
}
