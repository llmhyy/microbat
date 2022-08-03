package microbat.baseline.bytecode;

import java.util.stream.Stream;

/**
 * Byte code that is many to one
 * @author David
 *
 */
public enum MTOByteCodeType implements ByteCodeType {
	IFLE(158),			// If less than or equal to 
	IF_ICMPLT(161),		// If less than
	IF_ICMPNE(160),		// If not equal
	IF_ICMPGE(162),		// If value 1 is greater than value 2
	
	IFNE(154),			// If value is not zero
		
	CHECKCAST(192),		// Checks an object reference is of a certain type
	IREM(112),			// Find integer remainder
	;
	
	private final int opCode;
	
	MTOByteCodeType(final int opCode) {
		this.opCode = opCode;
	}
	
	@Override
	public int getOpCode() {
		// TODO Auto-generated method stub
		return this.opCode;
	}

	public static boolean containOpCode(int opCode) {
		return Stream.of(MTOByteCodeType.values()).map(element -> element.getOpCode()).anyMatch(element -> element == opCode);
	}

	public static ByteCodeType parseType(int opCode) {
		for (MTOByteCodeType byteCode : MTOByteCodeType.values()) {
			if (byteCode.getOpCode() == opCode) {
				return byteCode;
			}
		}
		return null;
	}
}
