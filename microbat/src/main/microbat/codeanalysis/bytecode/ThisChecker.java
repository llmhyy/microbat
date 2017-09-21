package microbat.codeanalysis.bytecode;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import microbat.model.BreakPoint;

public class ThisChecker extends ASTVisitor{
	boolean containsValidThis = false;
	
	CompilationUnit cu;
	BreakPoint point;
	public ThisChecker(CompilationUnit cu, BreakPoint point) {
		super();
		this.cu = cu;
		this.point = point;
	}
	
	private int getStartLine(ASTNode node){
		return cu.getLineNumber(node.getStartPosition());
	}
	
	private int getEndLine(ASTNode node){
		return cu.getLineNumber(node.getStartPosition()+node.getLength());
	}
	
	private boolean isRelevant(ASTNode node){
		if(node==null) {
			return false;
		}
		
		int start = getStartLine(node);
		int end = getEndLine(node);
		
		if(start<=point.getLineNumber()&&point.getLineNumber()<=end){
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean visit(Assignment assignment){
		Expression expr = assignment.getRightHandSide();
		if(isRelevant(expr)){
			if(expr.toString().contains("this")){
				containsValidThis = true;
				return false;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean visit(ReturnStatement statement){
		Expression expr = statement.getExpression();
		if(isRelevant(expr)){
			if(expr.toString().contains("this")){
				containsValidThis = true;
				return false;
			}
		}
		return false;
	}
	
	@Override
	public boolean visit(ForStatement statement){
		Expression expr = statement.getExpression();
		if(isRelevant(expr)){
			if(expr.toString().contains("this")){
				containsValidThis = true;
				return false;
			}
		}
		return false;
	}
	
	@Override
	public boolean visit(IfStatement statement){
		Expression expr = statement.getExpression();
		if(isRelevant(expr)){
			if(expr.toString().contains("this")){
				containsValidThis = true;
				return false;
			}
		}
		return false;
	}
	
	@Override
	public boolean visit(ThrowStatement statement){
		Expression expr = statement.getExpression();
		if(isRelevant(expr)){
			if(expr.toString().contains("this")){
				containsValidThis = true;
				return false;
			}
		}
		return false;
	}
	
	@Override
	public boolean visit(WhileStatement statement){
		Expression expr = statement.getExpression();
		if(isRelevant(expr)){
			if(expr.toString().contains("this")){
				containsValidThis = true;
				return false;
			}
		}
		return false;
	}
}
