package microbat.evaluation.model;

import java.util.ArrayList;
import java.util.List;

import microbat.evaluation.util.TraceNodeVariableSimilarityComparator;
import microbat.model.trace.TraceNode;
import microbat.model.value.GraphNode;

public class TraceNodeWrapper implements GraphNode {

	private TraceNode traceNode;
	
	public TraceNodeWrapper(TraceNode traceNode) {
		this.setTraceNode(traceNode);
	}
	
	@Override
	public List<? extends GraphNode> getChildren() {
		List<TraceNode> abstractionChildren = traceNode.getAbstractChildren();
		List<TraceNodeWrapper> children = new ArrayList<>();
		for(TraceNode node: abstractionChildren){
			TraceNodeWrapper wrapper = new TraceNodeWrapper(node);
			children.add(wrapper);
		}
		return children;
	}

	@Override
	public List<? extends GraphNode> getParents() {
		TraceNode parent = traceNode.getAbstractionParent();
		List<TraceNodeWrapper> wrapperList = new ArrayList<>();
		
		if(parent != null){
			TraceNodeWrapper wrapper = new TraceNodeWrapper(parent);
			wrapperList.add(wrapper);
		}
		
		return wrapperList;
	}

	@Override
	public boolean match(GraphNode node) {
		if(node instanceof TraceNodeWrapper){
			TraceNodeWrapper thatNode = (TraceNodeWrapper)node;
			
			return this.traceNode.getLineNumber() == thatNode.getTraceNode().getLineNumber();
			
		}
		
		return false;
	}

	@Override
	public boolean isTheSameWith(GraphNode node) {
		if(node instanceof TraceNodeWrapper){
			TraceNodeWrapper thatNode = (TraceNodeWrapper)node;
			TraceNodeVariableSimilarityComparator comparator = new TraceNodeVariableSimilarityComparator();
			
			double sim = comparator.compute(this.getTraceNode(), thatNode.getTraceNode());
			
			return sim==1;
		}
		return false;
	}

//	@Override
//	public String getStringValue() {
//		return traceNode.toString();
//	}

	public TraceNode getTraceNode() {
		return traceNode;
	}

	public void setTraceNode(TraceNode traceNode) {
		this.traceNode = traceNode;
	}

	@Override
	public String toString(){
		return this.traceNode.toString();
	}
}
