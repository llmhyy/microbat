package microbat.debugpilot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import debuginfo.NodeFeedbacksPair;
import microbat.debugpilot.pathfinding.FeedbackPath;
import microbat.debugpilot.pathfinding.FeedbackPathUtil;
import microbat.debugpilot.pathfinding.PathFinder;
import microbat.debugpilot.pathfinding.PathFinderFactory;
import microbat.debugpilot.pathfinding.PathFinderType;
import microbat.debugpilot.propagation.ProbabilityPropagator;
import microbat.debugpilot.propagation.PropagatorFactory;
import microbat.debugpilot.propagation.PropagatorType;
import microbat.debugpilot.propagation.probability.PropProbability;
import microbat.debugpilot.propagation.spp.StepExplaination;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;

/**
 * StepwisePropagator is used to propagate
 * the probability of correctness
 * 
 * @author WYK
 *
 */
public class DebugPilot {
	
	protected final Trace trace;
	protected Set<VarValue> correctVars = new HashSet<>();
	protected Set<VarValue> wrongVars = new HashSet<>();
	protected final TraceNode outputNode;
	protected final PropagatorType propagatorType;
	protected final PathFinderType pathFinderType;
	
	protected List<TraceNode> slicedTrace = null;
	protected Collection<NodeFeedbacksPair> feedbackRecords = new ArrayList<>();
	protected TraceNode rootCause = null;
	protected FeedbackPath path = null;

	public DebugPilot(Trace trace, List<VarValue> inputs, List<VarValue> outputs, TraceNode outputNode, PropagatorType propagatorType, PathFinderType pathFinderType) {
		Objects.requireNonNull(trace, Log.genMsg(getClass(), "Given trace is null"));
		Objects.requireNonNull(inputs, Log.genMsg(getClass(), "Given inputs is null"));
		Objects.requireNonNull(outputs, Log.genMsg(getClass(), "Given outputs is null"));
		Objects.requireNonNull(outputNode, Log.genMsg(getClass(), "Given outputNode is null"));
		Objects.requireNonNull(propagatorType, Log.genMsg(getClass(), "Given propagatorType is null"));
		this.trace = trace;
		this.correctVars.addAll(inputs);
		this.wrongVars.addAll(outputs);
		this.slicedTrace = TraceUtil.dynamicSlic(trace, outputNode);
		this.outputNode = outputNode;
		this.propagatorType = propagatorType;
		this.pathFinderType = pathFinderType;
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
		ProbabilityPropagator propagator = PropagatorFactory.getPropagator(this.propagatorType, this.trace, this.slicedTrace, this.correctVars, this.wrongVars, this.feedbackRecords);
		propagator.propagate();
	}
	
	public UserFeedback giveFeedback(final TraceNode node) {
		for (NodeFeedbacksPair action : this.path) {
			if (action.getNode().equals(node)) {
				return action.getFirstFeedback();
			}
		}
		throw new NodeNotInPathException(Log.genMsg(getClass(), "This node " + node.getOrder() + " is not contained in path"));
	}
	
	public void locateRootCause() {
		this.rootCause = this.proposeRootCause();
	}
	
	public void locateRootCause(final TraceNode currentNode) {
		this.rootCause = this.proposeRootCause(currentNode);
		if (this.rootCause == null) {
			Log.printMsg(this.getClass(), "Cannot locate root cause");
		} else {
			Log.printMsg(this.getClass(), "Proposed root cause: " + this.rootCause.getOrder());
		}
	}
	
	public TraceNode getRootCause() {
		return this.rootCause;
	}
	
