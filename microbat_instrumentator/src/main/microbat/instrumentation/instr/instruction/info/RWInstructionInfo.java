package microbat.instrumentation.instr.instruction.info;

import org.apache.bcel.generic.ACONST_NULL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

import sav.common.core.utils.SignatureUtils;

public class RWInstructionInfo {
	private InstructionHandle instructionHandler;
	private int line;
	private String varName;
	private String varType;
	private boolean isStore; // if not write, this is a readInstruction. 
	private int varStackSize;
	
	public RWInstructionInfo(InstructionHandle insnHandler, int line) {
		this.instructionHandler = insnHandler;
		this.line = line;
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

	public void setIsStore(boolean isStore) {
		this.isStore = isStore;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getVarStackSize() {
		return varStackSize;
	}

	public void setVarStackSize(int varStackSize) {
		this.varStackSize = varStackSize;
	}
	
	public boolean isComputationalType1() {
		return varStackSize == 1;
	}
	
	public boolean isComputationalType2() {
		return varStackSize == 2;
	}
	
	public boolean isNextToAconstNull() {
		InstructionHandle prev = getInstructionHandler().getPrev();
		return prev != null && prev.getInstruction() instanceof ACONST_NULL;
	}
}
