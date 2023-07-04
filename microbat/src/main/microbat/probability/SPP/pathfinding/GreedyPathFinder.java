package microbat.probability.SPP.pathfinding;

import java.util.List;
import java.util.Objects;

import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.ChosenVariableOption;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;

public class GreedyPathFinder extends AbstractPathFinder {

	public GreedyPathFinder(Trace trace, List<TraceNode> slicedTrace) {
		super(trace, slicedTrace);
	}

	@Override
	public ActionPath findPath(TraceNode startNode, TraceNode endNode) {
		Objects.requireNonNull(startNode, Log.genMsg(getClass(), "start node should not be null"));
		Objects.requireNonNull(endNode, Log.genMsg(getClass(), "endNode should not be null"));
		ActionPath path = new ActionPath();
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
			UserFeedback feedback = this.giveFeedback(currentNode);
			path.addPair(currentNode, feedback);
			currentNode = TraceUtil.findNextNode(currentNode, feedback, this.trace);
		}
		return null;
	}
	
	protected UserFeedback giveFeedback(final TraceNode node) {
		UserFeedback feedback = new UserFeedback();
		
		TraceNode controlDom = node.getControlDominator();
		double controlProb = 2.0;
		if (controlDom != null) {
			controlProb = controlDom.getConditionResult().getProbability();
		}
		
		double minReadProb = 2.0;
		VarValue wrongVar = null;
		for (VarValue readVar : node.getReadVariables()) {
			// If the readVar is This variable, then ignore
			if (readVar.isThisVariable()) {
				continue;
			}
			double prob = readVar.getProbability();
			if (prob < minReadProb) {
				minReadProb = prob;
				wrongVar = readVar;
			}
		}
		
		// There are no controlDom and readVar
		if (controlProb == 2.0 && minReadProb == 2.0) {
			feedback.setFeedbackType(UserFeedback.UNCLEAR);
			return feedback;
		}
		if (controlProb <= minReadProb) {
			feedback.setFeedbackType(UserFeedback.WRONG_PATH);
		} else {
			feedback.setFeedbackType(UserFeedback.WRONG_VARIABLE_VALUE);
			feedback.setOption(new ChosenVariableOption(wrongVar, null));
		}
		return feedback;
	}
}