	public void constructPath() {
		PathFinder pathFinder = PathFinderFactory.getFinder(this.pathFinderType, this.trace, this.slicedTrace);
		
		FeedbackPath mustFollowPath = new FeedbackPath(this.feedbackRecords);
		for (NodeFeedbacksPair pair : mustFollowPath) {
			pair.getNode().reason = StepExplaination.USRE_CONFIRMED;
		}
		
		if (mustFollowPath == null || mustFollowPath.isEmpty()) {
			this.path = pathFinder.findPath(this.outputNode, this.rootCause);
			return;
		} else {
			NodeFeedbacksPair latestAction = mustFollowPath.peek();
			for (UserFeedback feedback : latestAction.getFeedbacks()) {
				final TraceNode nextNode = TraceUtil.findNextNode(latestAction.getNode(), feedback, this.trace);
				FeedbackPath consecutivePath = pathFinder.findPath(nextNode, this.rootCause);
				if (consecutivePath == null) continue;
				this.path = FeedbackPathUtil.concat(mustFollowPath, consecutivePath, this.trace);
				for (NodeFeedbacksPair pair : consecutivePath) {
					pair.getNode().updateReason(pair);
				}
				return;
			}
		}
		throw new RuntimeException(Log.genMsg(getClass(), "Suggest path out of loop"));
	}
	
