/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.formula;

import sav.common.core.formula.utils.ExpressionVisitor;
import sav.common.core.utils.StringUtils;


/**
 * @author LLT
 *
 */
public class Eq<T> extends VarAtom {
	private T value;
	
	public Eq(Var var, T value) {
		super(var, Operator.EQ);
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}
	
	@Override
	public String getDisplayValue() {
		return StringUtils.toString(value, "null");
	}
	
	@Override
	public void accept(ExpressionVisitor visitor) {
		visitor.visit(this); 
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + ((var == null) ? 0 : var.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Eq<?> other = (Eq<?>) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		if (var == null) {
			if (other.var != null)
				return false;
		} else if (!var.equals(other.var))
			return false;
		return true;
	}
	
	
}
