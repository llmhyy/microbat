package microbat.baseline.bytecode;

public class ByteCode {
	
	private String statement;
	
	private ByteCodeType type;
	
	public ByteCode(final String input) {
		this.statement = input;
		this.type = ByteCodeType.parseType(this.extractOpcode());
	}
	
	public boolean isOneToOne() {
		return this.type != null ? this.type instanceof OTOByteCodeType : false;
	}
	
	public boolean isManyToOne() {
		return this.type != null ? this.type instanceof MTOByteCodeType : false;
	}
	
	public boolean isArrayAccess() {
		return this.type != null ? this.type instanceof ArrayByteCodeType : false;
	}
	
	public boolean isAttrAccess() {
		return this.type != null ? this.type instanceof AttributeByteCodeType : false;
	}
	
	public boolean isFuncCall() {
		return this.type != null ? this.type instanceof FunctionByteCodeType : false;
	}
	
	public int getOpcode() {
		return this.type == null?  -1 : type.getOpCode();
	}
	
	/**
	 * Extract the Opcode of this byte code instruction
	 * The Opcode is store inside the square brackets
	 * @return Opcode
	 */
	private int extractOpcode() {
		return Integer.parseInt(this.statement.substring(this.statement.indexOf('[')+1, this.statement.indexOf(']')));
	}
	
	@Override
	public boolean equals(Object anotherObj) {
		if (anotherObj instanceof ByteCode) {
			ByteCode anotherByteCode = (ByteCode) anotherObj;
			return this.statement == anotherByteCode.statement;
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return this.statement;
	}
}
