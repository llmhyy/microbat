package microbat.instrumentation.cfgcoverage.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import microbat.codeanalysis.bytecode.CFGNode;
import microbat.instrumentation.cfgcoverage.graph.CFGInstance.UniqueNodeId;
import microbat.instrumentation.utils.CollectionUtils;

/**
 * @author lyly
 *
 */
public class CoverageSFNode implements IGraphNode<CoverageSFNode> {
	private CoverageSFlowGraph graph; // for debug
	private static final int INVALID_IDX = -1;
	private int cvgIdx;
	private int startIdx = INVALID_IDX;
	private int endIdx = INVALID_IDX;
	private UniqueNodeId startNodeId;
	private UniqueNodeId endNodeId; // probeNode
	private Type type;

	private List<CoverageSFNode> branchTargets = new ArrayList<>(2);
	private List<CoverageSFNode> parents = new ArrayList<>(2);
	
	private volatile List<String> coveredTestcases = new ArrayList<>();
	/* for block node */
	private List<Integer> content; // for a block node which contain all nodes in block from start to end.
	/* for conditional node */
	private volatile Map<CoverageSFNode, List<String>> coveredTestcasesOnBranches = new HashMap<CoverageSFNode, List<String>>();

	public CoverageSFNode(int cvgIdx, CoverageSFlowGraph graph) {
		this.graph = graph;
		this.cvgIdx = cvgIdx;
	}
	
	public CoverageSFNode(Type type, CFGNode startNode, CoverageSFlowGraph graph) {
		this.type = type;
		startIdx = startNode.getIdx();
		startNodeId = graph.getCfg().getUnitCfgNodeId(startNode);
		setGraph(graph);
	}
	
	public CoverageSFNode getCorrespondingBranch(String methodId) {
		for (CoverageSFNode branch : branchTargets) {
			if (branch.startNodeId.getMethodId().equals(methodId)) {
				return branch;
			}
		}
		return null; 
	}
	
	public boolean canReach(CoverageSFNode toNode) {
		Set<CoverageSFNode> set = new HashSet<>();
		return canReach(toNode, set);
	}
	
	private boolean canReach(CoverageSFNode toNode, Set<CoverageSFNode> set){
		if (this.getCvgIdx() == toNode.getCvgIdx()) {
			return true;
		}

		set.add(this);
		for (CoverageSFNode child : this.getBranchTargets()) {
			if(!set.contains(child)){
				boolean canReach = child.canReach(toNode);
				if (canReach) {
					return true;
				}
			}
			
		}

		return false;
	}
	
	public CoverageSFNode getCorrespondingBranch(String methodId, int nodeLocalIdx) {
		for (CoverageSFNode branch : branchTargets) {
			UniqueNodeId probeId = branch.getProbeNodeId();
			if (probeId.match(methodId, nodeLocalIdx)) {
				return branch;
			}
		}
		return null; 
	}
	
	public boolean isCovered() {
		return !CollectionUtils.isEmpty(coveredTestcases);
	}
	
	public List<CoverageSFNode> getCoveredBranches() {
		return new ArrayList<>(coveredTestcasesOnBranches.keySet());
	}

	public List<String> getCoveredTestcases() {
		return coveredTestcases;
	}

	public void setCoveredTestcases(List<String> coveredTestcases) {
		this.coveredTestcases = coveredTestcases;
	}

	public List<CoverageSFNode> getBranchTargets() {
		return branchTargets;
	}
	
	public List<Branch> getBranches() {
		List<Branch> branches = new ArrayList<>();
		for (CoverageSFNode branchTarget : getBranchTargets()) {
			branches.add(Branch.of(this, branchTarget));
		}
		return branches;
	}
	
	public void addBranch(CoverageSFNode branchNode) {
		branchTargets.add(branchNode);
		if (!branchNode.parents.contains(this)) {
			branchNode.addParent(this);
		}
	}
	
	private void addParent(CoverageSFNode parent) {
		parents.add(parent);
	}
	
