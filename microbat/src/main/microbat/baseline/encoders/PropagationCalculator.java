package microbat.baseline.encoders;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import microbat.baseline.Configs;
import microbat.model.value.VarValue;

/**
 * Propagation calculator is used to calculate the propagation probability of different statement.
 * It is used when baseline is creating A2 constraint
 * @author David
 *
 */
public class PropagationCalculator {
	
	public double calProb(ASTNode node, VarValue var) {
		double propagationProb = -1;
		// System.out.println("Handling: " + node);
		switch (node.getNodeType()) {
		case ASTNode.VARIABLE_DECLARATION_STATEMENT:
			VariableDeclarationStatement varDeclaraStat = (VariableDeclarationStatement) node;
			propagationProb = this.handleVarDeclaraStat(varDeclaraStat, var);
			break;
		case ASTNode.INFIX_EXPRESSION:
			InfixExpression infixExp = (InfixExpression) node;
			propagationProb = this.handleInfixExp(infixExp, var);
			break;
		default:
			propagationProb = Configs.UNCERTAIN;
			System.out.println("AST Node Type not handled: " + this.getASTNodeTypeName(node));
		}
		return propagationProb;
	}
	
	/**
	 * Handle infix expression.
	 * Probability depends on the operator
	 * @param infixExp Infix expression
	 * @param var Target variable
	 * @return Propagation probability
	 */
	private double handleInfixExp(InfixExpression infixExp, VarValue var) {
		double prob = -1;
		
		InfixExpression.Operator operator = infixExp.getOperator();
		if (operator == InfixExpression.Operator.EQUALS || operator == InfixExpression.Operator.TIMES ||
		    operator == InfixExpression.Operator.DIVIDE || operator == InfixExpression.Operator.PLUS ||
		    operator == InfixExpression.Operator.MINUS || operator == InfixExpression.Operator.CONDITIONAL_AND)
			// One to one operator
			prob = Configs.HIGH;
		else if (operator == InfixExpression.Operator.REMAINDER) {
			
		}
		return prob;
	}

	/**
	 * Handle Variable declaration statement.
	 * Initializer needed to be analysis to determine is it one to one or not.
	 * @param varDeclaraStat Variable declaration statement
	 * @param var Target read variable
	 * @return Propagation probability
	 */
	private double handleVarDeclaraStat(VariableDeclarationStatement varDeclaraStat, VarValue var) {
		// Variable declaration statement is not enough to defined as one to one
		// It depends on the initializer expression is one to one or not
		@SuppressWarnings("unchecked") List<VariableDeclarationFragment> fragments = varDeclaraStat.fragments();
		
		double propagationProb = -1;
		// Usually, there should be only one fragment
		if (fragments.size() > 1) {
			System.out.println("Variable Encoder Unhandle case: fragments have size more than one");
			return propagationProb;
		}
		
		for (VariableDeclarationFragment fragment : fragments) {
			System.out.println(fragment);
			Expression initializer = fragment.getInitializer();
			propagationProb = this.calProb(initializer, var);
		} 
		
		return propagationProb;
	}
	
	private boolean sameVar(String name, VarValue var) {
		return false;
	}
	/**
	 * Helper function to check the type of AST node. Mainly for debug purpose
	 * @param astNode AST Node to check
	 * @return Name of the type
	 */
	public String getASTNodeTypeName(ASTNode astNode) {
		return ASTNode.nodeClassForType(astNode.getNodeType()).getName();
	} 
}
