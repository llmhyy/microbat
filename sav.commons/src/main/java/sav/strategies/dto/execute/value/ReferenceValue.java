/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.dto.execute.value;

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.ClassType;

/**
 * @author LLT, modified by Yun Lin
 *
 */
@SuppressWarnings("restriction")
public class ReferenceValue extends ExecValue {
	protected static final String NULL_CODE = "isNull";
	
	private ClassType classType; 
	/**
	 * The virtual memory address
	 */
	private long referenceID = -1;

	public ReferenceValue(String name, boolean isNull, boolean isRoot, boolean isField, boolean isStatic) {
		super(name, isRoot, isField, isStatic);
//		BooleanValue child = BooleanValue.of(getChildId(NULL_CODE), isNull, false, true, false);
		BooleanValue child = BooleanValue.of(NULL_CODE, isNull, false, true, false);
		addChild(child);
		child.addParent(this);
	}
	
	public ReferenceValue(String id, boolean isNull, long referenceID, ClassType type, 
			boolean isRoot, boolean isField, boolean isStatic) {
		super(id, isRoot, isField, isStatic);
//		BooleanValue child = BooleanValue.of(getChildId(NULL_CODE), isNull, false, true, false);
		BooleanValue child = BooleanValue.of(NULL_CODE, isNull, false, true, false);
		addChild(child);
		child.addParent(this);
		
		setReferenceID(referenceID);
		setClassType(type);
	}
	
	public void setClassType(ClassType type) {
		this.classType = type;
	}
	
	public ClassType getClassType(){
		return classType;
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		if(classType != null){
			buffer.append(classType.name() + ": ");			
		}
		else{
			buffer.append("unknown type: ");
		}
		
		buffer.append(referenceID);
		String print = buffer.toString();
		
		return print;
	}
	
	public static ReferenceValue nullValue(String id, boolean isField, boolean isStatic) {
		return new ReferenceValue(id, true, false, isField, isStatic);
	}
	
	public long getReferenceID() {
		return referenceID;
	}

	public void setReferenceID(long referenceID) {
		this.referenceID = referenceID;
	}
	
	public String getConciseTypeName(){
		String qualifiedName = classType.name();
		String conciseName = qualifiedName.substring(qualifiedName.lastIndexOf(".")+1, qualifiedName.length());
		return conciseName;
	}

	@Override
	public ExecVarType getType() {
		return ExecVarType.REFERENCE;
	}
	
	@Override
	public boolean isTheSameWith(GraphNode nodeAfter) {
		return true;
	}
	
	@Override
	public ReferenceValue clone(){
		
//		ReferenceValue(String id, boolean isNull, long referenceID, ClassType type, 
//				boolean isRoot, boolean isField, boolean isStatic)
		
		BooleanValue isNullChild = (BooleanValue) getChildren().get(0);
		boolean isNull = isNullChild.getBoolValue();
		
		ReferenceValue clonedValue = new ReferenceValue(getVarName(), isNull, this.getReferenceID(), this.getClassType(),
				isRoot, isField, isStatic);
		List<ExecValue> clonedChildren = cloneChildren(clonedValue.getChildren());
		for(ExecValue clonedChild: clonedChildren){
			clonedValue.addChild(clonedChild);
			clonedChild.addParent(clonedValue);
		}
		
		
		return clonedValue;
	}
	
	private List<ExecValue> cloneChildren(List<ExecValue> children){
		List<ExecValue> clonedList = new ArrayList<>();
		for(ExecValue child: children){
			ExecValue clonedChild = child.clone();
			clonedList.add(clonedChild);
		}
		
		return clonedList;
	}
}
