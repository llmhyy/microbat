package microbat.model.trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariableDefinitions {
	public static final int USE_FIRST = 1;
	public static final int USE_LAST = 2;
	
	/**
	 * the trace node list for each variable is ordered.
	 */
	private Map<String, List<TraceNode>> nodeDefiningVariableMap = new HashMap<>();

	public void put(String varID, TraceNode currentNode) {
		List<TraceNode> list = nodeDefiningVariableMap.get(varID);
		if(list==null){
			list = new ArrayList<>();
		}
		
		if(!list.contains(currentNode)){
			list.add(currentNode);
		}
		nodeDefiningVariableMap.put(varID, list);
		
		Collections.sort(list, new Comparator<TraceNode>(){

			@Override
			public int compare(TraceNode o1, TraceNode o2) {
				return o1.getOrder() - o2.getOrder();
			}
		});
	}

	public TraceNode get(String varID, TraceNode currentNode, int defStepSelection) {
		List<TraceNode> list = nodeDefiningVariableMap.get(varID);
		if(list==null || list.isEmpty()){
			return null;			
		}
		
		if(defStepSelection==VariableDefinitions.USE_FIRST){
			return list.get(0);
		}
		
		TraceNode prev = null;
		for(TraceNode node: list){
			if(node.getOrder() < currentNode.getOrder()){
				prev = node;
			}
			else{
				return prev;
			}
		}
		
		return prev;
	}
}
