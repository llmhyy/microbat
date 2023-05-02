package microbat.probability.BP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import debuginfo.NodeFeedbackPair;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.probability.PropProbability;
import microbat.probability.BP.constraint.BitRepresentation;
import microbat.probability.BP.constraint.Constraint;
import microbat.probability.BP.constraint.PriorConstraint;
import microbat.probability.BP.constraint.VariableConstraint;
import microbat.probability.BP.constraint.VariableConstraintA1;
import microbat.probability.BP.constraint.VariableConstraintA2;
import microbat.probability.BP.constraint.VariableConstraintA3;
import microbat.recommendation.UserFeedback;
import debuginfo.NodeFeedbacksPair;

/**
 * Variable encoder calculate the probability of correctness of variables
 * @author David
 *
 */
public class VariableEncoderFG extends Encoder {
	
	private List<VarValue> correctVars;
	private List<VarValue> wrongVars;
	
	/**
	 * Constructor
	 * @param trace Complete trace for testing program
	 * @param executionList Sliced execution list
	 * @param inputVars List of input variables
	 * @param outputVars List of output variables
	 */
	public VariableEncoderFG(Trace trace, List<TraceNode> executionList, List<VarValue> inputVars, List<VarValue> outputVars, List<NodeFeedbacksPair> pairs) {
		super(trace, executionList);
		this.correctVars = inputVars;
		this.wrongVars = outputVars;
		this.construntVarIDMap();
		
		for (NodeFeedbacksPair pair : pairs) {
			final TraceNode node = pair.getNode();
			final TraceNode controlDom = node.getControlDominator();
			
			final List<VarValue> readVars = new ArrayList<>();
			readVars.addAll(node.getReadVariables());
			readVars.removeIf(var -> var.isThisVariable());
			
			final List<VarValue> writtenVars = new ArrayList<>();
			writtenVars.addAll(node.getWrittenVariables());
			writtenVars.removeIf(var -> var.isThisVariable());
			
			if (pair.getFeedbackType().equals(UserFeedback.CORRECT)) {
				this.correctVars.addAll(readVars);
				this.correctVars.addAll(writtenVars);
				if (controlDom != null) {
					this.correctVars.add(controlDom.getConditionResult());
				}
			} else if (pair.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
				if (controlDom == null) {
					throw new RuntimeException("There are no control dominator");
				}
				final VarValue controlDomVar = controlDom.getConditionResult();
				this.wrongVars.add(controlDomVar);
			} else if (pair.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
				List<VarValue> wrongReadVars = new ArrayList<>();
				for (UserFeedback feedback : pair.getFeedbacks()) {
					wrongReadVars.add(feedback.getOption().getReadVar());
				}
				this.wrongVars.addAll(wrongReadVars);
				this.wrongVars.addAll(node.getWrittenVariables());
				this.correctVars.add(controlDom.getConditionResult());
			}
		}
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
		BeliefPropagationClient client = new BeliefPropagationClient();
		
		MessageProcessor msgProcessor = new MessageProcessor();
		String graphMsg = msgProcessor.buildGraphMsg(constraints);
		String factorMsg = msgProcessor.buildFactorMsg(constraints);
		
		try {
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
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException("[Variable Encoder] Error occur when calculating variable probabilities");
		}
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

		// Generate constraint for correct variables
		for (VarValue inputVar : this.correctVars) {
			Constraint constraint = new PriorConstraint(inputVar, PropProbability.HIGH);
			constraints.add(constraint);
		}
		
		// Generate constraint for wrong variables
		for (VarValue outputVar : this.wrongVars) {
			Constraint constraint = new PriorConstraint(outputVar, PropProbability.LOW);
			constraints.add(constraint);
		}
		
		return constraints;
	}
}
