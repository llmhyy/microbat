package microbat.recommendation;

import java.util.ArrayList;
import java.util.List;
import microbat.model.value.VarValue;

public class ChosenVariablesOption extends ChosenVariableOption {
	private List<VarValue> readVars;
	private List<VarValue> writtenVars;
	
	public ChosenVariablesOption(List<VarValue> readVars, List<VarValue> writeVars) {
		super(null, null);
		this.readVars = readVars;
		this.writtenVars = writeVars;
		if (!readVars.isEmpty()) {
			this.readVar = this.readVars.get(0);
		}
		if (!writeVars.isEmpty()) {
			this.writtenVar = this.writtenVars.get(0);
		}
	}
	
	public void setReadVars(List<VarValue> readVars) {
		this.readVars = readVars;
	}
	
	public void setWrittenVars(List<VarValue> writtenVars) {
		this.writtenVars = writtenVars;
	}
	
	public List<VarValue> getReadVars() {
		return this.readVars;
	}
	
	public List<VarValue> getWrittenVars() {
		return this.writtenVars;
	}
	
	@Override
	public List<String> getIncludedWrongVarID() {
		List<String> varIDs = new ArrayList<>();
		for (VarValue readVar : this.readVars) {
			varIDs.add(readVar.getVarID());
		}
		for (VarValue writtenVar : this.writtenVars) {
			varIDs.add(writtenVar.getVarID());
		}
		return varIDs; 
	}
	
	@Override
	public List<VarValue> getIncludedWrongVars() {
		List<VarValue> vars = new ArrayList<>();
		vars.addAll(this.readVars);
		vars.addAll(this.writtenVars);
		return vars;
	}
	
	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("[readVar=[");
		for (VarValue readVar : this.readVars) {
			strBuilder.append(readVar);
			strBuilder.append(",");
		}
		strBuilder.append("], writtenVar=[");
		for (VarValue writtenVar : this.writtenVars) {
			strBuilder.append(writtenVar);
			strBuilder.append(",");
		}
		strBuilder.append("]");
		return strBuilder.toString();
	}
}
