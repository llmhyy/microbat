package microbat.probability.SPP.pathfinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import debuginfo.NodeFeedbackPair;
import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.TraceNode;
import microbat.recommendation.UserFeedback;

public class ActionPath implements Iterable<NodeFeedbacksPair>{

	private List<NodeFeedbacksPair> path = new ArrayList<>();
	
	public ActionPath() {
		
	}
	
	public ActionPath (List<NodeFeedbacksPair> path) {
		this.path = path;
	}
	
	public ActionPath(final ActionPath other) {
		for (NodeFeedbacksPair pair : other) {
			this.addPair(new NodeFeedbacksPair(pair));
		}
	}
	
	public NodeFeedbacksPair get(final int i) {
		return this.path.get(i);
	}
	
	public boolean isEmpty() {
		return this.path.isEmpty();
	}
	
	public NodeFeedbacksPair peek() {
		return this.path.get(this.getLength()-1);
	}
	
	public boolean canReachRootCause() {
		NodeFeedbacksPair pair = this.peek();
		return pair.getFeedbackType().equals(UserFeedback.ROOTCAUSE);
	}
	
	public void setLastAction(final UserFeedback feedback) {
		this.peek().setFeedback(feedback);
	}
	
	public boolean isVisited(final TraceNode node) {
		for (NodeFeedbacksPair pair : this.path) {
			TraceNode pathNode = pair.getNode();
			if (pathNode.equals(node)) {
				return true;
			}
		}
		return false;
	}
	
	public void addPair(final TraceNode node, final UserFeedback feedback) {
		this.addPair(new NodeFeedbacksPair(node, feedback));
	}
	
	public void addPair(final NodeFeedbacksPair pair) {
		this.path.add(pair);
	}
	
	public int getLength() {
		return this.path.size();
	}
	
	public boolean contains(final TraceNode node) {
		for (NodeFeedbacksPair pair : this) {
			if (node.equals(pair.getNode())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		for (NodeFeedbacksPair pair : path) {
			strBuilder.append(pair.toString());
			strBuilder.append("\n");
		}
		return strBuilder.toString();
	}
	
	@Override
	public Iterator<NodeFeedbacksPair> iterator() {
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
				NodeFeedbacksPair thisPair = this.path.get(i);
				NodeFeedbacksPair otherPair = otherPath.path.get(i);
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
		
		if (this.getLength() < target.getLength()) {
			return false;
		}
		
		for (int i=0; i<target.getLength(); i++) {
			final NodeFeedbacksPair targetPair = target.get(i);
			final TraceNode targetNode = targetPair.getNode();

			final NodeFeedbacksPair pair = this.get(i);
			final TraceNode node = pair.getNode();

			if(!targetNode.equals(node)) {
				return false;
			}
			
			if(!targetPair.haveCommonFeedbackWith(pair)) {
				return false;
			}
		}
		
		return true;
	}

}
