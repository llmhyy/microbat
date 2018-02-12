package sav.common.core.formula;

import sav.common.core.formula.utils.ExpressionVisitor;

public interface Var {
	public String getLabel();
	
	public void accept(ExpressionVisitor visitor);
}
