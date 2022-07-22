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

public class VariableEncoder extends Encoder {
	
	private List<VarValue> inputVars;
	private List<VarValue> outputVars;
	
	private PropagationCalculator propCalculator;
	
	private List<NodeFeedbackPair> userFeedbacks;
	
	public VariableEncoder(Trace trace, List<TraceNode> executionList, List<VarValue> inputVars, List<VarValue> outputVars) {
		super(trace, executionList);
		
		this.inputVars = inputVars;
		this.outputVars = outputVars;
		
		this.propCalculator = new PropagationCalculator();
		
		this.userFeedbacks = new ArrayList<>();
	}
	
	public void encode() {
		List<Constraint> constraints = this.genConstraints();
		
		FactorGraphClient client = new FactorGraphClient();
		MessageProcessor msgProcessor = new MessageProcessor();
		
		String graphMsg = msgProcessor.buildGraphMsg(constraints);
		String factorMsg = msgProcessor.buildFactorMsg(constraints);
		
		try {
			String response = client.requestBP(graphMsg, factorMsg);
			System.out.println("response: " + response);
			
			Map<String, Double> varsProb = msgProcessor.recieveMsg(response);
			for (Map.Entry<String, Double> pair : varsProb.entrySet()) {
				String predID = pair.getKey();
				Double prob = pair.getValue();
				
				if (Constraint.isControlDomID(predID)) {
					int nodeOrder = Constraint.extractNodeOrderFromCDID(predID);
					TraceNode node = this.trace.getTraceNode(nodeOrder);
					node.setProbability(prob);
				} else {
					for (VarValue var : this.getVarByID(predID)) {
						var.setProbability(prob);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error when communicating with server");
			e.printStackTrace();
		} finally {
			client.disconnectServer();
		}
	}
	
	public void setFeedbacks(List<NodeFeedbackPair> userFeedbacks) {
		this.userFeedbacks = userFeedbacks;
	}
	
	protected List<Constraint> genConstraints() {
		List<Constraint> constraints = new ArrayList<>();
		for (TraceNode node : this.executionList) {
			constraints.addAll(this.genVarConstraints(node));
		}
		constraints.addAll(this.genPriorConstraints());
		return constraints;
	}
	
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
		
		// A2 Constraint
		for (int readIdx=0; readIdx<readLen; readIdx++) {
			BitRepresentation varsIncluded = new BitRepresentation(totalLen);
			varsIncluded.set(0, totalLen);
			
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
		
		return constraints;
	}
	
	private List<Constraint> genPriorConstraints() {
		List<Constraint> constraints = new ArrayList<>();
		
		final int totalLen = 1;
		
		// Set input to HIGH
		for (VarValue inputVar : this.inputVars) {
			BitRepresentation varsIncluded = new BitRepresentation(totalLen);
			varsIncluded.set(0);
			
			Constraint constraint = new PriorConstraint(varsIncluded, 0, Configs.HIGH);
			constraint.addReadVarID(inputVar.getVarID());

			constraints.add(constraint);
		}
		
		// Set output to LOW
		for (VarValue outputVar : this.outputVars) {
			BitRepresentation varsIncluded = new BitRepresentation(totalLen);
			varsIncluded.set(0);
			
			Constraint constraint = new PriorConstraint(varsIncluded, 0, Configs.LOW);
			constraint.addWriteVarID(outputVar.getVarID());
			
			constraints.add(constraint);
		}
		
		// Convert user feedbacks to constraints
		for (NodeFeedbackPair feedbackPair : this.userFeedbacks) {
			System.out.println("Have feedbacks ------------------");
			TraceNode node = feedbackPair.getNode();
			UserFeedback feedback = feedbackPair.getFeedback();
			
			if (feedback.getFeedbackType() == UserFeedback.CORRECT) {
				// Add constraint to all read and write variable to HIGH
				for (VarValue readVar : node.getReadVariables()) {
					BitRepresentation varsIncluded = new BitRepresentation(totalLen);
					varsIncluded.set(0);
					
					Constraint constraint = new PriorConstraint(varsIncluded, 0, Configs.HIGH);
					constraint.addReadVarID(readVar.getVarID());
					
					constraints.add(constraint);
				}
				for (VarValue writeVar : node.getWrittenVariables()) {
					BitRepresentation varsIncluded = new BitRepresentation(totalLen);
					varsIncluded.set(0);
					
					Constraint constraint = new PriorConstraint(varsIncluded, 0, Configs.HIGH);
					constraint.addWriteVarID(writeVar.getVarID());
					
					constraints.add(constraint);
				}
			} else if (feedback.getFeedbackType() == UserFeedback.WRONG_VARIABLE_VALUE) {
				// Add constraint to target variable to LOW
				for (VarValue wrongVar : feedback.getOption().getIncludedWrongVars()) {
					BitRepresentation varsIncluded = new BitRepresentation(totalLen);
					varsIncluded.set(0);
					
					Constraint constraint = new PriorConstraint(varsIncluded, 0, Configs.LOW);
					constraint.addReadVarID(wrongVar.getVarID());
					
					constraints.add(constraint);
				}
			} else if (feedback.getFeedbackType() == UserFeedback.WRONG_PATH) {
				// Add constraint to control dominator to LOW
				TraceNode controlDom = node.getControlDominator();
				
				BitRepresentation varsIncluded = new BitRepresentation(totalLen);
				varsIncluded.set(0);
				
				Constraint constraint = new PriorConstraint(varsIncluded, 0, Configs.LOW);
				constraint.setControlDomOrder(controlDom.getOrder());
				
				constraints.add(constraint);
			}
		}
		
		return constraints;
	}
}
