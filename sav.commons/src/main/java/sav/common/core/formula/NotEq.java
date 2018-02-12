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
public class NotEq<T> extends Eq<T> {

	public NotEq(Var var, T value) {
		super(var, value);
		op = Operator.NE;
	}

	@Override
	public void accept(ExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
