/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.dto;

import java.util.ArrayList;
import java.util.List;

import sav.common.core.utils.Assert;
import sav.common.core.utils.BreakpointUtils;
/**
 * @author khanh
 *
 */
/**
 * @author LLT
 *
 */
public class DebugLine extends BreakPoint {
	private List<Integer> orgLines;
	
	protected DebugLine(String className, int lineNo){
		super(className, lineNo);
	}
	
	public DebugLine(BreakPoint orgBkp, int newLineNo){
		super(orgBkp.getClassCanonicalName(), newLineNo);
		this.setVars(orgBkp.getVars());
		this.orgLines = new ArrayList<Integer>();
		orgLines.add(orgBkp.lineNo);
	}
	
	public void put(BreakPoint orgBkp, int newLineNo) {
		// verify breakpoint
		Assert.assertTrue(orgBkp.classCanonicalName.equals(this.classCanonicalName), 
				"inconsistent className.Expect ", this.classCanonicalName, "get", orgBkp.classCanonicalName);
		orgLines.add(orgBkp.lineNo);
	}

	@Override
	public List<Integer> getOrgLineNos() {
		return orgLines;
	}
	
	public List<String> getOrgBrkpIds() {
		List<String> ids = new ArrayList<String>(orgLines.size());
		for (Integer orgLine : orgLines) {
			ids.add(BreakpointUtils.getLocationId(classCanonicalName, orgLine));
		}
		return ids;
	}
	
	public void addOrgLineNo(int lineNo) {
		orgLines.add(lineNo);
	}

	@Override
	public String toString() {
		return "DebugLine [orgLines=" + orgLines + ", vars=" + vars + ", id="
				+ getId() + "]";
	}

	public void addOrgLineNos(List<Integer> newLines) {
		this.orgLines.addAll(newLines);
	}

	@Override
	public DebugLine clone() {
		DebugLine debugLine = new DebugLine(classCanonicalName, lineNo);
		debugLine.orgLines = new ArrayList<Integer>(orgLines);
		debugLine.vars = new ArrayList<Variable>(vars);
		return debugLine;
	}
}
