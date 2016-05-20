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
public class BooleanValue extends PrimitiveValue {
	private boolean value;

	public BooleanValue(boolean value, boolean isRoot, Variable variable) {
		super(String.valueOf(value), isRoot, variable);
		this.value = value;
		
		this.variable.setType("boolean");
	}

//	@Override
//	public double getDoubleVal() {
//		if (value) {
//			return 1;
//		} else {
//			return 0;
//		}
//	}
	
//	public static BooleanValue of(String name, boolean value, boolean isRoot, boolean isField, boolean isStatic) {
//		return new BooleanValue(name, value, isRoot, isField, isStatic);
//	}
	
//	@Override
//	public PrimitiveValue clone(){
//		BooleanValue clonedValue = new BooleanValue(getVarName(), value, 
//				isRoot, isField, isStatic);
//		return clonedValue;
//	}
	
	public boolean getBoolValue(){
		return this.value;
	}
}
