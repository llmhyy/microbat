package microbat.instrumentation.cfgcoverage.instr;

import org.apache.bcel.generic.InstructionHandle;

public class InstructionInfo {
	private InstructionHandle insnHandler;
	private int insnIdx;

	public InstructionInfo(InstructionHandle insnHandler, int insnIdx) {
		this.insnHandler = insnHandler;
		this.insnIdx = insnIdx;
	}
	
	public InstructionHandle getInsnHandler() {
		return insnHandler;
	}

	public int getInsnIdx() {
		return insnIdx;
	}
}
