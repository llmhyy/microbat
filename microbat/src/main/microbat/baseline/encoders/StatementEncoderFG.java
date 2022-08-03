package microbat.baseline.encoders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import microbat.baseline.BitRepresentation;
import microbat.baseline.Configs;
import microbat.baseline.constraints.Constraint;
import microbat.baseline.constraints.PriorConstraint;
import microbat.baseline.constraints.StatementConstraint;
import microbat.baseline.constraints.StatementConstraintA1;
import microbat.baseline.constraints.StatementConstraintA2;
import microbat.baseline.constraints.StatementConstraintA3;
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
		List<Constraint> constraints = new ArrayList<>();
		for (TraceNode node : this.executionList) {
			
//			if (this.countReadVars(node) == 0 || this.countWriteVars(node) == 0) {
//				this.handleNonConstraintNode(node);
//				continue;
//			}
			
			if (this.isSkippable(node)) {
				continue;
			}
			
//			final int totalLen = this.countPredicates(node);
//			if (totalLen == 2) {
//				if (this.countWriteVars(node) == 1) {
//					for (VarValue writeVar : node.getWrittenVariables()) {
//						node.setProbability(writeVar.getProbability());
//					}
//				} else if (this.countReadVars(node) == 1) {
//					for (VarValue readVar : node.getWrittenVariables()) {
//						node.setProbability(readVar.getProbability());
//					}
//				}
//				continue;
//			}
			
			constraints.addAll(this.genVarToStatConstraints(node));
			constraints.addAll(this.genPriorConstraints(node));
		}
		
		FactorGraphClient client = new FactorGraphClient();
		MessageProcessor msgProcessor = new MessageProcessor();
		
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
					TraceNode node = this.trace.getTraceNode(nodeOrder);
					node.setProbability(prob);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			client.disconnectServer();
		}
	}

	@Override
	protected int countPredicates(TraceNode node) {
		return super.countPredicates(node) + 1;
	}
	
	protected List<Constraint> genConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		constraints.addAll(this.genVarToStatConstraints(node));
		constraints.addAll(this.genPriorConstraints(node));
		return constraints;
	}
	
	private void handleNonConstraintNode(TraceNode node) {
		List<VarValue> vars = this.countReadVars(node) == 0 ? node.getWrittenVariables() : node.getReadVariables();
		double avgProb = 0;
		for (VarValue var : vars) {
			avgProb += var.getProbability();
		}
		avgProb /= vars.size();
		
		node.setProbability(avgProb);
	}
	
	private List<Constraint> genVarToStatConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		
		if (this.isSkippable(node)) {
			return constraints;
		}
		
		final int readLen = this.countReadVars(node);
		final int writeLen = this.countWriteVars(node);
		final int totalLen = this.countPredicates(node);
		
		TraceNode controlDom = node.getControlDominator();
		final boolean haveControlDom = controlDom != null;
		
		final int statementOrder = node.getOrder();
//		final int controlDomOrder = haveControlDom ? controlDom.getOrder() : Constraint.NaN;
		String controlDomID = "";
		if (haveControlDom) {
			VarValue controlDomValue = this.getControlDomValue(controlDom);
			controlDomID = controlDomValue.getVarID();
		}
		
		final int writeStartIdx = readLen == 0 ? 0 : readLen;
		final int predIdx = haveControlDom ? totalLen - 2 : -1;
		final int conclusionIdx = totalLen - 1;
		
		// Constraint A1, A2, A3 include the same variable
		BitRepresentation variableIncluded = new BitRepresentation(totalLen);
		variableIncluded.set(0, readLen + writeLen);
		
		if (haveControlDom) {
			variableIncluded.set(predIdx);
		}
		variableIncluded.set(conclusionIdx);
		
		// Constraint A1
		// Variable to statement constraint A1
		Constraint constraintA1 = new StatementConstraintA1(variableIncluded, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
		constraintA1.setVarsID(node);
		constraints.add(constraintA1);
		
		// Variable to statement constraint A2
		Constraint constraintA2 = new StatementConstraintA2(variableIncluded, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
		constraintA2.setVarsID(node);
		constraints.add(constraintA2);
		
		// Variable to statement constraint A3
		Constraint constraintA3 = new StatementConstraintA3(variableIncluded, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
		constraintA3.setVarsID(node);
		constraints.add(constraintA3);
		
		return constraints;
	}
	
	private List<Constraint> genPriorConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		
		final int totalLen = 1;
		
		for (VarValue readVar : node.getReadVariables()) {
			BitRepresentation varsIncluded = new BitRepresentation(totalLen);
			varsIncluded.set(0);
			
			Constraint constraint = new PriorConstraint(varsIncluded, 0, readVar.getProbability());
			constraint.addReadVarID(readVar.getVarID());
			
			constraints.add(constraint);
		}
		
		for (VarValue writeVar : node.getWrittenVariables()) {
			BitRepresentation varsIncluded = new BitRepresentation(totalLen);
			varsIncluded.set(0);
			
			Constraint constraint = new PriorConstraint(varsIncluded, 0, writeVar.getProbability());
			constraint.addWriteVarID(writeVar.getVarID());
			
			constraints.add(constraint);
		}
		
		TraceNode controlDom = node.getControlDominator();
		if (controlDom != null) {
			BitRepresentation varsIncluded = new BitRepresentation(totalLen);
			varsIncluded.set(0);
			
			VarValue controlDomValue = this.getControlDomValue(controlDom);
			
			Constraint constraint = new PriorConstraint(varsIncluded, 0, controlDomValue.getProbability());
//			constraint.setControlDomOrder(controlDom.getOrder());
			constraint.setControlDomID(controlDomValue.getVarID());
			constraints.add(constraint);
		}
		
		return constraints;
	}
}
