package microbat.evaluation.model;

import java.util.ArrayList;
import java.util.List;

import microbat.handler.CheckingState;

public class StateWrapper {
	private CheckingState state;
	private List<String> choosingVarID;
	private ArrayList<StepOperationTuple> jumpingSteps;

	public StateWrapper(CheckingState state, List<String> choosingVarID, ArrayList<StepOperationTuple> jumpingSteps) {
		super();
		this.state = state;
		this.choosingVarID = choosingVarID;
		this.jumpingSteps = jumpingSteps;
	}

	public CheckingState getState() {
		return state;
	}

	public void setState(CheckingState state) {
		this.state = state;
	}

	public List<String> getChoosingVarID() {
		return choosingVarID;
	}

	public void setChoosingVarID(List<String> choosingVarID) {
		this.choosingVarID = choosingVarID;
	}

	public ArrayList<StepOperationTuple> getJumpingSteps() {
		return jumpingSteps;
	}

	public void setJumpingSteps(ArrayList<StepOperationTuple> jumpingSteps) {
		this.jumpingSteps = jumpingSteps;
	}

	

}
