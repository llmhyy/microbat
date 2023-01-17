package microbat.probability.SPP;

import debuginfo.NodeFeedbackPair;
import microbat.model.trace.TraceNode;

public interface DijstraNode {
	public double getDistance();
	public void setDistance(final double distance);
//	public double calProb();
	
	public boolean isVisited();
	public void setVisisted(final boolean isVisited);
	
	public NodeFeedbackPair getPrevAction();
	public void setPrevAction(final NodeFeedbackPair node);
	
	public void init(boolean isStartNode);
	
	public TraceNode getTraceNode();
}
