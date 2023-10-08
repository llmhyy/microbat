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

public abstract class GreedyPathFinder extends AbstractPathFinder {

	public GreedyPathFinder(final PathFinderSettings settings) {
		this(settings.getTrace(), settings.getSlicedTrace());
	}
	
	public GreedyPathFinder(final Trace trace, final List<TraceNode> slicedTrace) {
		super(trace, slicedTrace);
	}
	
	// Do not allow public to construct finder without parameter
	protected GreedyPathFinder() {
		super(null, null);
	}
	
	@Override
	public FeedbackPath findPath(TraceNode startNode, TraceNode endNode) {
		Objects.requireNonNull(startNode, Log.genMsg(getClass(), "start node should not be null"));
		Objects.requireNonNull(endNode, Log.genMsg(getClass(), "endNode should not be null"));
		
		FeedbackPath path = new FeedbackPath();
		TraceNode currentNode = startNode;
		
		// Keep giving greedy feedback until endNode is reach, or it miss the endNode
		while (currentNode != null) {
			if (currentNode.equals(endNode)) {
				UserFeedback feedback = new UserFeedback(UserFeedback.ROOTCAUSE);
				path.addPair(currentNode, feedback);
				return path;
			}
			
			if (currentNode.getOrder() <= endNode.getOrder()) {
				return null;
			}
			
			UserFeedback greedyFeedback = this.giveFeedback(currentNode);
			currentNode = TraceUtil.findNextNode(currentNode, greedyFeedback, trace);
		}
		return null;
	}
	
	/**
	 * Give greedy feedback on give node
	 * @param node Target node
	 * @return Greedy feedback
	 */
	public UserFeedback giveFeedback(final TraceNode node) {
		Objects.requireNonNull(node, Log.genMsg(CorrectnessGreedyPathFinder.class, "Given node is null"));
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
					.min((var1, var2) -> Double.compare(this.getCost(var1), this.getCost(var2)));
			feedback.setFeedbackType(UserFeedback.WRONG_VARIABLE_VALUE);
			feedback.setOption(new ChosenVariableOption(targetVarOptional.get(), null));
			return feedback;
		} else {
			double controlCost = this.getCost(controlDom.getConditionResult());
			Optional<VarValue> targetVarOptional = node.getReadVariables().stream()
					.min((var1, var2) -> Double.compare(this.getCost(var1), this.getCost(var2)));
			double dataCost = this.getCost(targetVarOptional.get());
			
			if (controlCost <= dataCost) {
				feedback.setFeedbackType(UserFeedback.WRONG_PATH);
			} else {
				feedback.setFeedbackType(UserFeedback.WRONG_VARIABLE_VALUE);
				feedback.setOption(new ChosenVariableOption(targetVarOptional.get(), null));
			}
			return feedback;
		}
	}
	
	/**
	 * Get the cost of choosing the given varValue as next step. Greedy method will choose the lowest one
	 * @param varValue Target variable
	 * @return Cost for choosing the give variable
	 */
	protected abstract double getCost(final VarValue varValue);

}
