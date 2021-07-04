package microbat.views;

import microbat.model.Cause;
import microbat.model.trace.TraceNode;
import microbat.recommendation.DebugState;
import microbat.recommendation.StepRecommender;

public class ReasonGenerator {
	public final String WRONG_PATH = "This step is responsible for wrong control condition.";
	public final String WRONG_VARIABLE_VALUE = "This step is responsible for assigning wrong variable.";
	public final String INSPECT_DETAIL = "This step lets you check more details.";
	public final String BINARY_SEARCH = "Ok, it seems that I over-skipped some steps. Let's check one of them!";
	public final String	SKIP = "This step is reponsible for a possible wrong assignment. ";
	public final String UNCLEAR = "I hope this step can help you better understand the context.";
	
	public final String NA = "NA";
	
	public String generateReason(StepRecommender recommender){
		if(recommender == null){
			return NA;
		}
		
		int currentState = recommender.getState();
		if(currentState == DebugState.SCLICING){
			Cause latestCause = recommender.getLatestCause();
			if(latestCause.isCausedByWrongPath()){
				return WRONG_PATH;
			}
			else if(latestCause.isCausedByWrongVariable()){
				return WRONG_VARIABLE_VALUE;
			}
		}
		else if(currentState == DebugState.DETAIL_INSPECT){
			return INSPECT_DETAIL;
		}
		else if(currentState == DebugState.BINARY_SEARCH){
			return BINARY_SEARCH;
		}
		else if(currentState == DebugState.SKIP){
			StringBuffer buffer = new StringBuffer();
			buffer.append(SKIP);
			
			if(!recommender.getLoopRange().getSkipPoints().isEmpty()){
				buffer.append("Note that I skip following steps because your feedback "
						+ "indicates similar case in the loop is bug free: \n");
				for(TraceNode skipPoint: recommender.getLoopRange().getSkipPoints()){
					String words = "* The " + skipPoint.getOrder() + "th step at line " 
							+ skipPoint.getLineNumber() + "\n"; 
					buffer.append(words);
				}
			}
			
			return buffer.toString();
		}
		else if(currentState == DebugState.UNCLEAR || currentState == DebugState.PARTIAL_CLEAR){
			return UNCLEAR;
		}
		
		return NA;
	}
}
