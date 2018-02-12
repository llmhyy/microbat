/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.dto.execute.value;



/**
 * @author LLT, modified by Yun Lin
 *
 */
public class PrimitiveValue extends ExecValue {
	/**
	 * indicate the value in form of String
	 */
	private String strVal;
	
	private String primitiveType;

	public PrimitiveValue(String name, String strVal, String type, boolean isRoot, boolean isField, boolean isStatic) {
		super(name, isRoot, isField, isStatic);
		this.strVal = strVal;
		this.primitiveType = type;
	}

	public String getStrVal() {
		return strVal;
	}
	
	@Override
	public double getDoubleVal() {
		try {
			return Double.parseDouble(strVal);
		} catch (NumberFormatException e) {
			return super.getDoubleVal();
		}
	}
	
	@Override
	public String toString() {
		return String.format("(%s:%s)", varName, strVal);
	}

	@Override
	public ExecVarType getType() {
		return ExecVarType.PRIMITIVE;
	}
	
	public String getPrimitiveType(){
		return this.primitiveType;
	}
	
	public void setPrimitiveType(String type){
		this.primitiveType = type;
	}

	@Override
	public boolean isTheSameWith(GraphNode nodeAfter) {
		if(nodeAfter instanceof PrimitiveValue){
			PrimitiveValue pv = (PrimitiveValue)nodeAfter;
			return this.getStrVal().equals(pv.getStrVal());
		}
		return false;
	}
	
	@Override
	public PrimitiveValue clone(){
		PrimitiveValue clonedValue = new PrimitiveValue(getVarName(), strVal, 
				getPrimitiveType(), isRoot, isField, isStatic);
		return clonedValue;
	}

//	@Override
//	public boolean match(GraphNode node) {
//		if(node instanceof PrimitiveValue){
//			PrimitiveValue thatValue = (PrimitiveValue)node;
//			if(thatValue.getPrimitiveType().equals(this.getPrimitiveType()) &&
//					thatValue.getStrVal().equals(this.getStrVal())){
//				return true;
//			}
//		}
//		return false;
//	}
}
