package microbat.instrumentation.trace.model;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.Type;

public class FieldInstructionInfo extends RWInstructionInfo {
	String refType;
	Type fieldBcType;
	int fieldIndex;

	public FieldInstructionInfo(InstructionHandle insnHandler, LineNumberGen lineGen) {
		super(insnHandler, lineGen);
	}

	public String getRefType() {
		return refType;
	}

	public int getFieldStackSize() {
		return fieldBcType.getSize();
	}

	public int getFieldIndex() {
		return fieldIndex;
	}

	public Type getFieldBcType() {
		return fieldBcType;
	}
	
	public String getFieldName() {
		return varName;
	}
	
	public String getFieldType() {
		return varType;
	}
	
	public boolean isComputationalType1() {
		return getFieldStackSize() == 1;
	}
	
	public boolean isComputationalType2() {
		return getFieldStackSize() == 2;
	}
}
