/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package mutation.mutator.insertdebugline;

import japa.parser.ast.Node;

import java.util.List;

/**
 * @author LLT
 *
 */
public class ReplacedLineData extends DebugLineData {
	private Node orgNode;
	private List<Node> replaceNodes;

	public ReplacedLineData(int loc, Node orgNode,
			List<Node> replaceNodes) {
		super(loc);
		this.orgNode = orgNode;
		this.replaceNodes = replaceNodes;
	}
	
	@Override
	public InsertType getInsertType() {
		return InsertType.REPLACE;
	}
	
	@Override
	public List<Node> getReplaceNodes() {
		return replaceNodes;
	}

	@Override
	public Node getOrgNode() {
		return orgNode;
	}
}
