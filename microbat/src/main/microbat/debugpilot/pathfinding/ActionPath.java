package microbat.debugpilot.pathfinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import debuginfo.NodeFeedbackPair;
import debuginfo.NodeFeedbacksPair;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;

public class ActionPath implements Iterable<NodeFeedbacksPair>{

	private List<NodeFeedbacksPair> path = new ArrayList<>();
	
	public ActionPath() {}
	
	public ActionPath(NodeFeedbacksPair pair) {
		this.path.add(pair);
	}
	public ActionPath (Collection<NodeFeedbacksPair> path) {
		this.path.addAll(path);
	}
	
	public ActionPath(final ActionPath other) {
		for (NodeFeedbacksPair pair : other) {
			this.addPair(new NodeFeedbacksPair(pair));
		}
	}
	
	public static ActionPath concat(final ActionPath path1, final ActionPath path2, final Trace trace) {
		if (path1 == null && path2 == null) return null;
		if (path1 == null) return path2;
		if (path2 == null) return path1;
		if (path1.isEmpty()) return path2;
		if (path2.isEmpty()) return path1;
		
		ActionPath path = new ActionPath(path1);
		TraceNode lastNode = path1.peek().getNode();
		TraceNode nextNode = path2.get(0).getNode();
		if (lastNode.equals(nextNode)) {
			path.pop();
		} 
		
		if (path1.peek().getFeedbacks().size() > 1) {
			final NodeFeedbacksPair pair = path1.pop();
			final TraceNode node = pair.getNode();
			NodeFeedbacksPair newPair = null;
			for (UserFeedback feedback : pair.getFeedbacks()) {
				TraceNode targetNextNode = TraceUtil.findNextNode(node, feedback, trace);
				if (targetNextNode.equals(nextNode)) {
					newPair = new NodeFeedbacksPair(node, feedback);
					break;
				}
			}
			if (newPair == null) {
				throw new RuntimeException(Log.genMsg(ActionPath.class, "Concatinating two path that is not connected"));
			}
			path.addPair(newPair);
		}
		
		for (NodeFeedbacksPair pair : path2) {
			path.addPair(pair);
		}
		
		return path;
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
	
	public void removeFirstAction() {
		if (!this.path.isEmpty()) {
			this.path.remove(0);
		}
	}
	
	public NodeFeedbacksPair pop() {
		if (!this.path.isEmpty()) {
			final int lastIdx = this.path.size()-1;
			NodeFeedbacksPair pair = this.path.get(lastIdx);
			this.path.remove(lastIdx);
			return pair;
		} else {
			throw new RuntimeException(Log.genMsg(getClass(), "Trying to pop an empty path"));
		}
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
	
	public static boolean isConnectedPath(final ActionPath path, final Trace trace) {
		if (path == null) throw new IllegalArgumentException("path should not be null");
		if (trace == null) throw new IllegalArgumentException("trace should not be null");
		if (path.isEmpty()) return false;
		
		// Path with length 1 is completed only when the action is root cause or correct
		if (path.getLength() == 1) {
			return path.get(0).getFeedbackType().equals(UserFeedback.ROOTCAUSE) ||
				   path.get(0).getFeedbackType().equals(UserFeedback.CORRECT);
		}
		
		NodeFeedbacksPair firstAction = path.get(0);
		Set<TraceNode> reachableNodes = TraceUtil.findAllNextNodes(firstAction.getNode(), firstAction.getFeedbacks(), trace);
		for (int idx=1; idx<path.getLength(); idx++) {
			final NodeFeedbacksPair action = path.get(idx);
			final TraceNode node = action.getNode();
			if (!reachableNodes.contains(node)) {
				return false;
			}
			if (action.getFeedbackType().equals(UserFeedback.ROOTCAUSE)) {
				break;
			}
			reachableNodes = TraceUtil.findAllNextNodes(node, action.getFeedbacks(), trace);
		}
		return true;
	}
}
