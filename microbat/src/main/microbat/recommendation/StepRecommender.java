package microbat.recommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import microbat.model.AttributionVar;
import microbat.model.Cause;
import microbat.model.trace.LoopSequence;
import microbat.model.trace.PathInstance;
import microbat.model.trace.PotentialCorrectPattern;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.trace.TraceNodeOrderComparator;
import microbat.model.trace.TraceNodeReverseOrderComparator;
import microbat.model.value.VarValue;
import microbat.model.variable.Variable;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;

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
	
	public class InspectingRange{
		TraceNode startNode;
		TraceNode endNode;
		
		private InspectingRange(TraceNode startNode, TraceNode endNode){
			this.startNode = startNode;
			this.endNode = endNode;
		}
		
//		public InspectingRange(Map<TraceNode, List<String>> dataDominator,
//				TraceNode suspiciousNode) {
//			ArrayList<TraceNode> dominators = new ArrayList<TraceNode>(dataDominator.keySet());
//			Collections.sort(dominators, new TraceNodeOrderComparator());
//			
//			startNode = dominators.get(0);
//			endNode = suspiciousNode;
//		}

		public InspectingRange clone(){
			InspectingRange inspectingRange = new InspectingRange(startNode, endNode);
			return inspectingRange;
		}
	}
	
	private int state = DebugState.JUMP;
	
	/**
	 * Fields for clear state.
	 */
	private int latestClearState = -1;
	private TraceNode lastNode;
