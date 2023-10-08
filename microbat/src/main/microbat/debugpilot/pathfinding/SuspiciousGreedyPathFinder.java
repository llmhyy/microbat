package microbat.debugpilot.pathfinding;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import microbat.debugpilot.settings.PathFinderSettings;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.ChosenVariableOption;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;

public class SuspiciousGreedyPathFinder extends AbstractPathFinder {

	public SuspiciousGreedyPathFinder(final PathFinderSettings settings) {
		this(settings.getTrace(), settings.getSlicedTrace());
	}
	
	public SuspiciousGreedyPathFinder(Trace trace, List<TraceNode> slicedTrace) {
		super(trace, slicedTrace);
	}

	@Override
	public FeedbackPath findPath(TraceNode startNode, TraceNode endNode) {
		Objects.requireNonNull(startNode, Log.genMsg(getClass(), "start node should not be null"));
		Objects.requireNonNull(endNode, Log.genMsg(getClass(), "endNode should not be null"));
		FeedbackPath path = new FeedbackPath();
		TraceNode currentNode = startNode;
		while(currentNode != null) {
			if (currentNode.equals(endNode)) {
				UserFeedback feedback = new UserFeedback(UserFeedback.ROOTCAUSE);
				path.addPair(currentNode, feedback);
				return path;
			}
			if (currentNode.getOrder() <= endNode.getOrder()) {
				return null;
			}
			UserFeedback feedback = SuspiciousGreedyPathFinder.giveFeedback(currentNode);
			path.addPair(currentNode, feedback);
			currentNode = TraceUtil.findNextNode(currentNode, feedback, this.trace);
		}
		return null;
	}
	
	public static UserFeedback giveFeedback(final TraceNode node) {
		Objects.requireNonNull(node, Log.genMsg(GreedyPathFinder.class, "Given node is null"));
		UserFeedback feedback = new UserFeedback();
		
		final TraceNode controlDom = node.getControlDominator();
		
		if (controlDom == null && node.getReadVariables().isEmpty()) {
			feedback.setFeedbackType(UserFeedback.UNCLEAR);
			return feedback;
		} else if (controlDom != null && node.getReadVariables().isEmpty()) {
			feedback.setFeedbackType(UserFeedback.WRONG_PATH);
			return feedback;
		} else if (controlDom == null && !node.getReadVariables().isEmpty()) {
			Optional<VarValue> targetVarOptional = node.getReadVariables().stream()
					.min((s1, s2) -> Double.compare(1.0d/(s1.getSuspiciousness()+AbstractPathFinder.eps), 1.0d/s2.getSuspiciousness()));
			feedback.setFeedbackType(UserFeedback.WRONG_VARIABLE_VALUE);
			feedback.setOption(new ChosenVariableOption(targetVarOptional.get(), null));
			return feedback;
		} else {
			double controlScore = 1.0d / controlDom.getConditionResult().getSuspiciousness();
			Optional<VarValue> targetVarOptional = node.getReadVariables().stream()
					.min((s1, s2) -> Double.compare(1.0d/s1.getSuspiciousness(), 1.0d/s2.getSuspiciousness()));
			double dataScore = 1.0d / targetVarOptional.get().getSuspiciousness();
			
			if (controlScore <= dataScore) {
				feedback.setFeedbackType(UserFeedback.WRONG_PATH);
			} else {
				feedback.setFeedbackType(UserFeedback.WRONG_VARIABLE_VALUE);
				feedback.setOption(new ChosenVariableOption(targetVarOptional.get(), null));
			}
			return feedback;
		}
	}
}
