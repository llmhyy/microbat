package microbat.debugpilot.pathfinding;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import microbat.debugpilot.userfeedback.DPUserFeedback;
import microbat.log.Log;
import microbat.model.trace.TraceNode;
import microbat.util.TraceUtil;

/**
 * Path from certain node to another <br/>
 * The path is constructed by Debug Pilot user feedback
 */
public class FeedbackPath {
	
	protected LinkedList<DPUserFeedback> feedbacks = new LinkedList<>();
	
	public FeedbackPath() {}
	
	public FeedbackPath(final List<DPUserFeedback> feedbacks) {
		this.add(feedbacks);
	}
	
	public FeedbackPath(final DPUserFeedback... feedbacks) {
		this(Arrays.asList(feedbacks));
	}
	
	public FeedbackPath(final FeedbackPath otherPath) {
		this(otherPath.feedbacks);
	}
	
	public List<DPUserFeedback> getFeedbacks() {
		return this.feedbacks;
	}
	
	/**
	 * Get the idx-th feedback in path
	 * @param idx Target index
	 * @return idx-th feedback in path
	 */
	public DPUserFeedback get(final int idx) {
		return this.feedbacks.get(idx);
	}
	
	/**
	 * Get the index of give feedback in path
	 * @param feedback Target feedback
	 * @return Index of give feedback in path
	 */
	public int indexOf(final DPUserFeedback feedback) {
		return this.feedbacks.indexOf(feedback);
	}
	
	public void add(final int idx, final DPUserFeedback feedback) {
		this.feedbacks.add(idx, feedback);
	}
	
	public void add(final DPUserFeedback... feedbacks) {
		this.add(Arrays.asList(feedbacks));
	}
	
	public void add(final List<DPUserFeedback> feedbacks) {
		for (DPUserFeedback feedback : feedbacks) {
			if (this.feedbacks.contains(feedback)) {
				throw new IllegalArgumentException("Given feedback already exist in the path: " + feedback);
			}
			this.feedbacks.add(feedback);
		}
	}
	
	public void set(final int idx, final DPUserFeedback feedback) {
		this.feedbacks.set(idx, feedback);
	}
	
	public boolean containFeedbackByNode(final TraceNode node) {
		return this.containFeedbackByNodeOrder(node.getOrder());
	}
	
	public boolean containFeedbackByNodeOrder(final int nodeOrder) {
		return this.feedbacks.stream()
				.anyMatch(feedback -> feedback.getNode().getOrder() == nodeOrder);
	}
	
	public DPUserFeedback getFeedbackByNode(final TraceNode node) {
		Optional<DPUserFeedback> feedbackOptional = this.feedbacks.stream().filter(f -> f.getNode().equals(node)).findFirst();
		return feedbackOptional.isPresent() ? feedbackOptional.get() : null;
	}
	
	public DPUserFeedback getLastFeedback() {
		return this.feedbacks.getLast();
	}
	
	public void removeLast() {
		this.feedbacks.remove(this.feedbacks.getLast());
	}
	
	public void replaceLast(final DPUserFeedback feedback) {
		this.removeLast();
		this.add(feedback);
	}
	
	public DPUserFeedback getFirstFeedback() {
		return this.feedbacks.getFirst();
	}
	
	
	public void remove(final int idx) {
		this.feedbacks.remove(idx);
	}
	
	public void remove(final DPUserFeedback feedback) {
		this.feedbacks.remove(feedback);
	}
	
	/**
	 * Check is it a empty path
	 * @return True if the path is empty
	 */
	public boolean isEmpty() {
		return this.feedbacks.isEmpty();
	}
	
	/**
	 * Size of the path
	 * @return Size of the path
	 */
	public int length() {
		return this.feedbacks.size();
	}
	
