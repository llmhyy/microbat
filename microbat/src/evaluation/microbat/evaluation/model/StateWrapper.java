package microbat.evaluation.model;

import java.util.ArrayList;

import microbat.handler.CheckingState;
import microbat.recommendation.ChosenVariableOption;

public class StateWrapper {
	private CheckingState state;
	private ChosenVariableOption variableOption;
	private ArrayList<StepOperationTuple> jumpingSteps;

	public StateWrapper(CheckingState state, ChosenVariableOption option, ArrayList<StepOperationTuple> jumpingSteps) {
		super();
		this.state = state;
		this.variableOption = option;
		this.jumpingSteps = jumpingSteps;
	}

	public CheckingState getState() {
		return state;
	}

	public void setState(CheckingState state) {
		this.state = state;
	}

	public ArrayList<StepOperationTuple> getJumpingSteps() {
		return jumpingSteps;
	}

	public void setJumpingSteps(ArrayList<StepOperationTuple> jumpingSteps) {
		this.jumpingSteps = jumpingSteps;
	}

	public ChosenVariableOption getVariableOption() {
		return variableOption;
	}

	public void setVariableOption(ChosenVariableOption variableOption) {
		this.variableOption = variableOption;
	}

	

}
