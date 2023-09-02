package microbat.debugpilot.fsc;

import java.util.Objects;

import microbat.debugpilot.DebugPilot;
import microbat.debugpilot.NodeFeedbacksPair;

public class DebugPilotFiniteStateMachine {
	
	protected final DebugPilot debugPilot;
	
	protected DebugPilotState currentState = null;
	
	protected boolean end = false;
	
	public DebugPilotFiniteStateMachine(final DebugPilot debugPilot) {
		this.debugPilot = debugPilot;
	}
	
	public DebugPilotFiniteStateMachine(final DebugPilot debugPilot, final DebugPilotState initState) {
		this(debugPilot);
		Objects.requireNonNull(initState);
		this.currentState = initState;
	}
	 
	public void setState(final DebugPilotState newState) {
		this.currentState = newState;
	}
	
	public void handleFeedback() {
		this.currentState.handleFeedback();
	}
	
	public boolean isEnd() {
		return this.end;
	}
	
	public void setEnd(boolean isEnd) {
		this.end = isEnd;
	}
	
	public DebugPilot getDebugPilot() {
		return this.debugPilot;
	}
}
