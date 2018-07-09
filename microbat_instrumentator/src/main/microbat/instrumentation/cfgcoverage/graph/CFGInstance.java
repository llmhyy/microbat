package microbat.instrumentation.cfgcoverage.graph;

import java.util.ArrayList;
import java.util.List;

import microbat.codeanalysis.bytecode.CFG;
import microbat.codeanalysis.bytecode.CFGNode;

public class CFGInstance {
	private CFG cfg;
	private List<CFGNode> nodeList; // same order as in InstrucionList.
	private List<UniqueNodeId> unitCfgNodeIds;

	public CFGInstance(CFG unitCfg, String methodId, List<CFGNode> nodeList) {
		this.cfg = unitCfg;
		this.nodeList = nodeList;
		unitCfgNodeIds = new ArrayList<UniqueNodeId>(nodeList.size());
		
	}

	public CFG getCfg() {
		return cfg;
	}

	public List<CFGNode> getNodeList() {
		return nodeList;
	}

	public boolean isEmpty() {
		return cfg == null;
	}

	public List<UniqueNodeId> getUnitCfgNodeIds() {
		return unitCfgNodeIds;
	}
	
	public static class UniqueNodeId {
		String methodId;
		int localNodeIdx;
	}

	public UniqueNodeId getUnitCfgNodeId(CFGNode node) {
		return unitCfgNodeIds.get(node.getIdx());
	}
}
