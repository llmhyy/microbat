/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.formula.utils;

import static sav.common.core.formula.utils.AtomUtils.ATOMS_A_BOUND_B;
import static sav.common.core.formula.utils.AtomUtils.ATOMS_B_BOUND_A;
import static sav.common.core.formula.utils.AtomUtils.ATOMS_EQUAL;
import static sav.common.core.formula.utils.AtomUtils.ATOMS_NEGATION;
import static sav.common.core.formula.utils.AtomUtils.ATOMS_NO_RELATIONSHIP;

import java.util.List;

import sav.common.core.SavRtException;
import sav.common.core.formula.AndFormula;
import sav.common.core.formula.Atom;
import sav.common.core.formula.False;
import sav.common.core.formula.Formula;
import sav.common.core.formula.LIAAtom;
import sav.common.core.formula.NotFormula;
import sav.common.core.formula.OrFormula;
import sav.common.core.formula.True;

/**
 * @author LLT
 * 
 */
public class FormulaRestriction extends ExpressionVisitor {
	private List<Atom> vars;
	private List<Integer> vals;
	private Formula result;
	
	@Override
	public void visit(False atom) {
		result = atom;
	}
	
	@Override
	public void visit(True atom) {
		result = atom;
	}
	
	@Override
	public void visit(AndFormula and) {
		AndFormula newAnd = new AndFormula();
		for (Formula clause : and.getElements()) {
			// TODO LLT: NICE TO HAVE - extend the current visitor instead?
			Formula expr = restrict(clause, vars, vals);
			if (Formula.FALSE.equals(expr)) {
				result = Formula.FALSE;
				return;
			} else if (!Formula.TRUE.equals(expr)) {
				newAnd.add(expr);
			}
		}

		if (newAnd.getElements().size() == 0) {
			result = Formula.TRUE;
		} else if (newAnd.getElements().size() == 1) {
			result = newAnd.getElements().get(0);
		} else {
			result = newAnd;
		}
	}
	
	@Override
	public void visit(OrFormula or) {
		OrFormula newOr = new OrFormula();
		for (Formula term : or.getElements()) {
			Formula expr = restrict(term, vars, vals);
			if (Formula.TRUE.equals(expr)) {
				result = Formula.TRUE;
				return;
			} else if (!Formula.FALSE.equals(expr)) {
				newOr.add(expr);
			}
		}

		if (newOr.getElements().size() == 0) {
			result = Formula.FALSE;
		} else if (newOr.getElements().size() == 1) {
			result = newOr.getElements().get(0);
		} else {
			result = newOr;
		}
	}
	
	@Override
	public void visitAtom(Atom atom) {
		for (int index = 0; index < vars.size(); index++) {
			Atom curAtom = vars.get(index);
			if (atom.equals(curAtom)) {
				result = associatedTo(vals.get(index));
				break;
			}
		}
	}
	
	@Override
	public void visit(LIAAtom liaAtom) {
		if (liaAtom.getMVFOExpr().size() != 1) {
			super.visit(liaAtom);
			return;
		}
		for (int index = 0; index < vars.size(); index++) {
			Atom curAtom = vars.get(index);
			if (curAtom instanceof LIAAtom) {
				int relationship = AtomUtils.getRelationship((LIAAtom)curAtom, liaAtom);
				if (relationship == ATOMS_NO_RELATIONSHIP) {
					continue;
				}
				Formula curVal = associatedTo(vals.get(index));
				if (relationship == ATOMS_EQUAL) {
					result = curVal;
					break;
				}					
				if (relationship == ATOMS_NEGATION) {
					result = FormulaUtils.not(curVal);
					break;
				}
				if (relationship == ATOMS_A_BOUND_B && curVal == Formula.TRUE) {
					result = curVal;
					break;
				}
				if (relationship == ATOMS_B_BOUND_A && curVal == Formula.FALSE) {
					result = curVal;
					break;
				}
			}
		}
	}
	
	private Formula associatedTo(int binaryValue) {
		if (binaryValue == 0) {
			return Formula.FALSE;
		}
		if (binaryValue == 1) {
			return Formula.TRUE;
		}
		throw new SavRtException("Expect binary value(0, 1), get "
				+ binaryValue);
	}

	@Override
	public void visit(NotFormula notFormula) {
		result = FormulaUtils.not(restrict(notFormula.getChild(), vars, vals));
	}
	
	public static Formula restrict(Formula formula, List<Atom> vars, List<Integer> vals) {
		FormulaRestriction visitor = new FormulaRestriction();
		visitor.vars = vars;
		visitor.vals = vals;
		formula.accept(visitor);
		if (visitor.result == null) {
			return formula;
		}
		return visitor.result;
	}

}
