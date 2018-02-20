package microbat.instrumentation.instr.instruction.info;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LocalVariableInstruction;

public class LocalVarInstructionInfo extends RWInstructionInfo {
	private int varScopeStartLine;
	private int varScopeEndLine;
	
	public LocalVarInstructionInfo(InstructionHandle insnHandler, int line, String varName,
			String varType) {
		super(insnHandler, line);
		setVarName(varName);
		setVarType(varType);
	}
	
	public int getVarScopeStartLine() {
		return varScopeStartLine;
	}

	public void setVarScopeStartLine(int varScopeStartLine) {
		this.varScopeStartLine = varScopeStartLine;
	}

	public int getVarScopeEndLine() {
		return varScopeEndLine;
	}

	public void setVarScopeEndLine(int varScopeEndLine) {
		this.varScopeEndLine = varScopeEndLine;
	}

	@Override
	public LocalVariableInstruction getInstruction() {
		return (LocalVariableInstruction) super.getInstruction();
	}
}
