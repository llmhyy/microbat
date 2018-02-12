package microbat.evaluation.model;

import java.util.ArrayList;
import java.util.List;

import microbat.model.trace.TraceNode;

public class PairList {
	private List<TraceNodePair> pairList = new ArrayList<>();

	public PairList(List<TraceNodePair> pairList) {
		super();
		this.pairList = pairList;
	}

	public List<TraceNodePair> getPairList() {
		return pairList;
	}

	public void setPairList(List<TraceNodePair> pairList) {
		this.pairList = pairList;
	}
	
	public void add(TraceNodePair pair){
		this.pairList.add(pair);
	}

	public TraceNodePair findByMutatedNode(TraceNode node) {
		for(TraceNodePair pair: pairList){
			if(pair.getMutatedNode().equals(node)){
				return pair;
			}
		}
		return null;
	}
	
	public TraceNodePair findByOriginalNode(TraceNode node) {
		for(TraceNodePair pair: pairList){
			if(pair.getOriginalNode().equals(node)){
				return pair;
			}
		}
		return null;
	}
	
	public int size(){
		return pairList.size();
	}
}
