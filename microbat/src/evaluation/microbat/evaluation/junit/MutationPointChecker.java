package microbat.evaluation.junit;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class MutationPointChecker extends ASTVisitor{
	
	private CompilationUnit cu;
	private int lineNumber;
	
	private boolean isLoopInsider = false;
	
	public MutationPointChecker(CompilationUnit cu, int lineNumber) {
		super();
		this.cu = cu;
		this.lineNumber = lineNumber;
	}
	
//	@Override
//	public boolean visit(Statement statement){
//		return true;
//	}
	
	public boolean visit(IfStatement statement){
		int start = cu.getLineNumber(statement.getExpression().getStartPosition());
		int end = cu.getLineNumber(statement.getStartPosition()+statement.getLength());
		if(start < lineNumber && lineNumber <= end){
			if(isContainedInLoop(statement)){
				setLoopInsider(true);				
			}
		}
		
		return false;
	}

	private boolean isContainedInLoop(IfStatement statement) {
		ASTNode parent = statement.getParent();
		while(parent != null){
			if(isLoopASTNode(parent)){
				return true;
			}
			parent = parent.getParent();
		}
		
		return false;
	}
	
	private boolean isLoopASTNode(ASTNode node){
		return (node instanceof DoStatement) ||
				(node instanceof EnhancedForStatement) ||
				(node instanceof ForStatement)||
				(node instanceof WhileStatement);
	}

//	public boolean visit(DoStatement statement){
//		int start = cu.getLineNumber(statement.getExpression().getStartPosition());
//		int end = cu.getLineNumber(statement.getStartPosition()+statement.getLength());
//		if(start <= lineNumber && lineNumber <= end){
//			setLoopInsider(true);
//		}
//		
//		return false;
//	}
//	
//	public boolean visit(EnhancedForStatement statement){
//		int start = cu.getLineNumber(statement.getExpression().getStartPosition());
//		int end = cu.getLineNumber(statement.getStartPosition()+statement.getLength());
//		if(start <= lineNumber && lineNumber <= end){
//			setLoopInsider(true);
//		}
//		
//		return false;
//	}
//	
//	public boolean visit(ForStatement statement){
//		int start = cu.getLineNumber(statement.getExpression().getStartPosition());
//		int end = cu.getLineNumber(statement.getStartPosition()+statement.getLength());
//		if(start <= lineNumber && lineNumber <= end){
//			setLoopInsider(true);
//		}
//		
//		return false;
//	}
//	
//	public boolean visit(WhileStatement statement){
//		int start = cu.getLineNumber(statement.getExpression().getStartPosition());
//		int end = cu.getLineNumber(statement.getStartPosition()+statement.getLength());
//		if(start <= lineNumber && lineNumber <= end){
//			setLoopInsider(true);
//		}
//		
//		return false;
//	}

	public boolean isLoopInsider() {
		return isLoopInsider;
	}

	public void setLoopInsider(boolean isLoopInsider) {
		this.isLoopInsider = isLoopInsider;
	}
}
