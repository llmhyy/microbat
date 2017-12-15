package microbat.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import microbat.algorithm.graphdiff.GraphDiff;
import microbat.algorithm.graphdiff.HierarchyGraphDiffer;
import microbat.evaluation.accuracy.Accuracy;
import microbat.evaluation.model.LCSMatcher;
import microbat.evaluation.model.PairList;
import microbat.evaluation.model.StateWrapper;
import microbat.evaluation.model.StepOperationTuple;
import microbat.evaluation.model.TraceNodePair;
import microbat.evaluation.model.TraceNodeWrapper;
import microbat.evaluation.model.Trial;
import microbat.evaluation.util.TraceNodeComprehensiveSimilarityComparator;
import microbat.handler.CheckingState;
import microbat.model.UserInterestedVariables;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.trace.TraceNodeReverseOrderComparator;
import microbat.model.value.VarValue;
import microbat.recommendation.ChosenVariableOption;
import microbat.recommendation.StepRecommender;
import microbat.recommendation.UserFeedback;
import microbat.util.Settings;
import sav.strategies.dto.ClassLocation;

public class SimulatedMicroBat {
	
	
	List<TraceNode> falsePositive = new ArrayList<>();
	List<TraceNode> falseNegative = new ArrayList<>();
	
	private SimulatedUser user = new SimulatedUser();
	private StepRecommender recommender;
	
	private PairList matchTraceNodePair(Trace mutatedTrace, Trace correctTrace) {
		
		TraceNodeWrapper mutatedTraceNodeWrapper = initVirtualRootWrapper(mutatedTrace);
		TraceNodeWrapper correctTraceNodeWrapper = initVirtualRootWrapper(correctTrace);
		
		HierarchyGraphDiffer differ = new HierarchyGraphDiffer();
		differ.diff(mutatedTraceNodeWrapper, correctTraceNodeWrapper, false, 
				new LCSMatcher(new TraceNodeComprehensiveSimilarityComparator()), -1);
		
		List<GraphDiff> diffList = differ.getDiffs();
		List<TraceNodePair> pList = new ArrayList<>();
		for(GraphDiff diff: diffList){
			if(diff.getDiffType().equals(GraphDiff.UPDATE)){
				TraceNodeWrapper wrapperBefore = (TraceNodeWrapper)diff.getNodeBefore();
				TraceNodeWrapper wrapperAfter = (TraceNodeWrapper)diff.getNodeAfter();
				
				TraceNodePair pair = new TraceNodePair(wrapperBefore.getTraceNode(), wrapperAfter.getTraceNode());
				pair.setExactSame(false);
				pList.add(pair);
			}
		}
		
		for(GraphDiff common: differ.getCommons()){
			TraceNodeWrapper wrapperBefore = (TraceNodeWrapper)common.getNodeBefore();
			TraceNodeWrapper wrapperAfter = (TraceNodeWrapper)common.getNodeAfter();
			
			TraceNodePair pair = new TraceNodePair(wrapperBefore.getTraceNode(), wrapperAfter.getTraceNode());
			pair.setExactSame(true);
			pList.add(pair);
		}
		
		Collections.sort(pList, new TraceNodePairReverseOrderComparator());
		PairList pairList = new PairList(pList);
		return pairList;
	}
	
	
	private TraceNodeWrapper initVirtualRootWrapper(Trace trace) {
		TraceNode virtualNode = new TraceNode(null, null, -1);
//		List<TraceNode> topList = trace.getTopMethodLevelNodes();
		List<TraceNode> topList = trace.getTopAbstractionLevelNodes();
		virtualNode.setInvocationChildren(topList);
		
		TraceNodeWrapper wrapper = new TraceNodeWrapper(virtualNode);
		
		return wrapper;
	}

	
	private PairList pairList;
	TraceNode rootCause;
	Map<Integer, TraceNode> allWrongNodeMap;
	TraceNode observedFaultNode;
	
