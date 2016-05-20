/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package microbat.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import microbat.model.variable.Variable;
import sav.common.core.utils.CollectionUtils;

/**
 * @author LLT
 * 
 */
public class BreakPoint extends ClassLocation {
	protected List<Variable> vars; // to keep order
	private int charStart;
	private int charEnd;
	
	private List<Variable> readVariables = new ArrayList<>();
	private List<Variable> writtenVariables = new ArrayList<>();
	
	private List<Variable> allVisibleVariables = new ArrayList<>();
	
	private boolean isReturnStatement;
	private boolean isConditioanl;
	private Scope conditionScope;
	
	public BreakPoint(String className, int linNum){
		super(className, null, linNum);
		vars = new ArrayList<Variable>();
	}
	
	public BreakPoint(String className, String methodSign, int lineNo) {
		super(className, methodSign, lineNo);
		vars = new ArrayList<Variable>();
	}
	
	public BreakPoint(String className, int lineNo, Variable... newVars) {
		this(className, null, lineNo);
		if (newVars != null) {
			addVars(newVars);
		}
	}
	
	public void addReadVariable(Variable var){
		if(!this.readVariables.contains(var)){
			this.readVariables.add(var);			
		}
	}
	
	public void addWrittenVariable(Variable var){
		if(!this.writtenVariables.contains(var)){
			this.writtenVariables.add(var);
		}
	}
	
	public List<Variable> getAllVisibleVariables() {
		return allVisibleVariables;
	}

	public void setAllVisibleVariables(List<Variable> allVisibleVariables) {
		this.allVisibleVariables = allVisibleVariables;
	}

	public List<Variable> getReadVariables() {
		return readVariables;
	}

	public void setReadVariables(List<Variable> readVariables) {
		this.readVariables = readVariables;
	}

	public List<Variable> getWrittenVariables() {
		return writtenVariables;
	}

	public void setWrittenVariables(List<Variable> writtenVariables) {
		this.writtenVariables = writtenVariables;
	}

	public void addVars(Variable... newVars) {
		for (Variable newVar : newVars) {
			vars.add(newVar);
		}
	}
	
	public void addVars(List<Variable> newVars) {
		for (Variable newVar : newVars) {
			CollectionUtils.addIfNotNullNotExist(vars, newVar);
			
		}
	}
	
	public List<Variable> getVars() {
		return vars;
	}

	public void setVars(List<Variable> vars) {
		this.vars = vars;
	}
	
	public boolean valid() {
		return lineNo > 0;
	}
	
	public String getMethodSign() {
		if(methodSign == null){
			System.err.println("missing method name!");
		}
		return methodSign;
	}
	
	public int getCharStart() {
		return charStart;
	}

	public void setCharStart(int charStart) {
		this.charStart = charStart;
	}

	public int getCharEnd() {
		return charEnd;
	}

	public void setCharEnd(int charEnd) {
		this.charEnd = charEnd;
	}
	
	public List<Integer> getOrgLineNos() {
		return Arrays.asList(lineNo);
	}

	@Override
	public String toString() {
		return "BreakPoint [classCanonicalName=" + classCanonicalName
				+ ", methodName=" + methodSign + ", lineNo=" + lineNo
				+ ", vars=" + vars + ", charStart=" + charStart + ", charEnd="
				+ charEnd + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((classCanonicalName == null) ? 0 : classCanonicalName
						.hashCode());
		result = prime * result + lineNo;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BreakPoint other = (BreakPoint) obj;
		if (classCanonicalName == null) {
			if (other.classCanonicalName != null)
				return false;
		} else if (!classCanonicalName.equals(other.classCanonicalName))
			return false;
		if (lineNo != other.lineNo)
			return false;
		return true;
	}

	public boolean isReturnStatement() {
		return isReturnStatement;
	}

	public void setReturnStatement(boolean isReturnStatement) {
		this.isReturnStatement = isReturnStatement;
	}
	
	public String getDeclaringCompilationUnitName(){
		String className = super.getClassCanonicalName();
		if(className.contains("$")){
			className = className.substring(0, className.indexOf("$"));
		}
		return className;
	}
	
	public String getClassCanonicalName(){
		return super.getClassCanonicalName();
	}

	public void setConditional(boolean isConditional) {
		this.isConditioanl = isConditional;
	}
	
	public boolean isConditional(){
		return this.isConditioanl;
	}

	public Scope getConditionScope() {
		return conditionScope;
	}

	public void setConditionScope(Scope conditionScope) {
		this.conditionScope = conditionScope;
	}
}
