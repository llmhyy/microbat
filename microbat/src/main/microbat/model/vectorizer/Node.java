package microbat.model.vectorizer;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// A node represent a step in execution trace
public class Node {

	// True if the step is an "Throw Exception" step
	public boolean isThrowingException;

	// True if the step is inside any for loop
	public boolean isInsideLoop;

	// True if the step is inside any if statement
	public boolean isInsideIf;

	// True if the step is a condition
	public boolean isCondition;

	// True if there are any method called in the step
	public boolean haveMethodCalled;

	// One-hot vector of read variables used in the step
	public boolean[] rVariable;

	// One-hot vector of written variables used in the step
	public boolean[] wVariable;

	// Constructor set everything to false
	public Node(int size) {
		this.isThrowingException = false;
		this.isInsideLoop = false;
		this.isInsideIf = false;
		this.isCondition = false;
		this.rVariable = new boolean[size];
		this.wVariable = new boolean[size];
	}

	public void setRead(int i) {
		if (i >= this.rVariable.length) {
			throw new IllegalArgumentException("index exceeds array length");
		}
		rVariable[i] = true;
	}

	public void setWrite(int i) {
		if (i >= this.wVariable.length) {
			throw new IllegalArgumentException("index exceeds array length");
		}
		wVariable[i] = true;
	}

	public String convertToCSV() {
		return Stream
				.of(this.isThrowingException, this.isInsideLoop, this.isInsideIf, this.isCondition,
						this.stringifyBoolArray(rVariable), this.stringifyBoolArray(wVariable))
				.map(b -> b.toString()).collect(Collectors.joining(","));
	}
	
	private String stringifyBoolArray(boolean[] arr) {
		return Arrays.toString(arr).replace("true", "1").replace("false", "0");
	}

	@Override
	public String toString() {
		String attributes = String.format("isException: %s; isInLoop: %s; isInConditional: %s; isCondition: %s;",
				this.isThrowingException, this.isInsideLoop, this.isInsideIf, this.isCondition);
		return attributes + Arrays.toString(rVariable) + Arrays.toString(wVariable);
	}
}