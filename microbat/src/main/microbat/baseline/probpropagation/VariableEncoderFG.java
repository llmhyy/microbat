package microbat.baseline.probpropagation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import debuginfo.NodeFeedbackPair;
import microbat.baseline.constraints.BitRepresentation;
import microbat.baseline.constraints.Constraint;
import microbat.baseline.constraints.PriorConstraint;
import microbat.baseline.constraints.VariableConstraint;
import microbat.baseline.constraints.VariableConstraintA1;
import microbat.baseline.constraints.VariableConstraintA2;
import microbat.baseline.constraints.VariableConstraintA3;
import microbat.baseline.factorgraph.FactorGraphClient;
import microbat.baseline.factorgraph.MessageProcessor;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;

/**
 * Variable encoder calculate the probability of correctness of variables
 * @author David
 *
 */
public class VariableEncoderFG extends Encoder {
	
	private List<VarValue> inputVars;
	private List<VarValue> outputVars;
	private List<NodeFeedbackPair> userFeedbacks = new ArrayList<>();
	
	/**
	 * Constructor
	 * @param trace Complete trace for testing program
	 * @param executionList Sliced execution list
	 * @param inputVars List of input variables
	 * @param outputVars List of output variables
	 */
	public VariableEncoderFG(Trace trace, List<TraceNode> executionList, List<VarValue> inputVars, List<VarValue> outputVars) {
		super(trace, executionList);
		this.inputVars = inputVars;
		this.outputVars = outputVars;
		this.construntVarIDMap();
	}
	
	/**
	 * Calculate the variable probability. It will undergo the following process:
	 * 1. Generate all the constraints involved
	 * 2. Run sum product algorithm on python server to calculate variable probability
	 * 3. Assign calculated probability to each variable
	 */
	@Override
	public void encode() {
		
		// Generate all constraints
		List<Constraint> constraints = this.genConstraints();
		System.out.println("Variable Encoder: " + constraints.size() + " constraints.");
		
		// Request the python server to run sum product algorithm
		FactorGraphClient client = new FactorGraphClient();
		
		MessageProcessor msgProcessor = new MessageProcessor();
		String graphMsg = msgProcessor.buildGraphMsg(constraints);
		String factorMsg = msgProcessor.buildFactorMsg(constraints);
		
		client.conntectServer();
		
		// Response contain the probability of each variable
		String response = client.requestBP(graphMsg, factorMsg);
		
		// Assign calculate probability to corresponding variable
		Map<String, Double> varsProb = msgProcessor.recieveMsg(response);
		for (Map.Entry<String, Double> pair : varsProb.entrySet()) {
			String predID = pair.getKey();
			Double prob = pair.getValue();
			
			for (VarValue var : this.getVarByID(predID)) {
				var.setProbability(prob);
			}
		}
		
		client.disconnectServer();
	}
	
	/**
	 * Add the node feedback pair
	 * @param userFeedbacks List of node feedback pair
	 */
	public void setFeedbacks(List<NodeFeedbackPair> userFeedbacks) {
		this.userFeedbacks = userFeedbacks;
	}
	
	/**
	 * Generate all involved constraints
	 * @return List of constraints
	 */
	protected List<Constraint> genConstraints() {
		List<Constraint> constraints = new ArrayList<>();
		for (TraceNode node : this.executionList) {
			constraints.addAll(this.genVarConstraints(node));
		}
		constraints.addAll(this.genPriorConstraints());
		return constraints;
	}
	
	/**
	 * Generate variable constraints of given trace node
	 * @param node Target trace node
	 * @return List of variable constraints
	 */
	private List<Constraint> genVarConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		
		if (this.isSkippable(node)) {
			return constraints;
		}
		
		/**
		 * Generate variable constraint A1 if the node contain 
		 * written variable and fulfill one of the following condition:
		 * 1. Contain at least one read variable
		 * 2. Contain control dominator
		 */
		if (Constraint.countReadVars(node) != 0 || node.getControlDominator() != null) {
			for (VarValue writeVar : node.getWrittenVariables()) {
				Constraint constraint = new VariableConstraintA1(node, writeVar, PropProbability.HIGH);
				constraints.add(constraint);
			}			
		}