	public void setBranches(List<CoverageSFNode> branches) {
		this.branchTargets = branches;
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

	public void addCoveredTestcase(String testcase) {
		if (!coveredTestcases.contains(testcase)) {
			coveredTestcases.add(testcase);
		}
	}

	public void markCoveredBranch(CoverageSFNode branch, String testcase) {
		List<String> tcs = coveredTestcasesOnBranches.get(branch);
		if (tcs == null) {
			tcs = new ArrayList<>();
			coveredTestcasesOnBranches.put(branch, tcs);
		}
		if (!tcs.contains(testcase)) {
			tcs.add(testcase);
		}
	}
	
	public void setEndIdx(int endIdx) {
		this.endIdx = endIdx;
	}

	public Map<CoverageSFNode, List<String>> getCoveredTestcasesOnBranches() {
		return coveredTestcasesOnBranches;
	}

	public void setCoveredTestcasesOnBranches(Map<CoverageSFNode, List<String>> coveredTestcasesOnBranches) {
		this.coveredTestcasesOnBranches = coveredTestcasesOnBranches;
	}

	public boolean isAliasNode() {
		return getType() == Type.ALIAS_NODE;
	}

	public int getCvgIdx() {
		return cvgIdx;
	}

	public void setCvgIdx(int cvgIdx) {
		this.cvgIdx = cvgIdx;
	}
	
	public List<Integer> getCorrespondingCfgNodeIdxies() {
		return content;
	}

	public void setBlockScope() {
		if (content == null) {
			endIdx = startIdx;
			content = Arrays.asList(startIdx);
		} else {
			if (startIdx == INVALID_IDX) {
				startIdx = content.get(0);
			} else {
				if (!content.contains(startIdx)) {
					content.add(0, startIdx);
				}
			}
			endIdx = content.get(content.size() - 1);
		}
	}

	@Override
	public String toString() {
		return "CoverageSFNode [id=" + cvgIdx + ", type=" + type + ", startCfgNode={" + getNodeString(startIdx)
				+ "}, endCfgNode{" + getNodeString(endIdx) + "}, branches=" + getBranchesString() + ", endNodeId=" + endNodeId
				+ ", cvgIdx=" + cvgIdx + "]";
	}

	private String getBranchesString() {
		if (graph == null) {
			List<String> branchIdxies = Collections.emptyList();
			if (branchTargets != null) {
				branchIdxies = new ArrayList<>();
				for (CoverageSFNode branch : branchTargets) {
					branchIdxies.add(String.format("{%d, %d}", branch.getStartIdx(), branch.getEndIdx()));
				}
			}
			return branchIdxies.toString();
		} else {
			StringBuilder sb = new StringBuilder();
			int i = 0;
			for (CoverageSFNode branch : sav.common.core.utils.CollectionUtils.nullToEmpty(branchTargets)) {
				sb.append(getNodeString(branch.startIdx));
				if (i != (branchTargets.size() - 1)) {
					sb.append(", ");
				}
			}
			return sb.toString();
		}
	}
	
	
	
	private String getNodeString(int... idxies) {
		if (graph == null || graph.getCfg() == null) {
			return Arrays.toString(idxies);
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < idxies.length; i++) {
			sb.append(graph.getCfg().getNodeList().get(idxies[i]));
			if (i != (idxies.length - 1)) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	@Override
	public List<CoverageSFNode> getChildren() {
		return branchTargets;
	}

	@Override
	public List<CoverageSFNode> getParents() {
		return parents;
	}

	public boolean isConditionalNode() {
		return type == Type.CONDITION_NODE;
	}

	public void clearCoverageInfo() {
		coveredTestcases.clear();
		coveredTestcasesOnBranches.clear();
	}

	public void setGraph(CoverageSFlowGraph graph) {
		this.graph = graph;
	}
	
	public List<CFGNode> getCorrespondingCFGNodes() {
		List<CFGNode> cfgNodes = new ArrayList<>(getContent().size());
		for (Integer idx : getContent()) {
			cfgNodes.add(graph.getCfg().getNodeList().get(idx));
		}
		return cfgNodes;
	}
	
	public CFGNode getLastCFGNode() {
		return graph.getCfg().getNodeList().get(endIdx);
	}
	
	public CFGNode getFirstCFGNode() {
		return graph.getCfg().getNodeList().get(startIdx);
	}
	
	public CoverageSFlowGraph getGraph() {
		return graph;
	}
}
