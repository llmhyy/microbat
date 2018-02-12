/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.formula.utils;
import static sav.common.core.formula.Formula.FALSE;
import static sav.common.core.formula.Formula.TRUE;
import sav.common.core.formula.AndFormula;
import sav.common.core.formula.ConjunctionFormula;
import sav.common.core.formula.Formula;
import sav.common.core.formula.Operator;
import sav.common.core.formula.OrFormula;

/**
 * @author LLT
 *
 */
public class FormulaConjunction extends ExpressionVisitor {
	private Formula result;
	private Formula newCond;
	private Operator op;

	@Override
	public void visitConjunctionFormula(ConjunctionFormula conj) {
		if (conj.getOperator() != op) {
			return;
		}
		ConjunctionFormula newConj = conj.createNew();
		newConj.add(newCond);
		for (Formula ele : conj.getElements()) {
			newConj.add(ele);
		}
		result = newConj;
	}
	
	@Override
	public void visit(AndFormula and) {
		if (Formula.TRUE.equals(newCond)) {
			result = and;
			return;
		}
		if (Formula.FALSE.equals(newCond)) {
			result = Formula.FALSE;
			return;
		}
		visitConjunctionFormula(and);
	}
	
	@Override
	public void visit(OrFormula or) {
		if (Formula.FALSE.equals(newCond)) {
			result = or;
			return;
		}
		if (Formula.TRUE.equals(newCond)) {
			result = Formula.TRUE;
			return;
		}
		visitConjunctionFormula(or);
	}
	
	/**
	 * remember that the order of the conjunction formula can impact on the
	 * simplifier result!!.
	 */
	public static Formula and(Formula curCond, Formula newCond) {
		if (curCond == null || TRUE.equals(curCond)) {
			return newCond;
		}
		
		if (newCond == null || TRUE.equals(newCond)) {
			return curCond;
		}
		
		Formula result = conjOf(curCond, newCond, Operator.AND);
		if (result == null) {
			return new AndFormula(newCond, curCond);
		}
		return result;
	}

	private static Formula conjOf(Formula curCond, Formula newCond, Operator op) {
		FormulaConjunction visitor = new FormulaConjunction();
		visitor.newCond = newCond;
		visitor.op = op;
		curCond.accept(visitor);
		if (visitor.result == null) {
			visitor.newCond = curCond;
			newCond.accept(visitor);
		}
		return visitor.result;
	}
	
	public static Formula or(Formula curCond, Formula newCond) {
		if (curCond == null || FALSE.equals(curCond)) {
			return newCond;
		}
		
		if (newCond == null || FALSE.equals(newCond)) {
			return curCond;
		}
		Formula result = conjOf(curCond, newCond, Operator.OR);
		if (result == null) {
			return new OrFormula(newCond, curCond);
		}
		return result;
	}
}
