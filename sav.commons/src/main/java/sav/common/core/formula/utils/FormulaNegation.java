/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.formula.utils;

import sav.common.core.formula.AndFormula;
import sav.common.core.formula.ConjunctionFormula;
import sav.common.core.formula.False;
import sav.common.core.formula.Formula;
import sav.common.core.formula.LIAAtom;
import sav.common.core.formula.NotFormula;
import sav.common.core.formula.Operator;
import sav.common.core.formula.OrFormula;
import sav.common.core.formula.True;

/**
 * @author LLT
 *
 */
public class FormulaNegation extends ExpressionVisitor {
	private Formula notFormula;

	@Override
	public void visit(AndFormula and) {
		notFormula = new OrFormula();
		visitConjunctionFormula(and);
	}
	
	@Override
	public void visit(OrFormula or) {
		notFormula = new AndFormula();
		visitConjunctionFormula(or);
	}
	
	@Override
	public void visitConjunctionFormula(ConjunctionFormula cond) {
		for (Formula ele : cond.getElements()) {
			((ConjunctionFormula)notFormula).add(not(ele));
		}
	}
	
	@Override
	public void visit(False cond) {
		notFormula = True.getInstance();
	}
	
	@Override
	public void visit(True cond) {
		notFormula = False.getInstance();
	}
	
	@Override
	public void visit(LIAAtom liaAtom) {
		notFormula = new LIAAtom(liaAtom.getMVFOExpr(),
				notOf(liaAtom.getOperator()), liaAtom.getConstant());
	}
	
	private Operator notOf(Operator op) {
		return Operator.notOf(op);
	}
	
	public Formula getNotFormula() {
		return notFormula;
	}
	
	public static Formula not(Formula cond) {
		FormulaNegation visitor = new FormulaNegation();
		cond.accept(visitor);
		if (visitor.getNotFormula() == null) {
			return new NotFormula(cond);
		}
		return visitor.getNotFormula();
	}
}
