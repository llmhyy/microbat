package microbat.recommendation;

import java.util.ArrayList;
import java.util.List;

import microbat.model.value.VarValue;

/**
 * This class represents an option when a simulated user provide wrong-variable-value feedback.
 * 
 * @author Yun Lin
 *
 */
public class ChosenVariableOption {
	protected VarValue readVar;
	protected VarValue writtenVar;

	public ChosenVariableOption(VarValue readVar, VarValue writtenVar) {
		super();
		this.readVar = readVar;
		this.writtenVar = writtenVar;
	}

	@Override
	public String toString() {
		return "[readVar=" + 
				(readVar == null ? readVar : readVar.getVarName()) + 
				", writtenVar=" + 
				(writtenVar == null ? writtenVar : writtenVar.getVarName()) + "]";
	}

	public List<String> getIncludedWrongVarID(){
		List<String> varIDs = new ArrayList<>();
		if(readVar != null){
			varIDs.add(readVar.getVarID());
		}
		if(writtenVar != null){
			varIDs.add(writtenVar.getVarID());
		}
		
		return varIDs;
	}
	
	public List<VarValue> getIncludedWrongVars(){
		List<VarValue> vars = new ArrayList<>();
		if(readVar != null){
			vars.add(readVar);
		}
		if(writtenVar != null){
			vars.add(writtenVar);
		}
		
		return vars;
	}

	public VarValue getReadVar() {
		return readVar;
	}

	public VarValue getWrittenVar() {
		return writtenVar;
	}

	public void setReadVar(VarValue readVar) {
		this.readVar = readVar;
	}

	public void setWrittenVar(VarValue writtenVar) {
		this.writtenVar = writtenVar;
	}

}
