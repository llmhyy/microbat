/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.formula.utils;

import sav.common.core.formula.ConjunctionFormula;
import sav.common.core.formula.Formula;
import sav.common.core.formula.LIAAtom;
import sav.common.core.formula.LIATerm;
import sav.common.core.formula.NotFormula;
import sav.common.core.formula.Var;
import sav.common.core.formula.VarAtom;

/**
 * @author LLT
 *
 */
public class DisplayVisitor extends ExpressionVisitor {
	private StringBuilder sb;

	public DisplayVisitor() {
		sb = new StringBuilder();
	}
	
	@Override
	public void visit(Var var) {
		sb.append(var.getLabel());
	}

	@Override
	public void visit(NotFormula formula) {
		sb.append("!(");
		formula.getChild().accept(this);
		sb.append(")");
	}
	
	@Override
	public void visit(LIAAtom liaAtom) {
		int size = liaAtom.getMVFOExpr().size();
		for (int index = 0; index < size; index++) {
			LIATerm liaTerm = liaAtom.getMVFOExpr().get(index);
			if (index > 0 && liaTerm.getCoefficient() >= 0) {
				sb.append(" + ");
			}
			liaTerm.accept(this);
		}
		sb.append(liaAtom.getOperator().getCodeWithSpace());
		sb.append(liaAtom.getConstant());
	}
	
	@Override
	public void visit(LIATerm liaTerm) {
		if (liaTerm.getCoefficient() != 1) {
			sb.append("").append(liaTerm.getCoefficient()).append("*");
		}
		liaTerm.getVariable().accept(this);
	}
	
	@Override
	public void visitConjunctionFormula(ConjunctionFormula cond) {
		int size = cond.getElements().size();
		for (int index = 0; index < size; index++) {
			Formula clause = cond.getElements().get(index);
			sb.append("(");
			clause.accept(this);
			sb.append(")");
			if (index < size - 1) {
				sb.append(cond.getOperator().getCodeWithSpace());
			}
		}
	}
	
	@Override
	public void visitVarAtom(VarAtom atom) {
		atom.getVar().accept(this);
		sb.append(atom.getOperator().getCodeWithSpace());
		sb.append(atom.getDisplayValue());
	}
	
	public String getResult() {
		return sb.toString();
	}
}
