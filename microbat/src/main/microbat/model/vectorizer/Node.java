package microbat.model.vectorizer;

import java.util.Vector;

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

	// One-hot vector of variables used in the step
	public Vector<Boolean> variable = new Vector<Boolean>();

	// Constructor set everything to false
	public Node() {
		this.isThrowingException = false;
		this.isInsideLoop = false;
		this.isInsideIf = false;
		this.isCondition = false;
	}

}