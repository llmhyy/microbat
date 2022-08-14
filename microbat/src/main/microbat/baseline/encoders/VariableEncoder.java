package microbat.baseline.encoders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import microbat.baseline.BitRepresentation;
import microbat.baseline.Configs;
import microbat.baseline.constraints.Constraint;
import microbat.baseline.constraints.PriorConstraint;
import microbat.baseline.constraints.VariableConstraint;
import microbat.baseline.factorgraph.FactorGraphClient;
import microbat.baseline.factorgraph.MessageProcessor;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;

/**
 * Variable encoder calculate the probability of correctness of variables
 * @author David, Siang Hwee
 *
 */
public class VariableEncoder extends Encoder {
	
	private List<VarValue> inputVars;
	private List<VarValue> outputVars;
	
	private PropagationCalculator propCalculator;
	
	private List<NodeFeedbackPair> userFeedbacks;
	
	private List<VarValue> involvedArrays;
	
	private boolean duplicateIDFixed;
	
	/**
	 * Constructor
	 * @param trace Complete trace for testing program
	 * @param executionList Sliced execution list
	 * @param inputVars List of input variables
	 * @param outputVars List of output variables
	 */
	public VariableEncoder(Trace trace, List<TraceNode> executionList, List<VarValue> inputVars, List<VarValue> outputVars) {
		super(trace, executionList);
		
		this.inputVars = inputVars;
		this.outputVars = outputVars;
		
		for (VarValue inputVar : this.inputVars) {
			System.out.println("InputVar:" + inputVar.getVarID());
		}
		for (VarValue outputVar : this.outputVars) {
			System.out.println("OutputVar: " + outputVar.getVarID());
		}
		
		this.propCalculator = new PropagationCalculator();
		
		this.userFeedbacks = new ArrayList<>();
		this.involvedArrays = new ArrayList<>();
		
		this.construntVarIDMap();
	}
	
	/**
	 * Calculate the variable probability. It will undergo the following process:
	 * 1. Generate all the constraints involved
	 * 2. Run sum product algorithm on python server to calculate variable probability
	 * 3. Assign calculated probability to each variable
	 */
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
		
		System.out.println("response: " + response);
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
	
	public void setFeedbacks(List<NodeFeedbackPair> userFeedbacks) {
		this.userFeedbacks = userFeedbacks;
	}
	
