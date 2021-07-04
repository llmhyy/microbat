package microbat.recommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import microbat.model.Cause;
import microbat.model.trace.PathInstance;
import microbat.model.trace.PotentialCorrectPattern;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.trace.TraceNodeOrderComparator;
import microbat.model.value.VarValue;
import microbat.model.variable.Variable;
import microbat.util.Settings;
import microbat.util.VariableUtil;

public class StepRecommender {
	
	public class LoopRange{
		/**
		 * all the skipped trace node by loop inference.
		 */
		ArrayList<TraceNode> skipPoints = new ArrayList<>();
		TraceNode startNode;
		TraceNode endNode;
		
		TraceNode binaryLandmark;

		public TraceNode binarySearch() {
			Collections.sort(skipPoints, new TraceNodeOrderComparator());
			
			int startIndex = skipPoints.indexOf(startNode);
			int endIndex = skipPoints.indexOf(endNode);
			if(endIndex == -1){
				endIndex = skipPoints.size()-1;
			}
			
			int index = (startIndex+endIndex)/2;
			if(index == startIndex){
				index = endIndex;
			}
			
			return skipPoints.get(index);
		}
		
		@SuppressWarnings("unchecked")
		public LoopRange clone(){
			LoopRange loopRange = new LoopRange();
			loopRange.startNode = this.startNode;
			loopRange.endNode = this.endNode;
			loopRange.binaryLandmark = this.binaryLandmark;
			loopRange.skipPoints = (ArrayList<TraceNode>) this.skipPoints.clone();
			
			return loopRange;
		}
		
		public List<TraceNode> getSkipPoints(){
			return this.skipPoints;
		}

		public TraceNode findCorrespondingStartNode(TraceNode endNode2) {
			for(int i=0; i<skipPoints.size(); i++){
				TraceNode node = skipPoints.get(i);
				if(node.getOrder() == endNode2.getOrder()){
					if(i>0){
						TraceNode startNode = skipPoints.get(i-1);
						return startNode;						
					}
					else{
						return this.startNode;
					}
				}
			}
			
			return null;
		}
		
		public TraceNode getBinaryLandMark(){
			return binaryLandmark;
		}

		public void clearSkipPoints() {
			binaryLandmark = null;
			skipPoints.clear();
		}

		TraceNode backupStartNode;
		TraceNode backupEndNode;
		
		public void backupStartAndEndNode() {
			backupStartNode = startNode;
			backupEndNode = endNode;
		}

		public boolean checkedSkipPointsContains(TraceNode suspiciousNode) {
			for(TraceNode skipPoint: skipPoints){
				if(skipPoint.hasChecked() && skipPoint.equals(suspiciousNode)){
					return true;
				}
			}
			return false;
		}
	}
	
	
	
	public StepRecommender(boolean enableLoopInference){
		this.setEnableLoopInference(enableLoopInference);
	}
	
	private boolean enableLoopInference = true;
	
	private int state = DebugState.SCLICING;
	
	/**
	 * Fields for clear state.
	 */
	private int latestClearState = -1;
	private TraceNode lastNode;
//	private TraceNode lastRecommendNode;
	private Cause latestCause = new Cause();
	private LoopRange loopRange = new LoopRange();
	private DetailInspector detailInspector = DetailInspectorFactory.createInspector();
	
	private List<TraceNode> visitedUnclearNodeList = new ArrayList<>();
	
