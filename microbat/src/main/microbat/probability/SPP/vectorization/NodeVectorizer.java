package microbat.probability.SPP.vectorization;

import java.util.Map;

import microbat.model.trace.TraceNode;
import sav.common.core.Pair;

public class NodeVectorizer {
	
	private static Map<String, Pair<Integer, Integer>> apiConfidenceMap = null;
	
	public NodeVector vectorize(final TraceNode node) {
		return null;
	}
	
	public static void setAPIConfidence(final Map<String, Pair<Integer, Integer>> apiConfidenceMap) {
		NodeVectorizer.apiConfidenceMap = apiConfidenceMap;
	}
}
