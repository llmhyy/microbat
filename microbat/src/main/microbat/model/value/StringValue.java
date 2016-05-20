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
//	private static final String LENGTH_CODE = "length";
//	private static final String IS_EMPTY = "isEmpty";
	
	public StringValue(String val, boolean isRoot, Variable var) {
		super(val, isRoot, var);
		var.setType("String");
//		BooleanValue child = new BooleanValue(getChildId(IS_EMPTY), val.isEmpty(), false);
//		add(child);
//		child.addParent(this);
//		add(new PrimitiveValue(getChildId(LENGTH_CODE), String.valueOf(val.length())));
	}
	
	@Override
	protected boolean needToRetrieveValue() {
		return false;
	}
	
	
//	@Override
//	public PrimitiveValue clone(){
//		StringValue clonedValue = new StringValue(getVarName(), getStrVal(), 
//				isRoot, isField, isStatic);
//		return clonedValue;
//	}
}
