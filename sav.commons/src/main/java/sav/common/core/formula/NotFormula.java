package sav.common.core.formula;

import java.util.List;

import sav.common.core.formula.utils.ExpressionVisitor;

/**
 * donot init this formula, call FormulaNegation instead.
 *
 */
public final class NotFormula implements Formula {
	private Formula operand;

	public NotFormula(Formula operand) {
		this.operand = operand;
	}

	public List<Var> getReferencedVariables() {
		return operand.getReferencedVariables();
	}

	@Override
	public String toString() {
		return "!" + operand.toString();
	}

	public List<Atom> getAtomics() {
		return this.operand.getAtomics();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof NotFormula)) {
			return false;
		}

		NotFormula obj = (NotFormula) o;

		return obj.operand.equals(operand);
	}

	@Override
	public int hashCode() {
		return operand.hashCode();
	}

	@Override
	public void accept(ExpressionVisitor visitor) {
		visitor.visit(this);
	}
	
	public Formula getChild() {
		return operand;
	}
}
