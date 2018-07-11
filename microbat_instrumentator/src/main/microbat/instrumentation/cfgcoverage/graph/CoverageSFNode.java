package microbat.instrumentation.cfgcoverage.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.codeanalysis.bytecode.CFGNode;
import microbat.instrumentation.cfgcoverage.graph.CFGInstance.UniqueNodeId;

public class CoverageSFNode {
	private int startIdx;
	private int endIdx;
	private UniqueNodeId startNodeId;
	private UniqueNodeId endNodeId;
	private UniqueNodeId probeNodeId;
	private List<CoverageSFNode> branches;
	private List<Integer> content; // for a block node.
	private Type type;

	private List<Integer> coveredTestcases = new ArrayList<>();
	private Map<CoverageSFNode, List<Integer>> coveredTestcasesOnBranches = new HashMap<CoverageSFNode, List<Integer>>();

	public CoverageSFNode(Type type, CFGNode startNode, CFGInstance cfg) {
		this.type = type;
		startIdx = startNode.getIdx();
		startNodeId = cfg.getUnitCfgNodeId(startNode);
	}
	
	public CoverageSFNode getCorrespondingBranch(String methodId) {
		for (CoverageSFNode branch : branches) {
			if (branch.startNodeId.getMethodId().equals(methodId)) {
				return branch;
			}
		}
		return null; 
	}
	
	public CoverageSFNode getCorrespondingBranch(String methodId, int nodeLocalIdx) {
		for (CoverageSFNode branch : branches) {
			UniqueNodeId probeId = branch.probeNodeId;
			if (probeId.getMethodId().equals(methodId) && probeId.localNodeIdx == nodeLocalIdx) {
				return branch;
			}
		}
		return null; 
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
		this.probeNodeId = endNodeId;
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
	
	public UniqueNodeId getProbeNodeId() {
		return probeNodeId;
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

	public void addCoveredTestcase(int testcaseIdx) {
		coveredTestcases.add(testcaseIdx);
	}

	public void markCoveredBranch(CoverageSFNode branch, int testcaseIdx) {
		List<Integer> tcs = coveredTestcasesOnBranches.get(branch);
		if (tcs == null) {
			tcs = new ArrayList<>();
			coveredTestcasesOnBranches.put(branch, tcs);
		}
		tcs.add(testcaseIdx);
	}

	
}
