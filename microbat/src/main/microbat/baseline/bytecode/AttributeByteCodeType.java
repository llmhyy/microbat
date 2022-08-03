package microbat.baseline.bytecode;

import java.util.stream.Stream;

/**
 * Byte code that access the class attributes
 * @author David
 *
 */
public enum AttributeByteCodeType implements ByteCodeType {
	PUTFIELD(181),		// Assign value into class member
	PUTSTATIC(179),		// Assign value into class static member
	GETFIELD(180),		// Get class member
	GETSTATIC(178),		// Get class static member
	
	ARRAYLENGH(190),	// Load array length
	;
	
	private final int opCode;
	
	AttributeByteCodeType(final int opCode) {
		this.opCode = opCode;
	}
	
	@Override
	public int getOpCode() {
		return this.opCode;
	}

	public static boolean containOpCode(int opCode) {
		return Stream.of(AttributeByteCodeType.values()).map(element -> element.getOpCode()).anyMatch(element -> element == opCode);
	}

	public static ByteCodeType parseType(int opCode) {
		for (AttributeByteCodeType byteCode : AttributeByteCodeType.values()) {
			if (byteCode.getOpCode() == opCode) {
				return byteCode;
			}
		}
		return null;
	}
}