	public void prepare(Trace mutatedTrace, Trace correctTrace, ClassLocation mutatedLocation, 
			String testCaseName, String mutatedFile){
//		PairList pairList = DiffUtil.generateMatchedTraceNodeList(mutatedTrace, correctTrace);
		setPairList(matchTraceNodePair(mutatedTrace, correctTrace)); 
		
		rootCause = findRootCause(mutatedLocation.getClassCanonicalName(), 
				mutatedLocation.getLineNo(), mutatedTrace, getPairList());
		
		allWrongNodeMap = findAllWrongNodes(getPairList(), mutatedTrace);
		
		if(!allWrongNodeMap.isEmpty()){
			List<TraceNode> wrongNodeList = new ArrayList<>(allWrongNodeMap.values());
			Collections.sort(wrongNodeList, new TraceNodeReverseOrderComparator());
			observedFaultNode = findObservedFault(wrongNodeList, getPairList());
		}
		
	}
	
	private boolean isObservedFaultWrongPath(TraceNode observableNode, PairList pairList){
		TraceNodePair pair = pairList.findByMutatedNode(observableNode);
		if(pair == null){
			return true;
		}
		
		if(pair.getOriginalNode() == null){
			return true;
		}
		
		return false;
	}
	
	private TraceNode findObservedFault(List<TraceNode> wrongNodeList, PairList pairList){
		TraceNode observedFaultNode = wrongNodeList.get(0);
		
		/**
		 * If the last portion of steps in trace are all wrong-path nodes, then we choose
		 * the one at the beginning of this portion as the observable step. 
		 */
		if(isObservedFaultWrongPath(observedFaultNode, pairList)){
			int index = 1;
			observedFaultNode = wrongNodeList.get(index);
			while(isObservedFaultWrongPath(observedFaultNode, pairList)){
				index++;
				if(index < wrongNodeList.size()){
					observedFaultNode = wrongNodeList.get(index);					
				}
				else{
					break;
				}
			}
			
			observedFaultNode = wrongNodeList.get(index-1);
			
			if(observedFaultNode.getControlDominator() == null){
				if(index < wrongNodeList.size()){
					observedFaultNode = wrongNodeList.get(index);					
				}
			}
		}
		
		return observedFaultNode;
	}

