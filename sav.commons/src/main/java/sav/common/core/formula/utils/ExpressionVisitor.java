/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.formula.utils;

import sav.common.core.formula.AndFormula;
import sav.common.core.formula.Atom;
import sav.common.core.formula.ConjunctionFormula;
import sav.common.core.formula.Eq;
import sav.common.core.formula.False;
import sav.common.core.formula.LIAAtom;
import sav.common.core.formula.LIATerm;
import sav.common.core.formula.NotEq;
import sav.common.core.formula.NotFormula;
import sav.common.core.formula.OrFormula;
import sav.common.core.formula.True;
import sav.common.core.formula.Var;
import sav.common.core.formula.VarAtom;

/**
 * @author LLT
 *
 */
public abstract class ExpressionVisitor {

	public void visit(NotFormula notFormula) {
		// do nothing by default 		
	}

	public void visit(True atom) {
		visitAtom(atom);
	}

	public void visit(False atom) {
		visitAtom(atom);
	}

	public void visit(LIAAtom liaAtom) {
		visitAtom(liaAtom);
	}
	
	public void visit(LIATerm liaTerm) {
		// do nothing by default		
	}

	public void visit(Var var) {
		// do nothing by default
	}
	
	public void visit(AndFormula and) {
		visitConjunctionFormula(and);
	}
	
	public void visit(OrFormula or) {
		visitConjunctionFormula(or);
	}

	public <T> void visit(Eq<T> eq) {
		visitVarAtom(eq);
	}
	
	public <T> void visit(NotEq<T> ne) {
		visitVarAtom(ne);
	}

	/**
	 * this part is for abstract formula, 
	 * if these abstract formula are visited (return true), their subClass will not be visited.
	 */
	public void visitConjunctionFormula(ConjunctionFormula conj) {
		// do nothing by default
	}

	public void visitAtom(Atom atom) {
		// do nothing by default
	}

	public void visitVarAtom(VarAtom varAtom) {
		// do nothing by default
	}

	public void visit(String var) {
		// do nothing by default
	}
}
