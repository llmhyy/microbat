package microbat.debugpilot.propagation.BP;

import java.util.HashMap;
import java.util.Map;

import microbat.model.value.VarValue;

/**
 * VarIDConverter handle the conversion between variable ID and graph node ID
 * 
 * The main reason of using VarIDConverter is that, some of the character used in the
 * variable ID was used as the splitting delimiter when building the factor
 * graph, so that we need to change those character when we are using factor graph.
 * 
 * Also, we need to convert those character back after using the factor graph in
 * order to maintain the consistence of ID.
 * 
 * @author David
 *
 */
public class VarIDConverter {
	
	private final Map<String, String> replacementMap;
	
	public VarIDConverter() {
		
		/*
		 * The splitting delimiter used in factor graph
		 * include ",", "(", ")".
		 */
		this.replacementMap = new HashMap<>();
		this.replacementMap.put(",", "@");
		this.replacementMap.put("(", "%");
		this.replacementMap.put(")", "^");
	}
	
	/**
	 * Convert variable ID to factor graph node ID 
	 * @param varID Variable ID to be converted
	 * @return Graph node ID
	 */
	public String varID2GraphID(String varID) {
		String finalStr = varID;
		for (Map.Entry<String, String> pair : this.replacementMap.entrySet()) {
			String origin = pair.getKey();
			String replacement = pair.getValue();
			finalStr = finalStr.replace(origin, replacement);
		}
		return finalStr;
	}
	
	/**
	 * Convert the graph node ID back to variable ID
	 * @param graphNodeID Graph node ID to be converted
	 * @return Variable ID
	 */
	public String graphID2VarID(String graphNodeID) {
		String finalStr = graphNodeID;
		for (Map.Entry<String, String> pair : this.replacementMap.entrySet()) {
			String origin = pair.getValue();
			String replacement = pair.getKey();
			finalStr = finalStr.replace(origin, replacement);
		}
		return finalStr;
	}
}
