/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package microbat.model.value;

import microbat.model.variable.Variable;

/**
 * @author LLT
 *
 */
public class StringValue extends PrimitiveValue {
	
	public StringValue(String val, boolean isRoot, Variable var) {
		super(val, isRoot, var);
		var.setType("String");
	}
	
	@Override
	protected boolean needToRetrieveValue() {
		return false;
	}
	
	
}
