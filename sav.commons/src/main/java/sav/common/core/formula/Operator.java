package sav.common.core.formula;

import java.util.ArrayList;
import java.util.List;

import sav.common.core.Pair;

public enum Operator {
	GE(">="), LE("<="), GT(">"), LT("<"), EQ("=="), NE("!="), AND("&&"), OR(
			"||");

	public static final List<Pair<Operator, Operator>> OPPOSITE_PAIRS;

	static {
		OPPOSITE_PAIRS = new ArrayList<Pair<Operator, Operator>>();
		OPPOSITE_PAIRS.add(Pair.of(GE, LT));
		OPPOSITE_PAIRS.add(Pair.of(LE, GT));
		OPPOSITE_PAIRS.add(Pair.of(EQ, NE));
		OPPOSITE_PAIRS.add(Pair.of(AND, OR));
	}
	private final String operator;

	private Operator(String op) {
		this.operator = op;
	}

	public boolean evaluate(double left, double right) {
		switch (this) {
		case GE:
			return left >= right;
		case LE:
			return left <= right;
		case GT:
			return left > right;
		case LT:
			return left < right;
		case EQ:
			return left == right;
		case NE:
			return left != right;
		default:
			return false;
		}
	}

	@Override
	public String toString() {
		return operator;
	}

	public String getCode() {
		return operator;
	}

	public String getCodeWithSpace() {
		return " " + getCode() + " ";
	}

	public Operator negateIfPlusNegValue(double coefficient) {
		if (coefficient < 0) {
			return notOf(this);
		}
		return this;
	}

	public Operator negative() {
		return notOf(this);
	}

	public static Operator notOf(Operator op) {
		for (Pair<Operator, Operator> pair : Operator.OPPOSITE_PAIRS) {
			if (op == pair.first()) {
				return pair.second();
			}
			if (op == pair.second()) {
				return pair.first();
			}
		}
		return null;
	}

	public boolean isGT() {
		return this == GT || this == GE;
	}

	public boolean isLT() {
		return this == LT || this == LE;
	}
}
