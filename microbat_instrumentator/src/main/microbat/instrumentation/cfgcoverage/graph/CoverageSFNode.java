package microbat.instrumentation.cfgcoverage.graph;

import java.util.ArrayList;
import java.util.List;

import microbat.codeanalysis.bytecode.CFGNode;
import microbat.instrumentation.cfgcoverage.graph.CFGInstance.UniqueNodeId;

public class CoverageSFNode {
	private int startIdx;
	private int endIdx;
	private UniqueNodeId startNodeId;
	private UniqueNodeId endNodeId;
	private List<CoverageSFNode> branches;
	private List<Integer> content; // for a block node.
	private Type type;

	private List<Integer> coveredTestcases;

	public CoverageSFNode(Type type, CFGNode startNode, CFGInstance cfg) {
		this.type = type;
		startIdx = startNode.getIdx();
		startNodeId = cfg.getUnitCfgNodeId(startNode);
	}

	public List<Integer> getCoveredTestcases() {
		return coveredTestcases;
	}

	public void setCoveredTestcases(List<Integer> coveredTestcases) {
		this.coveredTestcases = coveredTestcases;
	}

	public List<CoverageSFNode> getBranches() {
		return branches;
	}
	
	public void addBranch(CoverageSFNode branchNode) {
		if (branches == null) {
			branches = new ArrayList<>(2);
		}
		branches.add(branchNode);
	}

	public void setBranches(List<CoverageSFNode> branches) {
		this.branches = branches;
	}

	public int getStartIdx() {
		return startIdx;
	}

	public void setStartIdx(int startIdx) {
		this.startIdx = startIdx;
	}
	
	public void addContentNode(int nodeIdx) {
		if (content == null) {
			content = new ArrayList<>();
		}
		content.add(nodeIdx);
	}

	public List<Integer> getContent() {
		return content;
	}
	
	public int getEndIdx() {
		return endIdx;
	}

	public void setEndIdx(int endIdx, UniqueNodeId uniqueNodeId) {
		this.endIdx = endIdx;
		this.endNodeId = uniqueNodeId;
	}

	public UniqueNodeId getStartNodeId() {
		return startNodeId;
	}

	public void setStartNodeId(UniqueNodeId startNodeId) {
		this.startNodeId = startNodeId;
	}

	public UniqueNodeId getEndNodeId() {
		return endNodeId;
	}

	public void setEndNodeId(UniqueNodeId endNodeId) {
		this.endNodeId = endNodeId;
	}

	public void setContent(List<Integer> content) {
		this.content = content;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public enum Type {
		CONDITION_NODE, BLOCK_NODE, INVOKE_NODE, ALIAS_NODE
	}

	
}