	public FeedbackPath getPath() {
		return this.path;
	}
	
	
	public TraceNode proposeRootCause() {
		TraceNode rootCause = null;
		double maxDrop = -1.0;
		for (TraceNode node : this.slicedTrace) {
			
			if (this.isFeedbackGiven(node) || this.outputNode.equals(node)) {
				continue;
			}
			
//			List<VarValue> readVars = node.getReadVariables().stream().filter(var -> !var.isThisVariable()).collect(Collectors.toList());
//			List<VarValue> writtenVars = node.getWrittenVariables().stream().filter(var -> !var .isThisVariable()).collect(Collectors.toList());
			List<VarValue> readVars = node.getReadVariables();
			List<VarValue> writtenVars = node.getWrittenVariables();
			/*
			 * We need to handle:
			 * 1. Node without any variable
			 * 2. Node with only control dominator
			 * 3. Node with only read variables
			 * 4. Node with only written variables
			 * 5. Node with written variable and control dominator
			 * 6. Node with both read and written variables
			 * 
			 * It will ignore the node that already have feedback
			 */
			double drop = 0.0;
			if (writtenVars.isEmpty() && readVars.isEmpty() && node.getControlDominator() == null) {
				// Case 1
				continue;
			} else if (writtenVars.isEmpty() && readVars.isEmpty() && node.getControlDominator() != null) {
				// Case 2
				drop = PropProbability.UNCERTAIN - node.getControlDominator().getConditionResult().getProbability();
			} else if (writtenVars.isEmpty()) {
				// Case 3
				double prob = readVars.stream().mapToDouble(var -> var.getProbability()).average().orElse(0.5);
				drop = PropProbability.UNCERTAIN - prob;
			} else if (readVars.isEmpty()) {
				// Case 4
				double prob = writtenVars.stream().mapToDouble(var -> var.getProbability()).average().orElse(0.5);
				drop = PropProbability.UNCERTAIN - prob;
			} else if (!writtenVars.isEmpty() && readVars.isEmpty() && node.getControlDominator() != null){
				// Case 5
				drop = PropProbability.UNCERTAIN - node.getControlDominator().getConditionResult().getProbability();
			} else {
				// Case 6
				double readProb = readVars.stream().mapToDouble(var -> var.getProbability()).min().orElse(0.5);
				double writtenProb = writtenVars.stream().mapToDouble(var -> var.getProbability()).min().orElse(0.5);
				drop = readProb - writtenProb;
			}
			
			node.setDrop(drop);
			if (drop < 0) {
				continue;
			} else {
				if (drop > maxDrop) {
					maxDrop = drop;
					rootCause = node;
				}
			}
		}
		
		if (rootCause == null) {
			rootCause = this.slicedTrace.get(0);
		}
		if (this.pathFinderType == PathFinderType.Random) {
			rootCause.reason = StepExplaination.RANDOM;
		} else {
			rootCause.reason = StepExplaination.LAREST_GAP;
		}
		return rootCause;
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
	public TraceNode proposeRootCause(final TraceNode currentNode) {
		TraceNode rootCause = null;
		double maxDrop = -1.0;
		for (TraceNode node : this.slicedTrace) {
			
			if (this.isFeedbackGiven(node) || this.outputNode.equals(node) || node.getOrder() > currentNode.getOrder()) {
				continue;
			}
			
//			List<VarValue> readVars = node.getReadVariables().stream().filter(var -> !var.isThisVariable()).collect(Collectors.toList());
//			List<VarValue> writtenVars = node.getWrittenVariables().stream().filter(var -> !var .isThisVariable()).collect(Collectors.toList());
			List<VarValue> readVars = node.getReadVariables();
			List<VarValue> writtenVars = node.getWrittenVariables();
			/*
			 * We need to handle:
			 * 1. Node without any variable
			 * 2. Node with only control dominator
			 * 3. Node with only read variables
			 * 4. Node with only written variables
			 * 5. Node with written variable and control dominator
			 * 6. Node with both read and written variables
			 * 
			 * It will ignore the node that already have feedback
			 */
			double drop = 0.0;
			if (writtenVars.isEmpty() && readVars.isEmpty() && node.getControlDominator() == null) {
				// Case 1
				continue;
			} else if (writtenVars.isEmpty() && readVars.isEmpty() && node.getControlDominator() != null) {
				// Case 2
				drop = PropProbability.UNCERTAIN - node.getControlDominator().getConditionResult().getProbability();
			} else if (writtenVars.isEmpty()) {
				// Case 3
				double prob = readVars.stream().mapToDouble(var -> var.getProbability()).average().orElse(0.5);
				drop = PropProbability.UNCERTAIN - prob;
			} else if (readVars.isEmpty()) {
				// Case 4
				double prob = writtenVars.stream().mapToDouble(var -> var.getProbability()).average().orElse(0.5);
				drop = PropProbability.UNCERTAIN - prob;
			} else if (!writtenVars.isEmpty() && readVars.isEmpty() && node.getControlDominator() != null){
				// Case 5
				drop = PropProbability.UNCERTAIN - node.getControlDominator().getConditionResult().getProbability();
			} else {
				// Case 6
				double readProb = readVars.stream().mapToDouble(var -> var.getProbability()).min().orElse(0.5);
				double writtenProb = writtenVars.stream().mapToDouble(var -> var.getProbability()).min().orElse(0.5);
				drop = readProb - writtenProb;
			}
			
			node.setDrop(drop);
			if (drop < 0) {
				continue;
			} else {
				if (drop > maxDrop) {
					maxDrop = drop;
					rootCause = node;
				}
			}
		}
		
		if (rootCause == null) {
			rootCause = this.slicedTrace.get(0);
		}
		rootCause.reason = "Largest gap";
		return rootCause;
	}
	
	public void updateFeedbacks(Collection<NodeFeedbacksPair> feedbacks) {
		this.feedbackRecords.clear();
		this.feedbackRecords.addAll(feedbacks);
	}
	
	private boolean isFeedbackGiven(final TraceNode node) {
		for (NodeFeedbacksPair pair : this.feedbackRecords) {
			if (node.equals(pair.getNode())) {
				return true;
			}
		}
		return false;
	}
	
	public void multiSlicing() {
		Set<TraceNode> relatedNodes = new HashSet<>();
		relatedNodes.addAll(TraceUtil.dynamicSlic(this.trace, this.outputNode));
		for (NodeFeedbacksPair pair : this.feedbackRecords) {
			final TraceNode node = pair.getNode();
			relatedNodes.retainAll(TraceUtil.dynamicSlic(this.trace, node));
			if (pair.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
				relatedNodes.retainAll(TraceUtil.dynamicSlic(trace, node.getControlDominator()));
			}
		}
		List<TraceNode> newSlicedNodes = new ArrayList<>();
		newSlicedNodes.addAll(relatedNodes);
		newSlicedNodes.sort(new Comparator<TraceNode>() {
			@Override
			public int compare(TraceNode t1, TraceNode t2) {
				return t1.getOrder() - t2.getOrder();
			}
		});
		this.slicedTrace = newSlicedNodes;
	}
}