	public TraceNode recommendNode(Trace trace, TraceNode currentNode, UserFeedback userFeedback){
		InspectingRange range = detailInspector.inspectingRange;
		detailInspector = DetailInspectorFactory.createInspector();
		detailInspector.inspectingRange = range;
		
		String feedbackType = userFeedback.getFeedbackType();
		if(feedbackType.equals(UserFeedback.WRONG_PATH)){
			Settings.wrongPathNodeOrder.add(currentNode.getOrder());
		}
		
		if(feedbackType.equals(UserFeedback.UNCLEAR)){
			
			if(state==DebugState.SCLICING || state==DebugState.SKIP || state==DebugState.BINARY_SEARCH || state==DebugState.DETAIL_INSPECT){
				latestClearState = state;
			}
			
			state = DebugState.UNCLEAR;
			visitedUnclearNodeList.add(currentNode);
			Collections.sort(visitedUnclearNodeList, new TraceNodeOrderComparator());
			//TraceNode node = findMoreClearNode(trace, currentNode);
			TraceNode node = currentNode.getAbstractionParent();
			if(node == null){
				node = currentNode.getStepInPrevious();
				if(node == null){
					node = currentNode;
				}
			}
			
			return node;
		}
		else if((state==DebugState.UNCLEAR || state==DebugState.PARTIAL_CLEAR) && feedbackType.equals(UserFeedback.CORRECT)){
			state = DebugState.PARTIAL_CLEAR;
			
			Iterator<TraceNode> iter = visitedUnclearNodeList.iterator();
			while(iter.hasNext()){
				TraceNode visitedUnclearNode = iter.next();
				if(visitedUnclearNode.getOrder() <= currentNode.getOrder()){
					iter.remove();
				}
			}
			TraceNode earliestVisitedNode = null;
			if(!visitedUnclearNodeList.isEmpty()){
				earliestVisitedNode = visitedUnclearNodeList.get(0);
			}
			
			if(earliestVisitedNode == null){
				state = latestClearState;
				TraceNode node = recommendSuspiciousNode(trace, currentNode, userFeedback);
				return node;
			}
			else{
				TraceNode node = findClearerNodeInBetween(trace, currentNode, earliestVisitedNode);
				if(node.equals(currentNode)){
					return earliestVisitedNode;
				}
				else{
					return node;
				}
			}
		}
		else if((state==DebugState.UNCLEAR || state==DebugState.PARTIAL_CLEAR) && 
				(feedbackType.equals(UserFeedback.WRONG_VARIABLE_VALUE) || feedbackType.equals(UserFeedback.WRONG_PATH))){
			visitedUnclearNodeList.clear();
			state = latestClearState;
			TraceNode node = recommendSuspiciousNode(trace, currentNode, userFeedback);
			return node;
		}
		else{
			TraceNode node = recommendSuspiciousNode(trace, currentNode, userFeedback);
			return node;
		}
	}
	
	private TraceNode findClearerNodeInBetween(Trace trace, TraceNode currentNode, TraceNode earliestVisitedUnclearNode) {
		TraceNode latestWrongNode = trace.getLatestWrongNode();
		
		if(latestWrongNode != null){
			Map<Integer, TraceNode> dominatorMap = latestWrongNode.findAllDominators();
			List<TraceNode> dominators = new ArrayList<>(dominatorMap.values());
			Collections.sort(dominators, new TraceNodeOrderComparator());
			
			Iterator<TraceNode> iter = dominators.iterator();
			while(iter.hasNext()){
				TraceNode dominator = iter.next();
				boolean shouldRemove = dominator.getOrder() < currentNode.getOrder() || 
						dominator.getOrder() > earliestVisitedUnclearNode.getOrder() ||
						dominator.getAbstractionLevel() > earliestVisitedUnclearNode.getAbstractionLevel();
				if(shouldRemove){
					iter.remove();
				}
			}
			
			if(!dominators.isEmpty()){
				int index = dominators.size()/2;
				TraceNode moreDetailedNodeInBetween = dominators.get(index);
				return moreDetailedNodeInBetween;
			}
			else{
				return earliestVisitedUnclearNode;
			}
		}
		else{
			System.err.println("Cannot find latest wrong node in findMoreDetailedNodeInBetween()");
		}
		
		return null;
	}

	private TraceNode recommendSuspiciousNode(Trace trace, TraceNode currentNode, UserFeedback userFeedBack){
		
		if(lastNode != null){
			PathInstance path = new PathInstance(currentNode, lastNode);
			if(path.isPotentialCorrect()){
				Settings.potentialCorrectPatterns.addPathForPattern(path);			
			}
		}
		
		TraceNode lastRecommendNode = latestCause.getRecommendedNode();
		if(lastRecommendNode!= null && !currentNode.equals(lastRecommendNode)){
			state = DebugState.SCLICING;
		}
		
		TraceNode suspiciousNode = null;
		if(state == DebugState.SCLICING){
			suspiciousNode = handleSimpleInferenceState(trace, currentNode, userFeedBack);
			
		}
		else if(state == DebugState.SKIP){
			suspiciousNode = handleSkipState(trace, currentNode, userFeedBack);
		}
		else if(state == DebugState.BINARY_SEARCH){
			suspiciousNode = handleBinarySearchState(trace, currentNode, userFeedBack);
		}
		else if(state == DebugState.DETAIL_INSPECT){
			suspiciousNode = handleDetailInspectingState(trace, currentNode, userFeedBack);
		}
		
		latestCause.setRecommendedNode(suspiciousNode);
		
		return suspiciousNode;
	}

