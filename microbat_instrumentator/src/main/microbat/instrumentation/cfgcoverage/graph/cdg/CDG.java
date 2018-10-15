package microbat.instrumentation.cfgcoverage.graph.cdg;

import java.util.ArrayList;
import java.util.List;

import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;

public class CDG {
	private CoverageSFlowGraph coverageGraph;
	private List<CDGNode> nodeList = new ArrayList<>();
	private List<CDGNode> startNodes = new ArrayList<>();
	private List<CDGNode> endNodes = new ArrayList<>();
	
	public CDG(CoverageSFlowGraph coverageGraph) {
		this.coverageGraph = coverageGraph;
	}

	public void addNode(CDGNode node) {
		node.setId(nodeList.size());
		nodeList.add(node);
	}

	public List<CDGNode> getNodeList() {
		return nodeList;
	}

	public void setNodeList(List<CDGNode> nodeList) {
		this.nodeList = nodeList;
	}

	public List<CDGNode> getStartNodes() {
		return startNodes;
	}

	public void setStartNodes(List<CDGNode> startNodes) {
		this.startNodes = startNodes;
	}

	public List<CDGNode> getEndNodes() {
		return endNodes;
	}

	public void setEndNodes(List<CDGNode> endNodes) {
		this.endNodes = endNodes;
	}
	
	public void addStartNode(CDGNode cdgNode) {
		startNodes.add(cdgNode);
	}

	public void addExitNode(CDGNode cdgNode) {
		endNodes.add(cdgNode);
	}
	
	public void updateCoverage(CoverageSFlowGraph newCoverage) {
		this.coverageGraph.addCoverageInfo(newCoverage);
	}

	@Override
	public String toString() {
		return "CDG [nodeList=" + nodeList + ", startNodes=" + startNodes + ", endNodes=" + endNodes + "]";
	}
}
