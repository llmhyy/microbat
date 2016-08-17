/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.formula;

import java.util.List;

import sav.common.core.formula.utils.ExpressionVisitor;
import sav.common.core.utils.CollectionUtils;

/**
 * @author LLT
 *
 */
public abstract class VarAtom extends Atom {
	protected Var var;
	protected Operator op;
	
	public VarAtom(Var var, Operator op) {
		this.var = var;
		this.op = op;
	}

	public Operator getOperator() {
		return op;
	}
	
	public Var getVar() {
		return var;
	}

	@Override
	public List<Var> getReferencedVariables() {
		return CollectionUtils.listOf(var);
	}
	
	public abstract String getDisplayValue();

	@Override
	public void accept(ExpressionVisitor visitor) {
		visitor.visitVarAtom(this);
	}

}
