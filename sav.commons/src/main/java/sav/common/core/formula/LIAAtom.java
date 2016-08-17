package sav.common.core.formula;

import java.util.ArrayList;
import java.util.List;

import sav.common.core.formula.utils.ExpressionVisitor;

/**
 * The relational expression in Linear Integer Arithmetic establish the relation
 * between a set of {@link LIATerm}s and an integer constant.
 * 
 * @author Spencer Xiao
 * 
 */
public class LIAAtom extends Atom {

	private double constant;
	/**
	 * Multiple Variables First Order expression
	 */
	private List<LIATerm> MVFOExpr;
	private Operator operator;

	public LIAAtom(List<LIATerm> terms, Operator op, double right) {
		MVFOExpr = terms;
		operator = op;
		constant = right;
	}

	public List<Var> getReferencedVariables() {
		List<Var> result = new ArrayList<Var>(MVFOExpr.size());
		for (LIATerm term : MVFOExpr) {
			result.add(term.getVariable());
		}

		return result;
	}

	@Override
	public int hashCode() {
		return MVFOExpr.hashCode() * 31 + operator.hashCode() * 19
				+ (int) (constant * 1000);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof LIAAtom)) {
			return false;
		}

		LIAAtom obj = (LIAAtom) o;

		return MVFOExpr.equals(obj.MVFOExpr) && operator == obj.operator
				&& obj.constant == constant;
	}

	@Override
	public void accept(ExpressionVisitor visitor) {
		visitor.visit(this);
	}

	public List<LIATerm> getMVFOExpr() {
		return MVFOExpr;
	}
	
	public Operator getOperator() {
		return operator;
	}
	
	public double getConstant() {
		return constant;
	}

	/**
	 * only return first term is if the expression is only for 1 variable.
	 */
	public LIATerm getSingleTerm() {
		if (MVFOExpr.size() == 1) {
			return MVFOExpr.get(0);
		}
		return null;
	}
}
