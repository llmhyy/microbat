package microbat.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import microbat.evaluation.model.PairList;
import microbat.evaluation.model.TraceNodePair;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;
import microbat.util.Settings;

public class SimulatedUser {

	private HashMap<TraceNode, Integer> labeledUnclearNodeVisitedTimes = new HashMap<>();
	
	private List<List<String>> otherOptions = new ArrayList<>();
	
	private List<List<String>> checkWrongVariableIDOptions(TraceNodePair pair, Trace mutatedTrace){
		List<List<String>> options = new ArrayList<>();
		
		List<String> wrongReadVarIDs = pair.findSingleWrongReadVarID(mutatedTrace);
		List<String> wrongWrittenVarIDs = pair.findSingleWrongWrittenVarID(mutatedTrace);
		
		System.currentTimeMillis();
		
		if(wrongReadVarIDs.size() < 2 && wrongWrittenVarIDs.size() < 2){
			List<String> wrongVarIDs = new ArrayList<>();
			wrongVarIDs.addAll(wrongReadVarIDs);
			wrongVarIDs.addAll(wrongWrittenVarIDs);
			
			options.add(wrongVarIDs);
		}
		else{
			List<String> varIDsWithLargeSize; 
			List<String> varIDsWithSmallSize;
			if(wrongReadVarIDs.size() == 2){
				varIDsWithLargeSize = wrongReadVarIDs;
				varIDsWithSmallSize = wrongWrittenVarIDs;
			}
			else{
				varIDsWithLargeSize = wrongWrittenVarIDs;
				varIDsWithSmallSize = wrongReadVarIDs;
			}
			
			for(String wrongVarLarge: varIDsWithLargeSize){
				if(varIDsWithSmallSize.size() > 0){
					for(String wrongVarSmall: varIDsWithSmallSize){
						List<String> wrongVarIDs = new ArrayList<>();
						wrongVarIDs.add(wrongVarLarge);
						wrongVarIDs.add(wrongVarSmall);
						
						options.add(wrongVarIDs);
					}
				}
				else{
					List<String> wrongVarIDs = new ArrayList<>();
					wrongVarIDs.add(wrongVarLarge);
					
					options.add(wrongVarIDs);
				}
			}
		}
		
		return options;
	}
	
	public String feedback(TraceNode suspiciousNode, Trace mutatedTrace, PairList pairList, int checkTime, boolean isFirstTime, boolean enableUnclear) {
		
		otherOptions.clear();
		
		String feedback;
		
		boolean isClear = isClear(suspiciousNode, labeledUnclearNodeVisitedTimes, isFirstTime, enableUnclear);
		if(!isClear){
			feedback = UserFeedback.UNCLEAR;
		}
		else{
			TraceNodePair pair = pairList.findByMutatedNode(suspiciousNode);
			boolean isWrongPath = (pair==null);
			if(isWrongPath){
				feedback = UserFeedback.WRONG_PATH;
			}
			else{
//				System.currentTimeMillis();
//				List<String> wrongVarIDs = pair.findWrongVarIDs();
				
				if(suspiciousNode.getLineNumber() == 51){
//					System.currentTimeMillis();
				}
				
				List<String> wrongVarIDs = new ArrayList<>();
				List<List<String>> options = checkWrongVariableIDOptions(pair, mutatedTrace);
				wrongVarIDs = options.get(0);
				
				for(int i=1; i<options.size(); i++){
					otherOptions.add(options.get(i));
				}
				
				if(!wrongVarIDs.isEmpty()){
					for(String wrongVarID: wrongVarIDs){
						Settings.interestedVariables.add(wrongVarID, checkTime);
					}			
					feedback = UserFeedback.INCORRECT;
				}
				else{
					for(VarValue writtenVar: suspiciousNode.getWrittenVariables()){
						Settings.interestedVariables.remove(writtenVar.getVarID());
					}
					for(VarValue readVar: suspiciousNode.getReadVariables()){
						Settings.interestedVariables.remove(readVar.getVarID());
					}
					
					feedback = UserFeedback.CORRECT;
					
				}
				
			}
			
		}
		
		return feedback;
		
	}

	
	
	private boolean isClear(TraceNode suspiciousNode, HashMap<TraceNode, Integer> labeledUnclearNodeVisitedTimes, 
			boolean isFirstTime, boolean enableUnclear) {
		
		if(!enableUnclear){
			return true;
		}
		
		Integer times = labeledUnclearNodeVisitedTimes.get(suspiciousNode);
		if(times == null){
			times = 1;
		}
		else{
			times++;
		}
		labeledUnclearNodeVisitedTimes.put(suspiciousNode, times);
		
		
		if(isFirstTime){
			return true;
		}
		
		int layerNum = suspiciousNode.getInvocationLevel();
		
		double unclearPossibility = (1-1/(Math.pow(Math.E, layerNum-1)))/times;
		
		double dice = Math.random();
		
		if(dice<unclearPossibility){
			return false;
		}
		else{
			return true;			
		}
	}

	public List<List<String>> getOtherOptions() {
		return otherOptions;
	}

	public void setOtherOptions(List<List<String>> otherOptions) {
		this.otherOptions = otherOptions;
	}

//	private String findReachingReadVariablesFromSuspiciousNodeToRootCause(
//			TraceNode suspiciousNode, Fault rootCause) {
//		
//		Map<TraceNode, List<String>> dominatorMap = suspiciousNode.getDataDominator();
//		
//		List<String> workingIDs = new ArrayList<>();
//		for(TraceNode dominator: dominatorMap.keySet()){
//			
//			
//			if(dominator.equals(rootCause.getBuggyNode())){
//				List<String> varIDs = dominatorMap.get(dominator);
//				workingIDs.add(varIDs.get(0));
//			}
//			else{
//				String varID = findReachingReadVariablesFromSuspiciousNodeToRootCause(dominator, rootCause);
//				if(varID != null){
//					List<String> varIDs = dominatorMap.get(dominator);
//					workingIDs.add(varIDs.get(0));
//				}
//			}
//		}
//		
//		String varID = getFittestVar(workingIDs);
//		
//		return varID;
//	}
//
//	private String getFittestVar(List<String> workingIDs) {
//		if(workingIDs.isEmpty()){
//			return null;			
//		}
//		else if(workingIDs.size() == 1){
//			return workingIDs.get(0);
//		}
//		else{
//			String varID = workingIDs.get(0);
//			
//			for(String workingID: workingIDs){
//				if(!workingID.contains("vir")){
//					varID = workingID;
//					return varID;
//				}
//			}
//			
//			return varID;
//		}
//	}

	
	
}
