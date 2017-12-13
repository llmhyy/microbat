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
	public static final String TYPE = "String";
	
	public StringValue(String val, boolean isRoot, Variable var) {
		super(val, isRoot, var);
		var.setType(TYPE);
	}
	
	@Override
	protected boolean needToRetrieveValue() {
		return false;
	}
	
	
}
