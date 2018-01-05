package microbat.recommendation.conflicts;

import java.util.ArrayList;
import java.util.List;

import microbat.model.trace.StepVariableRelationEntry;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.util.Settings;

public class DominateeConflictRule3 extends ConflictRule {
	/**
	 * The steps will always be marked as "correct" in the process of debugging, otherwise, the debugging process is finished.
	 * 
	 * Given the order of a trace node. If all the read variables of this node (step) is incorrect. Thus, all of the 
	 * dominator trace nodes of this node is read-variable-incorrect.
	 * <br><br>
	 * If the above is not the case, then a conflict happen.
	 * 
	 * This method will return the node causing the conflicts if there is a conflict indeed.
	 * 
	 * @param order
	 * @return
	 */
	@Override
	public TraceNode checkConflicts(Trace trace, int order) {
		TraceNode node = trace.getExecutionList().get(order-1);
		if(node.getReadVarCorrectness(Settings.interestedVariables, false)==TraceNode.READ_VARS_INCORRECT){
			if(node.getDataDominatee().keySet().isEmpty()){
				return null;
			}
			
			List<TraceNode> consumerList = new ArrayList<>();
			for(VarValue var: node.getWrittenVariables()){
				String varID = var.getVarID();
				StepVariableRelationEntry entry = trace.getStepVariableTable().get(varID);
				
				for(TraceNode consumer: entry.getConsumers()){
					if(consumer.getReadVarCorrectness(Settings.interestedVariables, false)==TraceNode.READ_VARS_CORRECT){
						consumerList.add(consumer);
					}
				}
			}
			
			if(!consumerList.isEmpty()){
				TraceNode jumpNode = findOldestConflictNode(consumerList);
				return jumpNode;
			}
			else{
				return null;
			}
			
		}

		return null;
	}

}
