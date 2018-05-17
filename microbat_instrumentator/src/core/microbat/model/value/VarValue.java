/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package microbat.model.value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import microbat.model.variable.ArrayElementVar;
import microbat.model.variable.FieldVar;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;

/**
 * @author Yun Lin
 *
 */
public abstract class VarValue implements GraphNode, Serializable {
	private static final long serialVersionUID = -4243257984929286188L;
	protected String stringValue;
	protected List<VarValue> parents = null;
	protected Variable variable;
	protected List<VarValue> children = null;
	
	/**
	 * indicate whether this variable is a top-level variable in certain step.
	 */
	protected boolean isRoot = false;
	
	
	public static final int NOT_NULL_VAL = 1;
	
	public VarValue(){}
	
	protected VarValue(boolean isRoot, Variable variable) {
		this.isRoot = isRoot;
		this.variable = variable;
		
	}
	
	public abstract VarValue clone();
	
	/**
	 * if the toString() of an object is undefined, the default toString() may return something like
	 * "pack.Class@12fa231". Based on this observation, I build this method.
	 * @param stringValue
	 * @return
	 */
	public boolean isDefinedToStringMethod(){
		if(stringValue == null){
			return false;
		}
		else{
			if(stringValue.contains("@") && stringValue.contains(".")){
				return false;
			}
			else{
				return true;
			}
		}
	}
	
	public VarValue findVarValue(String varID){
		Set<String> visitedIDs = new HashSet<>();
		VarValue value = findVarValue(varID, visitedIDs);
		return value;
	}
	
	protected VarValue findVarValue(String varID, Set<String> visitedIDs ){
		
		if(getChildren() != null){
			for(VarValue value: getChildren()){
				if(visitedIDs.contains(value.getVarID())){
					continue;
				}
				else if(value.getVarID().equals(varID)){
					return value;
				}
				else{
					
					visitedIDs.add(value.getVarID());
					VarValue targetValue = value.findVarValue(varID, visitedIDs);
					
					if(targetValue != null){
						return targetValue;
					}
				}
			}
		}
		
		return null;
	}
	
	@Override
	public List<VarValue> getChildren() {
		if (children == null) {
			return Collections.emptyList();
		}
		return children;
	}
	
	@Override
	public int hashCode(){
		return variable.getVarID().hashCode();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof VarValue){
			VarValue otherVal = (VarValue)obj;
			
			if(this.variable!=null && otherVal.getVariable()!=null){
				return this.variable.getVarID().equals(otherVal.getVariable().getVarID());				
			}
			
		}
		
		return false;
	}
	
	public List<VarValue> getAllDescedentChildren(){
		HashSet<VarValue> valueSet = new HashSet<>();
		
		if(this.children != null){
			increaseVariableSet(valueSet, this.children);			
		}
		
		ArrayList<VarValue> list = new ArrayList<>(valueSet);
		Collections.sort(list, new Comparator<VarValue>() {
			@Override
			public int compare(VarValue o1, VarValue o2) {
				return o1.getVarName().compareTo(o2.getVarName());
			}
		});
		return list;
	}
	

	private void increaseVariableSet(HashSet<VarValue> valueSet, List<VarValue> parsedChildren) {
		for(VarValue value: parsedChildren){
			if(!valueSet.contains(value)){
				valueSet.add(value);
				increaseVariableSet(valueSet, value.getChildren());
			}
		}
	}

	public String getVarName(){
		return this.variable.getName();
	}
	
	public String getVarID() {
		return this.variable.getVarID();
	}

	public void setVarID(String varID) {
		if(varID == null){
			System.currentTimeMillis();
		}
		this.variable.setVarID(varID);
	}
	
	public void setAliasVarID(String aliasVarID) {
		this.variable.setAliasVarID(aliasVarID);
	}

	public void addChild(VarValue child) {
		if (children == null) {
			children = new ArrayList<VarValue>();
		}
		children.add(child);
	}
	
	public String getType() {
		return variable.getType();
	}
	
	public String getRuntimeType() {
		return variable.getRuntimeType();
	}
	
	/* only affect for the current execValue, not for its children */
	protected boolean needToRetrieveValue() {
		return true;
	}

	@Override
	public String toString() {
		return String.format("(%s:%s)", this.variable.getName(), getChildren());
	}
	
	public boolean isElementOfArray() {
		return variable instanceof ArrayElementVar;
	}
	
	@Override
	public List<VarValue> getParents() {
		if (parents == null) {
			return Collections.emptyList();
		}
		return parents;
	}

	public void setParents(List<VarValue> parents) {
		this.parents = parents;
	}
	
	public void addParent(VarValue parent) {
		if (parents == null) {
			parents = new ArrayList<>();
		}
		if(!this.parents.contains(parent)){
			this.parents.add(parent);
		}
	}
	
	@Override
	public boolean match(GraphNode node) {
		if(node instanceof GraphNode){
			VarValue thatValue = (VarValue)node;
			if(thatValue.getVarName().equals(this.getVarName()) && 
					thatValue.getType().equals(this.getType())){
				return true;
			}
		}
		return false;
	}
	
	public boolean isRoot() {
		return isRoot;
	}

	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}
	
//	public ExecValue getFirstRootParent(){
//		if(this.isRoot()){
//			return this;
//		}
//		else{
//			ExecValue parentValue = this;
//			while(!parentValue.isRoot()){
//				parentValue = parentValue.getParents().get(0);
//				AgentLogger.debug("loop");
//			}
//			
//			return parentValue;
//		}
//	}
	
	public boolean isField() {
		return this.variable instanceof FieldVar;
	}
	
	public boolean isLocalVariable(){
		return this.variable instanceof LocalVar;
	}

	public boolean isStatic() {
		if(this.variable instanceof FieldVar){
			FieldVar var = (FieldVar)this.variable;
			return var.isStatic();
		}
		
		return false;
	}

	public void setChildren(List<VarValue> children) {
		this.children = children;
	}
	
	public String getManifestationValue() {
		return stringValue;
	}
	
	public String getStringValue(){
		if(stringValue==null) {
			return "null";
		}
		
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public Variable getVariable() {
		return variable;
	}

	public void setVariable(Variable variable) {
		this.variable = variable;
	}
	
	public String getAliasVarID(){
		return this.variable.getAliasVarID();
	}

	public void linkAchild(VarValue value) {
		this.addChild(value);
		value.addParent(this);
	}

	public void ensureChildrenSize(int size) {
		if (children == null) {
			children = new ArrayList<>(size);
		} else {
			((ArrayList<?>) children).ensureCapacity(size);
		}
	}
//	public abstract VarValue clone();

	public int size() {
		return getChildren().size() + 1;
	}
}
