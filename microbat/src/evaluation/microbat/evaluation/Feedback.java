package microbat.evaluation;

import microbat.model.value.VarValue;

public class Feedback {
	private boolean isAllVariableCorrect = false;
	private VarValue causingVariable;
	
	public Feedback(boolean isAllVariableCorrect, VarValue causingVariable) {
		super();
		this.isAllVariableCorrect = isAllVariableCorrect;
		this.causingVariable = causingVariable;
	}
	
	public boolean isAllVariableCorrect() {
		return isAllVariableCorrect;
	}
	public void setAllVariableCorrect(boolean isAllVariableCorrect) {
		this.isAllVariableCorrect = isAllVariableCorrect;
	}
	public VarValue getCausingVariable() {
		return causingVariable;
	}
	public void setCausingVariable(VarValue causingVariable) {
		this.causingVariable = causingVariable;
	}
}
