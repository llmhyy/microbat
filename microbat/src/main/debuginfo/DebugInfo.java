package debuginfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;

/**
 * DebugInfo is used to store the information given from user
 * such as input, output, and feedback for each node
 * 
 * It will be interacted with the button in debug view
 * 
 * @author David
 *
 */
public class DebugInfo {
	
	private static List<VarValue> inputs = new ArrayList<>();
	private static List<VarValue> outputs = new ArrayList<>();
	private static NodeFeedbackPair nodeFeedbackPair = null;
	private static boolean rootCauseFound = false;
	private static boolean feedbackUpdatedFlag = false;
	
	private final static long SLEEP_TIME = 200;
	
	/**
	 * Add the given inputs
	 * @param inputs List of inputs
	 */
	public static void addInputs(Collection<VarValue> inputs) {
		DebugInfo.inputs.addAll(inputs);
		DebugInfo.printInputs();
	}
	
	public static void addInput(VarValue input) {
		DebugInfo.inputs.add(input);
		DebugInfo.printInputs();
	}
	
	public static void addOutputs(Collection<VarValue> outputs) {
		DebugInfo.outputs.addAll(outputs);
		DebugInfo.printOutputs();
	}
	
	public static void addOutput(VarValue output) {
		DebugInfo.outputs.add(output);
		DebugInfo.printOutputs();
	}
	
	public static void waitForFeedbackOrRootCause() {
		while (!DebugInfo.feedbackUpdatedFlag && !DebugInfo.rootCauseFound) {
			try {
				Thread.sleep(DebugInfo.SLEEP_TIME);
			} catch (Exception e) {}
		}
		DebugInfo.feedbackUpdatedFlag = false;
	}
	
	public static void waitForFeedback() {
		while (!DebugInfo.feedbackUpdatedFlag) {
			try {
				Thread.sleep(DebugInfo.SLEEP_TIME);
			} catch (Exception e) {}
		}
		DebugInfo.feedbackUpdatedFlag = false;
	}
	
	public static void addNodeFeedbackPair(NodeFeedbackPair nodeFeedbackPair) {
		DebugInfo.nodeFeedbackPair = nodeFeedbackPair;
		DebugInfo.feedbackUpdatedFlag = true;
	}
	
	public static void addNodeFeedbackPair(TraceNode node, UserFeedback feedback) {
		NodeFeedbackPair pair = new NodeFeedbackPair(node, feedback);
		DebugInfo.addNodeFeedbackPair(pair);
	}
	
	public static boolean isRootCauseFound() {
		return DebugInfo.rootCauseFound;
	}
	
	public static void setRootCauseFound(boolean found) {
		DebugInfo.rootCauseFound = found;
	}
	
	/**
	 * Get the list of input given from users
	 * @return List of input variables
	 */
	public static List<VarValue> getInputs() {
		return DebugInfo.inputs;
	}
	
	/**
	 * Get the list of outputs given from users 
	 * @return List of output variables
	 */
	public static List<VarValue> getOutputs() {
		return DebugInfo.outputs;
	}
	
	/**
	 * Get the list of node feedback pair from users
	 * @return Node Feedback Pair
	 */
	public static NodeFeedbackPair getNodeFeedbackPair() {
		DebugInfo.feedbackUpdatedFlag = false;
		return DebugInfo.nodeFeedbackPair;
	}
	
	/**
	 * Clear all inputs
	 */
	public static void clearInputs() {
		DebugInfo.inputs.clear();
		System.out.println("DebugInfo - Clear Inputs");
	}
	
	/**
	 * Clear all outputs
	 */
	public static void clearOutputs() {
		DebugInfo.outputs.clear();
		System.out.println("DebugInfo - Clear Outputs");
	}
	
	/**
	 * Clear all node feedback pairs
	 */
	public static void clearNodeFeedbackPair() {
		DebugInfo.nodeFeedbackPair = null;
	}
	
	/**
	 * Clear all data including inputs, outputs, and node feedback pairs
	 */
	public static void clearData() {
		DebugInfo.clearInputs();
		DebugInfo.clearOutputs();
		DebugInfo.clearNodeFeedbackPair();
		DebugInfo.feedbackUpdatedFlag = false;
		DebugInfo.rootCauseFound = false;
	}
	
	/**
	 * Display given inputs
	 */
	public static void printInputs() {
		for (VarValue input : DebugInfo.inputs) {
			System.out.println("DebugInfo - Selected inputs: " + input.getVarID());
		}
	}
	
	/**
	 * Display given outputs
	 */
	public static void printOutputs() {
		for (VarValue output : DebugInfo.outputs) {
			System.out.println("DebugInfo - Selected outputs: " + output.getVarID());
		}
	}
	
	/**
	 * Display given node feedback pairs
	 */
	public static void printNodeFeedbackPairs() {
		System.out.println("DebugInfo - Given feedbacks: " + DebugInfo.nodeFeedbackPair);
	}
	
}
