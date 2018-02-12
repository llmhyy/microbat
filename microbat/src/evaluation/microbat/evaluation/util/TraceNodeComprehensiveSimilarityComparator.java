package microbat.evaluation.util;

import microbat.model.trace.TraceNode;

public class TraceNodeComprehensiveSimilarityComparator implements TraceNodeSimilarityComparator{
	public double compute(TraceNode traceNode1, TraceNode traceNode2) {
		TraceNodeVariableSimilarityComparator vComparator = new TraceNodeVariableSimilarityComparator();
		TraceNodeStructureSimilarityComparator sComparator = new TraceNodeStructureSimilarityComparator();
		
		if(traceNode1.isAbstractParent() && traceNode2.isAbstractParent()){
			double s1 = vComparator.compute(traceNode1, traceNode2);
			double s2 = sComparator.compute(traceNode1, traceNode2);
			
			return (s1+s2)/2;
		}
		else if(!traceNode1.isAbstractParent() && !traceNode2.isAbstractParent()){
			return vComparator.compute(traceNode1, traceNode2);
		}
		else{
			return 0;
		}
	}
}
