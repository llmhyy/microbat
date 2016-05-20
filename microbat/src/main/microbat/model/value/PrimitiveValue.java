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
 * @author Yun Lin
 *
 */
public class PrimitiveValue extends VarValue {
	/**
	 * indicate the value in form of String
	 */
	//private String strVal;
	
//	private String primitiveType;

	public PrimitiveValue(String strVal, boolean isRoot, Variable variable) {
		super(isRoot, variable);
		this.stringValue = strVal;
//		this.primitiveType = type;
	}

//	public String getStrVal() {
//		return strVal;
//	}
//	
//	public void setStrVal(String strVal){
//		this.strVal = strVal;
//	}
	
//	@Override
//	public double getDoubleVal() {
//		try {
//			return Double.parseDouble(strVal);
//		} catch (NumberFormatException e) {
//			return super.getDoubleVal();
//		}
//	}
	
	@Override
	public String toString() {
		return String.format("(%s:%s)", getVarName(), this.stringValue);
	}

	
	public String getPrimitiveType(){
		return this.variable.getType();
	}
	
	public void setPrimitiveType(String type){
		this.variable.setType(type);
	}

	@Override
	public boolean isTheSameWith(GraphNode nodeAfter) {
		if(nodeAfter instanceof PrimitiveValue){
			PrimitiveValue pv = (PrimitiveValue)nodeAfter;
			return this.getStringValue().equals(pv.getStringValue());
		}
		return false;
	}
	
	@Override
	public String getManifestationValue() {
		return stringValue + " (id=" + variable.getVarID() + ")";
	}
	
	
	
//	@Override
//	public PrimitiveValue clone(){
//		PrimitiveValue clonedValue = new PrimitiveValue(getVarName(), strVal, 
//				getPrimitiveType(), isRoot, isField, isStatic);
//		return clonedValue;
//	}

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
