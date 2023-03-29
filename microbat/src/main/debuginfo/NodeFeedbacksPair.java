package debuginfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import microbat.model.trace.TraceNode;
import microbat.recommendation.UserFeedback;

public class NodeFeedbacksPair {
	private TraceNode node = null;
	private List<UserFeedback> feedbacks = new ArrayList<>();
	
	public NodeFeedbacksPair(final TraceNode node, final List<UserFeedback> feedbacks) {
		this.node = node;
		this.feedbacks.addAll(feedbacks);
	}
	
	public NodeFeedbacksPair(final TraceNode node, final UserFeedback feedback) {
		this.node = node;
		this.feedbacks.add(feedback);
	}
	
	public boolean doNotHaveFeedback() {
		return this.feedbacks.isEmpty();
	}
	
	public NodeFeedbacksPair(final NodeFeedbacksPair pair) {
		this.node = pair.getNode();
		this.feedbacks.addAll(pair.getFeedbacks());
	}
	
	public NodeFeedbacksPair(final TraceNode node) {
		this.node = node;
	}
	
	public NodeFeedbacksPair() {
		
	}
	
	public TraceNode getNode() {
		return this.node;
	}
	
	public List<UserFeedback> getFeedbacks() {
		return this.feedbacks;
	}
	
	public void setNode(final TraceNode node) {
		this.node = node;
	}
	
	public void setFeedback(final UserFeedback feedback) {
		this.feedbacks.clear();
		this.feedbacks.add(feedback);
	}
	
	public void setFeedbacks(final List<UserFeedback> feedbacks) {
		this.feedbacks.clear();
		this.feedbacks.addAll(feedbacks);
	}
	
	public void addFeedback(final UserFeedback feedback) {
		this.feedbacks.add(feedback);
	}
	
	public void addFeedbacks(final Collection<UserFeedback> feedbacks) {
		this.feedbacks.addAll(feedbacks);
	}
	
	public boolean containsFeedback(final UserFeedback feedback) {
		return this.feedbacks.contains(feedback);
	}
	
	public UserFeedback getFirstFeedback() {
		if (this.feedbacks.isEmpty()) {
			return null;
		}
		return this.feedbacks.get(0);
	}
	
	public boolean haveCommonFeedbackWith(final NodeFeedbacksPair otherPair) {
		Set<UserFeedback> thisFeedbackSet = new HashSet<>();
		thisFeedbackSet.addAll(this.feedbacks);
		
		Set<UserFeedback> otherFeedbackSet = new HashSet<>();
		otherFeedbackSet.addAll(otherPair.getFeedbacks());
		
		thisFeedbackSet.retainAll(otherFeedbackSet);
		
		return !thisFeedbackSet.isEmpty();
	}
	
	public String getFeedbackType() {
		if (this.feedbacks.isEmpty()) {
			return "";
		}
		String feedbackType = this.feedbacks.get(0).getFeedbackType();
		if (this.feedbacks.size() == 1) {
			return feedbackType;
		}
		
		// Verify that the feedback type are the same
		for (int i=1; i<this.feedbacks.size(); i++) {
			UserFeedback feedback = this.feedbacks.get(i);
			if (!feedbackType.equals(feedback.getFeedbackType())) {
				return null;
			}
		}
		return feedbackType;
	}
	
	public boolean equalNode(final NodeFeedbackPair otherPair) {
		return this.equalNode(otherPair.getNode());
	}
	
	public boolean equalNode(final TraceNode node) {
		return this.node.equals(node);
	}
	
	@Override
	public boolean equals(Object otherObj) {
		if (otherObj instanceof NodeFeedbacksPair) {
			NodeFeedbacksPair otherPair = (NodeFeedbacksPair) otherObj;
			if (!this.node.equals(otherPair.getNode())) {
				return false;
			}
			Set<UserFeedback> thisFeedbackSet = new HashSet<>();
			thisFeedbackSet.addAll(this.feedbacks);
			
			Set<UserFeedback> otherFeedbackSet = new HashSet<>();
			otherFeedbackSet.addAll(otherPair.getFeedbacks());
			
			return thisFeedbackSet.equals(otherFeedbackSet);
		}
		return false;
	}
	
	@Override
	public String toString() {
		String string = "Node: " + this.node.getOrder() + " with feedback: ";
		for (UserFeedback feedback : this.feedbacks) {
			string += feedback;
			string += ",";
		}
		return string;
	} 
}
