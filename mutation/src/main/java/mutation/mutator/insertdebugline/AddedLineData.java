/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package mutation.mutator.insertdebugline;

import japa.parser.ast.Node;

/**
 * @author LLT
 *
 */
public class AddedLineData extends DebugLineData {
	private Node insertStmt;
	
	public AddedLineData(int loc, Node insertStmt) {
		super(loc);
		this.insertStmt = insertStmt;
	}
	
	@Override
	public InsertType getInsertType() {
		return InsertType.ADD;
	}
	
	@Override
	public Node getInsertNode() {
		return insertStmt;
	}
}
