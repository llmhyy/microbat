package microbat.debugpilot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
import microbat.debugpilot.settings.DebugPilotSettings;
import microbat.debugpilot.settings.PropagatorSettings;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;

/**
 * StepwisePropagator is used to propagate the probability of correctness
 * 
 * @author WYK
 *
 */
public class DebugPilot {

	DebugPilotSettings debugPilotSettings;

	public DebugPilot(final DebugPilotSettings settings) {
		Objects.requireNonNull(settings, Log.genMsg(getClass(), "Settings should not be null"));
		this.debugPilotSettings = settings;
		List<TraceNode> slicedTrace = TraceUtil.dynamicSlic(settings.getTrace(), settings.getOutputNode());
		this.debugPilotSettings.setSlicedTrace(slicedTrace);
	}

	public FeedbackPath constructPath(final TraceNode rootCause) {
		PathFinder pathFinder = PathFinderFactory.getFinder(this.debugPilotSettings.getPathFinderSettings());

		FeedbackPath mustFollowPath = new FeedbackPath(this.debugPilotSettings.getFeedbacks());
		for (NodeFeedbacksPair pair : mustFollowPath) {
			pair.getNode().reason = StepExplaination.USRE_CONFIRMED;
		}

		if (mustFollowPath == null || mustFollowPath.isEmpty()) {
			return pathFinder.findPath(this.debugPilotSettings.getOutputNode(), rootCause);
		} else {
			NodeFeedbacksPair latestAction = mustFollowPath.getLastFeedback();
			for (UserFeedback feedback : latestAction.getFeedbacks()) {
				final TraceNode nextNode = TraceUtil.findNextNode(latestAction.getNode(), feedback,
						this.debugPilotSettings.getTrace());
				FeedbackPath consecutivePath = pathFinder.findPath(nextNode, rootCause);
				if (consecutivePath == null)
					continue;
				FeedbackPath path = FeedbackPathUtil.concat(mustFollowPath, consecutivePath);
				for (NodeFeedbacksPair pair : consecutivePath) {
					if (pair.getNode().equals(rootCause)) {
						pair.getNode().reason = StepExplaination.LAREST_GAP;
					} else {						
						pair.getNode().updateReason(pair);
					}
				}
				return path;
			}
		}
		throw new RuntimeException(Log.genMsg(getClass(), "Cannot construct path"));
	}

	public TraceNode locateRootCause() {
		RootCauseLocator locator = RootCauseLocatorFactory
				.getLocator(this.debugPilotSettings.getRootCauseLocatorSettings());
		TraceNode rootCause = locator.locateRootCause();
		rootCause.reason = StepExplaination.LAREST_GAP;
		return rootCause;
	}

	public void multiSlicing() {
		final Trace trace = this.debugPilotSettings.getTrace();
		final TraceNode outputNode = this.debugPilotSettings.getOutputNode();

		Set<TraceNode> relatedNodes = new HashSet<>();
		relatedNodes.addAll(TraceUtil.dynamicSlic(trace, outputNode));
		for (NodeFeedbacksPair pair : this.debugPilotSettings.getFeedbacks()) {
			final TraceNode node = TraceUtil.findNextNode(pair.getNode(), pair.getFirstFeedback(), trace);
			relatedNodes.retainAll(TraceUtil.dynamicSlic(trace, node));
		}
		List<TraceNode> newSlicedNodes = new ArrayList<>();
		newSlicedNodes.addAll(relatedNodes);
		newSlicedNodes.sort(new Comparator<TraceNode>() {
			@Override
			public int compare(TraceNode t1, TraceNode t2) {
				return t1.getOrder() - t2.getOrder();
			}
		});

		this.debugPilotSettings.setSlicedTrace(newSlicedNodes);
	}

	public void propagate() {
		ProbabilityPropagator propagator = PropagatorFactory
				.getPropagator(this.debugPilotSettings.getPropagatorSettings());
		propagator.propagate();
	}

	public void updateFeedbacks(Collection<NodeFeedbacksPair> feedbacks) {
		this.debugPilotSettings.setFeedbackRecords(feedbacks);
	}
}
