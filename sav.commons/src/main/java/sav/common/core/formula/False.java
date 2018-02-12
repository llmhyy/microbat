package sav.common.core.formula;

import java.util.ArrayList;
import java.util.List;

import sav.common.core.formula.utils.ExpressionVisitor;

public class False extends Atom {

	private final List<Var> variables = new ArrayList<Var>();

	private static final False instance = new False();

	private False() {
	}

	public static False getInstance() {
		return instance;
	}

	public List<Var> getReferencedVariables() {
		return variables;
	}

	@Override
	public String toString() {
		return "false";
	}

	public boolean evaluate(Object[] objects) {
		return false;
	}

	@Override
	public Formula restrict(List<Atom> vars, List<Integer> vals) {
		return this;
	}

	@Override
	public int hashCode() {
		return 3;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		return o instanceof False;
	}

	@Override
	public void accept(ExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
