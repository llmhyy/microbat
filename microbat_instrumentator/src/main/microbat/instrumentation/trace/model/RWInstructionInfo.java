package microbat.instrumentation.trace.model;

import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LineNumberGen;

public class RWInstructionInfo {
	private InstructionHandle instructionHandler;
	private int line;
	protected String varName;
	protected String varType;
	boolean isStore; // if not write, this is a readInstruction. 
	
	public RWInstructionInfo(InstructionHandle insnHandler, LineNumberGen lineGen) {
		this.instructionHandler = insnHandler;
		this.line = lineGen.getSourceLine();
	}
	
	public RWInstructionInfo(InstructionHandle insnHandler, LineNumberGen lineGen, String varName, String varType) {
		this(insnHandler, lineGen);
		this.varName = varName;
		this.varType = varType;
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
}
