package microbat.evaluation.util;

import java.util.List;

import microbat.evaluation.model.PairList;
import microbat.model.trace.TraceNode;

public class TraceNodeStructureSimilarityComparator implements TraceNodeSimilarityComparator{
	public double compute(TraceNode traceNode1, TraceNode traceNode2) {
		List<TraceNode> children1 = traceNode1.getAbstractChildren();
		List<TraceNode> children2 = traceNode2.getAbstractChildren();
		
		if(!children1.isEmpty() && !children2.isEmpty()){
			TraceNode[] array1 = children1.toArray(new TraceNode[0]);
			TraceNode[] array2 = children2.toArray(new TraceNode[0]);
			
			PairList commonList = DiffUtil.generateMatchedTraceNodeList(array1, array2, new TraceNodeLocationSimilarityComparator());
			
			double value = 2.0*commonList.size()/(array1.length+array2.length);
			return value;
		}
		else if(children1.isEmpty() && children2.isEmpty()){
			return 1;
		}
		else{
			return 0;
		}
	}
}
