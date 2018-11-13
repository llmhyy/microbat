package microbat.instrumentation.cfgcoverage.graph.cdg;

import java.util.ArrayList;
import java.util.List;

import microbat.instrumentation.cfgcoverage.graph.Branch;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;

public class CDGNode {
	private int id;
	private CoverageSFNode cfgNode;
	private List<CDGNode> children = new ArrayList<>(2);
	private List<CDGNode> parent = new ArrayList<>(1);
	
	public CDGNode(CoverageSFNode cfgNode) {
		this.cfgNode = cfgNode;
	}
	
	public void setChild(CDGNode dependenteeCdgNode) {
		children.add(dependenteeCdgNode);
		dependenteeCdgNode.parent.add(this);
	}

	public List<CDGNode> getChildren() {
		return children;
	}

	public void setChildren(List<CDGNode> children) {
		this.children = children;
	}

	public List<CDGNode> getParent() {
		return parent;
	}

	public void setParent(List<CDGNode> parent) {
		this.parent = parent;
	}

	public CoverageSFNode getCfgNode() {
		return cfgNode;
	}

	public void setCfgNode(CoverageSFNode cfgNode) {
		this.cfgNode = cfgNode;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "CDGNode [id=" + id + ", cfgNode=" + cfgNode + "]";
	}
	
	public List<Branch> findDirectParentBranches() {
		CoverageSFNode thisCFGNode = this.getCfgNode();
		List<Branch> list = new ArrayList<>();
		for (CDGNode parent : this.getParent()) {
			CoverageSFNode parentCFGNode = parent.getCfgNode();
			for (CoverageSFNode childCFGNode : parentCFGNode.getBranchTargets()) {
				if (childCFGNode.canReach(thisCFGNode)) {
					Branch branch = Branch.of(parentCFGNode, childCFGNode);
					list.add(branch);
				}
			}
		}

		return list;
	}

}
