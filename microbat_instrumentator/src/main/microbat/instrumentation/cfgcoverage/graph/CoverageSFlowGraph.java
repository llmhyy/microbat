package microbat.instrumentation.cfgcoverage.graph;

import java.util.ArrayList;
import java.util.List;

public class CoverageSFlowGraph {
	private CoverageSFNode startNode;
	private List<CoverageSFNode> nodeList;
	
	public CoverageSFlowGraph(CFGInstance cfg) {
		nodeList = new ArrayList<>(cfg.getNodeList().size() / 2);
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
}
