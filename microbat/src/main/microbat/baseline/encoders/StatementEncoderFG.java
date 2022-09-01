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
import microbat.baseline.constraints.StatementConstraintA4;
import microbat.baseline.constraints.StatementConstraintA5;
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
	
	@Override
	protected List<Constraint> genVarToStatConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		
		final int readLen = this.countReadVars(node);
		final int writeLen = this.countWriteVars(node);
		
		TraceNode controlDominator = node.getControlDominator();
		boolean haveControlDom = controlDominator != null;
		
		String controlDomID = haveControlDom ? this.getControlDomValue(controlDominator).getVarID() : "";
		
		final int statementOrder = node.getOrder();
		
		/*
		 * Consider the following special cases
		 * 1. Trace Node only have written variable
		 * 2. Trace Node only have read variable
		 * 3. Trace Node only have control dominator
		 * 
		 * Then the statement correctness will only
		 * depends on the predicate it has (only have
		 * one constraint)
		 * 
		 * Note that the case that node does not have anything
		 * does not exist, because this kind of node will not
		 * be included during the dynamic slicing
		 */
		if ((readLen == 0 && writeLen == 0)) { // 3rd case
			// Bit representation will be [0: controlDom, 1: statement]
			final int totalLen = 2;
			// Since there are no write variable, we set the starting index to be the end
			final int writeStartIdx = totalLen;
			final int conclusionIdx = totalLen - 1;
			BitRepresentation variableIncluded = new BitRepresentation(totalLen);
			variableIncluded.set(0, totalLen);
			Constraint constraintA1 = new StatementConstraintA1(variableIncluded, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
			constraints.add(constraintA1);
		} else if ((readLen == 0 && writeLen != 0) ||(readLen != 0 && writeLen == 0)) { // 1st and 2nd case
			// Bit representation will be [0: read/write, 1: controlDom, 2: statement]
			final int totalLen = haveControlDom ? 3 : 2;
			final int writeStartIdx = writeLen == 0 ? totalLen-1 : 0;
			final int conclusionIdx = totalLen - 1;
			
			for (int readIdx=0; readIdx<readLen; readIdx++) {
				VarValue readVar = node.getReadVariables().get(readIdx);
				BitRepresentation variableIncluded = new BitRepresentation(totalLen);
				variableIncluded.set(0, totalLen);
				
				Constraint constraintA4 = new StatementConstraintA4(variableIncluded, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
				constraintA4.addReadVarID(readVar.getVarID());
				constraintA4.setOrder(node.getOrder());
				constraints.add(constraintA4);
				
				Constraint constraintA5 = new StatementConstraintA5(variableIncluded, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
				constraintA5.addReadVarID(readVar.getVarID());
				constraintA5.setOrder(node.getOrder());
				constraints.add(constraintA5);
			}
			
			for (int writeIdx=0; writeIdx<writeLen; writeIdx++) {
				VarValue writeVar = node.getWrittenVariables().get(writeIdx);
				BitRepresentation variableIncluded = new BitRepresentation(totalLen);
				variableIncluded.set(0, totalLen);
				
				Constraint constraintA4 = new StatementConstraintA4(variableIncluded, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
				constraintA4.addWriteVarID(writeVar.getVarID());
				constraintA4.setOrder(node.getOrder());
				constraints.add(constraintA4);
				
				Constraint constraintA5 = new StatementConstraintA5(variableIncluded, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
				constraintA5.addWriteVarID(writeVar.getVarID());
				constraintA5.setOrder(node.getOrder());
				constraints.add(constraintA5);
			}
		} else {
			final int totalLen = this.countPredicates(node);
			final int writeStartIdx = readLen == 0 ? 0 : readLen;
			final int conclusionIdx = totalLen - 1;
			
			BitRepresentation variableIncluded = new BitRepresentation(totalLen);
			variableIncluded.set(0, totalLen);
			
			Constraint constraintA1 = new StatementConstraintA1(variableIncluded, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
			constraintA1.setVarsID(node);
			constraints.add(constraintA1);
			
			Constraint constraintA2 = new StatementConstraintA2(variableIncluded, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
			constraintA2.setVarsID(node);
			constraints.add(constraintA2);
			
			Constraint constraintA3 = new StatementConstraintA3(variableIncluded, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
			constraintA3.setVarsID(node);
			constraints.add(constraintA3);
		}
		
		return constraints;
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
	
	@Override
	protected List<Constraint> genSpecialCaseConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		
		final int readLen = this.countReadVars(node);
		final int writeLen = this.countWriteVars(node);
		int totalLen = this.countPredicates(node);
		
		// Check control dominator exist or not
		TraceNode controlDominator = node.getControlDominator();
		boolean haveControlDominator = controlDominator != null;
		
		// Index of statement in bit representation
		int conclusionIdx = totalLen - 1;

		BitRepresentation variableIncluded = new BitRepresentation(totalLen);
		variableIncluded.set(0, readLen + writeLen);	// Include all read and write variables
		if (haveControlDominator) {
			variableIncluded.set(totalLen - 2);	// Include control dominator if exist
		}
		variableIncluded.set(conclusionIdx);	// Include conclusion statement
		
		// Index of starting write variable in the bit representation
		int writeStartIdx = readLen == 0 ? 0 : readLen;
		
		// Order of current node
		final int statementOrder = node.getOrder();
		
		// Get the ID of control dominator variable if exists
		String controlDomID = "";
		if (haveControlDominator) {
			VarValue controlDomValue = this.getControlDomValue(controlDominator);
			controlDomID = controlDomValue.getVarID();
		}

//		Constraint constraintA1 = new StatementConstraintA1(variableIncluded, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
//		constraintA1.setVarsID(node);
//		constraints.add(constraintA1);
		
		totalLen = haveControlDominator ? 3 : 2;
		conclusionIdx = totalLen - 1;
		writeStartIdx = haveControlDominator ? totalLen - 2 : totalLen - 1;;
		
		for (int readIdx=0; readIdx<readLen; readIdx++) {
			BitRepresentation br = new BitRepresentation(totalLen);
			VarValue readVar = node.getReadVariables().get(readIdx);
			br.set(0);
			br.set(conclusionIdx);
			if (haveControlDominator) {
				br.set(totalLen-2);
			}
			
			Constraint constraintA4 = new StatementConstraintA4(br, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
			constraintA4.addReadVarID(readVar.getVarID());
			constraintA4.setOrder(statementOrder);
			constraints.add(constraintA4);
			
			Constraint constraintA5 = new StatementConstraintA5(br, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
			constraintA5.addReadVarID(readVar.getVarID());
			constraintA5.setOrder(statementOrder);
			constraints.add(constraintA5);
		}
		
		for (int offset=0; offset<writeLen; offset++) {
			final int writeIdx = readLen + offset;
			BitRepresentation br = new BitRepresentation(totalLen);
			VarValue writeVar = node.getWrittenVariables().get(writeIdx);
			br.set(0);
			br.set(conclusionIdx);
			if (haveControlDominator) {
				br.set(totalLen-2);
			}
			
			Constraint constraintA4 = new StatementConstraintA4(br, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
			constraintA4.addWriteVarID(writeVar.getVarID());
			constraintA4.setOrder(statementOrder);
			constraints.add(constraintA4);
			
			Constraint constraintA5 = new StatementConstraintA5(br, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
			constraintA5.addReadVarID(writeVar.getVarID());
			constraintA5.setOrder(statementOrder);
			constraints.add(constraintA5);
		}
		
		return constraints;
	}
}