	private TraceNode handleBinarySearchState(Trace trace, TraceNode currentNode, UserFeedback userFeedback) {
		TraceNode suspiciousNode = null;
		
		boolean isOverSkipping = currentNode.isAllReadWrittenVarCorrect(false);
		if(isOverSkipping){
			state = DebugState.BINARY_SEARCH;
			
			this.loopRange.startNode = currentNode;
//			this.range.update();
			
			suspiciousNode = this.loopRange.binarySearch();
			
			this.loopRange.binaryLandmark = suspiciousNode;
			this.lastNode = currentNode;
		}
		else{
			TraceNode endNode = currentNode;
			TraceNode startNode = this.loopRange.findCorrespondingStartNode(endNode);
			if(startNode != null){
				PathInstance fakePath = new PathInstance(startNode, endNode);
				
				PotentialCorrectPattern pattern = Settings.potentialCorrectPatterns.getPattern(fakePath);
				if(pattern != null){
					PathInstance labelPath = pattern.getLabelInstance();
					Variable causingVariable = labelPath.findCausingVar();
					
					Variable compatiableVariable = findCompatibleReadVariable(causingVariable, currentNode);
					if(compatiableVariable != null){
						
						this.latestCause.setBuggyNode(currentNode);
						this.latestCause.setWrongPath(false);
						this.latestCause.setWrongVariableID(compatiableVariable.getVarID());
						
						state = DebugState.BINARY_SEARCH;
						if(currentNode.isAllReadWrittenVarCorrect(false)){
							this.loopRange.startNode = currentNode;
						}
						else{
							this.loopRange.endNode = currentNode;
						}
						
//						this.range.update();
						suspiciousNode = this.loopRange.binarySearch();
						
						this.lastNode = currentNode;
					}
					else{
						state = DebugState.SCLICING;
						suspiciousNode = handleSimpleInferenceState(trace, currentNode, userFeedback);
					}
				}
				else{
					System.err.println("error in binary search for " + fakePath.getLineTrace() + ", cannot find corresponding pattern");
				}
			}
			else{
				System.err.println("cannot find the start node in binary_search state");
			}
			
		}
		
		
		return suspiciousNode;
	}

	private Variable findCompatibleReadVariable(Variable causingVariable, TraceNode currentNode) {
		List<VarValue> markedReadVars = currentNode.findMarkedReadVariable();
		for(VarValue readVar: markedReadVars){
			Variable readVariable = readVar.getVariable();
			if(VariableUtil.isEquivalentVariable(causingVariable, readVariable)){
				return readVariable;
			}
		}
		
		return null;
	}

	private TraceNode handleSkipState(Trace trace, TraceNode currentNode, UserFeedback userFeedback) {
		TraceNode suspiciousNode;
		boolean isOverSkipping = currentNode.isAllReadWrittenVarCorrect(false);
		if(isOverSkipping){
			state = DebugState.BINARY_SEARCH;
			
			this.loopRange.startNode = currentNode;
			this.loopRange.backupStartAndEndNode();
			
			suspiciousNode = this.loopRange.binarySearch();
			
			this.loopRange.binaryLandmark = suspiciousNode;
			this.lastNode = currentNode;
		}
		else{
			state = DebugState.SCLICING;
			
			this.loopRange.clearSkipPoints();
			
			suspiciousNode = handleSimpleInferenceState(trace, currentNode, userFeedback);
		}
		return suspiciousNode;
	}
	
	private TraceNode handleControlSlicing(Trace trace, TraceNode currentNode, String feedback){
//		TraceNode suspiciousNode = trace.findSuspiciousControlDominator(currentNode, feedback);
		TraceNode suspiciousNode = currentNode.getControlDominator();
		
		this.latestCause.setBuggyNode(currentNode);
		this.latestCause.setWrongPath(true);
		this.latestCause.setWrongVariableID(null);
		
		return suspiciousNode;
	}
	
