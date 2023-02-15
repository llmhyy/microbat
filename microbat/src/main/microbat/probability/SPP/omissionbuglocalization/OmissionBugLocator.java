package microbat.probability.SPP.omissionbuglocalization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;

public class OmissionBugLocator {
	private final Trace trace;
	private NodeFeedbacksPair startFeedback;
	private NodeFeedbacksPair endFeedback;
	
	private Queue<TraceNode> suspiciousNodes;
	private OmissionBugScope bugScope;
	
	private Set<TraceNode> records = new HashSet<>();
	
	public OmissionBugLocator(Trace trace, NodeFeedbacksPair startFeedback, NodeFeedbacksPair endFeedback) {
		if (endFeedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
			assert startFeedback.getFeedbackType().equals(UserFeedback.CORRECT) : "It is not omission bug";
		}
		this.trace = trace;
		this.startFeedback = startFeedback;
		this.endFeedback = endFeedback;
		this.bugScope = new OmissionBugScope(this.startFeedback.getNode(), this.endFeedback.getNode());
		this.suspiciousNodes = this.getSuspiciousNodes();
	}
	
	public OmissionBugScope getBugScope() {
		return this.bugScope;
	}
	
	private Queue<TraceNode> getSuspiciousNodes() {
		Queue<TraceNode> suspiciousNodes = new LinkedList<>();
		if (startFeedback.doNotHaveFeedback() && !this.isFeedbackGiven(startFeedback.getNode())) {
			suspiciousNodes.add(startFeedback.getNode());
		}
		for (int order = startFeedback.getNode().getOrder(); order <=endFeedback.getNode().getOrder(); order++) {
			final TraceNode node = this.trace.getTraceNode(order);
			if (node.isBranch() && !this.isFeedbackGiven(node)) {
				suspiciousNodes.add(node);
				continue;
			}
			if (endFeedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
				final List<VarValue> outdatedVars = new ArrayList<>(startFeedback.getNode().getWrittenVariables());
				outdatedVars.retainAll(node.getReadVariables());
				if (!outdatedVars.isEmpty() && !this.isFeedbackGiven(node)) {
					suspiciousNodes.add(node);
				}
			}
		}
		return suspiciousNodes;
	}
	
	private boolean isFeedbackGiven(final TraceNode node) {
		return this.records.contains(node);
	}
	
	public boolean isInvestigationFinished() {
		return this.suspiciousNodes.isEmpty();
	}
	
	public TraceNode getSuspiciousNode() {
		return this.suspiciousNodes.poll();
	}
	
	public static TraceNode getStartNodeForControlFlowOmission(final TraceNode node, final Trace trace) {
		TraceNode targetNode = node.getInvocationParent();
		if (targetNode != null) {
			return targetNode;
		}
		
		List<TraceNode> candidateNodes = new ArrayList<>();
		for (VarValue readVar : node.getReadVariables()) {
			candidateNodes.add(trace.findDataDependency(node, readVar));
		}
		
		if (candidateNodes.isEmpty()) {
			return trace.getTraceNode(1);
		} else {
			targetNode = candidateNodes.get(0);
			for (TraceNode candidateNode : candidateNodes) {
				if (candidateNode.getOrder() < targetNode.getOrder()) {
					targetNode = candidateNode;
				}
			}
			return targetNode;
		}
	}
	
	public void takeFeedback(final NodeFeedbacksPair userFeedbackPair) {
		final TraceNode node = userFeedbackPair.getNode();
		if (userFeedbackPair.getFeedbackType().equals(UserFeedback.CORRECT)) {
			if (node.getOrder() > this.bugScope.getStartNode().getOrder()) {
				this.bugScope.updateStartNode(node);
			}
		} else if (userFeedbackPair.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
			TraceNode startNode = OmissionBugLocator.getStartNodeForControlFlowOmission(node, this.trace);
			NodeFeedbacksPair newStartFeedback = new NodeFeedbacksPair(startNode);
			this.startFeedback = newStartFeedback;
			this.endFeedback = userFeedbackPair;
			this.bugScope.updateStartNode(startNode);
			this.bugScope.updateEndNode(node);
			this.suspiciousNodes = this.getSuspiciousNodes();
		} else if (userFeedbackPair.getFeedbackType().equals((UserFeedback.WRONG_VARIABLE_VALUE))) {
			if (node.getOrder() < this.bugScope.getEndNode().getOrder()) {
				this.bugScope.updateEndNode(node);
			}
		}
		this.records.add(userFeedbackPair.getNode());
	}
}
