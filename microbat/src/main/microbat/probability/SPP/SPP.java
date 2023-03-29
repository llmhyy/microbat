package microbat.probability.SPP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import debuginfo.NodeFeedbackPair;
import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.probability.PropProbability;
import microbat.probability.SPP.pathfinding.ActionPath;
import microbat.probability.SPP.pathfinding.PathFinder;
import microbat.probability.SPP.propagation.ProbPropagator;
import microbat.recommendation.UserFeedback;
import microbat.recommendation.UserFeedback_M;
import microbat.util.TraceUtil;

/**
 * StepwisePropagator is used to propagate
 * the probability of correctness
 * 
 * It propagate the probability by only
 * considering the current node and it's
 * first order neighbors
 * 
 * Time complexity is linear. O(n) where n
 * is the trace length
 * 
 * @author David
 *
 */
public class SPP {
	
	/**
	 * Execution trace of target program
	 */
	private final Trace trace;
	
	/**
	 * List of input variables which assumed to be correct
	 */
	private Set<VarValue> correctVars = new HashSet<>();
	
	/**
	 * List of outputs variables which assumed to be wrong
	 */
	private Set<VarValue> wrongVars = new HashSet<>();
	
	/**
	 * List of executed trace node after dynamic slicing
	 */
	private List<TraceNode> slicedTrace = null;

	
	private List<NodeFeedbacksPair> feedbackRecords = new ArrayList<>();
	private final ProbAggregator aggregator = new ProbAggregator();
	
	/**
	 * Constructor
	 * @param trace Execution trace for target program
	 */
	public SPP(Trace trace) {
		this.trace = trace;
	}
	
	/**
	 * Constructor
	 * @param trace Execution trace for target program
	 * @param inputs Input variables which assumed to be correct
	 * @param outputs Output variables which assumed to be wrong
	 */
	public SPP(Trace trace, List<VarValue> inputs, List<VarValue> outputs) {
		this.trace = trace;
		this.correctVars.addAll(inputs);
		this.wrongVars.addAll(outputs);
		this.slicedTrace = TraceUtil.dyanmicSlice(trace, this.wrongVars);
	}
	
	public void addCorrectVar(VarValue correctVar) {
		this.correctVars.add(correctVar);
	}
	
	public void addWrongVar(VarValue wrongVar) {
		this.wrongVars.add(wrongVar);
	}
	/**
	 * Add input variables
	 * @param inputs Input variables
	 */
	public void addCorrectVars(Collection<VarValue> inputs) {
		this.correctVars.addAll(inputs);
	}
	
	/**
	 * Add output variables
	 * @param outputs Output variables
	 */
	public void addWrongVars(Collection<VarValue> outputs) {
		this.wrongVars.addAll(outputs);
	}
	
	public void propagate() {
		ProbPropagator propagator = new ProbPropagator(this.trace, this.slicedTrace, this.correctVars, this.wrongVars, this.feedbackRecords);
		propagator.propagate();
	}
	
	public ActionPath suggestPath(final TraceNode startNode, final TraceNode endNode) {
		if (startNode.getOrder() < endNode.getOrder()) {
			throw new IllegalArgumentException("EndNode: " + endNode.getOrder() + " is in the downstream of startNode: " + startNode.getOrder());
		}
		PathFinder finder = new PathFinder(this.trace);
		ActionPath path = finder.findPath_dijstra(startNode, endNode);
		return path;
	}
	
	public ActionPath suggestPath(final TraceNode startNode, final TraceNode endNode, final ActionPath mustFollowPath) {
		if (startNode.getOrder() < endNode.getOrder()) {
			throw new IllegalArgumentException("EndNode: " + endNode.getOrder() + " is in the downstream of startNode: " + startNode.getOrder());
		}
		
		// If there are no user path provided, the find path from the error node
		if (mustFollowPath == null) {
			return this.suggestPath(startNode, endNode);
		}
		if (mustFollowPath.isEmpty()) {
			return this.suggestPath(startNode, endNode);
		}
		
		// If must follow path is provided,
		// then find path starting from last node of the user path
		NodeFeedbacksPair latestAction = mustFollowPath.peek();
		TraceNode latestNode = TraceUtil.findNextNode(latestAction.getNode(), latestAction.getFirstFeedback(), trace);
		if (latestNode == null) {
			return null;
		}
		if (endNode.getOrder() > latestNode.getOrder()) {
			return null;
		}
		
		ActionPath consecutive_path = this.suggestPath(latestNode, endNode);
		if (!consecutive_path.canReachRootCause()) {
			PathFinder finder = new PathFinder(this.trace);
			consecutive_path = finder.findPathway_greedy(latestNode, endNode);
		}
		
		ActionPath path = ActionPath.concat(mustFollowPath, consecutive_path);
		if (!ActionPath.isConnectedPath(path, trace)) {
			throw new RuntimeException("Path is not connected");
		}
		return path;
	}
	
