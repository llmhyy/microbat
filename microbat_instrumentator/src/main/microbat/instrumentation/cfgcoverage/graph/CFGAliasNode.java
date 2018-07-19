package microbat.instrumentation.cfgcoverage.graph;

import microbat.codeanalysis.bytecode.CFGNode;

public class CFGAliasNode extends CFGNode {
	private CFGNode orgNode;
	private AliasNodeId aliasNodeId;

	public CFGAliasNode(CFGNode prevNode, CFGNode orgNode) {
		super(orgNode.getInstructionHandle());
		this.orgNode = orgNode;
		this.aliasNodeId = new AliasNodeId(prevNode.getIdx(), orgNode.getIdx());
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
	
	public void setAliasNodeId(AliasNodeId aliasNodeId) {
		this.aliasNodeId = aliasNodeId;
	}

	@Override
	public String toString() {
		return "CFGAliasNode [orgNode=" + orgNode + ", aliasNodeId=" + aliasNodeId + "]";
	}
	
}