	private TraceNode handleDataSlicing(Trace trace, TraceNode currentNode, UserFeedback userFeedback){
		
		String userFeedBackType = userFeedback.getFeedbackType();
		
		VarValue wrongVar = userFeedback.getOption().getReadVar();
		if(wrongVar == null){
			return currentNode;
		}
		
		String wrongVarID = wrongVar.getVarID();
		this.latestCause.setWrongVariableID(wrongVar.getVarID());
		wrongVarID = wrongVar.getVarID();
		
		/**
		 * no variable has been selected yet.
		 */
		if(wrongVarID == null){
			return currentNode;
		}
		
		/**
		 *
		 */
		TraceNode suspiciousNode = trace.findProducer(wrongVar, currentNode);
//		String parentVarID = wrongVarID;
//		while(suspiciousNode == null){
//			parentVarID = VariableUtil.generateSimpleParentVariableID(parentVarID);
//			/** It means that the wrong variable ID has a parent*/
//			if(parentVarID != null){
//				suspiciousNode = trace.getLatestProducerBySimpleVarIDForm(currentNode.getOrder(), parentVarID);
//			}
//			else{
//				return currentNode;			
//			}
//		}
		
		if(suspiciousNode==null){
			return currentNode;
		}
		
		if(userFeedBackType.equals(UserFeedback.WRONG_VARIABLE_VALUE)){
			this.latestCause.setBuggyNode(currentNode);
			this.latestCause.setWrongVariableID(wrongVarID);
			this.latestCause.setWrongPath(false);			
		}
		
		
		boolean isPathInPattern = false;
		PathInstance path = null;
		if(this.latestCause.getBuggyNode() != null){
			path = new PathInstance(suspiciousNode, this.latestCause.getBuggyNode());
			isPathInPattern = Settings.potentialCorrectPatterns.containsPattern(path);				
		}
		
		if(this.isEnableLoopInference() && isPathInPattern && !shouldStopOnCheckedNode(currentNode, path)){
			state = DebugState.SKIP;
			
			this.loopRange.endNode = path.getEndNode();
			this.loopRange.skipPoints.clear();
			
			TraceNode oldSusiciousNode = suspiciousNode;
			while(Settings.potentialCorrectPatterns.containsPattern(path) 
					&& !shouldStopOnCheckedNode(suspiciousNode, path)){
				
				Settings.potentialCorrectPatterns.addPathForPattern(path);
				
				oldSusiciousNode = suspiciousNode;
				
				suspiciousNode = Settings.potentialCorrectPatterns.inferNextSuspiciousNode(oldSusiciousNode);
				System.currentTimeMillis();
				
				if(suspiciousNode == null){
					break;
				}
				else{
					this.loopRange.skipPoints.add(oldSusiciousNode);
					path = new PathInstance(suspiciousNode, oldSusiciousNode);					
				}
			}
			
			/**
			 * In this case, it means that there is actually nothing skipped.
			 */
			if(this.loopRange.skipPoints.isEmpty()){
				state = DebugState.SCLICING;
			}
			
			this.lastNode = currentNode;
			return oldSusiciousNode;
		}
		else{
			state = DebugState.SCLICING;
			
			this.lastNode = currentNode;
			
			return suspiciousNode;				
		}
		
	}

	/**
	 * Given the state is simple inference, which means the state is caused by either simple data dominance or simple
	 * control dominance, this method interpret the user feedback into events to transit to a new state. 
	 * 
	 * @param trace
	 * @param currentNode
	 * @param userFeedBack
	 * @return
	 */
	private TraceNode handleSimpleInferenceState(Trace trace, TraceNode currentNode, UserFeedback userFeedBack) {
		
		TraceNode node;
		if(userFeedBack.getFeedbackType().equals(UserFeedback.WRONG_PATH)){
			node = handleControlSlicing(trace, currentNode, userFeedBack.getFeedbackType());
		}
		else if(userFeedBack.getFeedbackType().equals(UserFeedback.CORRECT)){
			//TODO it could be done in a more intelligent way.
			
			InspectingRange inspectingRange = new InspectingRange(currentNode, latestCause.getBuggyNode());
			this.detailInspector.setInspectingRange(inspectingRange);
			
			TraceNode recommendedNode = handleDetailInspectingState(trace, currentNode, userFeedBack);
			return recommendedNode;
		}
		else{
			node = handleDataSlicing(trace, currentNode, userFeedBack);
		}
		
		return node;
	}
	
