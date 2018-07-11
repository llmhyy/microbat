package microbat.instrumentation.cfgcoverage.graph;

import java.util.ArrayList;
import java.util.List;

public class CoverageSFlowGraph {
	private CoverageSFNode startNode;
	private List<CoverageSFNode> nodeList;
	private List<Integer> coveredTestcases = new ArrayList<>();
	private int cdgLayer;
	
	
	public CoverageSFlowGraph(CFGInstance cfg, int cdgLayer) {
		nodeList = new ArrayList<>(cfg.getNodeList().size() / 2);
		this.cdgLayer = cdgLayer;
	}
	
	public void addNode(CoverageSFNode node) {
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
		coveredTestcases.add(testcaseIdx);
	}
	
	public int getCdgLayer() {
		return cdgLayer;
	}
}
