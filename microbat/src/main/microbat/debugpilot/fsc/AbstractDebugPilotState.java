package microbat.debugpilot.fsc;

import java.util.Stack;

import microbat.debugpilot.NodeFeedbacksPair;
import microbat.debugpilot.pathfinding.FeedbackPath;

public abstract class AbstractDebugPilotState implements DebugPilotState {

	protected final DebugPilotFiniteStateMachine stateMachine;
	
	public AbstractDebugPilotState(final DebugPilotFiniteStateMachine stateMachine) {
		this.stateMachine = stateMachine;
	}
	
	@Override
	public abstract void handleFeedback();
	
	protected Stack<NodeFeedbacksPair> constructFeedbackRecords(final NodeFeedbacksPair pair, final FeedbackPath path) {
		Stack<NodeFeedbacksPair> newFeedbackRecords = new Stack<>();
		for (NodeFeedbacksPair originalFeedbacksPair : path) {
			if (!originalFeedbacksPair.getNode().equals(pair.getNode())) {
				newFeedbackRecords.add(originalFeedbacksPair);
			} else {
				newFeedbackRecords.add(pair);
				break;
			}
		}
		return newFeedbackRecords;
	}

}
