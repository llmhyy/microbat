package microbat.bytecode;

import java.util.stream.Stream;

/**
 * Byte code that calling a function
 * @author David
 *
 */
public enum FunctionByteCodeType implements ByteCode_Type_Not_Used {
	INVOKEDYNAMIC(186),		// Call dynamic method
	INVOKEINTERFACE(185),	// Call interface method
	INVOKESPECIAL(183),		// Call special method
	INVOKESTATIC(184),		// Call static method
	INVOKEVIRTUAL(182),		// Call virtual method
	
	;
	
	private final int opCode;
	
	FunctionByteCodeType(final int opCode) {
		this.opCode = opCode;
	}
	
	@Override
	public int getOpCode() {
		return this.opCode;
	}

	public static boolean containOpCode(int opCode) {
		return Stream.of(FunctionByteCodeType.values()).map(element -> element.getOpCode()).anyMatch(element -> element == opCode);
	}

	public static ByteCode_Type_Not_Used parseType(int opCode) {
		for (FunctionByteCodeType byteCode : FunctionByteCodeType.values()) {
			if (byteCode.getOpCode() == opCode) {
				return byteCode;
			}
		}
		return null;
	}
}
