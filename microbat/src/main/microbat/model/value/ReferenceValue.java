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
		if(getClassType() != null){
			buffer.append(getClassType() + ": ");			
		}
		else{
			buffer.append("unknown type: ");
		}
		
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
		return stringValue;
	}

	public long getUniqueID() {
		return uniqueID;
	}

	public void setUniqueID(long uniqueID) {
		this.uniqueID = uniqueID;
	}
	
}
