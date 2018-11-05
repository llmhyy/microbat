package microbat.instrumentation.cfgcoverage.graph;

import java.io.Serializable;

public class Branch implements Serializable {
	private static final long serialVersionUID = -1054499814399081119L;
	protected CoverageSFNode fromNode;
	protected CoverageSFNode toNode;
	
	public Branch(CoverageSFNode fromNode, CoverageSFNode toNode) {
		this.fromNode = fromNode;
		this.toNode = toNode;
	}
	
	public String getBranchID(){
		return fromNode.getCvgIdx() + "-" + toNode.getCvgIdx();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + fromNode.getCvgIdx();
		result = prime * result + toNode.getCvgIdx();
		return result;
	}
	
	public boolean isCovered(){
		return this.getFromNode().getCoveredBranches().contains(this.getToNode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Branch other = (Branch) obj;
		if (fromNode.getCvgIdx() != other.fromNode.getCvgIdx())
			return false;
		if (toNode.getCvgIdx() != other.toNode.getCvgIdx())
			return false;
		return true;
	}

	public int getFromNodeIdx() {
		return fromNode.getCvgIdx();
	}

	public int getToNodeIdx() {
		return toNode.getCvgIdx();
	}
	
	public CoverageSFNode getFromNode() {
		return fromNode;
	}
	
	public CoverageSFNode getToNode() {
		return toNode;
	}

	@Override
	public String toString() {
		return "Branch [from=" + fromNode + ", to=" + toNode + "]";
	}
}
