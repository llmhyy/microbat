package microbat.instrumentation.instr.instruction.info;

import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.Type;

public class ArrayInstructionInfo extends RWInstructionInfo {
	private Type elementType;
	
	public ArrayInstructionInfo(InstructionHandle insnHandler, int line) {
		super(insnHandler, line);
	}

	@Override
	public ArrayInstruction getInstruction() {
		return (ArrayInstruction) super.getInstruction();
	}

	public Type getElementType() {
		return elementType;
	}

	public void setElementType(Type elementType) {
		this.elementType = elementType;
	}
	
}
