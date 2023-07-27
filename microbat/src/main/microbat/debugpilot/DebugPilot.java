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
import microbat.debugpilot.rootcausefinder.RootCauseLocator;
import microbat.debugpilot.rootcausefinder.RootCauseLocatorFactory;
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
	
	public TraceNode locateRootCause() {
		RootCauseLocator locator = RootCauseLocatorFactory.getLocator(this.propagatorType, this.slicedTrace, this.feedbackRecords, this.outputNode);
		return locator.locateRootCause();
	}
	
	public FeedbackPath constructPath(final TraceNode rootCause) {
		PathFinder pathFinder = PathFinderFactory.getFinder(this.pathFinderType, this.trace, this.slicedTrace);
		
		FeedbackPath mustFollowPath = new FeedbackPath(this.feedbackRecords);
		for (NodeFeedbacksPair pair : mustFollowPath) {
			pair.getNode().reason = StepExplaination.USRE_CONFIRMED;
		}
		
		if (mustFollowPath == null || mustFollowPath.isEmpty()) {
			return pathFinder.findPath(this.outputNode, rootCause);
		} else {
			NodeFeedbacksPair latestAction = mustFollowPath.getLastFeedback();
			for (UserFeedback feedback : latestAction.getFeedbacks()) {
				final TraceNode nextNode = TraceUtil.findNextNode(latestAction.getNode(), feedback, this.trace);
				FeedbackPath consecutivePath = pathFinder.findPath(nextNode, rootCause);
				if (consecutivePath == null) continue;
				FeedbackPath path = FeedbackPathUtil.concat(mustFollowPath, consecutivePath);
				for (NodeFeedbacksPair pair : consecutivePath) {
					pair.getNode().updateReason(pair);
				}
				return path;
			}
		}
		throw new RuntimeException(Log.genMsg(getClass(), "Cannot construct path"));
	}
	
	public void updateFeedbacks(Collection<NodeFeedbacksPair> feedbacks) {
		this.feedbackRecords.clear();
		this.feedbackRecords.addAll(feedbacks);
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
