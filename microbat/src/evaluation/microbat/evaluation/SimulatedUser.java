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
	private int unclearFeedbackNum = 0;
	
	private List<List<VarValue>> checkWrongVariableOptions(TraceNodePair pair, Trace mutatedTrace){
		List<List<VarValue>> options = new ArrayList<>();
		
		List<VarValue> wrongReadVars = pair.findSingleWrongReadVar(mutatedTrace);
		List<VarValue> wrongWrittenVars = pair.findSingleWrongWrittenVarID(mutatedTrace);
		
		System.currentTimeMillis();
		
		if(wrongReadVars.isEmpty() && wrongWrittenVars.isEmpty()){
			options.add(new ArrayList<VarValue>());
			return options;
		}
		else if(wrongReadVars.isEmpty() || wrongWrittenVars.isEmpty()){
			for(VarValue var: wrongWrittenVars){
				List<VarValue> list = new ArrayList<>();
				list.add(var);
				options.add(list);
			}
			
			for(VarValue var: wrongReadVars){
				List<VarValue> list = new ArrayList<>();
				list.add(var);
				options.add(list);
			}
			return options;
		}
		else{
			for(VarValue writtenVar: wrongWrittenVars){
				for(VarValue readVar: wrongReadVars){
					List<VarValue> list = new ArrayList<>();
					list.add(readVar);
					list.add(writtenVar);
					options.add(list);
				}
			}
			return options;
		}
	}
	
	public String feedback(TraceNode suspiciousNode, Trace mutatedTrace, PairList pairList, 
			int checkTime, boolean isFirstTime, int maxUnclearFeedbackNum) {
		
		otherOptions.clear();
		
		String feedback;
		
		boolean isClear = isClear(suspiciousNode, labeledUnclearNodeVisitedTimes, isFirstTime, maxUnclearFeedbackNum);
//		isClear = true;
		
		if(!isClear){
			feedback = UserFeedback.UNCLEAR;
			unclearFeedbackNum++;
		}
		else{
			TraceNodePair pair = pairList.findByMutatedNode(suspiciousNode);
			boolean isWrongPath = (pair==null);
			if(isWrongPath){
				feedback = UserFeedback.WRONG_PATH;
			}
			else{
				if(suspiciousNode.getOrder() == 33){
					System.currentTimeMillis();
				}
				
				List<VarValue> wrongVars = new ArrayList<>();
				List<List<VarValue>> options = checkWrongVariableOptions(pair, mutatedTrace);
				wrongVars = options.get(0); 
				
				for(int i=1; i<options.size(); i++){
					List<String> ids = new ArrayList<String>();
					for(VarValue varValue: options.get(i)){
						ids.add(varValue.getVarID());
					}
					otherOptions.add(ids);
				}
				
				if(!wrongVars.isEmpty()){
					for(VarValue var: wrongVars){
						String wrongVarID = var.getVarID();
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
			boolean isFirstTime, int maxUnclearFeedbackNum) {
		
		if(maxUnclearFeedbackNum == 0 || this.unclearFeedbackNum > maxUnclearFeedbackNum){
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
