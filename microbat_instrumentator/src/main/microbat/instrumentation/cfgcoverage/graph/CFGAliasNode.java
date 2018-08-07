package microbat.instrumentation.cfgcoverage.graph;

import microbat.codeanalysis.bytecode.CFGNode;

public class CFGAliasNode extends CFGNode {
	private CFGNode orgNode;

	public CFGAliasNode(CFGNode orgNode) {
		super(orgNode.getInstructionHandle());
		this.orgNode = orgNode;
	}

	@Override
	public String toString() {
		return super.toString() + "_alias";
	}
	
	public CFGNode getOrgNode() {
		return orgNode;
	}
}
