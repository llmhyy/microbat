package microbat.debugpilot;

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
public class DebugPilotInfo {
	
	private static volatile DebugPilotInfo instance;
	
	protected List<VarValue> inputs = new ArrayList<>();
	protected List<VarValue> outputs = new ArrayList<>();
	protected TraceNode outputNode = null;
	protected boolean isOutputNodeWrongBranch = false;

	protected NodeFeedbacksPair nodeFeedbacksPair = null;
	protected boolean feedbackUpdatedFlag = false;
	protected boolean stop = false;
	
	protected final static long SLEEP_TIME = 200;
	
	private DebugPilotInfo() {
		
	}
	
	public static DebugPilotInfo getInstance() {
		if (DebugPilotInfo.instance == null) {
			synchronized (DebugPilotInfo.class) {
				if (DebugPilotInfo.instance == null) {
					DebugPilotInfo.instance = new DebugPilotInfo();
				}
			}
		}
		return DebugPilotInfo.instance;
	}
	
	public boolean isStop() {
		return this.stop;
	}
	
	public void setStop(boolean stop) {
		this.stop = stop;
	}
	
	public void setOutputNode(final TraceNode node) {
		this.outputNode = node;
	}
	
	public TraceNode getOutputNode() {
		return this.outputNode;
	}
	
	/**
	 * Add the given inputs
	 * @param inputs List of inputs
	 */
	public void addInputs(Collection<VarValue> inputs) {
		this.inputs.addAll(inputs);
		this.printInputs();
	}
	
	public void addInput(VarValue input) {
		this.inputs.add(input);
		this.printInputs();
	}
	
	public void addInputs(VarValue ... inputs) {
		for (VarValue input : inputs) {
			this.inputs.add(input);
		}
		this.printInputs();
	}
	
	public void addOutputs(Collection<VarValue> outputs) {
		this.outputs.addAll(outputs);
		this.printOutputs();
	}
	
	public void addOutputs(VarValue ... outputs) {
		for (VarValue output : outputs) {
			this.outputs.add(output);
		}
		this.printOutputs();
	}
	
	public void addOutput(VarValue output) {
		this.outputs.add(output);
		this.printOutputs();
	}
	
	public boolean isFeedbackUpdate() {
		return this.feedbackUpdatedFlag;
	}
	
	public void setFeedbackUpdate(final boolean updated) {
		this.feedbackUpdatedFlag = updated;
	}
	
	public void waifForFeedbacksPairOrStop() {
		while (!this.feedbackUpdatedFlag && !this.stop) {
			try {
				Thread.sleep(DebugPilotInfo.SLEEP_TIME);
			} catch (Exception e) {

			}
		}
		this.feedbackUpdatedFlag = false;
		this.stop = false;
	}
	
//	public void waitFor 
//	public static void waitForFeedbackOrRootCauseOrStop() {
//		while (!DebugPilotInfo.feedbackUpdatedFlag && !DebugPilotInfo.rootCauseFound && !DebugPilotInfo.stop) {
//			try {
//				Thread.sleep(DebugPilotInfo.SLEEP_TIME);
//			} catch (Exception e) {}
//		}
//		DebugPilotInfo.feedbackUpdatedFlag = false;
//		DebugPilotInfo.stop = false;
//	}
//	
//	public static void waitForFeedbackOrRootCause() {
//		while (!DebugPilotInfo.feedbackUpdatedFlag && !DebugPilotInfo.rootCauseFound) {
//			try {
//				Thread.sleep(DebugPilotInfo.SLEEP_TIME);
//			} catch (Exception e) {}
//		}
//		DebugPilotInfo.feedbackUpdatedFlag = false;
//	}
	
//	public static void waitForFeedback() {
//		while (!DebugPilotInfo.feedbackUpdatedFlag) {
//			try {
//				Thread.sleep(DebugPilotInfo.SLEEP_TIME);
//			} catch (Exception e) {}
//		}
//		DebugPilotInfo.feedbackUpdatedFlag = false;
//	}
	
	public void setNodeFeedbacksPair(NodeFeedbacksPair nodeFeedbackPair) {
		this.nodeFeedbacksPair = nodeFeedbackPair;
		this.feedbackUpdatedFlag = true;
	}
	
	public void setNodeFeedbacksPair(TraceNode node, List<UserFeedback> feedbacks) {
		NodeFeedbacksPair pair = new NodeFeedbacksPair(node, feedbacks);
		this.setNodeFeedbacksPair(pair);
	}
	
	/**
	 * Get the list of input given from users
	 * @return List of input variables
	 */
	public List<VarValue> getInputs() {
		return this.inputs;
	}
	
	/**
	 * Get the list of outputs given from users 
	 * @return List of output variables
	 */
	public List<VarValue> getOutputs() {
		return this.outputs;
	}
	
	/**
	 * Get the list of node feedback pair from users
	 * @return Node Feedback Pair
	 */
	public NodeFeedbacksPair getNodeFeedbackPair() {
		this.feedbackUpdatedFlag = false;
		return this.nodeFeedbacksPair;
	}
	
	/**
	 * Clear all inputs
	 */
	public void clearInputs() {
		this.inputs.clear();
		System.out.println("DebugInfo - Clear Inputs");
	}
	
	/**
	 * Clear all outputs
	 */
	public void clearOutputs() {
		this.outputs.clear();
		System.out.println("DebugInfo - Clear Outputs");
	}
	
	/**
	 * Clear all node feedback pairs
	 */
	public void clearNodeFeedbackPairs() {
		this.nodeFeedbacksPair = null;
	}
	
	public void clearOutputNode() {
		this.outputNode = null;
	}
	
	/**
	 * Clear all data including inputs, outputs, and node feedback pairs
	 */
	public void clearData() {
		this.clearInputs();
		this.clearOutputs();
		this.clearOutputNode();
		this.clearNodeFeedbackPairs();
		this.feedbackUpdatedFlag = false;
	}
	
	/**
	 * Display given inputs
	 */
	public void printInputs() {
		for (VarValue input : this.inputs) {
			System.out.println("DebugInfo - Selected inputs: " + input.getVarID());
		}
	}
	
	/**
	 * Display given outputs
	 */
	public void printOutputs() {
		for (VarValue output : this.outputs) {
			System.out.println("DebugInfo - Selected outputs: " + output.getVarID());
		}
	}
	
	public boolean isOutputNodeWrongBranch() {
		return this.isOutputNodeWrongBranch;
	}
	
	public void setOutputNodeWrongBranch(boolean isWrongBranch) {
		this.isOutputNodeWrongBranch = isWrongBranch;
	}
}
