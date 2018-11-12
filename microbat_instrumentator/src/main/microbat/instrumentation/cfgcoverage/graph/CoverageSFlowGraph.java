package microbat.instrumentation.cfgcoverage.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode.Type;

/**
 * 
 * @author lyly
 * Shortened Flow Graph for coverage recording.
 */
public class CoverageSFlowGraph implements IGraph<CoverageSFNode> {
	private CFGInstance cfg; // for debugging
	private CoverageSFNode startNode;
	private List<CoverageSFNode> nodeList;
	private List<CoverageSFNode> exitList = new ArrayList<>();
	private List<String> coveredTestcases = new ArrayList<>();
	private int extensionLayer;
	/* referenceCvgGraphIdx[cfgIdx] = CoverageSFlowGraph.nodeList.idx */
	private List<Integer> referenceCvgGraphIdx;
	private int cfgSize;
	private List<CoveragePath> coveragePaths;
	private Map<String, Branch> cachedBranches = new HashMap<>();

	public CoverageSFlowGraph(int cfgSize) {
		this.cfgSize = cfgSize;
		referenceCvgGraphIdx = new ArrayList<>(cfgSize);
		for (int i = 0; i < cfgSize; i++) {
			referenceCvgGraphIdx.add(-1);
		}
	}

	public CoverageSFlowGraph(CFGInstance cfg, int cdgLayer) {
		this(cfg.size());
		nodeList = new ArrayList<>(cfg.size() / 2);
		this.extensionLayer = cdgLayer;
		this.cfg = cfg;
	}

	public void addNode(CoverageSFNode node) {
		node.setCvgIdx(nodeList.size());
		nodeList.add(node);
		for (Integer idx : node.getCorrespondingCfgNodeIdxies()) {
			referenceCvgGraphIdx.set(idx, node.getCvgIdx());
		}
	}

	public CoverageSFNode getStartNode() {
		return startNode;
	}

	public void setStartNode(CoverageSFNode startNode) {
		this.startNode = startNode;
	}

	public List<CoverageSFNode> getNodeList() {
		return nodeList;
	}

	public int addCoveredTestcase(String testcase) {
		coveredTestcases.add(testcase);
		return coveredTestcases.size() - 1;
	}

	public int getExtensionLayer() {
		return extensionLayer;
	}
	
	public List<String> getCoveredTestcases() {
		return coveredTestcases;
	}

	public void setNodeList(List<CoverageSFNode> nodeList) {
		this.nodeList = nodeList;
		for (CoverageSFNode node : nodeList) {
			for (Integer idx : node.getCorrespondingCfgNodeIdxies()) {
				referenceCvgGraphIdx.set(idx, node.getCvgIdx());
			}
		}
	}

	public void setCoveredTestcases(List<String> coveredTestcases) {
		this.coveredTestcases = coveredTestcases;
	}

	public void setCdgLayer(int cdgLayer) {
		this.extensionLayer = cdgLayer;
	}

	public int getCfgSize() {
		return cfgSize;
	}
	
	public CoverageSFNode getCoverageNode(int cfgNodeIdx) {
		Integer coverageNodeIdx = referenceCvgGraphIdx.get(cfgNodeIdx);
		return nodeList.get(coverageNodeIdx);
	}

	public void setBlockScope() {
		for (CoverageSFNode node : nodeList) {
			node.setBlockScope();
		}
	}
	
	public List<CoveragePath> getCoveragePaths() {
		return coveragePaths;
	}

	public void setCoveragePaths(List<CoveragePath> coveragePaths) {
		this.coveragePaths = coveragePaths;
	}

	@Override
	public List<CoverageSFNode> getExitList() {
		return exitList;
	}

	public int size() {
		return nodeList.size();
	}

	public void addExitNode(CoverageSFNode node) {
		exitList.add(node);
	}
	
	private List<CoverageSFNode> cacheDecisionNodes;

	public List<CoverageSFNode> getDecisionNodes() {
		if (cacheDecisionNodes == null) {
			cacheDecisionNodes = new ArrayList<>();
			for (CoverageSFNode node : nodeList) {
				if (node.getType() == Type.CONDITION_NODE) {
					cacheDecisionNodes.add(node);
				}
			}
		}
		return cacheDecisionNodes;
	}

	public void clearData() {
		coveredTestcases.clear();
		for (CoverageSFNode node : nodeList) {
			node.clearCoverageInfo();
		}
	}
	
	public void addCoverageInfo(CoverageSFlowGraph otherCoverage) {
		this.coveredTestcases.addAll(otherCoverage.coveredTestcases);
		for (CoverageSFNode nodeCoverage : this.nodeList) {
			CoverageSFNode otherNodeCoverage = otherCoverage.nodeList.get(nodeCoverage.getCvgIdx());
			for (String testcase : otherNodeCoverage.getCoveredTestcases()) {
				nodeCoverage.addCoveredTestcase(testcase);
			}
			for (Entry<CoverageSFNode, List<String>> entry : otherNodeCoverage.getCoveredTestcasesOnBranches()
					.entrySet()) {
				for (String testcase : entry.getValue()) {
					nodeCoverage.markCoveredBranch(nodeList.get(entry.getKey().getCvgIdx()), testcase);
				}
			}
		}
	}
	
	public void setCfg(CFGInstance cfg) {
		this.cfg = cfg;
		for (CoverageSFNode node : nodeList) {
			node.setGraph(this);
		}
	}
	
	public CFGInstance getCfg() {
		return cfg;
	}

	public Branch getBranch(CoverageSFNode fromNode, CoverageSFNode toNode) {
		Branch branch = cachedBranches.get(Branch.getBranchId(fromNode, toNode));
		if (branch == null) {
			branch = new Branch(fromNode, toNode);
			cachedBranches.put(branch.getBranchID(), branch);
		}
		return branch;
	}
	
	public Set<Branch> getAllBranches() {
		Set<Branch> branches = new HashSet<>();
		for (CoverageSFNode decisionNode : getDecisionNodes()) {
			branches.addAll(decisionNode.getBranches());
		}
		return branches;
	}
}
