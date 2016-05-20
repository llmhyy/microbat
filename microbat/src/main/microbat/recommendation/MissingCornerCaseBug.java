package microbat.recommendation;

import java.util.List;

import microbat.model.trace.PotentialCorrectPattern;
import microbat.model.value.VarValue;

public class MissingCornerCaseBug extends Bug {

	private List<VarValue> readVariables;
	
	public MissingCornerCaseBug(List<VarValue> readVariables,
			PotentialCorrectPattern pattern) {
		this.readVariables = readVariables;
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("This bug may be caused by missing a corner case when:\n");
		for(VarValue value: readVariables){
			String str = "variable " + value.getVarName() + " = " + value.getStringValue();
			buffer.append(str);
			buffer.append("\n");
		}
		
		String message = buffer.toString();
		setMessage(message);
	}

	public List<VarValue> getReadVariables() {
		return readVariables;
	}


	public void setReadVariables(List<VarValue> readVariables) {
		this.readVariables = readVariables;
	}

	
	
	

}
