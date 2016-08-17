/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.dto.execute.value;

import sav.common.core.formula.Var;
import sav.common.core.formula.utils.ExpressionVisitor;

/**
 * @author LLT
 * 
 */
public class ExecVar implements Var {
	private final String varId;
	private ExecVarType type;

	public ExecVar(String varId) {
		this.varId = varId;
	}

	public ExecVar(String varId, ExecVarType type) {
		this.varId = varId;
		this.type = type;
	}

	public String getVarId() {
		return varId;
	}

	public ExecVarType getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((varId == null) ? 0 : varId.hashCode());
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
		ExecVar other = (ExecVar) obj;
		if (varId == null) {
			if (other.varId != null)
				return false;
		} else if (!varId.equals(other.varId))
			return false;
		return true;
	}
	
	@Override
	public String getLabel() {
		return varId;
	}
	
	@Override
	public String toString() {
		return getLabel();
	}

	@Override
	public void accept(ExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
