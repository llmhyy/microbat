package microbat.debugpilot.pathfinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import microbat.debugpilot.settings.PathFinderSettings;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;
import microbat.recommendation.ChosenVariableOption;

public class RandomPathFinder extends AbstractPathFinder {

	protected static final double ROOT_CAUSE_PROB = 0.1;
	protected static final double CONTROL_PROB = 0.3;
	
	public RandomPathFinder(final PathFinderSettings settings) {
		this(settings.getTrace(), settings.getSlicedTrace());
	}
	
	public RandomPathFinder(Trace trace, List<TraceNode> slicedTrace) {
		super(trace, slicedTrace);
	}

	@Override
	public FeedbackPath findPath(TraceNode startNode, TraceNode endNode) {
		Objects.requireNonNull(startNode, Log.genMsg(getClass(), "start node should not be null"));
		FeedbackPath path = new FeedbackPath();
		TraceNode currentNode = startNode;
		UserFeedback feedback;
		while (!(feedback = this.giveRandomFeedback(currentNode)).getFeedbackType().equals(UserFeedback.ROOTCAUSE)) {
			path.addPair(currentNode, feedback);
			currentNode = TraceUtil.findNextNode(currentNode, feedback, this.trace);
			if (currentNode == null) {
				// If there are not next node, then set the previous as root cause
				path.setLastAction(new UserFeedback(UserFeedback.ROOTCAUSE));
				return path;
			}
		}
		path.addPair(currentNode, feedback);
		return path;
	}
	
	protected UserFeedback giveRandomFeedback(final TraceNode node) {
		List<UserFeedback> feedbacks = this.getAllPossibleFeedbacks(node);
        Random random = new Random();
        int randomIndex = random.nextInt(feedbacks.size());
        return feedbacks.get(randomIndex);
	}
	
	protected List<UserFeedback> getAllPossibleFeedbacks(final TraceNode node) {
		List<UserFeedback> feedbacks = new ArrayList<>();
		feedbacks.add(new UserFeedback(UserFeedback.ROOTCAUSE));
		
		if (node.getControlDominator()!= null) {
			feedbacks.add(new UserFeedback(UserFeedback.WRONG_PATH));
		}
		
		for (VarValue readVar : node.getReadVariables()) {
			UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_VARIABLE_VALUE);
			feedback.setOption(new ChosenVariableOption(readVar, null));
			feedbacks.add(feedback);
		}
		
		return feedbacks;
		
		
	}
}
