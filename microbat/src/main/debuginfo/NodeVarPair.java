package debuginfo;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * This class contains information for writing IO.
 * 
 * @author hongshu
 */
public class NodeVarPair {
	private TraceNode node;
	private VarValue variable;
	private int varContainingNodeID;
	
	public NodeVarPair(TraceNode node, VarValue variable) {
		this.node = node;
		this.variable = variable;
		this.varContainingNodeID = node.getOrder();
	}
	
	public NodeVarPair(TraceNode node, VarValue variable, int varContainingNodeID) {
		this.node = node;
		this.variable = variable;
		this.varContainingNodeID = varContainingNodeID;
	}
	
	public TraceNode getNode() {
		return this.node;
	}
	
	public VarValue getVariable() {
		return this.variable;
	}
	
	public int getVarContainingNodeID() {
		return this.varContainingNodeID;
	}
}
