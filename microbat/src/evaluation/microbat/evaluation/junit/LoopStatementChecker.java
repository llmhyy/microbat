package microbat.evaluation.junit;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * This class checks whether a given AST node is above loop.
 * 
 * @author Yun Lin
 *
 */

public class LoopStatementChecker extends ASTVisitor {
	CompilationUnit cu;
	ASTNode node;

	boolean isValid = false;

	public LoopStatementChecker(ASTNode node, CompilationUnit cu) {
		this.node = node;
		this.cu = cu;
	}

	public boolean visit(DoStatement statement) {
		if(isNodeAboveLoop(node, statement)){
			this.isValid = true;
		}
		return false;
	}

	public boolean visit(EnhancedForStatement statement) {
		if(isNodeAboveLoop(node, statement)){
			this.isValid = true;
		}
		return false;
	}

	public boolean visit(ForStatement statement) {
		if(isNodeAboveLoop(node, statement)){
			this.isValid = true;
		}
		return false;
	}

	public boolean visit(WhileStatement statement) {
		if(isNodeAboveLoop(node, statement)){
			this.isValid = true;
		}
		return false;
	}

	public boolean isNodeAboveLoop(ASTNode node, Statement loopStatement) {
		int nodeLineNumber = cu.getLineNumber(node.getStartPosition() + node.getLength());
		int statementLineNumber = cu.getLineNumber(loopStatement.getStartPosition());

		return nodeLineNumber < statementLineNumber;
	}
}
