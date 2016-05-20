package microbat.recommendation.conflicts;

import java.util.ArrayList;
import java.util.List;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public class ConflictRuleChecker {
	
	List<ConflictRule> rules = new ArrayList<>();
	
	public ConflictRuleChecker(){
		rules.add(new DominatorConflictRule1());
		rules.add(new DominatorConflictRule2());
		rules.add(new DominateeConflictRule3());
	}
	
	public TraceNode checkConflicts(Trace trace, int order){
		TraceNode node = null;
		
		for(ConflictRule rule: rules){
			node = rule.checkConflicts(trace, order);
			if(node != null){
				return node;
			}
			
		}

		return null;
	}
	
//	/**
//	 * The steps will always be marked as "correct" in the process of debugging, otherwise, the debugging process is finished.
//	 * 
//	 * Given the order of a trace node. If all the read variables of this node (step) is incorrect. Thus, at least one of the 
//	 * dominator trace nodes of this node is read-variable-incorrect.
//	 * <br><br>
//	 * If the above is not the case, then a conflict happen.
//	 * 
//	 * This method will return the node causing the conflicts if there is a conflict indeed.
//	 * 
//	 * @param order
//	 * @return
//	 */
//	public TraceNode checkDominatorConflictRule1(Trace trace, int order) {
//		TraceNode node = trace.getExectionList().get(order-1);
//		assert node.getVarsCorrectness()==TraceNode.READVARS_INCORRECT;
//
//		if(node.getDominator().keySet().isEmpty()){
//			return null;
//		}
//		
//		List<TraceNode> producerList = new ArrayList<>();
//		boolean isConflict = true;
//		
//		for(VarValue var: node.getReadVariables()){
//			String varID = var.getVarID();
//			StepVariableRelationEntry entry = trace.getStepVariableTable().get(varID);
//			
//			if(!entry.getProducers().isEmpty()){
//				TraceNode producer = entry.getProducers().get(0);
//				producerList.add(producer);
//				
//				if(producer.getVarsCorrectness()==TraceNode.READVARS_UNKNOWN){
//					return null;
//				}
//				else{
//					isConflict = isConflict && producer.getVarsCorrectness()==TraceNode.READVARS_CORRECT;
//				}
//				
//				if(!isConflict){
//					return null;
//				}
//			}
//		}
//		
//		TraceNode jumpNode = findOldestConflictNode(producerList);
//		
//		return jumpNode;
//	}
//	
//	/**
//	 * The steps will always be marked as "correct" in the process of debugging, otherwise, the debugging process is finished.
//	 * 
//	 * Given the order of a trace node. If all the read variables of this node (step) is correct. Thus, all of the 
//	 * dominator trace nodes of this node is read-variable-correct.
//	 * <br><br>
//	 * If the above is not the case, then a conflict happen.
//	 * 
//	 * This method will return the node causing the conflicts if there is a conflict indeed.
//	 * 
//	 * @param order
//	 * @return
//	 */
//	public TraceNode checkDominatorConflictRule2(Trace trace, int order) {
//		TraceNode node = trace.getExectionList().get(order-1);
//		assert node.getVarsCorrectness()==TraceNode.READVARS_CORRECT;
//
//		if(node.getDominator().keySet().isEmpty()){
//			return null;
//		}
//		
//		List<TraceNode> producerList = new ArrayList<>();
//		for(VarValue var: node.getReadVariables()){
//			String varID = var.getVarID();
//			StepVariableRelationEntry entry = trace.getStepVariableTable().get(varID);
//			
//			if(!entry.getProducers().isEmpty()){
//				TraceNode producer = entry.getProducers().get(0);
//				if(producer.getVarsCorrectness()==TraceNode.READVARS_INCORRECT){
//					producerList.add(producer);
//				}
//			}
//		}
//		
//		if(!producerList.isEmpty()){
//			TraceNode jumpNode = findOldestConflictNode(producerList);
//			return jumpNode;
//		}
//		else{
//			return null;
//		}
//	}
//	
//	/**
//	 * The steps will always be marked as "correct" in the process of debugging, otherwise, the debugging process is finished.
//	 * 
//	 * Given the order of a trace node. If all the read variables of this node (step) is incorrect. Thus, all of the 
//	 * dominator trace nodes of this node is read-variable-incorrect.
//	 * <br><br>
//	 * If the above is not the case, then a conflict happen.
//	 * 
//	 * This method will return the node causing the conflicts if there is a conflict indeed.
//	 * 
//	 * @param order
//	 * @return
//	 */
//	public TraceNode checkDominateeConflictRule3(Trace trace, int order) {
//		TraceNode node = trace.getExectionList().get(order-1);
//		assert node.getVarsCorrectness()==TraceNode.READVARS_INCORRECT;
//
//		if(node.getDominatee().keySet().isEmpty()){
//			return null;
//		}
//		
//		List<TraceNode> consumerList = new ArrayList<>();
//		for(VarValue var: node.getReadVariables()){
//			String varID = var.getVarID();
//			StepVariableRelationEntry entry = trace.getStepVariableTable().get(varID);
//			
//			for(TraceNode consumer: entry.getConsumers()){
//				if(consumer.getVarsCorrectness()==TraceNode.READVARS_CORRECT){
//					consumerList.add(consumer);
//				}
//			}
//		}
//		
//		if(!consumerList.isEmpty()){
//			TraceNode jumpNode = findOldestConflictNode(consumerList);
//			return jumpNode;
//		}
//		else{
//			return null;
//		}
//	}
}
