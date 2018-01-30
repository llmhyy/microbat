package microbat.instrumentation.trace.model;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LineNumberGen;

public class ArrayInstructionInfo extends RWInstructionInfo {

	public ArrayInstructionInfo(InstructionHandle insnHandler, LineNumberGen lineGen) {
		super(insnHandler, lineGen);
	}

}
