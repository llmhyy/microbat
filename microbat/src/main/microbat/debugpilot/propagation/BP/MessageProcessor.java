package microbat.debugpilot.propagation.BP;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.debugpilot.propagation.BP.constraint.Constraint;
import microbat.debugpilot.propagation.BP.constraint.StatementConstraint;

/**
 * MessageProcessor are used to encode/decode the messages
 * send to / receive from the python server
 * 
 * Since we are going to send the message of all constraints
 * once to the python server, it will be better to create
 * a message processor to handle the message content.
 * @author David
 *
 */
public class MessageProcessor {
	
	private final String DELIMITER_1;
	private final String DELIMITER_2;
	private final String MUL_SIGN;
	
	public MessageProcessor() {
		this(",", "&", "*");
	}
	
	public MessageProcessor(String delimiter_1, String delimiter_2, String mul_sign) {
		this.DELIMITER_1 = delimiter_1;
		this.DELIMITER_2 = delimiter_2;
		this.MUL_SIGN = mul_sign;
	}
	
	public String getDelimieter_1() {
		return this.DELIMITER_1;
	}
	
	public String getDelimiter_2() {
		return this.DELIMITER_2;
	}
	
	/**
	 * Generate graph structure message.
	 * It contain the information of connection
	 * between each variable node and factor node.
	 * 
	 * @param constraints List of constraints
	 * @return Graph structure message
	 */
	public String buildGraphMsg(List<Constraint> constraints) {
		StringBuilder strBuilder = new StringBuilder();
		
		VarIDConverter IDConverter = new VarIDConverter();
		
		for (Constraint constraint : constraints) {
			
			strBuilder.append(constraint.getConstraintID());
			strBuilder.append("(");
			
			for (String predID : constraint.getInvolvedPredIDs()) {
				// We need to convert the variable ID to graph node ID
				String convertedID = IDConverter.varID2GraphID(predID);
				strBuilder.append(convertedID);
				strBuilder.append(this.DELIMITER_1);
			}
			
			// Remove the last delimiter
			strBuilder.deleteCharAt(strBuilder.length()-1);
			strBuilder.append(")");
		}
		return strBuilder.toString();
	}
	
	/**
	 * Generate factor message.
	 * 
	 * It contain the probability information of each factor node
	 * @param constraints List of constraints
	 * @return Factor message
	 */
	public String buildFactorMsg(List<Constraint> constraints) {
		StringBuilder strBuilder = new StringBuilder();
		
		VarIDConverter IDConverter = new VarIDConverter();
		
		for (Constraint constraint : constraints) {
			
			strBuilder.append(constraint.getOrder());
			strBuilder.append(this.DELIMITER_2);
			strBuilder.append(constraint.getConstraintID());
			strBuilder.append(this.DELIMITER_2);

			for (String predID : constraint.getInvolvedPredIDs()) {
				String convertedID = IDConverter.varID2GraphID(predID);
				strBuilder.append(convertedID);
				strBuilder.append(this.DELIMITER_1);
			}
			// Remove the last delimiter
			strBuilder.deleteCharAt(strBuilder.length()-1);
			strBuilder.append(this.DELIMITER_2);
			
			final int maxCase = constraint.getMaxCaseNo();
			
			double prevProb = constraint.getProbability(0);
			int count = 1;
			for (int caseNo=1; caseNo < maxCase; caseNo++) {
				double prob = constraint.getProbability(caseNo);
				if (prob == prevProb) {
					count += 1;
				} else {
					strBuilder.append(String.format("%.2f", prevProb));
					strBuilder.append(this.MUL_SIGN);
					strBuilder.append(count);
					strBuilder.append(this.DELIMITER_1);
					prevProb = prob;
					count = 1;
				}
			}
			
			strBuilder.append(String.format("%.2f", prevProb));
			strBuilder.append(this.MUL_SIGN);
			strBuilder.append(count);
			// Remove the last delimiter
//			strBuilder.deleteCharAt(strBuilder.length()-1);
			
			strBuilder.append(this.DELIMITER_2);
			
			
		}
		
		// Remove the last delimiter
		strBuilder.deleteCharAt(strBuilder.length()-1);
		return strBuilder.toString();
	}
	
	/**
	 * Decode the message received from python server
	 * 
	 * The message contain the calculate marginal probability
	 * of variable. This message will convert the message into
	 * mapping between variable ID and probability
	 * 
	 * @param msg Message received from python server 
	 * @return Mapping between variable ID and probability
	 */
	public Map<String, Double> recieveMsg(final String msg) {
		Map<String, Double> varsProb = new HashMap<>();
		
		VarIDConverter IDConverter = new VarIDConverter();
		
		String[] tokens = msg.split(this.DELIMITER_2);
		for (String token : tokens) {
			String[] str_pair = token.split(this.DELIMITER_1);
			
			// Convert the graph node ID back to variable ID
			String predID = IDConverter.graphID2VarID(str_pair[0]);
			Double prob = Double.valueOf(str_pair[1]);
			
			varsProb.put(predID, prob);
		}
		
		return varsProb;
	}
}
