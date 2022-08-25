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

public class StatementEncoderFG extends StatementEncoder {
	
	public StatementEncoderFG(Trace trace, List<TraceNode> executionList) {
		super(trace, executionList);
	}

	@Override
	public void encode() {
		List<Constraint> constraints = new ArrayList<>();
		for (TraceNode node : this.executionList) {

			if (this.isSkippable(node)) {
				continue;
			}

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
	protected List<Constraint> genPriorConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		
		final int totalLen = 1;
		
		for (VarValue readVar : node.getReadVariables()) {
			constraints.add(this.genPriorConstraint(readVar, readVar.getProbability()));
		}
		
		for (VarValue writeVar : node.getWrittenVariables()) {
			constraints.add(this.genPriorConstraint(writeVar, writeVar.getProbability()));
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
