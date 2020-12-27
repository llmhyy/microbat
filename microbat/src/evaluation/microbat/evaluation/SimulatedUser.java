package microbat.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import microbat.evaluation.model.OptionComparator;
import microbat.evaluation.model.PairList;
import microbat.evaluation.model.TraceNodePair;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.ChosenVariableOption;
import microbat.recommendation.UserFeedback;
import microbat.util.Settings;

public class SimulatedUser {

	private HashMap<TraceNode, Integer> labeledUnclearNodeVisitedTimes = new HashMap<>();
	
	private List<ChosenVariableOption> otherOptions = new ArrayList<>();
	private int unclearFeedbackNum = 0;
	
	private List<ChosenVariableOption> checkWrongVariableOptions(TraceNodePair pair, Trace mutatedTrace){
		
		List<ChosenVariableOption> options = new ArrayList<>();
		
		List<VarValue> wrongReadVars = pair.findSingleWrongReadVar(mutatedTrace);
		List<VarValue> wrongWrittenVars = pair.findSingleWrongWrittenVarID(mutatedTrace);
		
		System.currentTimeMillis();
		
		if(wrongReadVars.isEmpty() && wrongWrittenVars.isEmpty()){
			return options;
		}
		else if(wrongReadVars.isEmpty() || wrongWrittenVars.isEmpty()){
			for(VarValue wrongWrittenVar: wrongWrittenVars){
				ChosenVariableOption option = new ChosenVariableOption(null, wrongWrittenVar);
				options.add(option);
			}
			
			for(VarValue wrongReadVar: wrongReadVars){
				ChosenVariableOption option = new ChosenVariableOption(wrongReadVar, null);
				options.add(option);
			}
			return options;
		}
		else{
			for(VarValue writtenVar: wrongWrittenVars){
				for(VarValue readVar: wrongReadVars){
					ChosenVariableOption option = new ChosenVariableOption(readVar, writtenVar);
					options.add(option);
				}
			}
			return options;
		}
	}
	
	public UserFeedback feedback(TraceNode suspiciousNode, Trace mutatedTrace, PairList pairList, 
			int checkTime, boolean isFirstTime, int maxUnclearFeedbackNum) {
		
		otherOptions.clear();
		
		UserFeedback feedback = new UserFeedback();
		
		boolean isClear = isClear(suspiciousNode, labeledUnclearNodeVisitedTimes, isFirstTime, maxUnclearFeedbackNum);
//		isClear = true;
		
		if(!isClear){
			feedback.setFeedbackType(UserFeedback.UNCLEAR);
			unclearFeedbackNum++;
		}
		else{
			TraceNodePair pair = pairList.findByMutatedNode(suspiciousNode);
			boolean isWrongPath = (pair==null);
			if(isWrongPath){
				feedback.setFeedbackType(UserFeedback.WRONG_PATH);
			}
			else{
				List<ChosenVariableOption> options = checkWrongVariableOptions(pair, mutatedTrace);
				/**
				 * I prioritize the option so that the feedback number can be quickly reduced.
				 */
				Collections.sort(options, new OptionComparator());
				
				if(options.isEmpty()){
					for(VarValue writtenVar: suspiciousNode.getWrittenVariables()){
						Settings.interestedVariables.remove(writtenVar);
					}
					for(VarValue readVar: suspiciousNode.getReadVariables()){
						Settings.interestedVariables.remove(readVar);
					}
					
					feedback.setFeedbackType(UserFeedback.CORRECT);
				}
				else{
					ChosenVariableOption option = options.get(0);
					
					for(int i=1; i<options.size(); i++){
						otherOptions.add(options.get(i));
					}
					
					List<VarValue> wrongVars = option.getIncludedWrongVars();
					for(VarValue wrongVar: wrongVars){
						Settings.interestedVariables.add(checkTime, wrongVar);						
					}
					
					feedback.setFeedbackType(UserFeedback.WRONG_VARIABLE_VALUE);
					feedback.setOption(option);
				}
			}
			
		}
		
		return feedback;
		
	}

	
	
	private List<VarValue> getPriorOption(List<List<VarValue>> options) {
		return null;
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

	public List<ChosenVariableOption> getOtherOptions() {
		return otherOptions;
	}

	public void setOtherOptions(List<ChosenVariableOption> otherOptions) {
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
