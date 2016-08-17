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
 *
 */
public class OrFormula extends ConjunctionFormula {
	
	public OrFormula() {
		super();
	}
	
	public OrFormula(Formula left, Formula right) {
		super(left, right);
	}

	@Override
	public Operator getOperator() {
		return Operator.OR;
	}

	@Override
	public ConjunctionFormula createNew() {
		return new OrFormula();
	}
	
	@Override
	public void accept(ExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