		/**
		 * Generate variable constraint A2 if the node contain
		 * read variable and fulfill one of the following condition:
		 * 1. Contain at least one written variable
		 * 2. Contain control dominator
		 */
		if (Constraint.countWrittenVars(node) != 0 || node.getControlDominator() != null) {
			for (VarValue readVar : node.getReadVariables()) {
				Constraint constraint = new VariableConstraintA2(node, readVar, PropProbability.HIGH);
				constraints.add(constraint);
			}
		}

		
		// Generate variable constraint A3 if control dominator exist
		/**
		 * Generate variable constraint A3 if control dominator
		 * exist and fulfill one of the following condition:
		 * 1. Contain any read variables
		 * 2. Contain any written variables
		 */
		if (node.getControlDominator() != null && (Constraint.countReadVars(node) + Constraint.countWrittenVars(node) > 0)) {
			Constraint constraint = new VariableConstraintA3(node, PropProbability.HIGH);
			constraints.add(constraint);
		}

		return constraints;
	}
	
	/**
	 * Generate all the prior constraints, which included
	 * 1. Input and Output constraints (including their child)
	 * 2. Feedback from users
	 * @return List of prior constraint
	 */
	private List<Constraint> genPriorConstraints() {
		List<Constraint> constraints = new ArrayList<>();

		// Generate constraint for input variables
		for (VarValue inputVar : this.inputVars) {
			Constraint constraint = new PriorConstraint(inputVar, PropProbability.HIGH);
			constraints.add(constraint);
		}
		
		// Generate constraint for output variables
		for (VarValue outputVar : this.outputVars) {
			Constraint constraint = new PriorConstraint(outputVar, PropProbability.LOW);
			constraints.add(constraint);
		}

		// Convert user feedbacks to prior constraints
		System.out.println("Feedbacks Count: " + this.userFeedbacks.size());
		for (NodeFeedbackPair feedbackPair : this.userFeedbacks) {
			TraceNode node = feedbackPair.getNode();
			UserFeedback feedback = feedbackPair.getFeedback();
			
			if (feedback.getFeedbackType() == UserFeedback.CORRECT) {
				
				/**
				 * If the user feedback is CORRECT, then the following are all correct:
				 * 1. Read variables
				 * 2. Written variables
				 * 3. Control dominator
				 */
				
				for (VarValue readVar : node.getReadVariables()) {
					Constraint constraint = new PriorConstraint(readVar, PropProbability.HIGH);
					constraints.add(constraint);
				}
				
				for (VarValue writeVar : node.getWrittenVariables()) {
					Constraint constraint = new PriorConstraint(writeVar, PropProbability.HIGH);
					constraints.add(constraint);
				}
				
				TraceNode controlDominator = node.getControlDominator();
				if (controlDominator != null) {
					VarValue controlDomValue = Constraint.extractControlDomVar(controlDominator);
					Constraint constraint = new PriorConstraint(controlDomValue, PropProbability.HIGH);
					constraints.add(constraint);
				}
				
			} else if (feedback.getFeedbackType() == UserFeedback.WRONG_VARIABLE_VALUE) {
				
				/**
				 * If the user feedback is WRONG_VARIABLE_VALUE, then
				 * 1. Selected read variable is wrong
				 * 2. All written variable is wrong
				 * 3. No comment on control dominator
				 */
				
				VarValue wrongVar = feedback.getOption().getReadVar();
				constraints.add(new PriorConstraint(wrongVar, PropProbability.LOW));
				
				for (VarValue writeVar : node.getWrittenVariables()) {
					Constraint constraint = new PriorConstraint(writeVar, PropProbability.LOW);
					constraints.add(constraint);
				}
				
			} else if (feedback.getFeedbackType() == UserFeedback.WRONG_PATH) {
				
				/**
				 * If the user feedback is WRONG_PATH, then
				 * 1. The control dominator is wrong
				 * 2. No comment for read and write variable
				 */
				
				TraceNode controlDom = node.getControlDominator();
				VarValue controlDomValue = Constraint.extractControlDomVar(controlDom);
				Constraint constraint = new PriorConstraint(controlDomValue, PropProbability.LOW);
				constraints.add(constraint);
			}
		}
		
		return constraints;
	}
}
