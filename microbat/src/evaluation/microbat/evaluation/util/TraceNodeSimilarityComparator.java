package microbat.evaluation.util;

import microbat.model.trace.TraceNode;

public interface TraceNodeSimilarityComparator {
	public double compute(TraceNode traceNode1, TraceNode traceNode2);
}
