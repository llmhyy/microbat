package microbat.instrumentation.cfgcoverage.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.codeanalysis.bytecode.CFGNode;
import microbat.instrumentation.cfgcoverage.graph.CFGInstance.UniqueNodeId;
import sav.common.core.utils.CollectionUtils;

public class CoverageSFNode {
	private int cvgIdx;
	private int startIdx;
	private int endIdx;
	private UniqueNodeId startNodeId;
	private UniqueNodeId endNodeId; // probeNode
	private Type type;
	private List<Integer> coveredTestcases = new ArrayList<>();
	/* for alias node */
	private AliasNodeId aliasId;
	/* for block node */
	private List<Integer> content; // for a block node which contain all nodes in block from start to end.
	/* for conditional node */
	private List<CoverageSFNode> branches;
	private Map<Branch, List<Integer>> coveredTestcasesOnBranches = new HashMap<Branch, List<Integer>>();

	public CoverageSFNode(int cvgIdx) {
		this.cvgIdx = cvgIdx;
	}
	
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
			UniqueNodeId probeId = branch.getProbeNodeId();
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
	
	public void setStartEndIdx() {
		if (content == null) {
			endIdx = startIdx;
		} else {
			startIdx = content.get(0);
			endIdx = content.get(content.size() - 1);
		}
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
	
	public UniqueNodeId getProbeNodeId() {
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

	public void addCoveredTestcase(int testcaseIdx) {
		coveredTestcases.add(testcaseIdx);
	}

	public void markCoveredBranch(CoverageSFNode branch, int testcaseIdx) {
		Branch condBranch = new Branch(this.endIdx, branch.getStartIdx());
		List<Integer> tcs = coveredTestcasesOnBranches.get(condBranch);
		if (tcs == null) {
			tcs = new ArrayList<>();
			coveredTestcasesOnBranches.put(condBranch, tcs);
		}
		tcs.add(testcaseIdx);
	}
	
	public Map<Branch, List<Integer>> getCoveredTestcasesOnBranches() {
		return coveredTestcasesOnBranches;
	}

	public void setEndIdx(int endIdx) {
		this.endIdx = endIdx;
	}

	public void setCoveredTestcasesOnBranches(Map<Branch, List<Integer>> coveredTestcasesOnBranches) {
		this.coveredTestcasesOnBranches = coveredTestcasesOnBranches;
	}

	public AliasNodeId getAliasId() {
		return aliasId;
	}
	
	public boolean isAliasNode() {
		return aliasId != null;
	}

	public void setAliasId(AliasNodeId aliasId) {
		this.aliasId = aliasId;
	}

	public int getCvgIdx() {
		return cvgIdx;
	}

	public void setCvgIdx(int cvgIdx) {
		this.cvgIdx = cvgIdx;
	}
	
	public List<Integer> getCorrespondingCfgNodeIdxies() {
		if (CollectionUtils.isEmpty(content)) {
			return Arrays.asList(startIdx);
		}
		return content;
	}
}
