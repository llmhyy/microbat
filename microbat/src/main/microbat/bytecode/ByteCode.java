package microbat.bytecode;

public class ByteCode {
	
	/**
	 * String representation of opcode
	 */
	private final String statement;
	
	/**
	 * Opcode
	 */
	private final short opCode;
	
	/**
	 * Self-Defined Opcode type
	 */
	private final OpcodeType type;
	
//	private ByteCode_Type_Not_Used type;
	
	public ByteCode(final String instruction) {
		if (instruction == null) {
			throw new IllegalArgumentException("[Error] ByteCode Constructor: Input instruction is null");
		}
		
		if (instruction.isEmpty()) {
			throw new IllegalArgumentException("[Error] ByteCode Constructor: Input empty instruction");
		}
		
		this.statement = instruction;
		this.opCode = ByteCode.extractOpcode(instruction);
		this.type = OpcodeType.parse(this.opCode);
	}
	
	
	
	/**
	 * Extract the Opcode of this byte code instruction
	 * The Opcode is store inside the square brackets
	 * @return Opcode
	 */
	public static short extractOpcode(final String instruction) {
		return Short.parseShort(instruction.substring(instruction.indexOf('[')+1, instruction.indexOf(']')));
	}
	
	public short getOpcode() {
		return this.opCode;
	}
	
	public OpcodeType getOpcodeType() {
		return this.type;
	}

	
	@Override
	public boolean equals(Object anotherObj) {
		if (anotherObj instanceof ByteCode) {
			ByteCode anotherByteCode = (ByteCode) anotherObj;
			return this.opCode == anotherByteCode.opCode;
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return this.statement;
	}
}
