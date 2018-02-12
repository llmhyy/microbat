/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sav.common.core.Constants;
import sav.common.core.utils.Assert;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.StringUtils;

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
		Assert.notNull(methodSign, "missing method name!");
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
	
	
//	@Deprecated
	public static class Variable {
		private final String parentName;
		private final String fullName;
		private final VarScope scope;
		private String id;
		
		public Variable(String name, String fullName, VarScope scope) {
			this.parentName = name;
			this.fullName = fullName;
			this.scope = scope;
		}
		
		public Variable(String name, String fullName) {
			this(name, fullName, VarScope.UNDEFINED);
		}

		public Variable(String name) {
			this.parentName = name;
			this.fullName = name;
			scope = VarScope.UNDEFINED;
		}

		public String getParentName() {
			return parentName;
		}
		
		public String getFullName() {
			return fullName;
		}

		public String getSimpleName() {
			int l = fullName.lastIndexOf(Constants.DOT);
			return fullName.substring(l + 1);
		}
		
		public String getId() {
			if (id == null) {
				id = genId(scope, fullName);
			}
			return id;
		}
		
		public static String genId(VarScope scope, String name) {
			return StringUtils.dotJoin(scope.getDisplayName(), name);
		}

		public VarScope getScope() {
			return scope;
		}

		@Override
		public String toString() {
			return "Variable [name=" + parentName + ", fullName=" + fullName
					+ ", scope=" + scope + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((fullName == null) ? 0 : fullName.hashCode());
			result = prime * result + ((scope == null) ? 0 : scope.hashCode());
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
			Variable other = (Variable) obj;
			if (fullName == null) {
				if (other.fullName != null)
					return false;
			} else if (!fullName.equals(other.fullName))
				return false;
			if (scope != other.scope)
				return false;
			return true;
		}



		public static enum VarScope {
			THIS ("this"),
			UNDEFINED (""),
			STATIC ("static");
			
			private String displayName;
			
			private VarScope(String displayName) {
				this.displayName = displayName;
			}
			
			public String getDisplayName() {
				return displayName;
			}
		}
	}
	
}
