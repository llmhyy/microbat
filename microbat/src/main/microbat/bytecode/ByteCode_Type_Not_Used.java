package microbat.bytecode;

public interface ByteCode_Type_Not_Used {	

	public int getOpCode();
	
	/**
	 * Parse the given opCode number into self defined byteCode type. Call return null
	 * @param opCode Target opCode number
	 * @return ByteCode type. Null if the given opCode is not classified.
	 */
	static public ByteCode_Type_Not_Used parseType(final int opCode) {

		if (OTOByteCodeType.containOpCode(opCode)) {
			return OTOByteCodeType.parseType(opCode);
		}
		
		if (MTOByteCodeType.containOpCode(opCode)) {
			return MTOByteCodeType.parseType(opCode);
		}
		
		if (ArrayByteCodeType.containOpCode(opCode)) {
			return ArrayByteCodeType.parseType(opCode);
		}
		
		if (AttributeByteCodeType.containOpCode(opCode)) {
			return AttributeByteCodeType.parseType(opCode);
		}
		
		if (FunctionByteCodeType.containOpCode(opCode)) {
			return FunctionByteCodeType.parseType(opCode);
		}
		
//		System.out.println("Opcode: " + opCode + " is not handled");
		return null;
	}
}
