package microbat.debugpilot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import microbat.debugpilot.pathfinding.FeedbackPath;
import microbat.debugpilot.pathfinding.PathFinder;
import microbat.debugpilot.pathfinding.PathFinderFactory;
import microbat.debugpilot.propagation.ProbabilityPropagator;
import microbat.debugpilot.propagation.PropagatorFactory;
import microbat.debugpilot.propagation.spp.StepExplaination;
import microbat.debugpilot.rootcausefinder.RootCauseLocator;
import microbat.debugpilot.rootcausefinder.RootCauseLocatorFactory;
import microbat.debugpilot.settings.DebugPilotSettings;
import microbat.debugpilot.userfeedback.DPUserFeedback;
import microbat.debugpilot.userfeedback.DPUserFeedbackType;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
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
		this.multiSlicing();
	}

	public FeedbackPath constructPath(final TraceNode rootCause) {
		PathFinder pathFinder = PathFinderFactory.getFinder(this.debugPilotSettings.getPathFinderSettings());

		FeedbackPath mustFollowPath = new FeedbackPath(this.debugPilotSettings.getFeedbacks());
		for (DPUserFeedback pair : mustFollowPath.getFeedbacks()) {
			pair.getNode().reason = StepExplaination.USRE_CONFIRMED;
			pair.getNode().confirmed = true;
		}

		if (mustFollowPath == null || mustFollowPath.isEmpty()) {
			return pathFinder.findPath(this.debugPilotSettings.getOutputNode(), rootCause);
		} else {
			FeedbackPath tempPath = null;
			Set<TraceNode> possibleNextNodes = TraceUtil.findAllNextNodes(mustFollowPath.getLastFeedback());
			for (TraceNode nextNode : possibleNextNodes) {
				FeedbackPath consecutivePath = pathFinder.findPath(nextNode, rootCause);
				if (consecutivePath == null) {
					tempPath = new FeedbackPath(mustFollowPath);
					tempPath.add(new DPUserFeedback(DPUserFeedbackType.ROOT_CAUSE, nextNode));
					continue;
				}
				consecutivePath.getFeedbacks().stream().forEach(feedback -> {
					if (feedback.getType() == DPUserFeedbackType.ROOT_CAUSE) {
						feedback.getNode().reason = StepExplaination.LAREST_GAP;
					} else {						
						feedback.getNode().updateReason(feedback);
					}
				});
				FeedbackPath path = FeedbackPath.concat(mustFollowPath, consecutivePath);
				return path;
			}
			return tempPath;
		}
//		FeedbackPath tempPath = null;
//		if (mustFollowPath == null || mustFollowPath.isEmpty()) {
//			return pathFinder.findPath(this.debugPilotSettings.getOutputNode(), rootCause);
//		} else {
//			DPUserFeedback latestAction = mustFollowPath.getLastFeedback();
//			for (UserFeedback feedback : latestAction.getFeedbacks()) {
//				final TraceNode nextNode = TraceUtil.findNextNode(latestAction.getNode(), feedback,
//						this.debugPilotSettings.getTrace());
//				FeedbackPath consecutivePath = pathFinder.findPath(nextNode, rootCause);
//				if (consecutivePath == null) {
//					tempPath = new FeedbackPath(mustFollowPath);
//					tempPath.addPair(new NodeFeedbacksPair(nextNode, new UserFeedback(UserFeedback.ROOTCAUSE)));
//					continue;
//				}
//				FeedbackPath path = FeedbackPathUtil.concat(mustFollowPath, consecutivePath);
//				for (NodeFeedbacksPair pair : consecutivePath) {
//					if (pair.getNode().equals(rootCause)) {
//						pair.getNode().reason = StepExplaination.LAREST_GAP;
//					} else {						
//						pair.getNode().updateReason(pair);
//					}
//				}
//				return path;
//			}
//		}
//		
//		return tempPath;
	}

	public TraceNode locateRootCause() {
		RootCauseLocator locator = RootCauseLocatorFactory
				.getLocator(this.debugPilotSettings.getRootCauseLocatorSettings());
		TraceNode rootCause = locator.locateRootCause();
//		rootCause.reason = StepExplaination.LAREST_GAP;
		return rootCause;
	}

	public void multiSlicing() {
		final Trace trace = this.debugPilotSettings.getTrace();
		final TraceNode outputNode = this.debugPilotSettings.getOutputNode();

		Set<TraceNode> relatedNodes = new HashSet<>();
		relatedNodes.addAll(TraceUtil.dynamicSlice(trace, outputNode));
		for (DPUserFeedback feedback : this.debugPilotSettings.getFeedbacks()) {
			Set<TraceNode> possibleRelatedNodes = new HashSet<>();
			for (TraceNode nextNode : TraceUtil.findAllNextNodes(feedback)) {
				possibleRelatedNodes.addAll(TraceUtil.dynamicSlice(trace, nextNode));
			}
			relatedNodes.retainAll(possibleRelatedNodes);
//			final TraceNode node = TraceUtil.findNextNode(pair.getNode(), pair.getFirstWrongFeedback(), trace);
//			if (node != null) {
//				relatedNodes.retainAll(TraceUtil.dynamicSlice(trace, node));				
//			}
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

	public void updateFeedbacks(Collection<DPUserFeedback> feedbacks) {
		this.debugPilotSettings.setFeedbackRecords(feedbacks);
	}
}
