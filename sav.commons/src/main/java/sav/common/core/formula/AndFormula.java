/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.formula;

import sav.common.core.formula.utils.ExpressionVisitor;

/**
 * @author LLT 
 * replace CNF
 */
public class AndFormula extends ConjunctionFormula {
	
	public AndFormula(Formula left, Formula right) {
		super(left, right);
	}
	
	public AndFormula() {
		super();
	}

	@Override
	public ConjunctionFormula createNew() {
		return new AndFormula();
	}

	@Override
	public Operator getOperator() {
		return Operator.AND;
	}

	@Override
	public void accept(ExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
