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
				return path;
			}
		}
		path.addPair(currentNode, feedback);
		return path;
	}
	
	protected UserFeedback giveRandomFeedback(final TraceNode node) {
		List<UserFeedback> feedbacks = this.getAllPossibleFeedbacks(node);
        Random random = new Random();

        // Generate a random index within the range of valid indices
        int randomIndex = random.nextInt(feedbacks.size());

        // Retrieve the random element from the list
        return feedbacks.get(randomIndex);
		
		// Root Cause
//		if (this.genProb() < RandomPathFinder.ROOT_CAUSE_PROB) {
//			return new UserFeedback(UserFeedback.ROOTCAUSE);
//		}
//		
//		final List<VarValue> readVars = this.getFilteredReadVarList(node);
//		final TraceNode controlDom = node.getControlDominator();
//		if (controlDom == null && readVars.isEmpty()) {
//			return new UserFeedback(UserFeedback.ROOTCAUSE);
//		} else if (controlDom != null && readVars.isEmpty()) {
//			return new UserFeedback(UserFeedback.WRONG_PATH);
//		} else if (controlDom == null && !readVars.isEmpty()) {
//			return this.giveRandomWrongVarFeedback(node);
//		} else {
//			return this.genProb() < RandomPathFinder.CONTROL_PROB ? new UserFeedback(UserFeedback.WRONG_PATH) : this.giveRandomWrongVarFeedback(node);
//		}
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
	protected double genProb() {
		return Math.random();
	}
	
	protected UserFeedback giveRandomWrongVarFeedback(final TraceNode node) {
		final List<VarValue> readVars = this.getFilteredReadVarList(node);
		VarValue wrongVar;
		if (readVars.size() == 1) {
			wrongVar = readVars.get(0);
		} else {
			int maxIdx = -1;
			double maxProb = -1.0d;
			for (int idx=0; idx<readVars.size(); idx++) {
				final double prob = this.genProb();
				if (prob > maxProb) {
					maxIdx = idx;
					maxProb = prob;
				}
			}
			wrongVar = readVars.get(maxIdx);
		}
		final ChosenVariableOption option = new ChosenVariableOption(wrongVar, null);
		return new UserFeedback(option, UserFeedback.WRONG_VARIABLE_VALUE);
	}
	
	protected List<VarValue> getFilteredReadVarList(final TraceNode node) {
//		return node.getReadVariables().stream().filter(var -> 
//			(!var.isThisVariable()) && this.trace.findDataDependency(node, var) != null
//		).toList();
		return node.getReadVariables().stream().filter(var -> this.trace.findDataDependency(node, var) != null).toList();
	}
	

}
