package microbat.handler;

import java.util.HashMap;
import java.util.Map;

import microbat.model.AttributionVar;
import microbat.model.UserInterestedVariables;
import microbat.model.trace.PotentialCorrectPatternList;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.recommendation.StepRecommender;

public class CheckingState {
	private int currentNodeOrder;
	private int currentNodeCheckTime;
	private Map<AttributionVar, Double> currentNodeSuspicousScoreMap = new HashMap<AttributionVar, Double>();
	
	private StepRecommender recommender;
	
	private int traceCheckTime;
	
	private UserInterestedVariables interestedVariables;
	private PotentialCorrectPatternList potentialCorrectPatterns;
	
	public void recordCheckingState(TraceNode currentNode,
			StepRecommender recommender, Trace trace,
			UserInterestedVariables interestedVariables,
			PotentialCorrectPatternList potentialCorrectPatterns) {
		
		this.currentNodeOrder = currentNode.getOrder();
		this.currentNodeCheckTime = currentNode.getCheckTime();
		Map<AttributionVar, Double> map = currentNode.getSuspicousScoreMap();
		for(AttributionVar var: map.keySet()){
			this.currentNodeSuspicousScoreMap.put(var, map.get(var));
		}
		
		this.recommender = recommender.clone();
		
		this.traceCheckTime = trace.getCheckTime();
		
		this.interestedVariables = interestedVariables.clone();
		this.potentialCorrectPatterns = potentialCorrectPatterns.clone();
	}
	

	public int getCurrentNodeCheckTime() {
		return currentNodeCheckTime;
	}

	public void setCurrentNodeCheckTime(int currentNodeCheckTime) {
		this.currentNodeCheckTime = currentNodeCheckTime;
	}

	public Map<AttributionVar, Double> getCurrentNodeSuspicousScoreMap() {
		return currentNodeSuspicousScoreMap;
	}

	public void setCurrentNodeSuspicousScoreMap(
			Map<AttributionVar, Double> currentNodeSuspicousScoreMap) {
		this.currentNodeSuspicousScoreMap = currentNodeSuspicousScoreMap;
	}

	public StepRecommender getRecommender() {
		return recommender;
	}

	public void setRecommender(StepRecommender recommender) {
		this.recommender = recommender;
	}

	public int getTraceCheckTime() {
		return traceCheckTime;
	}

	public void setTraceCheckTime(int traceCheckTime) {
		this.traceCheckTime = traceCheckTime;
	}

	public UserInterestedVariables getInterestedVariables() {
		return interestedVariables;
	}

	public void setInterestedVariables(UserInterestedVariables interestedVariables) {
		this.interestedVariables = interestedVariables;
	}

	public int getCurrentNodeOrder() {
		return currentNodeOrder;
	}

	public void setCurrentNodeOrder(int currentNodeOrder) {
		this.currentNodeOrder = currentNodeOrder;
	}

	public PotentialCorrectPatternList getPotentialCorrectPatterns() {
		return potentialCorrectPatterns;
	}

	public void setPotentialCorrectPatterns(PotentialCorrectPatternList potentialCorrectPatterns) {
		this.potentialCorrectPatterns = potentialCorrectPatterns;
	}

	
	
	
}
