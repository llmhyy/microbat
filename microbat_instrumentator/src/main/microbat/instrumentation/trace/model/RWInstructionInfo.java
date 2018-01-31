package microbat.instrumentation.trace.model;

import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LineNumberGen;

import sav.common.core.utils.SignatureUtils;

public class RWInstructionInfo {
	private InstructionHandle instructionHandler;
	private int line;
	private String varName;
	private String varType;
	private boolean isStore; // if not write, this is a readInstruction. 
	
	public RWInstructionInfo(InstructionHandle insnHandler, LineNumberGen lineGen) {
		this.instructionHandler = insnHandler;
		this.line = lineGen.getSourceLine();
	}
	
	public int getLine() {
		return line;
	}

	public Instruction getInstruction() {
		return instructionHandler.getInstruction();
	}
	
	public InstructionHandle getInstructionHandler() {
		return instructionHandler;
	}
	
	public boolean isStoreInstruction() {
		return isStore;
	}
	
	public boolean isLoadInstruction() {
		return !isStore;
	}

	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public String getVarType() {
		return varType;
	}

	public void setVarType(String varType) {
		this.varType = signatureToName(varType);
	}

	protected String signatureToName(String sign) {
		return SignatureUtils.signatureToName(sign);
	}

	public boolean isStore() {
		return isStore;
	}

	public void setIsStore(boolean isStore) {
		this.isStore = isStore;
	}

	public void setLine(int line) {
		this.line = line;
	}
}