	/**
	 * Propose the root cause node. <br><br>
	 * 
	 * It will compare the drop of correctness
	 * probability from read variables to the
	 * written variables. <br><br>
	 * 
	 * The one with the maximum drop will be the
	 * root cause. <br><br>
	 * 
	 * @return Root cause node
	 */
	public TraceNode proposeRootCause() {
		TraceNode rootCause = null;
		double maxDrop = 0.0;
		for (TraceNode node : this.slicedTrace) {
			
			if (this.isFeedbackGiven(node)) {
				continue;
			}
			
			/*
			 * We need to handle:
			 * 1. Node without any variable
			 * 2. Node with only control dominator
			 * 3. Node with only read variables
			 * 4. Node with only written variables
			 * 5. Node with both read and written variables
			 * 
			 * It will ignore the node that already have feedback
			 */
			double drop = 0.0;
			if (node.getWrittenVariables().isEmpty() && node.getReadVariables().isEmpty() && node.getControlDominator() == null) {
				// Case 1
				continue;
			} else if (node.getWrittenVariables().isEmpty() && node.getReadVariables().isEmpty() && node.getControlDominator() != null) {
				// Case 2
				drop = PropProbability.UNCERTAIN - node.getControlDominator().getConditionResult().getProbability();
			} else if (node.getWrittenVariables().isEmpty()) {
				// Case 3
				double prob = this.aggregator.aggregateProb(node.getReadVariables(), ProbAggregateMethods.AVG);
				drop = PropProbability.UNCERTAIN - prob;
			} else if (node.getReadVariables().isEmpty()) {
				// Case 4
				double prob = this.aggregator.aggregateProb(node.getWrittenVariables(), ProbAggregateMethods.AVG);
				drop = PropProbability.UNCERTAIN - prob;
			} else {
				double readProb = this.aggregator.aggregateForwardProb(node.getReadVariables(), ProbAggregateMethods.MIN);
				double writtenProb = this.aggregator.aggregateProb(node.getWrittenVariables(), ProbAggregateMethods.MIN);
				drop = readProb - writtenProb;
			}
			
			if (drop < 0) {
				// Case that the read variable is wrong but the written variable is correct
				// Ignore it by now
				
				System.out.println("Warning: Trace node " + node.getOrder() + " has negative drop");
				continue;
			} else {
				if (drop >= maxDrop) {
					maxDrop = drop;
					rootCause = node;
				}
			}
		}
		return rootCause;
	}
	
	/**
	 * Set probability based on the user feedbacks
	 * @param nodeFeedbackPairs List of user feedbacks
	 */
	public void responseToFeedbacks(Collection<NodeFeedbacksPair> nodeFeedbackPairs) {
		for (NodeFeedbacksPair pair : nodeFeedbackPairs) {
			this.responseToFeedback(pair);
		}
	}
	
	/**
	 * Set probability based on the user feedback
	 * @param nodeFeedbacksPair User feedback 
	 */
	public void responseToFeedback(final NodeFeedbacksPair nodeFeedbacksPair) {
		TraceNode node = nodeFeedbacksPair.getNode();
//		UserFeedback feedback = nodeFeedbacksPair.getFeedback();
		if (nodeFeedbacksPair.getFeedbackType().equals(UserFeedback.CORRECT)) {
			// If the feedback is CORRECT, then set every variable and control dom to be correct
			this.addCorrectVars(node.getReadVariables());
			this.addCorrectVars(node.getWrittenVariables());
			TraceNode controlDominator = node.getControlDominator();
			if (controlDominator != null) {
				VarValue controlDom = controlDominator.getConditionResult();
				this.correctVars.add(controlDom);
			}
		} else if (nodeFeedbacksPair.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
			// If the feedback is WRONG_PATH, set control dominator varvalue to wrong
			TraceNode controlDominator = node.getControlDominator();
			VarValue controlDom = controlDominator.getConditionResult();
			this.wrongVars.add(controlDom);
		} else if (nodeFeedbacksPair.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
			// If the feedback is WRONG_VARIABLE_VALUE, set selected to be wrong
			// and set control dominator to be correct
			List<VarValue> wrongReadVars = new ArrayList<>();
			for (UserFeedback feedback : nodeFeedbacksPair.getFeedbacks()) {
				wrongReadVars.add(feedback.getOption().getReadVar());
			}
			for (VarValue readVar : node.getReadVariables()) {
				if (wrongReadVars.contains(readVar)) {
					this.addWrongVar(readVar);
				} else {
//					this.addCorrectVar(readVar);
				}
			}
			this.addWrongVars(node.getWrittenVariables());
			TraceNode controlDom = node.getControlDominator();
			if (controlDom != null) {
				this.correctVars.add(controlDom.getConditionResult());
			}
		}
		this.recordFeedback(nodeFeedbacksPair);
	}
	
	public UserFeedback giveFeedback(final TraceNode node) {
		PathFinder finder = new PathFinder(this.trace);
		return finder.giveFeedback(node);
	}
	
	private void recordFeedback(final NodeFeedbacksPair pair) {
		this.feedbackRecords.add(pair);
	}
	
	private boolean isFeedbackGiven(final TraceNode node) {
		for (NodeFeedbacksPair pair : this.feedbackRecords) {
			if (node.equals(pair.getNode())) {
				return true;
			}
		}
		return false;
	}
}
