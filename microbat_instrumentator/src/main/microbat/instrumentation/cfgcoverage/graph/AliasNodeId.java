package microbat.instrumentation.cfgcoverage.graph;

public class AliasNodeId {
	protected int prevNodeIdx;
	protected int orgNodeIdx;
	
	public AliasNodeId(int prevNodeIdx, int orgNodeIdx) {
		this.prevNodeIdx = prevNodeIdx;
		this.orgNodeIdx = orgNodeIdx;
	}

	public String getStringId() {
		return String.format("%d_%d", prevNodeIdx, orgNodeIdx);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + prevNodeIdx;
		result = prime * result + orgNodeIdx;
		return result;
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
		if (prevNodeIdx != other.fromNodeIdx)
			return false;
		if (orgNodeIdx != other.toNodeIdx)
			return false;
		return true;
	}

	public int getPrevNodeIdx() {
		return prevNodeIdx;
	}

	public int getOrgNodeIdx() {
		return orgNodeIdx;
	}
	
}
