package microbat.instrumentation.cfgcoverage.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author lyly
 * Shortened Flow Graph for coverage recording.
 */
public class CoverageSFlowGraph {
	private CoverageSFNode startNode;
	private List<CoverageSFNode> nodeList;
	private List<Integer> coveredTestcaseIdexies = new ArrayList<>();
	private List<String> coveredTestcases = new ArrayList<>();
	private int cdgLayer;
	/* referenceCvgGraphIdx[cfgIdx] = CoverageSFlowGraph.nodeList.idx */
	private List<Integer> referenceCvgGraphIdx;
	private int cfgSize;
	private List<CoveragePath> coveragePaths;

	public CoverageSFlowGraph(int cfgSize) {
		this.cfgSize = cfgSize;
		referenceCvgGraphIdx = new ArrayList<>(cfgSize);
		for (int i = 0; i < cfgSize; i++) {
			referenceCvgGraphIdx.add(-1);
		}
	}

	public CoverageSFlowGraph(CFGInstance cfg, int cdgLayer) {
		this(cfg.getCfg().size());
		nodeList = new ArrayList<>(cfg.getNodeList().size() / 2);
		this.cdgLayer = cdgLayer;
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

	public void addCoveredTestcase(String testcase, int testcaseIdx) {
		coveredTestcases.add(testcase);
		coveredTestcaseIdexies.add(testcaseIdx);
	}

	public int getCdgLayer() {
		return cdgLayer;
	}

	public List<Integer> getCoveredTestcaseIdexies() {
		return coveredTestcaseIdexies;
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

	public void setCoveredTestcaseIdexies(List<Integer> coveredTestcaseIdexies) {
		this.coveredTestcaseIdexies = coveredTestcaseIdexies;
	}

	public void setCoveredTestcases(List<String> coveredTestcases) {
		this.coveredTestcases = coveredTestcases;
	}

	public void setCdgLayer(int cdgLayer) {
		this.cdgLayer = cdgLayer;
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
}
