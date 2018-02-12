package microbat.evaluation;

import java.util.Comparator;

import microbat.evaluation.model.TraceNodePair;

public class TraceNodePairReverseOrderComparator implements Comparator<TraceNodePair>{

	@Override
	public int compare(TraceNodePair pair1, TraceNodePair pair2) {
		return pair2.getMutatedNode().getOrder() - pair1.getMutatedNode().getOrder();
	}

	
	
}
