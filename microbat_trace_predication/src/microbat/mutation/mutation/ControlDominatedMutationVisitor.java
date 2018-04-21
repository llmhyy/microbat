package microbat.mutation.mutation;

import japa.parser.ast.expr.EnclosedExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.expr.UnaryExpr.Operator;
import japa.parser.ast.stmt.IfStmt;
import mutation.mutator.MutationVisitor;

public class ControlDominatedMutationVisitor extends MutationVisitor {

	public ControlDominatedMutationVisitor() {
	}

	@Override
	public boolean mutate(IfStmt n) {
		IfStmt ifStmt = (IfStmt) nodeCloner.visit(n, null);
		Expression condition = n.getCondition();
		MutationNode muNode = newNode(n);
		if ((condition instanceof UnaryExpr) && 
				((UnaryExpr) condition).getOperator() == Operator.not) {
			ifStmt.setCondition(((UnaryExpr) condition).getExpr());
		} else {
			EnclosedExpr expr = new EnclosedExpr(condition);
			UnaryExpr newNode = new UnaryExpr(expr, Operator.not);
			ifStmt.setCondition(newNode);
		}
		muNode.add(ifStmt, MutationType.NEGATE_IF_CONDITION.name());
		return false;
	}
}
