package microbat.baseline.factorgraph;

import java.util.HashMap;
import java.util.Map;

import microbat.model.value.VarValue;

public class VarIDConverter {
	
	private final Map<String, String> replacementMap;
	
	public VarIDConverter() {
		this.replacementMap = new HashMap<>();
		this.replacementMap.put(",", "@");
		this.replacementMap.put("(", "^");
		this.replacementMap.put(")", "*");
	}
	public String varID2GraphID(String varID) {
		String finalStr = varID;
//		System.out.println("converting: " + varID);
		for (Map.Entry<String, String> pair : this.replacementMap.entrySet()) {
			String origin = pair.getKey();
			String replacement = pair.getValue();
			finalStr = finalStr.replace(origin, replacement);
		}
//		System.out.println("to: " + finalStr);
		return finalStr;
	}
	
	public String graphID2VarID(String varID) {
		String finalStr = varID;
		for (Map.Entry<String, String> pair : this.replacementMap.entrySet()) {
			String origin = pair.getValue();
			String replacement = pair.getKey();
			finalStr = finalStr.replace(origin, replacement);
		}
		return finalStr;
	}
}
