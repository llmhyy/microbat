package microbat.instrumentation.cfgcoverage.graph;

import java.util.ArrayList;
import java.util.List;

public class CoverageSFlowGraph {
	private CoverageSFNode startNode;
	private List<CoverageSFNode> nodeList;
	private List<Integer> coveredTestcaseIdexies = new ArrayList<>();
	private List<String> coveredTestcases = new ArrayList<>();
	private int cdgLayer;

	public CoverageSFlowGraph() {
	}

	public CoverageSFlowGraph(CFGInstance cfg, int cdgLayer) {
		nodeList = new ArrayList<>(cfg.getNodeList().size() / 2);
		this.cdgLayer = cdgLayer;
	}

	public void addNode(CoverageSFNode node) {
		node.setCvgIdx(nodeList.size());
		nodeList.add(node);
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

}
