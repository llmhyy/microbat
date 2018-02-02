package microbat.instrumentation.trace.model;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.Type;

public class FieldInstructionInfo extends RWInstructionInfo {
	private String refType;
	private Type fieldBcType;
	private int fieldIndex;

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

	public void setFieldIndex(int fieldIndex) {
		this.fieldIndex = fieldIndex;
	}

	public boolean isComputationalType1() {
		return getFieldStackSize() == 1;
	}
	
	public boolean isComputationalType2() {
		return getFieldStackSize() == 2;
	}
}
