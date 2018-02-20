package microbat.instrumentation.instr.instruction.info;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.Type;

public class FieldInstructionInfo extends RWInstructionInfo {
	private String refType;
	private Type fieldBcType;

	public FieldInstructionInfo(InstructionHandle insnHandler, int line) {
		super(insnHandler, line);
	}

	public String getRefType() {
		return refType;
	}

	public int getFieldStackSize() {
		return fieldBcType.getSize();
	}

	public Type getFieldBcType() {
		return fieldBcType;
	}
	
	public String getFieldName() {
		return getVarName();
	}
	
	public String getFieldType() {
		return getVarType();
	}
	
	public void setRefType(String refType) {
		this.refType = signatureToName(refType);
	}

	public void setFieldBcType(Type fieldBcType) {
		this.fieldBcType = fieldBcType;
	}

	public boolean isComputationalType1() {
		return getFieldStackSize() == 1;
	}
	
	public boolean isComputationalType2() {
		return getFieldStackSize() == 2;
	}
}
