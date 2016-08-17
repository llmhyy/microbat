/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package mutation.parser;

import japa.parser.ast.Node;

/**
 * @author khanh
 *
 */
public class Position {

	private int beginLine;

	private int beginColumn;

	private int endLine;

	private int endColumn;
	
	public Position(int beginColumn, int endColumn, int beginLine, int endLine){
		this.beginColumn = beginColumn;
		this.endColumn = endColumn;
		this.beginLine = beginLine;
		this.endLine = endLine;
	}
	
	
	public static Position fromNode(Node node){
		return new Position(node.getBeginColumn(), node.getEndColumn(), node.getBeginLine(), node.getEndLine());
	}
	
	public boolean declaredBefore(int lineNumber, int column){
		return (lineNumber > endLine || (lineNumber == endLine && column >= endColumn));
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Position [beginLine=" + beginLine + ", beginColumn="
				+ beginColumn + ", endLine=" + endLine + ", endColumn="
				+ endColumn + "]";
	}
	
	
}
