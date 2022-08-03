package microbat.baseline.bytecode;

import java.util.stream.Stream;

/**
 * Container Access ByteCode
 * @author David
 *
 */
public enum ArrayByteCodeType implements ByteCodeType{
	IASTORE(79),		// Store value into array
	IALOAD(46),			// Load value from array

	POP(87),			// Pop the value out of stack
	;

	private final int opCode;
	
	ArrayByteCodeType(final int opCode) {
		this.opCode = opCode;
	}
	
	@Override
	public int getOpCode() {
		// TODO Auto-generated method stub
		return this.opCode;
	}

	public static boolean containOpCode(int opCode) {
		return Stream.of(ArrayByteCodeType.values()).map(element -> element.getOpCode()).anyMatch(element -> element == opCode);
	}

	public static ByteCodeType parseType(int opCode) {
		for (ArrayByteCodeType byteCode : ArrayByteCodeType.values()) {
			if (byteCode.getOpCode() == opCode) {
				return byteCode;
			}
		}
		return null;
	}
}
