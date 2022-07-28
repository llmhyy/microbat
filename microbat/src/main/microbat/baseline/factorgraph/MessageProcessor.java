package microbat.baseline.factorgraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.baseline.constraints.Constraint;

public class MessageProcessor {
	
	private final String DELIMITER_1;
	private final String DELIMITER_2;
	
	public MessageProcessor() {
		this(",", "&");
	}
	
	public MessageProcessor(String delimiter_1, String delimiter_2) {
		this.DELIMITER_1 = delimiter_1;
		this.DELIMITER_2 = delimiter_2;
	}
	
	public String getDelimieter_1() {
		return this.DELIMITER_1;
	}
	
	public String getDelimiter_2() {
		return this.DELIMITER_2;
	}
	
	public String buildGraphMsg(List<Constraint> constraints) {
		StringBuilder strBuilder = new StringBuilder();
		
		VarIDConverter IDConverter = new VarIDConverter();
		
		for (Constraint constraint : constraints) {
			strBuilder.append(constraint.getConstraintID());
			strBuilder.append("(");
			
			for (String predID : constraint.getInvolvedPredIDs()) {
				String convertedID = IDConverter.varID2GraphID(predID);
				strBuilder.append(convertedID);
				strBuilder.append(this.DELIMITER_1);
			}
			
			strBuilder.deleteCharAt(strBuilder.length()-1);
			strBuilder.append(")");
		}
		return strBuilder.toString();
	}
	
	public String buildFactorMsg(List<Constraint> constraints) {
		StringBuilder strBuilder = new StringBuilder();
		
		VarIDConverter IDConverter = new VarIDConverter();
		
		for (Constraint constraint : constraints) {
			strBuilder.append(constraint.getConstraintID());
			strBuilder.append(this.DELIMITER_2);
			
			for (String predID : constraint.getInvolvedPredIDs()) {
				String convertedID = IDConverter.varID2GraphID(predID);
				strBuilder.append(convertedID);
				strBuilder.append(this.DELIMITER_1);
			}
			
			strBuilder.deleteCharAt(strBuilder.length()-1);
			strBuilder.append(this.DELIMITER_2);
			
			final int totalLen = constraint.getPredicateCount();
			final int maxCase = 1 << totalLen;
			
			for (int caseNo=0; caseNo < maxCase; caseNo++) {
				double prob = constraint.getProbability(caseNo);
				strBuilder.append(prob);
				strBuilder.append(this.DELIMITER_1);
			}
			
			strBuilder.deleteCharAt(strBuilder.length()-1);
			strBuilder.append(this.DELIMITER_2);
		}
		
		strBuilder.deleteCharAt(strBuilder.length()-1);
		return strBuilder.toString();
	}
	
	public Map<String, Double> recieveMsg(final String msg) {
		Map<String, Double> varsProb = new HashMap<>();
		
		VarIDConverter IDConverter = new VarIDConverter();
		
		String[] tokens = msg.split(this.DELIMITER_2);
		for (String token : tokens) {
			String[] str_pair = token.split(this.DELIMITER_1);
			
			String predID = IDConverter.graphID2VarID(str_pair[0]);
			Double prob = Double.valueOf(str_pair[1]);
			
			varsProb.put(predID, prob);
		}
		
		return varsProb;
	}
}
