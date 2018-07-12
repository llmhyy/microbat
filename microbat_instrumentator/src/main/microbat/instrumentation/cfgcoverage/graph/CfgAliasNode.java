package microbat.instrumentation.cfgcoverage.graph;

import microbat.codeanalysis.bytecode.CFGNode;

public class CfgAliasNode extends CFGNode {
	private CFGNode orgNode;
	private AliasNodeId aliasNodeId;

	public CfgAliasNode(CFGNode orgNode) {
		super(orgNode.getInstructionHandle());
		this.orgNode = orgNode;
	}

	@Override
	public void setIdx(int idx) {
		// do nothing
	}
	
	@Override
	public int getIdx() {
		return -1;
	}

	public CFGNode getOrgNode() {
		return orgNode;
	}

	public AliasNodeId getAliasNodeId() {
		return aliasNodeId;
	}
}
