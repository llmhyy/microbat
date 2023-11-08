package microbat.debugpilot.pathfinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import microbat.debugpilot.settings.PathFinderSettings;
import microbat.debugpilot.userfeedback.DPUserFeedback;
import microbat.debugpilot.userfeedback.DPUserFeedbackType;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
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
				DPUserFeedback feedback = new DPUserFeedback(DPUserFeedbackType.ROOT_CAUSE, currentNode);
				path.add(feedback);
				return path;
//				UserFeedback feedback = new UserFeedback(UserFeedback.ROOTCAUSE);
//				path.addPair(currentNode, feedback);
//				return path;
			}
			
			if (currentNode.getOrder() <= endNode.getOrder()) {
				return null;
			}
			
			DPUserFeedback greedyFeedback = this.giveFeedback(currentNode);
			List<TraceNode> nextNodes = new ArrayList<>(TraceUtil.findAllNextNodes(greedyFeedback));
			currentNode = nextNodes.get(0);
//			UserFeedback greedyFeedback = this.giveFeedback(currentNode);
//			currentNode = TraceUtil.findNextNode(currentNode, greedyFeedback, trace);
		}
		return null;
	}
	
	/**
	 * Give greedy feedback on give node
	 * @param node Target node
	 * @return Greedy feedback
	 */
	public DPUserFeedback giveFeedback(final TraceNode node) {
		Objects.requireNonNull(node, Log.genMsg(CorrectnessGreedyPathFinder.class, "Given node is null"));
//		UserFeedback feedback = new UserFeedback();
		final TraceNode controlDom = node.getControlDominator();
		
		if (controlDom == null && node.getReadVariables().isEmpty()) {
			DPUserFeedback feedback = new DPUserFeedback(DPUserFeedbackType.ROOT_CAUSE, node);
			return feedback;
		} else if (controlDom != null && node.getReadVariables().isEmpty()) {
//			feedback.setFeedbackType(UserFeedback.WRONG_PATH);
//			return feedback;
			return new DPUserFeedback(DPUserFeedbackType.WRONG_PATH, node);
		} else if (controlDom == null && !node.getReadVariables().isEmpty()) {
			Optional<VarValue> targetVarOptional = node.getReadVariables().stream()
					.min((var1, var2) -> Double.compare(this.getCost(var1), this.getCost(var2)));
			DPUserFeedback feedback = new DPUserFeedback(DPUserFeedbackType.WRONG_VARIABLE, node);
			feedback.addWrongVar(targetVarOptional.get());
			return feedback;
//			feedback.setFeedbackType(UserFeedback.WRONG_VARIABLE_VALUE);
//			feedback.setOption(new ChosenVariableOption(targetVarOptional.get(), null));
//			return feedback;
		} else {
			double controlCost = this.getCost(controlDom.getConditionResult());
			Optional<VarValue> targetVarOptional = node.getReadVariables().stream()
					.min((var1, var2) -> Double.compare(this.getCost(var1), this.getCost(var2)));
			double dataCost = this.getCost(targetVarOptional.get());
			
			DPUserFeedback feedback;
			if (controlCost <= dataCost) {
//				feedback.setFeedbackType(UserFeedback.WRONG_PATH);
				feedback = new DPUserFeedback(DPUserFeedbackType.WRONG_PATH, node);
			} else {
				feedback = new DPUserFeedback(DPUserFeedbackType.WRONG_VARIABLE, node);
				feedback.addWrongVar(targetVarOptional.get());
//				feedback.setFeedbackType(UserFeedback.WRONG_VARIABLE_VALUE);
//				feedback.setOption(new ChosenVariableOption(targetVarOptional.get(), null));
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
