package microbat.instrumentation.cfgcoverage.graph.cdg;

import java.util.ArrayList;
import java.util.List;

import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;

public class CDGNode {
	private boolean controlNode;
	private CoverageSFNode conditionalNode;
	private List<CoverageSFNode> inconditionalDependentNodes = new ArrayList<>();
	private List<CDGNode> children = new ArrayList<>(2);
	private List<CDGNode> parent = new ArrayList<>(1);
	
	public CDGNode(CoverageSFNode conditionalNode) {
		this.conditionalNode = conditionalNode;
	}
	
	public void setChild(CDGNode dependenteeCdgNode) {
		children.add(dependenteeCdgNode);
		dependenteeCdgNode.parent.add(this);
	}

	public boolean isControlNode() {
		return controlNode;
	}

	public void setControlNode(boolean controlNode) {
		this.controlNode = controlNode;
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

	public CoverageSFNode getConditionalNode() {
		return conditionalNode;
	}

	public void setConditionalNode(CoverageSFNode conditionalNode) {
		this.conditionalNode = conditionalNode;
	}

	public void addContent(CoverageSFNode dependentee) {
		inconditionalDependentNodes.add(dependentee);
	}

	
}