	public FeedbackPath getSubPath(final int startIdx, final int endIdx) {
		if (startIdx < 0 || startIdx > this.feedbacks.size()) {
			throw new IndexOutOfBoundsException("Given startIdx is out of bound: " + startIdx);
		}
		if (endIdx < 0 || endIdx > feedbacks.size()) {
			throw new IndexOutOfBoundsException("Given endIdx is out of bound: " + endIdx);
		}
		if (endIdx < startIdx) {
			throw new IllegalArgumentException("Given endIdx: " + endIdx + " is smaller than startIdx: " + startIdx);
		}
		
		List<DPUserFeedback> newFeedbackList = this.feedbacks.subList(startIdx, endIdx);
		return new FeedbackPath(newFeedbackList);
	}
	
	public boolean isConnected() {
		if (this.isEmpty()) {
			return false;
		}
		
		if (this.length() == 1) {
			return true;
		}
		
		// Check can every previous feedback can lead to current node
		for (int idx=1; idx<this.length(); idx++) {
			final DPUserFeedback prevFeedback = this.get(idx-1);
			final Set<TraceNode> reachableNodes = TraceUtil.findAllNextNodes(prevFeedback);
			final TraceNode currentNode = this.get(idx).getNode();
			if (!reachableNodes.contains(currentNode)) {
				return false;
			}
		}
		
		return true;
	}
	
	public DPUserFeedback[] toArray() {
		return this.feedbacks.toArray(new DPUserFeedback[0]);
	}
	
	@Override
	public int hashCode() {
		return feedbacks.hashCode();
	}
	
	@Override
	public boolean equals(Object otherObj) {
		if (this == otherObj) return true;
		if (otherObj == null  || this.getClass() != otherObj.getClass()) return false;
		
		FeedbackPath otherPath = (FeedbackPath) otherObj;
		return this.feedbacks.equals(otherPath.feedbacks);
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		int idx = 0;
		for (DPUserFeedback feedback : this.feedbacks) {
			stringBuilder.append(idx++ + "," + feedback + "\n");
		}
		return stringBuilder.toString();
	}
	
	public static FeedbackPath concat(final FeedbackPath path1, final FeedbackPath path2) {
		Objects.requireNonNull(path1, Log.genMsg("FeedbackPath", "Given path1 cannot be null"));
		Objects.requireNonNull(path2, Log.genMsg("FeedbackPath", "Given path2 cannot be null"));
		
		if (path1.isEmpty()) return new FeedbackPath(path2);
		if (path2.isEmpty()) return new FeedbackPath(path1);
		
		FeedbackPath newPath = new FeedbackPath(path1);
		
		// Check is the last node of path1 and first node of path2 is the same
		final TraceNode lastNodeInPath1 = path1.getLastFeedback().getNode();
		final TraceNode firstNodeInPath2 = path2.getFirstFeedback().getNode();
		if (lastNodeInPath1.equals(firstNodeInPath2)) {
			newPath.removeLast();
			newPath.add(path2.getFeedbacks());
		} else {
			// Check can last node in path1 lead to first node in path2
			Set<TraceNode> possibleNextNodes = TraceUtil.findAllNextNodes(path1.getLastFeedback());
			if (!possibleNextNodes.contains(firstNodeInPath2)) {
				throw new RuntimeException(Log.genMsg("FeedbackPath", "path1 and path2 is not connected"));
			}
			newPath.add(path2.getFeedbacks());
		}
		if (!newPath.isConnected()) {
			throw new RuntimeException(Log.genMsg("FeedbackPath", "Concatenated path is not connected"));
		}
		return newPath;
	}
	
	public static FeedbackPath splicePathAtIndex(final FeedbackPath sourcePath, final FeedbackPath splicePath, final int idx) {
		Objects.requireNonNull(sourcePath, Log.genMsg("FeedbackPath", "Given source path cannot be null"));
		Objects.requireNonNull(splicePath, Log.genMsg("FeedbackPath", "Given splice path cannot be null"));
		if (idx == 0) {return splicePath;}
		if (idx > sourcePath.length()) {return FeedbackPath.concat(sourcePath, splicePath);}
		return FeedbackPath.concat(sourcePath.getSubPath(0, idx), splicePath);
	}
}
