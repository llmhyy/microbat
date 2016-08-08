package microbat.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.trace.TraceNodeReverseOrderComparator;
import microbat.model.value.VarValue;
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
				new LCSMatcher(new TraceNodeComprehensiveSimilarityComparator()));
		
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

	
	PairList pairList;
	TraceNode rootCause;
	Map<Integer, TraceNode> allWrongNodeMap;
	TraceNode observedFaultNode;
	
	public void prepare(Trace mutatedTrace, Trace correctTrace, ClassLocation mutatedLocation, 
			String testCaseName, String mutatedFile){
//		PairList pairList = DiffUtil.generateMatchedTraceNodeList(mutatedTrace, correctTrace);
		pairList = matchTraceNodePair(mutatedTrace, correctTrace); 
		
		rootCause = findRootCause(mutatedLocation.getClassCanonicalName(), 
				mutatedLocation.getLineNo(), mutatedTrace, pairList);
		
		allWrongNodeMap = findAllWrongNodes(pairList, mutatedTrace);
		
		if(!allWrongNodeMap.isEmpty()){
			List<TraceNode> wrongNodeList = new ArrayList<>(allWrongNodeMap.values());
			Collections.sort(wrongNodeList, new TraceNodeReverseOrderComparator());
			observedFaultNode = findObservedFault(wrongNodeList);
		}
		
	}

	public Trial detectMutatedBug(Trace mutatedTrace, Trace correctTrace, ClassLocation mutatedLocation, 
			String testCaseName, String mutatedFile, double unclearRate, boolean enableLoopInference) 
					throws SimulationFailException {
		mutatedTrace.resetCheckTime();
		if(observedFaultNode != null){
			try {
				Trial trial = startSimulation(observedFaultNode, rootCause, mutatedTrace, allWrongNodeMap, pairList, 
						testCaseName, mutatedFile, unclearRate, enableLoopInference);
				System.currentTimeMillis();
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
	
	

	private TraceNode findObservedFault(List<TraceNode> wrongNodeList){
		TraceNode observedFaultNode = null;
		
		for(TraceNode node: wrongNodeList){
			//if(!JTestUtil.isInTestCase(node.getDeclaringCompilationUnitName())){
				observedFaultNode = node;
				break;
			//}
		}
		
		return observedFaultNode;
	}
	
	private Trial startSimulation(TraceNode observedFaultNode, TraceNode rootCause, Trace mutatedTrace, 
			Map<Integer, TraceNode> allWrongNodeMap, PairList pairList, String testCaseName, String mutatedFile, 
			double unclearRate, boolean enableLoopInference) 
					throws SimulationFailException {
		Settings.interestedVariables.clear();
		Settings.localVariableScopes.clear();
		Settings.potentialCorrectPatterns.clear();
		
		recommender = new StepRecommender(enableLoopInference);
		user = new SimulatedUser();
		
		int traceLength = mutatedTrace.getExectionList().size();
		int maxUnclearFeedbackNum = (int)(traceLength*unclearRate);
		if(unclearRate == -1){
			maxUnclearFeedbackNum = traceLength;
		}
		
		Stack<StateWrapper> confusingStack = new Stack<>();
		ArrayList<StepOperationTuple> jumpingSteps = new ArrayList<>();
		
		try{
			TraceNode lastNode = observedFaultNode;
			TraceNode suspiciousNode = observedFaultNode;
			
			String feedbackType = operateFeedback(observedFaultNode,
					mutatedTrace, pairList, maxUnclearFeedbackNum, confusingStack,
					jumpingSteps, true);
			
			TraceNodePair pair = pairList.findByMutatedNode(suspiciousNode);
			TraceNode referenceNode = (pair==null)? null : pair.getOriginalNode();
			
			jumpingSteps.add(new StepOperationTuple(suspiciousNode, feedbackType, referenceNode));
			
			if(!feedbackType.equals(UserFeedback.UNCLEAR)){
				setCurrentNodeChecked(mutatedTrace, suspiciousNode);		
				updateVariableCheckTime(mutatedTrace, suspiciousNode);
			}
			
			int feedbackTimes = 1;
			
			boolean isBugFound = rootCause.getLineNumber()==suspiciousNode.getLineNumber();
			while(!isBugFound){
				suspiciousNode = findSuspicioiusNode(suspiciousNode, mutatedTrace, feedbackType);
				
				/** It means that the bug cannot be found now */
				if((suspiciousNode.getOrder() == lastNode.getOrder() 
						&& !feedbackType.equals(UserFeedback.UNCLEAR)) 
						|| (jumpingSteps.size() > mutatedTrace.size())){
//					break;
					
					System.out.println("=========An attempt fails=========");
					for(StepOperationTuple t: jumpingSteps){
						System.err.println(t);				
					}
					System.out.println();
					
					if(!confusingStack.isEmpty()){
						/** recover */
						StateWrapper stateWrapper = confusingStack.pop();
						
						jumpingSteps = stateWrapper.getJumpingSteps();

						CheckingState state = stateWrapper.getState();
						int checkTime = state.getTraceCheckTime();
						
						mutatedTrace.setCheckTime(state.getTraceCheckTime());
						suspiciousNode = mutatedTrace.getExectionList().get(state.getCurrentNodeOrder()-1);
						suspiciousNode.setSuspicousScoreMap(state.getCurrentNodeSuspicousScoreMap());
						suspiciousNode.setCheckTime(state.getCurrentNodeCheckTime());
						
						Settings.interestedVariables = state.getInterestedVariables();
						Settings.potentialCorrectPatterns = state.getPotentialCorrectPatterns();
						Settings.wrongPathNodeOrder = state.getWrongPathNodeOrder();
						recommender = state.getRecommender();
						
						List<String> wrongVarIDs = stateWrapper.getChoosingVarID();
						for(String wrongVarID: wrongVarIDs){
							Settings.interestedVariables.add(wrongVarID, checkTime);
						}
						feedbackType = UserFeedback.INCORRECT;
						
						pair = pairList.findByMutatedNode(suspiciousNode);
						referenceNode = (pair==null)? null : pair.getOriginalNode();
						jumpingSteps.add(new StepOperationTuple(suspiciousNode, feedbackType, referenceNode));
					}
					else{
						break;						
					}
				}
				else{
					isBugFound = rootCause.getLineNumber()==suspiciousNode.getLineNumber();
					
					if(!isBugFound){
						if(suspiciousNode.getOrder() == 132){
							System.currentTimeMillis();
						}
						
						feedbackType = operateFeedback(suspiciousNode,
								mutatedTrace, pairList, maxUnclearFeedbackNum, confusingStack,
								jumpingSteps, false);
						
						pair = pairList.findByMutatedNode(suspiciousNode);
						referenceNode = (pair==null)? null : pair.getOriginalNode();
						
						jumpingSteps.add(new StepOperationTuple(suspiciousNode, feedbackType, referenceNode));
						
						if(!feedbackType.equals(UserFeedback.UNCLEAR)){
							setCurrentNodeChecked(mutatedTrace, suspiciousNode);		
							updateVariableCheckTime(mutatedTrace, suspiciousNode);
						}
						
						feedbackTimes++;
						
						if(feedbackTimes > mutatedTrace.size()){
							break;
						}
					}
					else{
						jumpingSteps.add(new StepOperationTuple(suspiciousNode, "Bug Found", null));
					}
					
				}
				
				lastNode = suspiciousNode;
			}
			
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
	private String operateFeedback(TraceNode suspiciousNode,
			Trace mutatedTrace, PairList pairList, int maxUnclearFeedbackNum,
			Stack<StateWrapper> confusingStack,
			ArrayList<StepOperationTuple> jumpingSteps,
			boolean isFirstTime) {
		
		CheckingState state = new CheckingState();
		state.recordCheckingState(suspiciousNode, recommender, mutatedTrace, 
				Settings.interestedVariables, Settings.wrongPathNodeOrder, Settings.potentialCorrectPatterns);
		
		String feedbackType = user.feedback(suspiciousNode, mutatedTrace, pairList, 
				mutatedTrace.getCheckTime(), isFirstTime, maxUnclearFeedbackNum);
		
		/** 
		 * Adjust the effect of constraints. 
		 * If last feedback is wrong-path, then this step should not be
		 * a correct step.
		 */
		String lastFeedback = jumpingSteps.isEmpty() ? null : 
			jumpingSteps.get(jumpingSteps.size()-1).getUserFeedback();
		if(lastFeedback != null && lastFeedback.equals(UserFeedback.WRONG_PATH) 
				&& feedbackType.equals(UserFeedback.CORRECT)){
			feedbackType = UserFeedback.WRONG_PATH;
		}
		
		for(List<String> wrongVarIDs: user.getOtherOptions()){
			ArrayList<StepOperationTuple> clonedJumpingSteps = (ArrayList<StepOperationTuple>) jumpingSteps.clone();
			StateWrapper stateWrapper = new StateWrapper(state, wrongVarIDs, clonedJumpingSteps);
			confusingStack.push(stateWrapper);
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
		
		for(TraceNode mutatedNode: mutationTrace.getExectionList()){
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
		for(TraceNode mutatedTraceNode: mutatedTrace.getExectionList()){
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
		for(TraceNode node: mutatedTrace.getExectionList()){
			if(node.getDeclaringCompilationUnitName().equals(className) && node.getLineNumber()==lineNo){
				TraceNodePair pair = pairList.findByMutatedNode(node);
				
				if(pair == null){
					System.currentTimeMillis();
				}
				
				return pair.getMutatedNode();
			}
		}
		
		System.currentTimeMillis();
		
		return null;
	}

	private TraceNode findSuspicioiusNode(TraceNode currentNode, Trace trace, String feedbackType) {
		setCurrentNodeCheck(trace, currentNode);
		
		
		if(!feedbackType.equals(UserFeedback.UNCLEAR)){
			setCurrentNodeCheck(trace, currentNode);					
		}
		
		TraceNode suspiciousNode = recommender.recommendNode(trace, currentNode, feedbackType);
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


	
}