//	private TraceNode lastRecommendNode;
	private Cause latestCause = new Cause();
	private LoopRange loopRange = new LoopRange();
	private InspectingRange inspectingRange;
	
	private List<TraceNode> visitedUnclearNodeList = new ArrayList<>();
	
	public TraceNode recommendNode(Trace trace, TraceNode currentNode, String feedback){
		if(feedback.equals(UserFeedback.UNCLEAR)){
			
			if(state==DebugState.JUMP || state==DebugState.SKIP || state==DebugState.BINARY_SEARCH || state==DebugState.DETAIL_INSPECT){
				latestClearState = state;
			}
			
			state = DebugState.UNCLEAR;
			visitedUnclearNodeList.add(currentNode);
			Collections.sort(visitedUnclearNodeList, new TraceNodeOrderComparator());
			TraceNode node = findMoreClearNode(trace, currentNode);
			return node;
		}
		else if((state==DebugState.UNCLEAR || state==DebugState.PARTIAL_CLEAR) && feedback.equals(UserFeedback.CORRECT)){
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
				TraceNode node = recommendSuspiciousNode(trace, currentNode, feedback);
				return node;
			}
			else{
				TraceNode node = findMoreDetailedNodeInBetween(trace, currentNode, earliestVisitedNode);
				if(node.equals(currentNode)){
					return earliestVisitedNode;
				}
				else{
					return node;
				}
			}
		}
		else if((state==DebugState.UNCLEAR || state==DebugState.PARTIAL_CLEAR) && feedback.equals(UserFeedback.INCORRECT)){
			visitedUnclearNodeList.clear();
			state = latestClearState;
			TraceNode node = recommendSuspiciousNode(trace, currentNode, feedback);
			return node;
		}
		else{
			TraceNode node = recommendSuspiciousNode(trace, currentNode, feedback);
			return node;
		}
	}
	
	private TraceNode findMoreDetailedNodeInBetween(Trace trace, TraceNode currentNode, TraceNode earliestVisitedUnclearNode) {
		TraceNode earliestNodeWithWrongVar = trace.getEarliestNodeWithWrongVar();
		
		if(earliestNodeWithWrongVar != null){
			Map<Integer, TraceNode> dominatorMap = earliestNodeWithWrongVar.findAllDominators();
			List<TraceNode> dominators = new ArrayList<>(dominatorMap.values());
			Collections.sort(dominators, new TraceNodeOrderComparator());
			
			Iterator<TraceNode> iter = dominators.iterator();
			while(iter.hasNext()){
				TraceNode dominator = iter.next();
				boolean shouldRemove = dominator.getOrder() < currentNode.getOrder() || 
						dominator.getOrder() > earliestVisitedUnclearNode.getOrder() ||
						dominator.getInvocationLevel() > earliestVisitedUnclearNode.getInvocationLevel();
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
			System.err.println("Cannot find earliestNodeWithWrongVar in findMoreDetailedNodeInBetween()");
		}
		
		return null;
	}

	/**
	 * A dominator is an abstract dominator if it is either a method invocation or a loop head.
	 * @param loopSequence
	 * @param dominators, *sorted in a reverse order*
	 * @param currentNode
	 * @return
	 */
	private TraceNode findMoreAbstractDominator(LoopSequence loopSequence, List<TraceNode> dominators, TraceNode currentNode, Trace trace){
		
		TraceNode latestCorrectNode = trace.getLatestCorrectNode();
		
		TraceNode moreAbstractDominator = null;
		for(TraceNode dominator: dominators){
			
			boolean flag = (latestCorrectNode == null) ? false : dominator.getOrder() < latestCorrectNode.getOrder();
			
			if(dominator.getOrder() > currentNode.getOrder() || flag){
				continue;
			}
			
			if(dominator.getInvocationLevel() < currentNode.getInvocationLevel()){
				/**
				 * method invocation
				 */
				if(dominator.getOrder() <= currentNode.getInvocationParent().getOrder()){
					moreAbstractDominator = dominator;		
					break;
				}
			}

			if(dominator.getInvocationLevel() == currentNode.getInvocationLevel()){
				if(latestCorrectNode != null && dominator.getOrder() > latestCorrectNode.getOrder()){
					moreAbstractDominator = dominator;
				}
				
				/**
				 * loop head
				 */
				if(loopSequence != null){
					if(loopSequence.containsRangeOf(dominator) && 
							loopSequence.getStartOrder() != dominator.getOrder()){
						continue;
					}
					else{
						moreAbstractDominator = dominator;
						break;
					}
				}
			}
		}
		
		return moreAbstractDominator;
	}
	
	/**
	 * Find the invocation parent or nearest loop control dominator. If the node has neither
	 * invocation parent nor nearest loop control dominator, the previous step-over node will
	 * be chosen.
	 * 
	 * @param trace
	 * @param currentNode
	 * @return
	 */
	private TraceNode findMoreClearNode(Trace trace, TraceNode currentNode){
		List<TraceNode> clearNodeList = new ArrayList<>();
		
		TraceNode invocationParent = currentNode.getInvocationParent();
		TraceNode containingLoopControlDominator = currentNode.findContainingLoopControlDominator();
		
		if(invocationParent != null){
			clearNodeList.add(invocationParent);
		}
		if(containingLoopControlDominator != null){
			clearNodeList.add(containingLoopControlDominator);
		}
		
		if(!clearNodeList.isEmpty()){
			Collections.sort(clearNodeList, new TraceNodeReverseOrderComparator());
			return clearNodeList.get(0);
		}
		else{
			int order = currentNode.getOrder() - 1;
			if(order-1>=0){
				return trace.getExectionList().get(order-1);				
			}
			else{
				return currentNode;
			}
		}
	}
	
	/**
	 * More clear node means the data or control dominator in method invocation or loop head for current node.
	 * <p>
	 * I am considering that the aim of more clear node is for understanding, thus, I decide to use a more simple
	 * way to suggest unclear node. @see {@link microbat.recommendation.StepRecommender#findMoreClearNode()}
	 * 
	 * @param trace
	 * @param currentNode
	 * @return
	 */
	@Deprecated
	private TraceNode findMoreClearNode0(Trace trace, TraceNode currentNode) {
		TraceNode earliestNodeWithWrongVar = trace.getEarliestNodeWithWrongVar();
		
		if(earliestNodeWithWrongVar != null){
			Map<Integer, TraceNode> dominatorMap = earliestNodeWithWrongVar.findAllDominators();
			List<TraceNode> dominators = new ArrayList<>(dominatorMap.values());
			Collections.sort(dominators, new TraceNodeReverseOrderComparator());
			
			LoopSequence loopSequence = trace.findLoopRangeOf(currentNode);
			TraceNode moreAbstractDominator = findMoreAbstractDominator(loopSequence, dominators, currentNode, trace);
			
			if(moreAbstractDominator != null){
				/**
				 * try to find a dominator which has not been checked, if not, I just return the abstract dominator 
				 */
				if(moreAbstractDominator.hasChecked()){
					int index = dominators.indexOf(moreAbstractDominator);
					for(int i=index; i>=0; i--){
						TraceNode dominator = dominators.get(i);
						if(!dominator.equals(moreAbstractDominator)){
							if(!dominator.hasChecked()){
								return dominator;
							}
						}
					}
					
					return moreAbstractDominator;
				}
				else{
					return moreAbstractDominator;
				}
			}
			else if(!dominators.isEmpty()){
				return dominators.get(0);
			}
		}
		
//		System.currentTimeMillis();
		
		return null;
	}

	private TraceNode recommendSuspiciousNode(Trace trace, TraceNode currentNode, String userFeedBack){
		
		if(lastNode != null){
			PathInstance path = new PathInstance(currentNode, lastNode);
			if(path.isPotentialCorrect()){
				Settings.potentialCorrectPatterns.addPathForPattern(path);			
			}
		}
		
		TraceNode lastRecommendNode = getLatestCause().getRecommendedNode();
		if(lastRecommendNode!= null && !currentNode.equals(lastRecommendNode)){
			state = DebugState.JUMP;
		}
		
		TraceNode suspiciousNode = null;
		if(state == DebugState.JUMP){
			suspiciousNode = handleJumpBehavior(trace, currentNode, userFeedBack);
		}
		else if(state == DebugState.SKIP){
			suspiciousNode = handleSkipBehavior(trace, currentNode, userFeedBack);
		}
		else if(state == DebugState.BINARY_SEARCH){
			suspiciousNode = handleBinarySearchBehavior(trace, currentNode, userFeedBack);
		}
		else if(state == DebugState.DETAIL_INSPECT){
			suspiciousNode = handleDetailInspecting(trace, currentNode, userFeedBack);
		}
		
		getLatestCause().setRecommendedNode(suspiciousNode);
		
		return suspiciousNode;
	}

	private TraceNode handleBinarySearchBehavior(Trace trace, TraceNode currentNode, String userFeedback) {
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
						state = DebugState.JUMP;
						suspiciousNode = handleJumpBehavior(trace, currentNode, userFeedback);
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
			if(MicroBatUtil.isEquivalentVariable(causingVariable, readVariable)){
				return readVariable;
			}
		}
		
		return null;
	}

	private TraceNode handleSkipBehavior(Trace trace, TraceNode currentNode, String userFeedback) {
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
			state = DebugState.JUMP;
			
			this.loopRange.clearSkipPoints();
			
			suspiciousNode = handleJumpBehavior(trace, currentNode, userFeedback);
		}
		return suspiciousNode;
	}
	
	private TraceNode handleWrongPath(Trace trace, TraceNode currentNode, String feedback){
		TraceNode suspiciousNode = trace.findControlSuspiciousDominator(currentNode, feedback);
		
		this.latestCause.setBuggyNode(currentNode);
		this.latestCause.setWrongPath(true);
		this.latestCause.setWrongVariableID(null);
		
		return suspiciousNode;
	}
	
	private TraceNode handleWrongValue(Trace trace, TraceNode currentNode, String userFeedBack){
		
		List<VarValue> wrongReadVars = currentNode.findMarkedReadVariable();
		VarValue wrongVar = (wrongReadVars.isEmpty())? null : wrongReadVars.get(0);
		String wrongVarID = null;
		
		if(wrongVar != null){
			getLatestCause().setWrongVariableID(wrongVar.getVarID());
			wrongVarID = wrongVar.getVarID();
		}
		/**
		 * otherwise, there is two cases: 
		 */
		else{
			wrongVarID = Settings.interestedVariables.getNewestVarID();
		}
		
		/**
		 * no variable has been selected yet.
		 */
		if(wrongVarID == null){
			return currentNode;
		}
		
		TraceNode suspiciousNode = trace.getProducer(wrongVarID);
		
		if(suspiciousNode == null){
			return currentNode;
		}
		
		if(userFeedBack.equals(UserFeedback.INCORRECT)){
			this.latestCause.setBuggyNode(currentNode);
			this.latestCause.setWrongVariableID(wrongVarID);
			this.latestCause.setWrongPath(false);			
		}
		
		/**
		 * In this case, user provide an "incorrect" feedback for current node, while the wrong 
		 * variable in latest buggy node is caused by the definition of suspicious node. If this 
		 * suspicious node has been checked and the read variables are correct, then the bug
		 * happens between the suspicious node and buggy node. Therefore, we enter the INSPECT_DETAIL
		 * state. 
		 */
		if(userFeedBack.equals(UserFeedback.CORRECT) && 
				suspiciousNode.hasChecked() && suspiciousNode.findMarkedReadVariable().isEmpty()){
			//TODO it could be done in a more intelligent way.
			this.inspectingRange = new InspectingRange(suspiciousNode, getLatestCause().getBuggyNode());
			TraceNode recommendedNode = handleDetailInspecting(trace, currentNode, userFeedBack);
			return recommendedNode;
		}
		else{
			
			boolean isPathInPattern = false;
			PathInstance path = null;
			if(getLatestCause().getBuggyNode() != null){
				path = new PathInstance(suspiciousNode, getLatestCause().getBuggyNode());
				isPathInPattern = Settings.potentialCorrectPatterns.containsPattern(path);	//TODO				
			}
			
			if(isPathInPattern && !shouldStopOnCheckedNode(currentNode, path)){
				state = DebugState.SKIP;
				
				this.loopRange.endNode = path.getEndNode();
				this.loopRange.skipPoints.clear();
				//this.range.skipPoints.add(suspiciousNode);
				
				TraceNode oldSusiciousNode = suspiciousNode;
				while(Settings.potentialCorrectPatterns.containsPattern(path) 
						&& !shouldStopOnCheckedNode(suspiciousNode, path)){
					
					Settings.potentialCorrectPatterns.addPathForPattern(path);
					
//					PotentialCorrectPattern pattern = Settings.potentialCorrectPatterns.getPattern(path);
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
				
				this.lastNode = currentNode;
				return oldSusiciousNode;
			}
			else{
				state = DebugState.JUMP;
				
				this.lastNode = currentNode;
				
				return suspiciousNode;				
			}
		}
		
	}

	
//	private TraceNode handleWrongValue(Trace trace, TraceNode currentNode, String userFeedBack){
//		TraceNode oldSusiciousNode = currentNode;
//		
//		List<AttributionVar> readVars = constructAttributionRelation(currentNode, trace.getCheckTime());
//		AttributionVar focusVar = Settings.interestedVariables.findFocusVar(trace, currentNode, readVars);
//				
//		if(focusVar != null){
////			long t1 = System.currentTimeMillis();
//			trace.distributeSuspiciousness(Settings.interestedVariables);
////			long t2 = System.currentTimeMillis();
////			System.out.println("time for distributeSuspiciousness: " + (t2-t1));
//			TraceNode suspiciousNode = trace.findMostSupiciousNode(focusVar);
//			
//			/**
//			 * it means the suspiciousness of focusVar cannot be distributed to other trace node any more. 
//			 */
//			TraceNode producer = trace.getProducer(focusVar.getVarID());
////			if(suspiciousNode.isWrittenVariablesContains(focusVar.getVarID()) && suspiciousNode.equals(this.lastNode)){
//			if(suspiciousNode.getOrder()>currentNode.getOrder() && producer!=null && producer.equals(suspiciousNode)){
//				//TODO it could be done in a more intelligent way.
//				this.inspectingRange = new InspectingRange(suspiciousNode.getDataDominator(), suspiciousNode);
//				TraceNode recommendedNode = handleDetailInspecting(trace, currentNode, userFeedBack);
//				return recommendedNode;
//			}
//			else{
//				TraceNode readTraceNode = focusVar.getReadTraceNode();
//				boolean isPathInPattern = false;
//				PathInstance path = null;
//				if(readTraceNode != null){
//					path = new PathInstance(suspiciousNode, readTraceNode);
//					isPathInPattern = Settings.potentialCorrectPatterns.containsPattern(path)? true : false;					
//				}
//				
//				if(isPathInPattern){
//					state = DebugState.SKIP;
//					
//					this.loopRange.endNode = path.getEndNode();
//					this.loopRange.skipPoints.clear();
//					//this.range.skipPoints.add(suspiciousNode);
//					
//					while(Settings.potentialCorrectPatterns.containsPattern(path) 
//							&& !shouldStopOnCheckedNode(suspiciousNode, path)){
//						
//						Settings.potentialCorrectPatterns.addPathForPattern(path);
//						this.loopRange.skipPoints.add(suspiciousNode);
//						
//						PotentialCorrectPattern pattern = Settings.potentialCorrectPatterns.getPattern(path);
//						oldSusiciousNode = suspiciousNode;
//						
//						suspiciousNode = findNextSuspiciousNodeByPattern(pattern, oldSusiciousNode);
//						
//						if(suspiciousNode == null){
//							break;
//						}
//						else{
//							path = new PathInstance(suspiciousNode, oldSusiciousNode);					
//						}
//					}
//					
//					this.lastNode = currentNode;
//					return oldSusiciousNode;
//				}
//				else{
//					state = DebugState.JUMP;
//					
//					this.lastNode = currentNode;
//					return suspiciousNode;				
//				}
//			}
//		}
//		
//		System.err.println("fucosVar is null");
//		return currentNode;
//	}
	
	private TraceNode handleJumpBehavior(Trace trace, TraceNode currentNode, String userFeedBack) {
		
		TraceNode node;
		if(userFeedBack.equals(UserFeedback.WRONG_PATH)){
			node = handleWrongPath(trace, currentNode, userFeedBack);
		}
		else{
			node = handleWrongValue(trace, currentNode, userFeedBack);
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

	private TraceNode handleDetailInspecting(Trace trace, TraceNode currentNode, String userFeedBack) {
		
		if(userFeedBack.equals(UserFeedback.CORRECT)){
			this.state = DebugState.DETAIL_INSPECT;
			
			TraceNode nextNode;
			if(currentNode.getOrder() > this.inspectingRange.endNode.getOrder()){
				nextNode = this.inspectingRange.startNode;
			}
			else{
				nextNode = trace.getExectionList().get(currentNode.getOrder());
				
			}
			
			return nextNode;
		}
		else{
			TraceNode node = handleJumpBehavior(trace, currentNode, userFeedBack);
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
		
		for(TraceNode dominator: oldSusiciousNode.getDataDominator().keySet()){
			for(VarValue writtenVar: dominator.getWrittenVariables()){
				Variable writtenVariable = writtenVar.getVariable();
				
				if(MicroBatUtil.isEquivalentVariable(causingVariable, writtenVariable)){
					return dominator;					
				}
			}
		}
		
		
		return null;
	}
	
	
	

	private List<AttributionVar> constructAttributionRelation(TraceNode currentNode, int checkTime){
		List<AttributionVar> readVars = new ArrayList<>();
		for(VarValue writtenVarValue: currentNode.getWrittenVariables()){
			String writtenVarID = writtenVarValue.getVarID();
			if(Settings.interestedVariables.contains(writtenVarID)){
				for(VarValue readVarValue: currentNode.getReadVariables()){
					String readVarID = readVarValue.getVarID();
					if(Settings.interestedVariables.contains(readVarID)){
						
						AttributionVar writtenVar = Settings.interestedVariables.findOrCreateVar(writtenVarID, checkTime);
						AttributionVar readVar = Settings.interestedVariables.findOrCreateVar(readVarID, checkTime);
						readVar.setReadTraceNode(currentNode);
						
						readVar.addChild(writtenVar);
						writtenVar.addParent(readVar);
					}
				}						
			}
		}
		
		Settings.interestedVariables.updateAttributionTrees();
		
		for(VarValue readVarValue: currentNode.getReadVariables()){
			String readVarID = readVarValue.getVarID();
			if(Settings.interestedVariables.contains(readVarID)){
				AttributionVar readVar = Settings.interestedVariables.findOrCreateVar(readVarID, checkTime);
				readVar.setReadTraceNode(currentNode);
				readVars.add(readVar);
			}
		}		
		
		return readVars;
	}
	
	@SuppressWarnings("unchecked")
	public StepRecommender clone(){
		StepRecommender recommender = new StepRecommender();
		recommender.state = this.state;
		recommender.lastNode = this.lastNode;
		//recommender.lastRecommendNode = this.lastRecommendNode;
		recommender.setLatestCause(this.getLatestCause().clone());
		recommender.latestClearState = this.latestClearState;
		recommender.loopRange = this.loopRange.clone();
		if(this.inspectingRange != null){
			recommender.inspectingRange = this.inspectingRange.clone();			
		}
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
}
