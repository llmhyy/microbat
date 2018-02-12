package microbat.evaluation.model;

import java.util.ArrayList;
import java.util.List;

import microbat.algorithm.graphdiff.Matcher;
import microbat.algorithm.graphdiff.MatchingGraphPair;
import microbat.evaluation.util.DiffUtil;
import microbat.evaluation.util.TraceNodeSimilarityComparator;
import microbat.model.trace.TraceNode;
import microbat.model.value.GraphNode;

public class LCSMatcher implements Matcher {

	private TraceNodeSimilarityComparator comparator;
	
	public LCSMatcher(TraceNodeSimilarityComparator comparator) {
		this.comparator = comparator;
	}
	
	@Override
	public List<MatchingGraphPair> matchList(List<? extends GraphNode> childrenBefore,
			List<? extends GraphNode> childrenAfter) {
		
		TraceNode[] beforeList = transferToTraceNodeList(childrenBefore);
		TraceNode[] afterList = transferToTraceNodeList(childrenAfter);
		
		PairList pairList = DiffUtil.generateMatchedTraceNodeList(beforeList, afterList, comparator);
		
		List<MatchingGraphPair> matchingList = new ArrayList<>();
		for(TraceNodePair pair: pairList.getPairList()){
			TraceNodeWrapper wrapperBefore = new TraceNodeWrapper(pair.getMutatedNode());
			TraceNodeWrapper wrapperAfter = new TraceNodeWrapper(pair.getOriginalNode());
			
			MatchingGraphPair match = new MatchingGraphPair(wrapperBefore, wrapperAfter);
			matchingList.add(match);
		}
		
		return matchingList;
	}

	private TraceNode[] transferToTraceNodeList(List<? extends GraphNode> children) {
		List<TraceNode> list = new ArrayList<>();
		
		for(GraphNode child: children){
			if(child instanceof TraceNodeWrapper){
				TraceNodeWrapper node = (TraceNodeWrapper)child;
				list.add(node.getTraceNode());
			}
		}
		
		TraceNode[] array = list.toArray(new TraceNode[0]);
		
		return array;
	}

}
