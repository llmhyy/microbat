package microbat.debugpilot.pathfinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import microbat.debugpilot.NodeFeedbacksPair;
import microbat.evaluation.Feedback;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;
import microbat.vectorization.NodeFeatureRecord;

public class FeedbackPath implements Iterable<NodeFeedbacksPair>{

	private List<NodeFeedbacksPair> path = new ArrayList<>();
	
	public FeedbackPath() {}
	
	public FeedbackPath(NodeFeedbacksPair pair) {
		this.path.add(pair);
	}
	
	public FeedbackPath (Collection<NodeFeedbacksPair> path) {
		this.path.addAll(path);
	}
	
	public FeedbackPath(final FeedbackPath other) {
		for (NodeFeedbacksPair pair : other) {
			this.addPair(new NodeFeedbacksPair(pair));
		}
	}
	
	public NodeFeedbacksPair get(final int i) {
		return this.path.get(i);
	}
	
	public int indexOf(final NodeFeedbacksPair userFeedbacksPair) {
		return this.path.indexOf(userFeedbacksPair);
	}
	
	public NodeFeedbacksPair getPairByNode(final TraceNode node) {
		List<NodeFeedbacksPair> possiblePairs = this.path.stream().filter(pair -> pair.getNode().equals(node)).toList();
		if (possiblePairs.isEmpty()) {
			throw new IllegalArgumentException(Log.genMsg(getClass(), "Path does not contain node: " + node.getOrder()));
		}
		return possiblePairs.get(0);
	}
	
	public boolean isEmpty() {
		return this.path.isEmpty();
	}
	
	public NodeFeedbacksPair getLastFeedback() {
		return this.path.get(this.getLength()-1);
	}
	
	public boolean canReachRootCause() {
		NodeFeedbacksPair pair = this.getLastFeedback();
		return pair.getFeedbackType().equals(UserFeedback.ROOTCAUSE);
	}
	
	public void setLastAction(final UserFeedback feedback) {
		this.getLastFeedback().setFeedback(feedback);
	}
	
	public int getIndexOf(final TraceNode node) {
        int index = IntStream.range(0, this.path.size())
                .filter(i -> this.path.get(i).getNode().equals(node))
                .findFirst().orElseThrow();

        return index;
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
	
	public void addPairByOrder(final TraceNode node, final UserFeedback feedback) {
		this.addPairByOrder(new NodeFeedbacksPair(node, feedback));
	}
	
	public void addPairByOrder(final NodeFeedbacksPair pairToInsert) {
		final TraceNode nodeToBeIntert = pairToInsert.getNode();
		int idx=0;
		for (; idx<this.path.size(); idx++) {
			final TraceNode node = this.path.get(idx).getNode();
			if (node.getOrder() < nodeToBeIntert.getOrder()) {
				break;
			}
		}
		this.path.add(idx, pairToInsert);
	}
	
	public void replacePair(final TraceNode node, final UserFeedback feedback) {
		this.replacePair(new NodeFeedbacksPair(node, feedback));
	}
	
	public void replacePair(final NodeFeedbacksPair pairToReplace) {
		for (NodeFeedbacksPair pair : this.path) {
			final TraceNode node = pair.getNode();
			if (node.equals(pairToReplace.getNode())) {
				pair.setFeedbacks(pairToReplace.getFeedbacks());
				return;
			}
		}
		throw new IllegalArgumentException(Log.genMsg(getClass(), "Target node: " + pairToReplace.getNode().getOrder() + " does not appear in the path"));
	}
	
	public int getLength() {
		return this.path.size();
	}
	
	public FeedbackPath subPath(final int fromIdx, final int toIdx) {
		return new FeedbackPath(this.path.subList(fromIdx, toIdx));
	}
	
	public boolean contains(final TraceNode node) {
		for (NodeFeedbacksPair pair : this) {
			if (node.equals(pair.getNode())) {
				return true;
			}
		}
		return false;
	}
	
	public NodeFeedbacksPair getFeedback(final TraceNode node) {
		Objects.requireNonNull(node, Log.genMsg(getClass(), "Given node should not be null"));
		for (NodeFeedbacksPair pair : this.path) {
			if (pair.getNode().equals(node)) {
				return pair;
			}
		}
		throw new IllegalArgumentException(Log.genMsg(getClass(), "Path does not contian the node: " + node.getOrder()));
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
	public int hashCode() {
		final int prime = 7;
		int result = 1;
		for (NodeFeedbacksPair pair : this.path) {
			result = prime * result + pair.hashCode();
		}
		return result;
	}
	
	@Override
	public boolean equals(Object anotherObj) {
		if (anotherObj instanceof FeedbackPath) {
			FeedbackPath otherPath = (FeedbackPath) anotherObj;
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
	
	public boolean isFollowing(final FeedbackPath target) {
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
	
	public static boolean isConnectedPath(final FeedbackPath path, final Trace trace) {
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
	
	public void removePathAfterNode(final TraceNode node) {
		this.removePathAfterNodeOrder(node.getOrder());
	}
	
	public void removePathAfterNodeOrder(final int nodeOrder) {
		boolean nodeOrderInPath = this.path.stream().anyMatch(pair -> pair.getNode().getOrder() == nodeOrder);
		if (!nodeOrderInPath) {
			throw new IllegalArgumentException("Given node order: " + nodeOrder + " does not lies in path.");
		}
		List<NodeFeedbacksPair> newPath = new ArrayList<>();
		for (NodeFeedbacksPair pair : this.path) {
			newPath.add(pair);
			if (pair.getNode().getOrder() == nodeOrder) {
				break;
			}
		}
		this.path = newPath;
	}
	
	public void removePathBeforeNode(final TraceNode node) { 
		this.removePathBeforeNodeOrder(node.getOrder());
	}
	
	public void removePathBeforeNodeOrder(final int nodeOrder) {
		boolean nodeOrderInPath = this.path.stream().anyMatch(pair -> pair.getNode().getOrder() == nodeOrder);
		if (!nodeOrderInPath) {
			throw new IllegalArgumentException("Given node order: " + nodeOrder + " does not lies in path.");
		}
		List<NodeFeedbacksPair> newPath = new ArrayList<>();
		boolean startLoading = false;
		for (NodeFeedbacksPair pair : this.path) {
			if (pair.getNode().getOrder() == nodeOrder) {
				startLoading = true;
			}
			if (startLoading) {
				newPath.add(pair);
			}
		}
		this.path = newPath;
	}
	
	public NodeFeedbacksPair[] toArray() {
		return this.path.stream().toArray(NodeFeedbacksPair[]::new);
	}
}
