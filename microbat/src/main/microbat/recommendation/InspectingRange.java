package microbat.recommendation;

import microbat.model.trace.TraceNode;

public class InspectingRange {
	TraceNode startNode;
	TraceNode endNode;

	public InspectingRange(TraceNode startNode, TraceNode endNode) {
		this.startNode = startNode;
		this.endNode = endNode;
	}

	// public InspectingRange(Map<TraceNode, List<String>> dataDominator,
	// TraceNode suspiciousNode) {
	// ArrayList<TraceNode> dominators = new
	// ArrayList<TraceNode>(dataDominator.keySet());
	// Collections.sort(dominators, new TraceNodeOrderComparator());
	//
	// startNode = dominators.get(0);
	// endNode = suspiciousNode;
	// }

	public InspectingRange clone() {
		InspectingRange inspectingRange = new InspectingRange(startNode, endNode);
		return inspectingRange;
	}
}
