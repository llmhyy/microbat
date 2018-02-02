package microbat.instrumentation.trace.model;

import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.Type;

public class ArrayInstructionInfo extends RWInstructionInfo {
	private Type elementType;
	
	public ArrayInstructionInfo(InstructionHandle insnHandler, LineNumberGen lineGen) {
		super(insnHandler, lineGen);
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
