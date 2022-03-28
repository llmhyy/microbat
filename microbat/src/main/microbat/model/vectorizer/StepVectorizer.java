package microbat.model.vectorizer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.model.ControlScope;
import microbat.model.Scope;
import microbat.model.SourceScope;
import microbat.model.trace.*;
import microbat.model.value.VarValue;

public class StepVectorizer {
	
	// Input trace
	Trace trace;
	
	// Set of ID of variables used in trace
	Set<String> variableSet = new HashSet<String>();
	
	public StepVectorizer(Trace trace) {
		
		System.out.println("StepVectorizer Init ----- ");
		this.trace = trace;
		
		this.constructVarSet(this.trace);
		System.out.println("Number of variable = " + this.variableSet.size());
	}
	
	public Node vectorize(int order) {
		
		System.out.println("Vectoring Node ID = " + order);
		
		// Get the target step n*
		final TraceNode targetStep = this.trace.getTraceNode(order);
		BreakPoint breakPoint = targetStep.getBreakPoint();
		System.out.println("BreakPoint = " + breakPoint);
		
		// A node that contain vectorization features
		Node node = new Node();
		
		// Check is the step a throwing exception step
		if(targetStep.isException()) {
			System.out.println("Trace ID: " + order + " is an throwing Exception step");
		}
		node.isThrowingException = targetStep.isException();
		
		// Check is the step a condition
		if(targetStep.isConditional()) {
			System.out.println("Trace ID: " + order + " is a conditional step");
		}
		node.isCondition = targetStep.isConditional();
		
		// Do not know what is loop condition
		if(targetStep.isLoopCondition()) {
			System.out.println("Trace ID: " + order + " is a loop conditional step");
		} else {
			System.out.println("Trace ID: " + order + " is not a loop conditional step");
		}
		
		if(targetStep.insideException()) {
			System.out.println("Trace ID: " + order + " is inside Exception");
		}
		TraceNode parent = targetStep.getLoopParent();
		if(parent != null) {
			System.out.println("Trace ID: " + order + " 's parent is Trace ID: " + parent.getOrder());
		} else {
			System.out.println("Trace ID: " + order + " do not have loop parent");
		}

		// Check is there any method called in step
		node.haveMethodCalled = !targetStep.getInvocationChildren().isEmpty();

		
		if(targetStep.isBranch()) {
			System.out.println("Trace ID: " + order + " is a branch");
		}
		
		System.out.println("---------------------------------------");
		return node;
	}
	
	// Construct a list of variable used in whole trace
	private void constructVarSet(Trace trace) {
		
		for(int order = 1; order <= trace.size(); order++) {
			
			System.out.println("orderID = " + order);
			final TraceNode node = trace.getTraceNode(order);
			
			// Get all read variable
			for (VarValue readVar : node.getReadVariables()) {
				String varName = readVar.getVarName();
				String varID = readVar.getVarID();
				
				System.out.println("Read varName = " + varName + ", varID = " + varID);
				this.variableSet.add(varID);
			}
			
			// Get all write variable
			for (VarValue writeVar: node.getWrittenVariables()) {
				String varName = writeVar.getVarName();
				String varID = writeVar.getVarID();
				
				System.out.println("Write varName = " + varName + ", varID = " + varID);
				this.variableSet.add(varID);
			}
		}
	}
}
