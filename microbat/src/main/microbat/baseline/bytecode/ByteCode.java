package microbat.baseline.bytecode;

public class ByteCode {
	
	private String statement;
	
	public ByteCode(final String input) {
		this.statement = input;
	}
	
	public boolean isOneToOne() {
		return false;
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
