package microbat.probability.SPP.pathfinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import debuginfo.NodeFeedbackPair;
import microbat.model.trace.TraceNode;
import microbat.recommendation.UserFeedback;

public class ActionPath implements Iterable<NodeFeedbackPair>{

	private List<NodeFeedbackPair> path = new ArrayList<>();
	
	public ActionPath() {
		
	}
	
	public ActionPath (List<NodeFeedbackPair> path) {
		this.path = path;
	}
	
	public ActionPath(final ActionPath other) {
		for (NodeFeedbackPair pair : other) {
			this.addPair(new NodeFeedbackPair(pair));
		}
	}
	
	public NodeFeedbackPair get(final int i) {
		return this.path.get(i);
	}
	
	public boolean isEmpty() {
		return this.path.isEmpty();
	}
	
	public NodeFeedbackPair peek() {
		return this.path.get(this.getLength()-1);
	}
	
	public boolean canReachRootCause() {
		NodeFeedbackPair pair = this.peek();
		UserFeedback feedback = pair.getFeedback();
		return feedback.getFeedbackType().equals(UserFeedback.ROOTCAUSE);
	}
	
	public void setLastAction(final UserFeedback feedback) {
		this.peek().setFeedback(feedback);
	}
	
	public boolean isVisited(final TraceNode node) {
		for (NodeFeedbackPair pair : this.path) {
			TraceNode pathNode = pair.getNode();
			if (pathNode.equals(node)) {
				return true;
			}
		}
		return false;
	}
	
	public void addPair(final TraceNode node, final UserFeedback feedback) {
		this.addPair(new NodeFeedbackPair(node, feedback));
	}
	
	public void addPair(final NodeFeedbackPair pair) {
		this.path.add(pair);
	}
	
	public int getLength() {
		return this.path.size();
	}
	
	public boolean contains(final TraceNode node) {
		for (NodeFeedbackPair pair : this) {
			if (node.equals(pair.getNode())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		for (NodeFeedbackPair pair : path) {
			strBuilder.append(pair.toString());
			strBuilder.append("\n");
		}
		return strBuilder.toString();
	}
	
	@Override
	public Iterator<NodeFeedbackPair> iterator() {
		return this.path.iterator();
	}
	
	@Override
	public boolean equals(Object anotherObj) {
		if (anotherObj instanceof ActionPath) {
			ActionPath otherPath = (ActionPath) anotherObj;
			if (this.path.size() != otherPath.path.size()) {
				return false;
			}
			
			for (int i=0; i<this.path.size(); i++) {
				NodeFeedbackPair thisPair = this.path.get(i);
				NodeFeedbackPair otherPair = otherPath.path.get(i);
				if (!thisPair.equals(otherPair)) {
					return false;
				}
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isFollowing(final ActionPath target) {
		if (this.getLength() == 0) {
			return true;
		}
		
		if (this.getLength() > target.getLength()) {
			return false;
		}
		
		for (int i=0; i<target.getLength(); i++) {
			final NodeFeedbackPair targetPair = target.get(i);
			final TraceNode targetNode = targetPair.getNode();
			final UserFeedback targetFeedback = targetPair.getFeedback();
			
			final NodeFeedbackPair pair = this.get(i);
			final TraceNode node = pair.getNode();
			final UserFeedback feedback = pair.getFeedback();
			
			if(!targetNode.equals(node)) {
				return false;
			}
			
			if(!targetFeedback.week_equals(feedback)) {
				return false;
			}
		}
		
		return true;
	}

}
