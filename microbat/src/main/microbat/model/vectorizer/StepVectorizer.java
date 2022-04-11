package microbat.model.vectorizer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import microbat.instrumentation.utils.FileUtils;
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
	HashMap<String, Integer> variableSet = new HashMap<>();
	HashMap<TraceNode, Integer> idMapping = new HashMap<>();

	public StepVectorizer(Trace trace) {

		System.out.println("StepVectorizer Init ----- ");
		this.trace = trace;

		this.constructVarSet(this.trace);
		System.out.println("Number of variable = " + this.variableSet.size());

	}

	public Node vectorize(int order) {

		System.out.println("Vectoring Node ID = " + order);

		// Get the target step n*
		final TraceNode targetStep = this.trace.getTraceNode(order + 1);
		BreakPoint breakPoint = targetStep.getBreakPoint();
		System.out.println("BreakPoint = " + breakPoint);

		// A node that contain vectorization features
		Node node = new Node(this.variableSet.size());

		// Check is the step a throwing exception step
		if (targetStep.isException()) {
			System.out.println("Trace ID: " + order + " is an throwing Exception step");
		}
		node.isThrowingException = targetStep.isException();

		// Check is the step a condition
		if (targetStep.isConditional()) {
			System.out.println("Trace ID: " + order + " is a conditional step");
		}
		node.isCondition = targetStep.isConditional();

		// Do not know what is loop condition
		if (targetStep.isLoopCondition()) {
			System.out.println("Trace ID: " + order + " is a loop conditional step");
		} else {
			System.out.println("Trace ID: " + order + " is not a loop conditional step");
		}

		if (targetStep.insideException()) {
			System.out.println("Trace ID: " + order + " is inside Exception");
		}
		TraceNode parent = targetStep.getLoopParent();
		if (parent != null) {
			System.out.println("Trace ID: " + order + " 's parent is Trace ID: " + parent.getOrder());
		} else {
			System.out.println("Trace ID: " + order + " do not have loop parent");
		}

		// Check is there any method called in step
		node.haveMethodCalled = !targetStep.getInvocationChildren().isEmpty();

		if (targetStep.isBranch()) {
			System.out.println("Trace ID: " + order + " is a branch");
		}

		System.out.println("---------------------------------------");

		// set previous
		node.stepInPrev = Optional.ofNullable(targetStep.getStepInPrevious()).map(step -> step.getOrder()).orElse(0);
		node.stepOverPrev = Optional.ofNullable(targetStep.getStepOverPrevious()).map(step -> step.getOrder())
				.orElse(0);
		// set next
		node.stepOverNext = Optional.ofNullable(targetStep.getStepOverNext()).map(step -> step.getOrder()).orElse(0);
		node.stepInNext = Optional.ofNullable(targetStep.getStepInNext()).map(step -> step.getOrder()).orElse(0);
		// set control flow
		node.controlFlow.add(Optional.ofNullable(targetStep.getControlDominator()).map(s -> s.getOrder()).orElse(0));
		// set data flow
		Set<Integer> controlDominators = targetStep.findAllDominators().keySet();
		node.dataFlow.addAll(controlDominators);

		// one-hot encode read variables
		List<VarValue> readVariables = targetStep.getReadVariables();
		readVariables.stream().mapToInt(var -> this.variableSet.get(var.getVarID())).forEach(i -> node.setRead(i));
		// one-hot encode write variables
		List<VarValue> writeVariables = targetStep.getWrittenVariables();
		writeVariables.stream().mapToInt(var -> this.variableSet.get(var.getVarID())).forEach(i -> node.setWrite(i));
		return node;
	}

	public void exportCSV(String path) {
		File csvFile = FileUtils.getFileCreateIfNotExist(path);
		String headers = Stream.of("id", "isThrowingException", "isInsideLoop", "isInsideIf", "isCondition",
				"readVariables", "writeVariables", "stepOverNext", "stepInNext", "stepOverPrev", "stepOverPrev",
				"controlDominators", "dataDominators").collect(Collectors.joining(","));
		try (PrintWriter pw = new PrintWriter(csvFile)) {
			pw.println(headers);
			for (int i = 0; i < this.trace.getExecutionList().size(); i++) {
				pw.print(i + 1);
				pw.print(',');
				pw.println(this.vectorize(i).convertToCSV());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Construct a list of variable used in whole trace
	private void constructVarSet(Trace trace) {
		int index = 0;
		for (int order = 1; order <= trace.size(); order++) {

			System.out.println("orderID = " + order);
			final TraceNode node = trace.getTraceNode(order);

			// Get all read variable
			for (VarValue readVar : node.getReadVariables()) {
				String varName = readVar.getVarName();
				String varID = readVar.getVarID();

				System.out.println("Read varName = " + varName + ", varID = " + varID);
				if (!this.variableSet.containsKey(varID)) {
					this.variableSet.put(varID, index);
					index++;
				}
			}

			// Get all write variable
			for (VarValue writeVar : node.getWrittenVariables()) {
				String varName = writeVar.getVarName();
				String varID = writeVar.getVarID();

				System.out.println("Write varName = " + varName + ", varID = " + varID);
				if (!this.variableSet.containsKey(varID)) {
					this.variableSet.put(varID, index);
					index++;
				}
			}
		}
	}
}
