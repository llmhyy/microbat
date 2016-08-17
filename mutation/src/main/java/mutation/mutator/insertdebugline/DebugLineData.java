/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package mutation.mutator.insertdebugline;

import java.util.ArrayList;
import java.util.List;

import sav.common.core.utils.CollectionUtils;

import japa.parser.ast.Node;

/**
 * @author LLT
 * 
 */
public abstract class DebugLineData {
	private List<Integer> orgLines;
	private int debugLine;
	
	public DebugLineData() {
		this.orgLines = new ArrayList<Integer>();
	}

	public DebugLineData(int lineNo) {
		this();
		orgLines.add(lineNo);
	}

	public abstract InsertType getInsertType() ;

	public int getDebugLine() {
		return debugLine;
	}
	
	public void setDebugLine(int debugLine) {
		this.debugLine = debugLine;
	}
	
	public void addOrgLine(int orgLine) {
		orgLines.add(orgLine);
	}
	
	public int getLastOrgLine() {
		return CollectionUtils.getLast(orgLines);
	}

	public Node getInsertNode() {
		// by default
		throw new UnsupportedOperationException();
	}
	
	public List<Node> getReplaceNodes() {
		// by default
		throw new UnsupportedOperationException();
	}
	
	public Node getOrgNode() {
		// by default
		throw new UnsupportedOperationException();
	}

	public List<Integer> getOrgLines() {
		return orgLines;
	}
	
	public static enum InsertType {
		ADD, REPLACE
	}

	@Override
	public String toString() {
		return "DebugLineData [lineNo=" + orgLines + ", debugLine=" + debugLine
				+ "]";
	}
}
