package microbat.debugpilot.fsc;

public abstract class AbstractDebugPilotState implements DebugPilotState {

	protected final DebugPilotFiniteStateMachine stateMachine;
	
	public AbstractDebugPilotState(final DebugPilotFiniteStateMachine stateMachine) {
		this.stateMachine = stateMachine;
	}
	
	@Override
	public abstract void handleFeedback();

}
