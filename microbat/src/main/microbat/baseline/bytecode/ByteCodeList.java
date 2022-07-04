package microbat.baseline.bytecode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * ByteCodeList represent the list of byte code required to execute for a line of Java code
 * @author David
 *
 */
public class ByteCodeList implements Iterable<ByteCode> {

	private List<ByteCode> byteCodeExecuted;
	
	public ByteCodeList(final String statements) {
		this.byteCodeExecuted = new ArrayList<>();
		
		if (statements != "") {
			List<String> tokens = Arrays.asList(statements.split(","));
			for (String token : tokens) {
				this.byteCodeExecuted.add(new ByteCode(token));
			}
		}
	}
	
	public void addByteCode(final ByteCode byteCode) {
		this.byteCodeExecuted.add(byteCode);
	}

	public ByteCode getByteCode(final int idx) {
		return this.byteCodeExecuted.get(idx);
	}
	
	public boolean isEmpty() {
		return this.byteCodeExecuted.isEmpty();
	}
	
	public boolean isOneToOne() {
		for (ByteCode byteCode : this.byteCodeExecuted) {
			if (!byteCode.isOneToOne()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public Iterator<ByteCode> iterator() {
		return this.byteCodeExecuted.iterator();
	}
	
	@Override
	public boolean equals(Object anotherObj) {
		if (anotherObj instanceof ByteCodeList) {
			ByteCodeList anotherList = (ByteCodeList) anotherObj;
			if (this.byteCodeExecuted.size() != anotherList.byteCodeExecuted.size()) {
				return false;
			}
			for (int idx=0; idx<this.byteCodeExecuted.size(); idx++) {
				ByteCode byteCode = this.byteCodeExecuted.get(idx);
				ByteCode anotherByteCode = anotherList.byteCodeExecuted.get(idx);
				if (byteCode != anotherByteCode) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		String string = "";
		for (ByteCode byteCode : this.byteCodeExecuted) {
			string += byteCode.toString() + ",";
		}
		return string == "" ? string : string.substring(0, string.length()-1); // Remove the last comma
	}

}
