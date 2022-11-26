package microbat.bytecode;

import java.util.stream.Stream;

/**
 * Byte code that is one to one
 * @author David
 *
 */
public enum OTOByteCodeType implements ByteCode_Type_Not_Used {
	IADD(96),			// Add two integer
	ISUB(100),			// Subtract two integer
	IDIV(108),			// Divide two integer
	
	IINC(132),			// Increment integer
	
	IFEQ(153),			// If the value equal to zero
	
	ALOAD(25),			// Load load address
	ALOAD_0(42),		// Load 1st local address
	ALOAD_1(43),		// Load 2nd local address
	ALOAD_2(44),		// Load 3rd local address
	ALOAD_3(45),		// Load 4th local address
	
	ICONST_M1(2),		// Load -1
	ICONST_0(3),		// Load 0
	ICONST_1(4),		// Load 1
	ICONST_2(5),		// Load 2
	ICONST_3(6),		// Load 3
	ICONST_4(7),		// Load 4
	ICONST_5(8),		// Load 5
	
	ISTORE(54),			// Store value to variable
	ISTORE_0(59),		// Store value to variable 0
	ISTORE_1(60),		// Store value to variable 1
	ISTORE_2(61),		// Store value to variable 2
	ISTORE_3(62), 		// Store value to variable 3
	
	ILOAD(21),			// Load value from variable
	ILOAD_0(26),		// Load value from variable 1
	ILOAD_1(27),		// Load value from variable 2
	ILOAD_2(28),		// Load value from variable 3
	ILOAD_3(29),		// Load value from variable 4
	
	ASTORE(58),			// Store reference to variable
	ASTORE_0(75),		// Store reference to variable 1
	ASTORE_1(76),		// Store reference to variable 2
	ASTORE_2(77),		// Store reference to variable 3
	ASTORE_3(78),		// Store reference to variable 4
	
	DUP(89),			// Duplicate top element of stack
	
	GOTO(167),			// Goto another instruction
	GOTO_W(200),		// Goto another instruction
	
	NEW(187),			// Create a new object
	
	BIPUSH(16),			// Push a byte onto stack as integer value
	IRETURN(172),		// Return integer
	
	/*
	 *	The following are the opCode that
	 *	may be be one to one 
	 */
	NEWARRAY(188),		// Create a new array
	;
	private final int opCode;
	
	OTOByteCodeType(final int opCode) {
		this.opCode = opCode;
	}
	
	@Override
	public int getOpCode() {
		return this.opCode;
	}

	public static boolean containOpCode(int opCode) {
		return Stream.of(OTOByteCodeType.values()).map(element -> element.getOpCode()).anyMatch(element -> element == opCode);
	}

	public static ByteCode_Type_Not_Used parseType(int opCode) {
		for (OTOByteCodeType byteCode : OTOByteCodeType.values()) {
			if (byteCode.getOpCode() == opCode) {
				return byteCode;
			}
		}
		return null;
	}
}
