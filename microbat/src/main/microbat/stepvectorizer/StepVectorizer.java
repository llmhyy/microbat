package microbat.stepvectorizer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import microbat.instrumentation.utils.FileUtils;
import microbat.model.BreakPoint;
import microbat.model.trace.*;
import microbat.model.value.VarValue;

/**
 * StepVectorizer vector a step into a step vector
 * @author Ding YuChen & David
 *
 */
public class StepVectorizer {

	// Input trace
	private Trace trace;

	// Set of ID of variables used in trace
	private HashMap<String, Integer> variableSet = new HashMap<>();
	private HashMap<TraceNode, Integer> idMapping = new HashMap<>();

	public StepVectorizer(Trace trace) {

		// System.out.println("StepVectorizer Init ----- ");
		this.trace = trace;

		this.constructVarSet(this.trace);
		// System.out.println("Number of variable = " + this.variableSet.size());

	}

	public StepVector vectorize(int order) {

		// System.out.println("Vectoring Node ID = " + order);

		// Get the target step n*
		final TraceNode targetStep = this.trace.getTraceNode(order);
		BreakPoint breakPoint = targetStep.getBreakPoint();
		// System.out.println("BreakPoint = " + breakPoint);

		// A node that contain vectorization features
		StepVector stepVector = new StepVector(this.variableSet.size());

		// Check is the step a throwing exception step
//		if (targetStep.isException()) {
//			System.out.println("Trace ID: " + order + " is an throwing Exception step");
//		}
		stepVector.isThrowingException = targetStep.isException();

		// Check is the step a condition
//		if (targetStep.isConditional()) {
//			System.out.println("Trace ID: " + order + " is a conditional step");
//		}
		stepVector.isCondition = targetStep.isConditional();

		// Do not know what is loop condition
//		if (targetStep.isLoopCondition()) {
//			System.out.println("Trace ID: " + order + " is a loop conditional step");
//		} else {
//			System.out.println("Trace ID: " + order + " is not a loop conditional step");
//		}

//		if (targetStep.insideException()) {
//			System.out.println("Trace ID: " + order + " is inside Exception");
//		}
		TraceNode parent = targetStep.getLoopParent();
//		if (parent != null) {
//			System.out.println("Trace ID: " + order + " 's parent is Trace ID: " + parent.getOrder());
//		} else {
//			System.out.println("Trace ID: " + order + " do not have loop parent");
//		}

		// Check is there any method called in step
		stepVector.haveMethodCalled = !targetStep.getInvocationChildren().isEmpty();

//		if (targetStep.isBranch()) {
//			System.out.println("Trace ID: " + order + " is a branch");
//		}
//
//		System.out.println("---------------------------------------");

		// set previous
		stepVector.stepInPrev = Optional.ofNullable(targetStep.getStepInPrevious()).map(step -> step.getOrder()).orElse(0);
		stepVector.stepOverPrev = Optional.ofNullable(targetStep.getStepOverPrevious()).map(step -> step.getOrder())
				.orElse(0);
		// set next
		stepVector.stepOverNext = Optional.ofNullable(targetStep.getStepOverNext()).map(step -> step.getOrder()).orElse(0);
		stepVector.stepInNext = Optional.ofNullable(targetStep.getStepInNext()).map(step -> step.getOrder()).orElse(0);
		// set control flow
		stepVector.controlFlow.add(Optional.ofNullable(targetStep.getControlDominator()).map(s -> s.getOrder()).orElse(0));
		// set data flow
		Set<Integer> controlDominators = targetStep.findAllDominators().keySet();
		stepVector.dataFlow.addAll(controlDominators);
		
		// one-hot encode read variables
		List<VarValue> readVariables = targetStep.getReadVariables();
		readVariables.stream().mapToInt(var -> this.variableSet.get(var.getVarID())).forEach(i -> stepVector.setRead(i));
		// one-hot encode write variables
		List<VarValue> writeVariables = targetStep.getWrittenVariables();
		writeVariables.stream().mapToInt(var -> this.variableSet.get(var.getVarID())).forEach(i -> stepVector.setWrite(i));
		
		// max trace order
		int max_order = this.trace.getLatestNode().getOrder();
		stepVector.setMaxTraceOrder(max_order);
		
		// trace order
		stepVector.setOrder(order);
		
		return stepVector;
	}

	public void exportCSV(String path) {
		File csvFile = FileUtils.getFileCreateIfNotExist(path);
		String headers = Stream.of("id", "isThrowingException", "isInsideLoop", "isInsideIf", "isCondition",
				"readVariables", "writeVariables", "stepOverNext", "stepInNext", "stepOverPrev", "stepOverPrev",
				"controlDominators", "dataDominators").collect(Collectors.joining(","));
		try (PrintWriter pw = new PrintWriter(csvFile)) {
			pw.println(headers);
			for (int i = 0; i < this.trace.getExecutionList().size(); i++) {
//				pw.print(i + 1);
//				pw.print(',');
				pw.println(this.vectorize(i+1).convertToCSV());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Construct a list of variable used in whole trace
	private void constructVarSet(Trace trace) {
		int index = 0;
		for (int order = 1; order <= trace.size(); order++) {

//			System.out.println("orderID = " + order);
			final TraceNode node = trace.getTraceNode(order);

			// Get all read variable
			for (VarValue readVar : node.getReadVariables()) {
				String varName = readVar.getVarName();
				String varID = readVar.getVarID();

//				System.out.println("Read varName = " + varName + ", varID = " + varID);
				if (!this.variableSet.containsKey(varID)) {
					this.variableSet.put(varID, index);
					index++;
				}
			}

			// Get all write variable
			for (VarValue writeVar : node.getWrittenVariables()) {
				String varName = writeVar.getVarName();
				String varID = writeVar.getVarID();

//				System.out.println("Write varName = " + varName + ", varID = " + varID);
				if (!this.variableSet.containsKey(varID)) {
					this.variableSet.put(varID, index);
					index++;
				}
			}
		}
	}
}