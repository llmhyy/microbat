package microbat.instrumentation.cfgcoverage.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lyly
 *
 */
public class CoveragePath {
	private List<Integer> coveredTcs;
	private List<CoverageSFNode> path;

	public List<Integer> getCoveredTcs() {
		return coveredTcs;
	}

	public void setCoveredTcs(List<Integer> coveredTcs) {
		this.coveredTcs = coveredTcs;
	}

	public List<Integer> getIdPath() {
		List<Integer> ids = new ArrayList<>(path.size());
		for (CoverageSFNode node : path) {
			ids.add(node.getCvgIdx());
		}
		return ids;
	}
	
	public void setPath(List<CoverageSFNode> nodes) {
		this.path = nodes;
	}

	@Override
	public String toString() {
		return "CoveragePath [path=" + path + ", coveredTcs=" + coveredTcs + "]";
	}
	
	public List<CoverageSFNode> getPath() {
		return path;
	}
}
