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
public class StringVar implements Var {
	private String var;

	public StringVar(String var) {
		this.var = var;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	@Override
	public String toString() {
		return var;
	}

	@Override
	public String getLabel() {
		return var;
	}

	@Override
	public void accept(ExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