	public Trial detectMutatedBug(Trace mutatedTrace, Trace correctTrace, ClassLocation mutatedLocation, 
			String testCaseName, String mutatedFile, double unclearRate, boolean enableLoopInference, int optionSearchLimit) 
					throws SimulationFailException {
		mutatedTrace.resetCheckTime();
		if(observedFaultNode != null){
			try {
				Trial trial = startSimulation(observedFaultNode, rootCause, mutatedTrace, allWrongNodeMap, getPairList(), 
						testCaseName, mutatedFile, unclearRate, enableLoopInference, optionSearchLimit);
//				System.currentTimeMillis();
				return trial;			
			} catch (Exception e) {
				String errorMsg = "Test case: " + testCaseName + 
						" has exception when simulating debugging\n" + "Mutated File: " + mutatedFile +
						", unclearRate: " + unclearRate + ", enableLoopInference: " + enableLoopInference;
				System.err.println(errorMsg);
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	/** 
	 * Adjust the effect of constraints. 
	 * If last feedback is wrong-path, then this step should not be
	 * a correct step.
	 */
	private boolean hasConflicts(ArrayList<StepOperationTuple> jumpingSteps, UserFeedback currentFeedback){
		
		if(jumpingSteps.size() < 2){
			return false;
		}
		
		String lastFeedbackType = jumpingSteps.isEmpty() ? null : 
			jumpingSteps.get(jumpingSteps.size()-2).getUserFeedback().getFeedbackType();
		
		if(lastFeedbackType != null && lastFeedbackType.equals(UserFeedback.WRONG_PATH) 
				&& currentFeedback.getFeedbackType().equals(UserFeedback.CORRECT)){
			return true;
		}
		
		return false;
	}
	
	class Attempt{
		int suspiciousNodeOrder;
		ChosenVariableOption option;
		public Attempt(int suspiciousNodeOrder, ChosenVariableOption option) {
			super();
			this.suspiciousNodeOrder = suspiciousNodeOrder;
			this.option = option;
		}
		
		@Override
		public boolean equals(Object obj){
			if(obj instanceof Attempt){
				Attempt other = (Attempt)obj;
				if(this.suspiciousNodeOrder == other.suspiciousNodeOrder){
					
					String thisReadVarID = (this.option.getReadVar()!=null)? this.option.getReadVar().getVarID() : "null";
					String thatReadVarID = (other.option.getReadVar()!=null)? other.option.getReadVar().getVarID() : "null";
					
					String thisWrittenVarID = (this.option.getWrittenVar()!=null)? this.option.getWrittenVar().getVarID() : "null";
					String thatWrittenVarID = (other.option.getWrittenVar()!=null)? other.option.getWrittenVar().getVarID() : "null";
					
					if(thisReadVarID.equals(thatReadVarID) && thisWrittenVarID.equals(thatWrittenVarID)){
						return true;
					}
				}
			}
			
			return false;
		}
		
		@Override
		public int hashCode(){
			int readVarCode = (this.option.getReadVar() != null) ? this.option.getReadVar().getVarID().hashCode() : 0;
			int writtenVarCode = (this.option.getWrittenVar() != null) ? this.option.getWrittenVar().getVarID().hashCode() : 0;
			
			return this.suspiciousNodeOrder + readVarCode + writtenVarCode;
		}
		
		@Override
		public String toString(){
			StringBuffer buffer = new StringBuffer();
			buffer.append("order: " + this.suspiciousNodeOrder + "\n");
			buffer.append(this.option.toString());
			return buffer.toString();
		}
	}
	
	private Trial startSimulation(TraceNode observedFaultNode, TraceNode rootCause, Trace mutatedTrace, 
			Map<Integer, TraceNode> allWrongNodeMap, PairList pairList, String testCaseName, String mutatedFile, 
			double unclearRate, boolean enableLoopInference, int optionSearchLimit) 
					throws SimulationFailException {
		
		Settings.interestedVariables.clear();
//		Settings.localVariableScopes.clear();
		Settings.potentialCorrectPatterns.clear();
		
		/**
		 * this variable is for optimization. 
		 * when a suspicious node with certain option (i.e., wrong variables) is popped out
		 * of the confusing stack, it means that it is not possible to find the mutated bug
		 * with this option on the suspicious node. Therefore, there is no need to try such
		 * attempt again.
		 */
		HashSet<Attempt> failedAttempts = new HashSet<>();
		
		recommender = new StepRecommender(enableLoopInference);
		user = new SimulatedUser();
		
		int traceLength = mutatedTrace.getExecutionList().size();
		int maxUnclearFeedbackNum = (int)(traceLength*unclearRate);
		if(unclearRate == -1){
			maxUnclearFeedbackNum = traceLength;
		}
		
		Stack<StateWrapper> confusingStack = new Stack<>();
		ArrayList<StepOperationTuple> jumpingSteps = new ArrayList<>();
		
		try{
			TraceNode lastNode = observedFaultNode;
			TraceNode suspiciousNode = observedFaultNode;
			
			UserFeedback feedback = operateFeedback(observedFaultNode,
					mutatedTrace, pairList, maxUnclearFeedbackNum, confusingStack,
					jumpingSteps, true, failedAttempts);
			
			TraceNodePair pair = pairList.findByMutatedNode(suspiciousNode);
			TraceNode referenceNode = (pair==null)? null : pair.getOriginalNode();
			
			jumpingSteps.add(new StepOperationTuple(suspiciousNode, feedback, referenceNode, recommender.getState()));
			
			if(!feedback.getFeedbackType().equals(UserFeedback.UNCLEAR)){
				setCurrentNodeChecked(mutatedTrace, suspiciousNode);		
				updateVariableCheckTime(mutatedTrace, suspiciousNode);
			}
			
			int optionSearchTime = 0;
			StateWrapper currentConfusingState = null;
			
			boolean isBugFound = rootCause.getLineNumber()==suspiciousNode.getLineNumber();
			while(!isBugFound){
				if(hasConflicts(jumpingSteps, feedback)){
					return null;
				}
				
				TraceNode originalSuspiciousNode = suspiciousNode;
				suspiciousNode = findSuspicioiusNode(suspiciousNode, mutatedTrace, feedback);	
				if(suspiciousNode==null && feedback.getFeedbackType().equals(UserFeedback.WRONG_PATH)){
					UserFeedback f = new UserFeedback();
					f.setFeedbackType("Missing Control Dominator!");
					jumpingSteps.add(new StepOperationTuple(originalSuspiciousNode, f, null, recommender.getState()));
					break;
				}
				
				/** It means that the bug cannot be found now */
				if((jumpingSteps.size() > mutatedTrace.size())  
						|| (isContainedInJump(suspiciousNode, jumpingSteps) && unclearRate==0) 
						|| (lastNode.getOrder()==suspiciousNode.getOrder() && !feedback.getFeedbackType().equals(UserFeedback.UNCLEAR))
						/*|| cannotConverge(jumpingSteps)*/){
//					break;
					
					if(currentConfusingState != null){
						Attempt attempt = new Attempt(currentConfusingState.getState().getCurrentNodeOrder(), 
								currentConfusingState.getVariableOption());
						failedAttempts.add(attempt);
					}
					
					System.out.println("=========An attempt fails=========");
					for(StepOperationTuple t: jumpingSteps){
						System.err.println(t);	
						Thread.sleep(10);
					}
					System.out.println();
					
					if(!confusingStack.isEmpty()){
						if(optionSearchTime > optionSearchLimit){
							return null;
						}
						else{
							optionSearchTime++;
						}
						
						/** recover */
						StateWrapper stateWrapper = confusingStack.pop();
						currentConfusingState = stateWrapper;
						
						jumpingSteps = stateWrapper.getJumpingSteps();

						CheckingState state = stateWrapper.getState();
						int checkTime = state.getTraceCheckTime();
						
						mutatedTrace.setCheckTime(state.getTraceCheckTime());
						suspiciousNode = mutatedTrace.getExecutionList().get(state.getCurrentNodeOrder()-1);
						suspiciousNode.setSuspicousScoreMap(state.getCurrentNodeSuspicousScoreMap());
						suspiciousNode.setCheckTime(state.getCurrentNodeCheckTime());
						
						Settings.interestedVariables = state.getInterestedVariables();
						Settings.potentialCorrectPatterns = state.getPotentialCorrectPatterns();
						Settings.wrongPathNodeOrder = state.getWrongPathNodeOrder();
						recommender = state.getRecommender();
						
						ChosenVariableOption option = stateWrapper.getVariableOption();
						for(String wrongVarID: option.getIncludedWrongVarID()){
							Settings.interestedVariables.add(wrongVarID, checkTime);
						}
						feedback = new UserFeedback(option, UserFeedback.WRONG_VARIABLE_VALUE);
						
						pair = pairList.findByMutatedNode(suspiciousNode);
						referenceNode = (pair==null)? null : pair.getOriginalNode();
						jumpingSteps.add(new StepOperationTuple(suspiciousNode, feedback, referenceNode, recommender.getState()));
					}
					else{
						break;						
					}
				}
				else{
					isBugFound = rootCause.getLineNumber()==suspiciousNode.getLineNumber();
					
					if(!isBugFound){
						if(suspiciousNode.getOrder() == 132){
//							System.currentTimeMillis();
						}
						
						
						feedback = operateFeedback(suspiciousNode,
								mutatedTrace, pairList, maxUnclearFeedbackNum, confusingStack,
								jumpingSteps, false, failedAttempts);

						pair = pairList.findByMutatedNode(suspiciousNode);
						referenceNode = (pair==null)? null : pair.getOriginalNode();
						
						jumpingSteps.add(new StepOperationTuple(suspiciousNode, feedback, referenceNode, recommender.getState()));
						
						if(!feedback.getFeedbackType().equals(UserFeedback.UNCLEAR)){
							setCurrentNodeChecked(mutatedTrace, suspiciousNode);		
							updateVariableCheckTime(mutatedTrace, suspiciousNode);
						}
					}
					else{
						UserFeedback f = new UserFeedback();
						f.setFeedbackType("Bug Found!");
						jumpingSteps.add(new StepOperationTuple(suspiciousNode, f, null, recommender.getState()));
					}
					
				}
				
				lastNode = suspiciousNode;
			}
			
			System.out.println("number of attempts: " + optionSearchTime);
			Trial trial = constructTrial(rootCause, mutatedTrace, testCaseName,
					mutatedFile, isBugFound, jumpingSteps);
			
			return trial;
		}
		catch(Exception e){
			e.printStackTrace();
			for(StepOperationTuple t: jumpingSteps){
				System.err.println(t);				
			}
			System.out.println();
			String msg = "The program stuck in " + testCaseName +", the mutated line is " + rootCause.getLineNumber();
			SimulationFailException ex = new SimulationFailException(msg);
			throw ex;
		}
	}
	
	private boolean cannotConverge(ArrayList<StepOperationTuple> jumpingSteps) {
		if(jumpingSteps.size() > 10){
			int size = jumpingSteps.size();
			StepOperationTuple checkingStep = jumpingSteps.get(size-1);
			
			boolean canFindRecurringStep = canFindRecurringStep(checkingStep, jumpingSteps, size-1);
			if(canFindRecurringStep){
				return true;
			}
		}
		
		return false;
	}


	private boolean canFindRecurringStep(StepOperationTuple checkingStep, ArrayList<StepOperationTuple> jumpingSteps,
			int limit) {
		
		if(checkingStep.getUserFeedback().getFeedbackType().equals(UserFeedback.UNCLEAR) ||
				checkingStep.getUserFeedback().getFeedbackType().equals(UserFeedback.CORRECT)){
			return false;
		}
		
		for(int i=limit-1; i>=0; i--){
			StepOperationTuple step = jumpingSteps.get(i);
			
			
			if(step.getNode().getOrder() == checkingStep.getNode().getOrder()){
				if(!step.getUserFeedback().getFeedbackType().equals(UserFeedback.UNCLEAR)){
					if(step.getUserFeedback().equals(checkingStep.getUserFeedback())){
						return true;
					}
				}
			}
		}
		
		return false;
	}


	private boolean isContainedInJump(TraceNode suspiciousNode, ArrayList<StepOperationTuple> jumpingSteps) {
		
		for(int i=jumpingSteps.size()-1; i>=0; i--){
			StepOperationTuple tuple = jumpingSteps.get(i);
			if(tuple.getNode().getOrder() == suspiciousNode.getOrder()){
				return true;
			}
		}
		
		return false;
	}


	@SuppressWarnings("unchecked")
	/**
	 * Apart from feedback, this method also back up the state for future re-trial.
	 * 
	 * @param observedFaultNode
	 * @param mutatedTrace
	 * @param pairList
	 * @param enableClear
	 * @param confusingStack
	 * @param jumpingSteps
	 * @param suspiciousNode
	 * @param isFirstTime
	 * @return
	 */
	private UserFeedback operateFeedback(TraceNode suspiciousNode,
			Trace mutatedTrace, PairList pairList, int maxUnclearFeedbackNum,
			Stack<StateWrapper> confusingStack,
			ArrayList<StepOperationTuple> jumpingSteps,
			boolean isFirstTime, HashSet<Attempt> failedAttempts) {
		
		UserInterestedVariables interestedVariables = Settings.interestedVariables.clone();
		
		UserFeedback feedbackType = user.feedback(suspiciousNode, mutatedTrace, pairList, 
				mutatedTrace.getCheckTime(), isFirstTime, maxUnclearFeedbackNum);
		
		int size = user.getOtherOptions().size();
		for(int i=size-1; i>=0; i--){
			CheckingState state = new CheckingState();
			state.recordCheckingState(suspiciousNode, recommender, mutatedTrace, 
					Settings.interestedVariables, Settings.wrongPathNodeOrder, Settings.potentialCorrectPatterns);
			
			ChosenVariableOption option = user.getOtherOptions().get(i);
			ArrayList<StepOperationTuple> clonedJumpingSteps = (ArrayList<StepOperationTuple>) jumpingSteps.clone();
			StateWrapper stateWrapper = new StateWrapper(state, option, clonedJumpingSteps);
			
			Attempt newAttempt = new Attempt(suspiciousNode.getOrder(), option);
			
			if(!failedAttempts.contains(newAttempt)){
				confusingStack.push(stateWrapper);				
			}
			
		}
		
		return feedbackType;
	}

	private Trial constructTrial(TraceNode rootCause, Trace mutatedTrace,
			String testCaseName, String mutatedFile, boolean isBugFound, List<StepOperationTuple> jumpingSteps) {
		
		List<String> jumpStringSteps = new ArrayList<>();
		System.out.println("bug found: " + isBugFound);
		for(StepOperationTuple tuple: jumpingSteps){
			String correspondingStr = (tuple.getReferenceNode()==null)? "" : tuple.getReferenceNode().toString();
			
			String str = tuple.getNode().toString() + ": " + tuple.getUserFeedback() + " ... "
				+ correspondingStr + "\n";
			System.out.print(str);		
			jumpStringSteps.add(str);
		}
		System.out.println("Root Cause:" + rootCause);
		
		Trial trial = new Trial();
		trial.setTestCaseName(testCaseName);
		trial.setBugFound(isBugFound);
		trial.setMutatedLineNumber(rootCause.getLineNumber());
		trial.setJumpSteps(jumpStringSteps);
		trial.setTotalSteps(mutatedTrace.size());
		trial.setMutatedFile(mutatedFile);
		trial.setResult(isBugFound? Trial.SUCESS : Trial.FAIL);
		return trial;
	}
	
	private void setCurrentNodeChecked(Trace trace, TraceNode currentNode) {
		int checkTime = trace.getCheckTime()+1;
		currentNode.setCheckTime(checkTime);
		trace.setCheckTime(checkTime);
	}
	
	private void updateVariableCheckTime(Trace trace, TraceNode currentNode) {
		for(VarValue var: currentNode.getReadVariables()){
			String varID = var.getVarID();
			if(Settings.interestedVariables.contains(varID)){
				Settings.interestedVariables.add(varID, trace.getCheckTime());
			}
		}
		
		for(VarValue var: currentNode.getWrittenVariables()){
			String varID = var.getVarID();
			if(Settings.interestedVariables.contains(varID)){
				Settings.interestedVariables.add(varID, trace.getCheckTime());
			}
		}
	}

	
	protected List<TraceNode> findAllDominatees(Trace mutationTrace, ClassLocation mutatedLocation){
		Map<Integer, TraceNode> allDominatees = new HashMap<>();
		
		for(TraceNode mutatedNode: mutationTrace.getExecutionList()){
			if(mutatedNode.getClassCanonicalName().equals(mutatedLocation.getClassCanonicalName()) 
					&& mutatedNode.getLineNumber() == mutatedLocation.getLineNo()){
				
				if(allDominatees.get(mutatedNode.getOrder()) == null){
					Map<Integer, TraceNode> dominatees = mutatedNode.findAllDominatees();
					allDominatees.putAll(dominatees);
					allDominatees.put(mutatedNode.getOrder(), mutatedNode);
				}
				
			}
		}
		
		return new ArrayList<>(allDominatees.values());
	}
	
	private Map<Integer, TraceNode> findAllWrongNodes(PairList pairList, Trace mutatedTrace){
		Map<Integer, TraceNode> actualWrongNodes = new HashMap<>();
		for(TraceNode mutatedTraceNode: mutatedTrace.getExecutionList()){
			TraceNodePair foundPair = pairList.findByMutatedNode(mutatedTraceNode);
			if(foundPair != null){
				if(!foundPair.isExactSame()){
					TraceNode mutatedNode = foundPair.getMutatedNode();
					actualWrongNodes.put(mutatedNode.getOrder(), mutatedNode);
				}
			}
			else{
				actualWrongNodes.put(mutatedTraceNode.getOrder(), mutatedTraceNode);
			}
		}
		return actualWrongNodes;
	}
	
	public Accuracy computeAccuracy(List<TraceNode> dominatees, List<TraceNode> actualWrongNodes) {
		double modelInfluencedSize = dominatees.size();
		
		List<TraceNode> commonNodes = findCommonNodes(dominatees, actualWrongNodes);
		
		double precision = (double)commonNodes.size()/modelInfluencedSize;
		double recall = (double)commonNodes.size()/actualWrongNodes.size();
		
		Accuracy accuracy = new Accuracy(precision, recall);
		
		return accuracy;
	}

	private List<TraceNode> findCommonNodes(List<TraceNode> dominatees,
			List<TraceNode> actualWrongNodes) {
		List<TraceNode> commonNodes = new ArrayList<>();
		
		falsePositive = new ArrayList<>();
		falseNegative = new ArrayList<>();
		
		for(TraceNode domiantee: dominatees){
			if(actualWrongNodes.contains(domiantee)){
				commonNodes.add(domiantee);
			}
			else{
				falsePositive.add(domiantee);
			}
		}
		
		for(TraceNode acturalWrongNode: actualWrongNodes){
			if(!commonNodes.contains(acturalWrongNode)){
				falseNegative.add(acturalWrongNode);
			}
		}
		
		return commonNodes;
	}

	private TraceNode findRootCause(String className, int lineNo, Trace mutatedTrace, PairList pairList) {
		for(TraceNode node: mutatedTrace.getExecutionList()){
			if(node.getDeclaringCompilationUnitName().equals(className) && node.getLineNumber()==lineNo){
				TraceNodePair pair = pairList.findByMutatedNode(node);
				
				if(pair != null){
					return pair.getMutatedNode();
				}
				
			}
		}
		
		System.currentTimeMillis();
		
		return null;
	}

	private TraceNode findSuspicioiusNode(TraceNode currentNode, Trace trace, UserFeedback feedback) {
		setCurrentNodeCheck(trace, currentNode);
		
		
		if(!feedback.equals(UserFeedback.UNCLEAR)){
			setCurrentNodeCheck(trace, currentNode);					
		}
		
		TraceNode suspiciousNode = recommender.recommendNode(trace, currentNode, feedback);
		return suspiciousNode;
		
//		TraceNode suspiciousNode = null;
//		
//		ConflictRuleChecker conflictRuleChecker = new ConflictRuleChecker();
//		TraceNode conflictNode = conflictRuleChecker.checkConflicts(trace, currentNode.getOrder());
//		
//		if(conflictNode == null){
//			suspiciousNode = recommender.recommendNode(trace, currentNode, feedbackType);
//		}
//		else{
//			suspiciousNode = conflictNode;
//		}
//		
//		return suspiciousNode;
	}
	
	private void setCurrentNodeCheck(Trace trace, TraceNode currentNode) {
		int checkTime = trace.getCheckTime()+1;
		currentNode.setCheckTime(checkTime);
		trace.setCheckTime(checkTime);
	}


	public PairList getPairList() {
		return pairList;
	}


	public void setPairList(PairList pairList) {
		this.pairList = pairList;
	}


	
}
