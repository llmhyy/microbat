package microbat.debugpilot.propagation.BP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import microbat.debugpilot.propagation.BP.constraint.BitRepresentation;
import microbat.debugpilot.propagation.BP.constraint.Constraint;
import microbat.debugpilot.propagation.BP.constraint.PriorConstraint;
import microbat.debugpilot.propagation.BP.constraint.StatementConstraint;
import microbat.debugpilot.propagation.BP.constraint.StatementConstraintA1;
import microbat.debugpilot.propagation.BP.constraint.StatementConstraintA2;
import microbat.debugpilot.propagation.BP.constraint.StatementConstraintA3;
import microbat.debugpilot.propagation.BP.constraint.StatementConstraintA4;
import microbat.debugpilot.propagation.BP.constraint.StatementConstraintA5;
import microbat.debugpilot.propagation.BP.constraint.StatementConstraintA6;
import microbat.debugpilot.propagation.BP.constraint.VariableConstraint;
import microbat.debugpilot.propagation.probability.PropProbability;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;

/**
 * StatementEncoderFG is used to calculated the 
 * correctness probability for the statement
 * 
 * It will use the belief propagation approach to
 * speed up the process
 * 
 * If error occur when calculating the probability
 * of particular statement, it will set the probability
 * to 2.0
 * 
 * @author David
 *
 */
public class StatementEncoderFG extends Encoder {
	
	public StatementEncoderFG(Trace trace, List<TraceNode> executionList) {
		super(trace, executionList);
	}

	@Override
	public void encode() {
		
		BeliefPropagationClient client = new BeliefPropagationClient();
		MessageProcessor msgProcessor = new MessageProcessor();
		
		for (TraceNode node : this.executionList) {
			if (this.isSkippable(node)) {
				continue;
			}
			
			List<Constraint> constraints = new ArrayList<>();
			constraints.addAll(this.genVarToStatConstraints(node));
			constraints.addAll(this.genPriorConstraints(node));
			
			String graphMsg = msgProcessor.buildGraphMsg(constraints);
			String factorMsg = msgProcessor.buildFactorMsg(constraints);

			try {
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
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException("[Statement Encoder]: Error occur when calculating statement probability");
			}


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
			Constraint constraint = new StatementConstraintA6(node, PropProbability.HIGH);
			constraints.add(constraint);
		} else if ((readLen == 0 && writeLen != 0) ||(readLen != 0 && writeLen == 0)) { // 2nd case

			for (VarValue readVar : node.getReadVariables()) {
				Constraint constrainA4 = new StatementConstraintA4(node, readVar, PropProbability.HIGH);
				constraints.add(constrainA4);
				Constraint constraintA5 = new StatementConstraintA5(node, readVar, PropProbability.HIGH);
				constraints.add(constraintA5);
			}
			
			for (VarValue writeVar : node.getWrittenVariables()) {
				Constraint constrainA4 = new StatementConstraintA4(node, writeVar, PropProbability.HIGH);
				constraints.add(constrainA4);
				Constraint constraintA5 = new StatementConstraintA5(node, writeVar, PropProbability.HIGH);
				constraints.add(constraintA5);
			}
			
		} else { // 1st case

			Constraint constraintA1 = new StatementConstraintA1(node, PropProbability.HIGH);
			constraints.add(constraintA1);
			
			Constraint constraintA2 = new StatementConstraintA2(node, PropProbability.HIGH);
			constraints.add(constraintA2);
			
			Constraint constraintA3 = new StatementConstraintA3(node, PropProbability.HIGH);
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
	
	@Override
	protected boolean isSkippable(TraceNode node) {
		if (super.isSkippable(node)) {
			return true;
		}
		
		// Statement encoder will not encode the last node
		// which is assumed to be error node
		if (node.equals(this.executionList.get(this.executionList.size()-1))) {
			return true;
		}
		
		return false;
	}
}
