package microbat.evaluation.junit;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class MutationPointChecker extends ASTVisitor {

	private CompilationUnit cu;
	private int lineNumber;

	private boolean isLoopInsider = false;

	public MutationPointChecker(CompilationUnit cu, int lineNumber) {
		super();
		this.cu = cu;
		this.lineNumber = lineNumber;
	}

	@Override
	public boolean visit(VariableDeclarationStatement statement) {
		int linNum = cu.getLineNumber(statement.getStartPosition());
		if(linNum == this.lineNumber){
			if (isCaseI(statement) || isCaseII(statement)) {
				this.isLoopInsider = true;
			}			
		}

		return false;
	}

	@Override
	public boolean visit(ExpressionStatement statement) {
		int linNum = cu.getLineNumber(statement.getStartPosition());
		if(linNum == this.lineNumber){
			if (isCaseI(statement) || isCaseII(statement)) {
				this.isLoopInsider = true;
			}			
		}

		return false;
	}

	/**
	 * For the following case: for{ *** if(){ ... } else{ ... } }
	 * 
	 */
	private boolean isCaseI(Statement statement) {
		ASTNode node = findLoopParent(statement);
		if (node != null) {
			IfStatementChecker checker = new IfStatementChecker(statement, cu);
			node.accept(checker);
			if (checker.isValid) {
				return true;
			}
		}

		return false;
	}

	class IfStatementChecker extends ASTVisitor {
		CompilationUnit cu;
		ASTNode node;
		boolean isValid = false;

		public IfStatementChecker(ASTNode node, CompilationUnit cu) {
			this.node = node;
			this.cu = cu;
		}

		public boolean visit(IfStatement ifStatement) {
			if (ifStatement.getElseStatement() != null) {
				int nodeLineNumber = cu.getLineNumber(node.getStartPosition() + node.getLength());
				int ifStatementLineNumber = cu.getLineNumber(ifStatement.getStartPosition());

				if (nodeLineNumber < ifStatementLineNumber) {
					isValid = true;
				}
			}

			return false;
		}
	}

	/**
	 * For the following case: *** for{ if(){ ... } else{ ... } }
	 * 
	 */
	private boolean isCaseII(Statement statement) {

		ASTNode parent = statement.getParent();

		LoopStatementChecker checker = new LoopStatementChecker(statement, cu);
		parent.accept(checker);

		return checker.isValid;
	}

	

	/**
	 * For the following case: for{ if(){ *** } else{ *** } }
	 * 
	 */
	public boolean visit(IfStatement statement) {
		int start = cu.getLineNumber(statement.getExpression().getStartPosition());
		int end = cu.getLineNumber(statement.getStartPosition() + statement.getLength());
		if (start < lineNumber && lineNumber <= end) {
			if (findLoopParent(statement) != null && statement.getElseStatement() != null) {
				setLoopInsider(true);
			}
		}

		return false;
	}

	private ASTNode findLoopParent(Statement statement) {
		ASTNode parent = statement.getParent();
		while (parent != null) {
			if (isLoopASTNode(parent)) {
				return parent;
			}
			parent = parent.getParent();
		}

		return null;
	}

	private boolean isLoopASTNode(ASTNode node) {
		return (node instanceof DoStatement) || (node instanceof EnhancedForStatement) || (node instanceof ForStatement)
				|| (node instanceof WhileStatement);
	}

	public boolean isLoopInsider() {
		return isLoopInsider;
	}

	public void setLoopInsider(boolean isLoopInsider) {
		this.isLoopInsider = isLoopInsider;
	}
}
