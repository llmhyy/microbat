package microbat.instrumentation.cfgcoverage.graph;

import java.io.Serializable;

public class Branch implements Serializable {
	private static final long serialVersionUID = -1054499814399081119L;
	protected int fromNodeIdx;
	protected int toNodeIdx;
	
	public Branch(int fromNodeIdx, int toNodeIdx) {
		this.fromNodeIdx = fromNodeIdx;
		this.toNodeIdx = toNodeIdx;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + fromNodeIdx;
		result = prime * result + toNodeIdx;
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
		if (fromNodeIdx != other.fromNodeIdx)
			return false;
		if (toNodeIdx != other.toNodeIdx)
			return false;
		return true;
	}

	public int getFromNodeIdx() {
		return fromNodeIdx;
	}

	public int getToNodeIdx() {
		return toNodeIdx;
	}

	@Override
	public String toString() {
		return "Branch [fromNodeIdx=" + fromNodeIdx + ", toNodeIdx=" + toNodeIdx + "]";
	}
}
