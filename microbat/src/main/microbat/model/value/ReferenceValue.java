/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package microbat.model.value;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import microbat.model.variable.Variable;

/**
 * @Yun Lin
 *
 */

public class ReferenceValue extends VarValue {
	
	private long uniqueID;
	
	public ReferenceValue(boolean isNull, boolean isRoot, Variable variable) {
		super(isRoot, variable);
	}
	
	public ReferenceValue(boolean isNull, long referenceID, boolean isRoot, Variable variable) {
		super(isRoot, variable);
		this.variable.setVarID(String.valueOf(referenceID));
		this.uniqueID = referenceID;
	}
	
	
	public String getClassType(){
		return this.variable.getType();
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(getVariable());
		buffer.append(": ");
		
//		if(getClassType() != null){
//			buffer.append(getClassType() + ": ");			
//		}
//		else{
//			buffer.append("unknown type: ");
//		}
		
		buffer.append(getVarID());
		String print = buffer.toString();
		
		return print;
	}
	
	public static ReferenceValue nullValue(Variable var) {
		return new ReferenceValue(true, false, var);
	}
	
	public String getReferenceID() {
		return getVarID();
	}

	public void setReferenceID(long referenceID) {
		this.variable.setVarID(String.valueOf(referenceID));
	}
	
	public String getConciseTypeName(){
		String qualifiedName = getClassType();
		String conciseName = qualifiedName.substring(qualifiedName.lastIndexOf(".")+1, qualifiedName.length());
		return conciseName;
	}
	
	public String getStringContainingAllChildren() {
		StringBuffer buffer = new StringBuffer();
		String value = this.getVarName() + "=" + this.getStringValue();
		value = value.replaceAll("\\(id=\\d+\\)", "");
		buffer.append(value);
		
		List<VarValue> children = this.getAllDescedentChildren();
		Collections.sort(children, new Comparator<VarValue>() {
			@Override
			public int compare(VarValue o1, VarValue o2) {
				String str1 = (o1.stringValue==null)?"null":o1.stringValue;
				String str2 = (o2.stringValue==null)?"null":o2.stringValue;
				
				return str1.compareTo(str2);
			}
		});
		
		for(VarValue var: children) {
			String childValue = var.getVarName() + "=" + var.getStringValue();
			if(childValue.contains("id=")){
				childValue = childValue.replaceAll("\\(id=\\d+\\)", "");				
			}
			buffer.append(childValue);
		}
		
		return buffer.toString();
	}

	@Override
	public boolean isTheSameWith(GraphNode nodeAfter) {
		
		if(nodeAfter instanceof ReferenceValue){
			ReferenceValue thatRef = (ReferenceValue)nodeAfter;
			
			if(this.isDefinedToStringMethod() && thatRef.isDefinedToStringMethod()){
				String thisString = getStringValue();
				String thatString = thatRef.getStringValue();
				
				thisString = thisString.replaceAll("\\(id=\\d+\\)", "");
				thatString = thatString.replaceAll("\\(id=\\d+\\)", "");
				
				return thisString.equals(thatString);
			}
			else if(!this.isDefinedToStringMethod() && !thatRef.isDefinedToStringMethod()){
				return true;
			}
		}
		
		
		return false;
	}
	
	@Override
	public String getManifestationValue() {
		String str = "(id = " + getVarID() + ")";
		return stringValue + " " + str;
	}

	public long getUniqueID() {
		return uniqueID;
	}

	public void setUniqueID(long uniqueID) {
		this.uniqueID = uniqueID;
	}
	
}
