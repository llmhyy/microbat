package microbat.instrumentation.trace.model;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LineNumberGen;

public class LocalVarInstructionInfo extends RWInstructionInfo {

	public LocalVarInstructionInfo(InstructionHandle insnHandler, LineNumberGen lineGen, String varName,
			String varType) {
		super(insnHandler, lineGen);
	}
	
	public String getVarName() {
		return varName;
	}

	public String getVarType() {
		return varType;
	}
}
