package microbat.baseline.beliefpropagation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import microbat.baseline.constraints.BitRepresentation;
import microbat.baseline.constraints.PropagationProbability;
import microbat.baseline.constraints.Constraint;
import microbat.baseline.constraints.PriorConstraint;
import microbat.baseline.constraints.StatementConstraint;
import microbat.baseline.constraints.StatementConstraintA1;
import microbat.baseline.constraints.StatementConstraintA2;
import microbat.baseline.constraints.StatementConstraintA3;
import microbat.baseline.constraints.StatementConstraintA4;
import microbat.baseline.constraints.StatementConstraintA5;
import microbat.baseline.constraints.StatementConstraintA6;
import microbat.baseline.constraints.VariableConstraint;
import microbat.baseline.factorgraph.FactorGraphClient;
import microbat.baseline.factorgraph.MessageProcessor;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;

public class StatementEncoderFG extends Encoder {
	
	public StatementEncoderFG(Trace trace, List<TraceNode> executionList) {
		super(trace, executionList);
	}

	@Override
	public void encode() {
		
		FactorGraphClient client = new FactorGraphClient();
		MessageProcessor msgProcessor = new MessageProcessor();
		
		try {
			for (TraceNode node : this.executionList) {
				if (this.isSkippable(node)) {
					continue;
				}
				
				List<Constraint> constraints = new ArrayList<>();
				constraints.addAll(this.genVarToStatConstraints(node));
				constraints.addAll(this.genPriorConstraints(node));
				
				String graphMsg = msgProcessor.buildGraphMsg(constraints);
				String factorMsg = msgProcessor.buildFactorMsg(constraints);
				
				client.conntectServer();
				String response = client.requestBP(graphMsg, factorMsg);
				
				Map<String, Double> varsProb = msgProcessor.recieveMsg(response);
				for (Map.Entry<String, Double> pair : varsProb.entrySet()) {
					String predID = pair.getKey();
					Double prob = pair.getValue();
					if (StatementConstraint.isStatID(predID)) {
						int nodeOrder = StatementConstraint.extractStatOrderFromID(predID);
						if (nodeOrder == node.getOrder()) {
							node.setProbability(prob);
						}
					}
				}
				client.disconnectServer();
			}
		} catch (Exception e) {
			e.printStackTrace();
			client.disconnectServer();
		} finally {
//			client.disconnectServer();
		}
	}
	
	/**
	 * Generate statement constraints
	 * @param node Target node
	 * @return List of statement constraints
	 */
	protected List<Constraint> genVarToStatConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();

		if (this.isSkippable(node)) {
			return constraints;
		}
		
		final int readLen = Constraint.countReadVars(node);
		final int writeLen = Constraint.countWrittenVars(node);
		
		/**
		 * There are three possible case:
		 * 1. Have both read and written variables
		 * 2. Only have either read or written variables
		 * 3. Do not have any variables but only control dominator
		 */
		
		if ((readLen == 0 && writeLen == 0)) { // 3rd case
			Constraint constraint = new StatementConstraintA6(node, PropagationProbability.HIGH);
			constraints.add(constraint);
		} else if ((readLen == 0 && writeLen != 0) ||(readLen != 0 && writeLen == 0)) { // 2nd case

			for (VarValue readVar : node.getReadVariables()) {
				Constraint constrainA4 = new StatementConstraintA4(node, readVar, PropagationProbability.HIGH);
				constraints.add(constrainA4);
				Constraint constraintA5 = new StatementConstraintA5(node, readVar, PropagationProbability.HIGH);
				constraints.add(constraintA5);
			}
			
			for (VarValue writeVar : node.getWrittenVariables()) {
				Constraint constrainA4 = new StatementConstraintA4(node, writeVar, PropagationProbability.HIGH);
				constraints.add(constrainA4);
				Constraint constraintA5 = new StatementConstraintA5(node, writeVar, PropagationProbability.HIGH);
				constraints.add(constraintA5);
			}
			
		} else { // 1st case

			Constraint constraintA1 = new StatementConstraintA1(node, PropagationProbability.HIGH);
			constraints.add(constraintA1);
			
			Constraint constraintA2 = new StatementConstraintA2(node, PropagationProbability.HIGH);
			constraints.add(constraintA2);
			
			Constraint constraintA3 = new StatementConstraintA3(node, PropagationProbability.HIGH);
			constraints.add(constraintA3);
		}
		
		return constraints;
	}
	
	/**
	 * Generate prior constraints
	 * @param node Target node
	 * @return List of piror constraints
	 */
	protected List<Constraint> genPriorConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		
		for (VarValue readVar : node.getReadVariables()) {
			Constraint constraint = new PriorConstraint(readVar, readVar.getProbability());
			constraints.add(constraint);
		}
		
		for (VarValue writeVar : node.getWrittenVariables()) {
			Constraint constraint = new PriorConstraint(writeVar, writeVar.getProbability());
			constraints.add(constraint);
		}
		
		TraceNode controlDom = node.getControlDominator();
		if (controlDom != null) {
			VarValue controlDomValue = Constraint.extractControlDomVar(controlDom);
			Constraint constraint = new PriorConstraint(controlDomValue, controlDomValue.getProbability());
			constraints.add(constraint);
		}
		
		return constraints;
	}
}
