package microbat.evaluation.util;

import microbat.model.trace.TraceNode;

public class TraceNodeLocationSimilarityComparator implements TraceNodeSimilarityComparator {

	@Override
	public double compute(TraceNode traceNode1, TraceNode traceNode2) {
		if(traceNode1.hasSameLocation(traceNode2)){
			return 1;
		}
		else{
			return 0;
		}
	}

}
