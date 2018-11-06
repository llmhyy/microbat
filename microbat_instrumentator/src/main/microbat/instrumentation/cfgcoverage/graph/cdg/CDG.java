package microbat.instrumentation.cfgcoverage.graph.cdg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;

public class CDG {
	private CoverageSFlowGraph coverageGraph;
	private List<CDGNode> nodeList = new ArrayList<>();
	private List<CDGNode> startNodes = new ArrayList<>();
	private List<CDGNode> endNodes = new ArrayList<>();
	private Map<Integer, CDGNode> coverageSFNodeToCDGNodeMap = new HashMap<>();
	
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
	
	public Map<Integer, CDGNode> getCoverageSFNodeToCDGNodeMap() {
		return coverageSFNodeToCDGNodeMap;
	}

	public void setCoverageSFNodeToCDGNodeMap(Map<Integer, CDGNode> coverageSFNodeToCDGNodeMap) {
		this.coverageSFNodeToCDGNodeMap = coverageSFNodeToCDGNodeMap;
	}

	public CDGNode findCDGNode(CoverageSFNode node) {
		return coverageSFNodeToCDGNodeMap.get(node.getCvgIdx());
	}

	@Override
	public String toString() {
		return "CDG [nodeList=" + nodeList + ", startNodes=" + startNodes + ", endNodes=" + endNodes + "]";
	}
}