	public void setInvolvedArrays(List<VarValue> arrays) {
		this.involvedArrays = arrays;
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
		
		final int readLen = this.countReadVars(node);
		final int writeLen = this.countWriteVars(node);
		final int totalLen = this.countPredicates(node);
		
		final TraceNode controlDom = node.getControlDominator();
		final boolean haveControlDom = controlDom != null;
		
		final int predIdx = haveControlDom ? totalLen-1 : -1;
		
		// A1 Constraint
		for (int idx=0; idx<writeLen; idx++) {
			final int writeIdx = idx + readLen;
			
			BitRepresentation varsIncluded = new BitRepresentation(totalLen);
			varsIncluded.set(0, readLen);
			varsIncluded.set(writeIdx);
			
			if (haveControlDom) {
				varsIncluded.set(predIdx);
			}
			
			Constraint constraint = new VariableConstraint(varsIncluded, writeIdx, Configs.HIGH);
			constraint.setVarsID(node);
			constraints.add(constraint);
		}
		
//		System.out.println("TraceNode: " + node.getOrder());
//		System.out.println("ByteCode: " + node.getBytecode());
		
		// A2 Constraint
		for (int readIdx=0; readIdx<readLen; readIdx++) {
			BitRepresentation varsIncluded = new BitRepresentation(totalLen);
			varsIncluded.set(0, totalLen);
			
			// Calculate the propagation probability
			double propProb = Configs.HIGH;
			if (node.getBytecode() != "") {
				VarValue readVar = node.getReadVariables().get(readIdx);
				propProb = this.propCalculator.calPropProb(node, readVar);
			}
			
			Constraint constraint = new VariableConstraint(varsIncluded, readIdx, propProb);
			constraint.setVarsID(node);
			
			constraints.add(constraint);
		}
		
		// A3 Constraint
		if (haveControlDom) {
			BitRepresentation varsIncluded = new BitRepresentation(totalLen);
			varsIncluded.set(0, totalLen);
			
			Constraint constraint = new VariableConstraint(varsIncluded, predIdx, Configs.HIGH);
			constraint.setVarsID(node);
			
			constraints.add(constraint);
		}
		
//		MessageProcessor msgProcessor = new MessageProcessor();
//		System.out.println("TraceNode: " + node.getOrder());
//		for (Constraint constraint : constraints) {
//			System.out.println(constraint);
//		}
//		String graphMsg = msgProcessor.buildGraphMsg(constraints);
//		String factorMsg = msgProcessor.buildFactorMsg(constraints);
//		
//		System.out.println("graphMsg: " + graphMsg);
//		System.out.println("factorMsg: " + factorMsg);
		
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

		// Set input to HIGH
		for (VarValue inputVar : this.inputVars) {
			constraints.add(this.genPriorConstraint(inputVar, Configs.HIGH));
		}
		
		// Set output to LOW
		for (VarValue outputVar : this.outputVars) {
			constraints.add(this.genPriorConstraint(outputVar, Configs.LOW));
		}
		
		// Set up for children
		for (TraceNode node : this.executionList) {
			for (VarValue readVar : node.getReadVariables()) {
				for (VarValue parent : readVar.getParents()) {
					for (VarValue inputVar : this.inputVars) {
						if (parent.getVarID().equals(inputVar.getAliasVarID())) {
							constraints.add(this.genPriorConstraint(readVar, Configs.HIGH));
						}
					}
				}
			}
			for (VarValue writeVar : node.getWrittenVariables()) {
				for (VarValue parent : writeVar.getParents()) {
					for (VarValue outputVar : this.outputVars) {
						if (parent.getVarID().equals(outputVar.getAliasVarID())) {
							constraints.add(this.genPriorConstraint(writeVar, Configs.LOW));
						}
					}
				}
			}
		}
		
		
		// Convert user feedbacks to prior constraints
		System.out.println("Feedbacks Count: " + this.userFeedbacks.size());
		for (NodeFeedbackPair feedbackPair : this.userFeedbacks) {
			TraceNode node = feedbackPair.getNode();
			UserFeedback feedback = feedbackPair.getFeedback();
			
			if (feedback.getFeedbackType() == UserFeedback.CORRECT) {
				// Add constraint to all read and write variable to HIGH
				for (VarValue readVar : node.getReadVariables()) {
					constraints.add(this.genPriorConstraint(readVar, Configs.HIGH));
				}
				for (VarValue writeVar : node.getWrittenVariables()) {
					constraints.add(this.genPriorConstraint(writeVar, Configs.HIGH));
				}
			} else if (feedback.getFeedbackType() == UserFeedback.WRONG_VARIABLE_VALUE) {
				// Add constraint to target variable to LOW
				for (VarValue wrongVar : feedback.getOption().getIncludedWrongVars()) {
					constraints.add(this.genPriorConstraint(wrongVar, Configs.LOW));
				}
				
				// Also, since the target variable is wrong, then the write variable must be wrong as well
				for (VarValue writeVar : node.getWrittenVariables()) {
					constraints.add(this.genPriorConstraint(writeVar, Configs.LOW));
				}
			} else if (feedback.getFeedbackType() == UserFeedback.WRONG_PATH) {
				// Add constraint to control dominator to LOW
				TraceNode controlDom = node.getControlDominator();
				VarValue controlDomValue = this.getControlDomValue(controlDom);
				constraints.add(this.genPriorConstraint(controlDomValue, Configs.LOW));
			}
		}
		
//		MessageProcessor msgProcessor = new MessageProcessor();
//		System.out.println("Variable Encoder: Generating prior constraints");
//		for (Constraint constraint : constraints) {
//			System.out.println(constraint);
//		}
//		String graphMsg = msgProcessor.buildGraphMsg(constraints);
//		String factorMsg = msgProcessor.buildFactorMsg(constraints);
//		
//		System.out.println("graphMsg: " + graphMsg);
//		System.out.println("factorMsg: " + factorMsg);
		
		return constraints;
	}
}