	private boolean shouldStopOnCheckedNode(TraceNode suspiciousNode, PathInstance path) {
		if(!suspiciousNode.hasChecked()){
			return false;
		}
		else{
			if(suspiciousNode.isAllReadWrittenVarCorrect(false)){
				return true;
			}
			else{
				PotentialCorrectPattern pattern = Settings.potentialCorrectPatterns.getPattern(path);
				if(pattern != null){
					PathInstance labelPath = pattern.getLabelInstance();
					Variable causingVariable = labelPath.findCausingVar();
					
					Variable compatibaleVariable = findCompatibleReadVariable(causingVariable, suspiciousNode);
					if(compatibaleVariable != null){
						return false;
					}
				}
			}
		}
		
		return true;
	}

	private TraceNode handleDetailInspectingState(Trace trace, TraceNode currentNode, UserFeedback userFeedBack) {
		
		if(userFeedBack.getFeedbackType().equals(UserFeedback.CORRECT)){
			this.state = DebugState.DETAIL_INSPECT;
			
			VarValue wrongValue = null;
			for(TraceNode n: lastNode.getDataDominators().keySet()){
				if(n.getOrder()==currentNode.getOrder()){
					wrongValue = lastNode.getDataDominators().get(currentNode);
				}
			}
			TraceNode nextNode = this.detailInspector.recommendDetailNode(currentNode, trace, wrongValue);
			return nextNode;
		}
		else{
			TraceNode node = handleSimpleInferenceState(trace, currentNode, userFeedBack);
			return node;
		}
	}

	/**
	 * Find the variable causing the jump of label path of the <code>pattern</code>, noted as <code>var</code>, 
	 * then try finding the dominator of the <code>oldSusiciousNode</code> by following the same dominance chain 
	 * with regard to <code>var</code>. The dominator of <code>oldSusiciousNode</code> on <code>var</code> is the
	 * new suspicious node. 
	 * <br><br>
	 * If there is no such dominator, this method return null.
	 * 
	 * @param pattern
	 * @param oldSusiciousNode
	 * @return
	 */
	public TraceNode findNextSuspiciousNodeByPattern(PotentialCorrectPattern pattern, 
			TraceNode oldSusiciousNode){
		PathInstance labelPath = pattern.getLabelInstance();
		
		Variable causingVariable = labelPath.findCausingVar();
		
		for(TraceNode dominator: oldSusiciousNode.getDataDominators().keySet()){
			for(VarValue writtenVar: dominator.getWrittenVariables()){
				Variable writtenVariable = writtenVar.getVariable();
				
				if(VariableUtil.isEquivalentVariable(causingVariable, writtenVariable)){
					return dominator;					
				}
			}
		}
		
		
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	public StepRecommender clone(){
		StepRecommender recommender = new StepRecommender(this.isEnableLoopInference());
		recommender.state = this.state;
		recommender.lastNode = this.lastNode;
		//recommender.lastRecommendNode = this.lastRecommendNode;
		recommender.setLatestCause(this.latestCause.clone());
		recommender.latestClearState = this.latestClearState;
		recommender.loopRange = this.loopRange.clone();
//		if(this.inspectingRange != null){
//			recommender.inspectingRange = this.inspectingRange.clone();			
//		}
		recommender.detailInspector = this.detailInspector.clone();
		ArrayList<TraceNode> list = (ArrayList<TraceNode>)this.visitedUnclearNodeList;
		recommender.visitedUnclearNodeList = (ArrayList<TraceNode>) list.clone();
		
		return recommender;
	}
	
	public int getState(){
		return state;
	}
	
	public LoopRange getLoopRange(){
		return this.loopRange;
	}

	public Cause getLatestCause() {
		return latestCause;
	}

	public void setLatestCause(Cause latestCause) {
		this.latestCause = latestCause;
	}

	public boolean isEnableLoopInference() {
		return enableLoopInference;
	}

	public void setEnableLoopInference(boolean enableLoopInference) {
		this.enableLoopInference = enableLoopInference;
	}
}
