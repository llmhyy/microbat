package microbat.instrumentation.cfgcoverage.graph.cdg;

import java.util.ArrayList;
import java.util.List;

import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;

public class CDG {
	private List<CDGNode> nodeList = new ArrayList<>();
	private List<CDGNode> startNodes = new ArrayList<>();
	private List<CDGNode> endNodes = new ArrayList<>();
	private List<CoverageSFNode> inconditionalDependentNodes = new ArrayList<>();

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
	
	public void addContent(CoverageSFNode node) {
		inconditionalDependentNodes.add(node);
	}

	public void addStartNode(CDGNode cdgNode) {
		startNodes.add(cdgNode);
	}

	public void addExitNode(CDGNode cdgNode) {
		endNodes.add(cdgNode);
	}
}
