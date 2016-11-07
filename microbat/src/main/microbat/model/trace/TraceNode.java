package microbat.model.trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import microbat.algorithm.graphdiff.GraphDiff;
import microbat.algorithm.graphdiff.HierarchyGraphDiffer;
import microbat.model.AttributionVar;
import microbat.model.BreakPoint;
import microbat.model.BreakPointValue;
import microbat.model.Scope;
import microbat.model.UserInterestedVariables;
import microbat.model.value.VarValue;
import microbat.util.Settings;

public class TraceNode{
	
	public final static int STEP_CORRECT = 0;
	public final static int STEP_INCORRECT = 1;
	public final static int STEP_UNKNOWN = 2;
	
	public final static int READ_VARS_CORRECT = 3;
	public final static int READ_VARS_INCORRECT = 4;
	public final static int READ_VARS_UNKNOWN = 5;
	
	public final static int WRITTEN_VARS_CORRECT = 6;
	public final static int WRITTEN_VARS_INCORRECT = 7;
	public final static int WRITTEN_VARS_UNKNOWN = 8;
	
	private Map<AttributionVar, Double> suspicousScoreMap = new HashMap<>();
	
	private int checkTime = -1;
	
	private BreakPoint breakPoint;
	private BreakPointValue programState;
	private BreakPointValue afterStepInState;
	private BreakPointValue afterStepOverState;
	
	private List<GraphDiff> consequences;
	
	private List<VarValue> readVariables = new ArrayList<>();
	private List<VarValue> writtenVariables = new ArrayList<>();
	
	private Map<TraceNode, List<String>> dataDominators = new HashMap<>();
	private Map<TraceNode, List<String>> dataDominatees = new HashMap<>();
	private TraceNode controlDominator;
	private List<TraceNode> controlDominatees = new ArrayList<>();
	
	/**
	 * the order of this node in the whole trace, starting from 1.
	 */
	private int order;
	
	/**
	 * indicate whether this node has been marked correct/incorrect by user
	 */
//	private Boolean markedCorrrect;
	
	
	private TraceNode stepInNext;
	private TraceNode stepInPrevious;
	
	private TraceNode stepOverNext;
	private TraceNode stepOverPrevious;
	
	private List<TraceNode> invocationChildren = new ArrayList<>();
	private TraceNode invocationParent;
	
	private List<TraceNode> loopChildren = new ArrayList<>();
	private TraceNode loopParent;
	
	private boolean isException;
	
	public TraceNode(BreakPoint breakPoint, BreakPointValue programState, int order) {
		super();
		this.breakPoint = breakPoint;
		this.programState = programState;
		this.order = order;
	}
	
	public List<VarValue> findMarkedReadVariable(){
		List<VarValue> markedReadVars = new ArrayList<>();
		for(VarValue readVarValue: this.readVariables){
			String readVarID = readVarValue.getVarID();
			if(Settings.interestedVariables.contains(readVarID)){
				markedReadVars.add(readVarValue);
			}
		}		
		
		return markedReadVars;
	}
	
	/**
	 * @param isUICheck
	 * whether this trace node is checked on UI for now.
	 * 
	 * @return
	 */
	public boolean isAllReadWrittenVarCorrect(boolean isUICheck){
		boolean writtenCorrect = getWittenVarCorrectness(Settings.interestedVariables, isUICheck) == TraceNode.WRITTEN_VARS_CORRECT;
		boolean readCorrect = getReadVarCorrectness(Settings.interestedVariables, isUICheck) == TraceNode.READ_VARS_CORRECT;
		
		return writtenCorrect && readCorrect;
	}
	
	public int getReadVarCorrectness(UserInterestedVariables interestedVariables, boolean isUICheck){
		
		for(VarValue var: getReadVariables()){
			String readVarID = var.getVarID();
			if(interestedVariables.contains(readVarID)){
				return TraceNode.READ_VARS_INCORRECT;
			}
			
			List<VarValue> children = var.getAllDescedentChildren();
			for(VarValue child: children){
				if(interestedVariables.contains(child.getVarID())){
					return TraceNode.READ_VARS_INCORRECT;
				}
			}
		}
		
		/**
		 * Distinguish between "correct" and "unknown".
		 * 
		 * When none of the read variables is located in {@code interestedVariables}, the node is
		 * correct if either (1) the node has been checked or (2) it is being checked on the UI.
		 *  
		 */
		if(hasChecked() || isUICheck){
			return TraceNode.READ_VARS_CORRECT;			
		}
		else{
			return TraceNode.READ_VARS_UNKNOWN;
		}
		
	}
	
	public int getWittenVarCorrectness(UserInterestedVariables interestedVariables, boolean isUICheck){
		
		for(VarValue var: getWrittenVariables()){
			String writtenVarID = var.getVarID();
			if(interestedVariables.contains(writtenVarID)){
				return TraceNode.WRITTEN_VARS_INCORRECT;
			}
			
			List<VarValue> children = var.getAllDescedentChildren();
			for(VarValue child: children){
				if(interestedVariables.contains(child.getVarID())){
					return TraceNode.READ_VARS_INCORRECT;
				}
			}
		}
		
		if(hasChecked()|| isUICheck){
			return TraceNode.WRITTEN_VARS_CORRECT;
		}
		else{
			return TraceNode.WRITTEN_VARS_UNKNOWN;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + order;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TraceNode other = (TraceNode) obj;
		if (order != other.order)
			return false;
		return true;
	}

	public String toString(){
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("order ");
		buffer.append(getOrder());
		buffer.append("~");
		
		buffer.append(getClassCanonicalName());
		buffer.append(": line ");
		buffer.append(getLineNumber());
		
		String methodName = this.breakPoint.getMethodName();
		if(methodName != null){
			buffer.append(" in ");
			buffer.append(methodName);
			buffer.append("(...)");
		}
	
		return buffer.toString();
	}
	
	public String getClassCanonicalName(){
		return this.breakPoint.getClassCanonicalName();
	}
	
	public String getDeclaringCompilationUnitName(){
		return this.breakPoint.getDeclaringCompilationUnitName();
	}
	
	public int getLineNumber(){
		return this.breakPoint.getLineNumber();
	}

	public BreakPoint getBreakPoint() {
		return breakPoint;
	}

	public void setBreakPoint(BreakPoint breakPoint) {
		this.breakPoint = breakPoint;
	}

	public BreakPointValue getProgramState() {
		return programState;
	}

	public void setProgramState(BreakPointValue programState) {
		this.programState = programState;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

//	public Boolean getMarkedCorrrect() {
//		return markedCorrrect;
//	}
//
//	public void setMarkedCorrrect(Boolean markedCorrrect) {
//		this.markedCorrrect = markedCorrrect;
//	}

	public BreakPointValue getAfterState() {
		if(this.afterStepOverState != null){
			return this.afterStepOverState;
		}
		else{
			return afterStepInState;			
		}
		
	}
//
//	public void setAfterState(BreakPointValue afterState) {
//		this.afterStepInState = afterState;
//	}

	public TraceNode getStepInNext() {
		return stepInNext;
	}

	public void setStepInNext(TraceNode stepInNext) {
		this.stepInNext = stepInNext;
	}

	public TraceNode getStepInPrevious() {
		return stepInPrevious;
	}

	public void setStepInPrevious(TraceNode stepInPrevious) {
		this.stepInPrevious = stepInPrevious;
	}

	public TraceNode getStepOverNext() {
		return stepOverNext;
	}

	public void setStepOverNext(TraceNode stepOverNext) {
		this.stepOverNext = stepOverNext;
	}

	public TraceNode getStepOverPrevious() {
		return stepOverPrevious;
	}

	public void setStepOverPrevious(TraceNode stepOverPrevious) {
		this.stepOverPrevious = stepOverPrevious;
	}

	public List<TraceNode> getInvocationChildren() {
		return invocationChildren;
	}

	public void setInvocationChildren(List<TraceNode> invocationChildren) {
		this.invocationChildren = invocationChildren;
	}

	public void addInvocationChild(TraceNode node){
		this.invocationChildren.add(node);
	}

	public TraceNode getInvocationParent() {
		return invocationParent;
	}

	public void setInvocationParent(TraceNode invocationParent) {
		this.invocationParent = invocationParent;
	}

	public BreakPointValue getAfterStepInState() {
		return afterStepInState;
	}

	public void setAfterStepInState(BreakPointValue afterStepInState) {
		this.afterStepInState = afterStepInState;
	}

	public BreakPointValue getAfterStepOverState() {
		return afterStepOverState;
	}

	public void setAfterStepOverState(BreakPointValue afterStepOverState) {
		this.afterStepOverState = afterStepOverState;
	}

	public List<GraphDiff> getConsequences() {
		return consequences;
	}

	public void setConsequences(List<GraphDiff> consequences) {
		this.consequences = consequences;
	}

	public void conductStateDiff() {
		BreakPointValue nodeBefore = getProgramState();
		BreakPointValue nodeAfter = getAfterState();
		
//		if(getOrder() == 6){
//			System.currentTimeMillis();
//		}
		
		HierarchyGraphDiffer differ = new HierarchyGraphDiffer();
		differ.diff(nodeBefore, nodeAfter, false);
		List<GraphDiff> diffs = differ.getDiffs();
		this.consequences = diffs;
	}

	public Map<TraceNode, List<String>> getDataDominator() {
		return dataDominators;
	}

	public void setDataDominator(Map<TraceNode, List<String>> dominator) {
		this.dataDominators = dominator;
	}

	public Map<TraceNode, List<String>> getDataDominatee() {
		return dataDominatees;
	}

	public void setDataDominatee(Map<TraceNode, List<String>> dominatee) {
		this.dataDominatees = dominatee;
	}
	
	public void addDataDominator(TraceNode node, List<String> variables){
		List<String> varIDs = this.dataDominators.get(node);
		if(varIDs == null){
			this.dataDominators.put(node, variables);
		}
		else{
			varIDs.addAll(variables);
		}
	}
	
	public void addDataDominatee(TraceNode node, List<String> variables){
		List<String> varIDs = this.dataDominatees.get(node);
		if(varIDs == null){
			this.dataDominatees.put(node, variables);
		}
		else{
			varIDs.addAll(variables);
		}
	}

	public boolean isException() {
		return isException;
	}

	public void setException(boolean isException) {
		this.isException = isException;
	}

	public List<VarValue> getReadVariables() {
		return readVariables;
	}

	public void setReadVariables(List<VarValue> readVariables) {
		this.readVariables = readVariables;
	}
	
	public void addReadVariable(VarValue var){
		this.readVariables.add(var);
	}

	public List<VarValue> getWrittenVariables() {
		return writtenVariables;
	}

	public void setWrittenVariables(List<VarValue> writtenVariables) {
		this.writtenVariables = writtenVariables;
	}
	
	public void addWrittenVariable(VarValue var){
		this.writtenVariables.add(var);
	}

	public Double getSuspicousScore(AttributionVar var) {
		return this.suspicousScoreMap.get(var);
	}

	public void setSuspicousScore(AttributionVar var, double suspicousScore) {
		this.suspicousScoreMap.put(var, suspicousScore);
	}
	
	public void addSuspicousScore(AttributionVar var, double score) {
		Double ss = getSuspicousScore(var);
		if(ss == null){
			setSuspicousScore(var, score);
		}
		else{
			setSuspicousScore(var, ss+score);
		}
	}

	public boolean hasChecked(){
		return checkTime != -1;
	}
	
	public int getCheckTime() {
		return checkTime;
	}

	public void setCheckTime(int markTime) {
		this.checkTime = markTime;
	}

//	public int getStepCorrectness() {
//		return stepCorrectness;
//	}
//
//	public void setStepCorrectness(int stepCorrectness) {
//		this.stepCorrectness = stepCorrectness;
//	}

	public List<TraceNode> getUncheckedDataDominators() {
		List<TraceNode> uncheckedDominators = new ArrayList<>();
		for(TraceNode dominator: dataDominators.keySet()){
			if(!dominator.hasChecked()){
				uncheckedDominators.add(dominator);
			}
		}
		
//		TraceNode controlDominator = getControlDominator();
//		if(controlDominator != null && !controlDominator.hasChecked()){
//			uncheckedDominators.add(controlDominator);
//		}
		
		return uncheckedDominators;
	}

	public boolean isReadVariablesContains(String varID){
		for(VarValue readVar: this.getReadVariables()){
			if(readVar.getVarID().equals(varID)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isWrittenVariablesContains(String varID){
		for(VarValue writtenVar: this.getWrittenVariables()){
			if(writtenVar.getVarID().equals(varID)){
				return true;
			}
		}
		return false;
	}
	
	public Map<AttributionVar, Double> getSuspicousScoreMap() {
		return suspicousScoreMap;
	}

	public void setSuspicousScoreMap(Map<AttributionVar, Double> suspicousScoreMap) {
		this.suspicousScoreMap = suspicousScoreMap;
	}

	public Map<Integer, TraceNode> findAllDominators() {
		Map<Integer, TraceNode> dominators = new HashMap<>();
		
		findDominators(this, dominators);
		
		return dominators;
	}

	private void findDominators(TraceNode node, Map<Integer, TraceNode> dominators) {
		for(TraceNode dominator: node.getDataDominator().keySet()){
			if(!dominators.containsKey(dominator.getOrder())){
				dominators.put(dominator.getOrder(), dominator);		
				findDominators(dominator, dominators);				
			}
		}
		
		if(this.controlDominator != null){
			dominators.put(this.controlDominator.getOrder(), this.controlDominator);
		}
		
//		for(TraceNode controlDominator: node.getControlDominators()){
//			if(!dominators.containsKey(controlDominator.getOrder())){
//				dominators.put(controlDominator.getOrder(), controlDominator);
//				findDominators(controlDominator, dominators);				
//			}
//		}
		
	}
	
	public Map<Integer, TraceNode> findAllDominatees() {
		Map<Integer, TraceNode> dominatees = new HashMap<>();
		
		findDominatees(this, dominatees);
		
		return dominatees;
	}

	private void findDominatees(TraceNode node, Map<Integer, TraceNode> dominatees) {
		for(TraceNode dominatee: node.getDataDominatee().keySet()){
			if(!dominatees.containsKey(dominatee.getOrder())){
				if(dominatee.getOrder() == 1){
					System.currentTimeMillis();
				}
				
				dominatees.put(dominatee.getOrder(), dominatee);		
				findDominatees(dominatee, dominatees);				
			}
		}
		
		for(TraceNode controlDominatee: node.getControlDominatees()){
			if(!dominatees.containsKey(controlDominatee.getOrder())){
				if(controlDominatee.getOrder() == 1){
					System.currentTimeMillis();
				}
				
				dominatees.put(controlDominatee.getOrder(), controlDominatee);
				findDominatees(controlDominatee, dominatees);				
			}
		}
	}
	
	public void setControlDominator(TraceNode controlDominator){
		this.controlDominator = controlDominator;
	}
	
	public TraceNode getControlDominator(){
		return this.controlDominator;
	}

//	public List<TraceNode> getControlDominators() {
//		return controlDominators;
//	}
//
//	public void setControlDominators(List<TraceNode> controlDominators) {
//		this.controlDominators = controlDominators;
//	}
//	
//	public void addControlDominator(TraceNode dominator){
//		if(!this.controlDominators.contains(dominator)){
//			this.controlDominators.add(dominator);
//		}
//	}

	public List<TraceNode> getControlDominatees() {
		return controlDominatees;
	}

	public void setControlDominatees(List<TraceNode> controlDominatees) {
		this.controlDominatees = controlDominatees;
	}
	
	public void addControlDominatee(TraceNode dominatee){
		if(!this.controlDominatees.contains(dominatee)){
			this.controlDominatees.add(dominatee);
		}
	}

	public boolean isConditional(){
		return this.breakPoint.isConditional();
	}
	
	public Scope getControlScope(){
		return this.breakPoint.getControlScope();
	}
	
	public Scope getLoopScope(){
		return this.breakPoint.getLoopScope();
	}

	public int getInvocationLevel() {
		int level = 0;
		TraceNode parent = getInvocationParent();
		while(parent != null){
			parent = parent.getInvocationParent();
			level++;
		}
		
		return level;
	}
	
	public int getAbstractionLevel() {
		int level = 0;
		TraceNode parent = getAbstractionParent();
		while(parent != null){
			parent = parent.getAbstractionParent();
			level++;
		}
		
		return level;
	}

	public boolean isLoopCondition() {
		if(isConditional()){
			Scope scope = getControlScope();
			if(scope != null){
				return scope.isLoop();
			}
		}
		return false;
	}

	public List<TraceNode> findAllControlDominatees() {
		List<TraceNode> controlDominatees = new ArrayList<>();
		findAllControlDominatees(this, controlDominatees);
		return controlDominatees;
	}

	private void findAllControlDominatees(TraceNode node, List<TraceNode> controlDominatees) {
		for(TraceNode dominatee: node.getControlDominatees()){
			if(!controlDominatees.contains(dominatee)){
				controlDominatees.add(dominatee);
				findAllControlDominatees(dominatee, controlDominatees);				
			}
		}
	}

	public boolean hasSameLocation(TraceNode node) {
		return getDeclaringCompilationUnitName().equals(node.getDeclaringCompilationUnitName()) && 
				getLineNumber()==node.getLineNumber();
	}

	/**
	 * The nearest control diminator which is a loop condition.
	 * @return
	 */
	public TraceNode findContainingLoopControlDominator() {
//		for(TraceNode controlDominator: this.controlDominators){
//			if(controlDominator.isLoopCondition()){
//				if(controlDominator.getConditionScope().containsNodeScope(this)){
//					return controlDominator;
//				}
//			}
//		}
		
		if(this.controlDominator != null){
			TraceNode controlDominator = this.controlDominator;
			while(controlDominator != null){
				if(controlDominator.isLoopCondition()  && controlDominator.isLoopContainsNodeScope(this)){
					return controlDominator;
				}
				else{
					controlDominator = controlDominator.getControlDominator();
				}
			}
		}
		
//		if(!this.controlDominators.isEmpty()){
//			TraceNode controlDominator = this.controlDominators.get(0);
//			while(controlDominator != null){
//				if(controlDominator.isLoopCondition()  && controlDominator.isLoopContainsNodeScope(this)){
//					return controlDominator;
//				}
//				else{
//					List<TraceNode> controlDominators = controlDominator.getControlDominators();
//					if(!controlDominators.isEmpty()){
//						controlDominator = controlDominators.get(0);
//					}
//					else{
//						controlDominator = null;
//					}
//				}
//			}
//		}
		
		return null;
	}

	/**
	 * If this node is a loop condition node, given another node <code>traceNode</code>, check 
	 * whether <code>traceNode</code> is under the scope of this node. For example, the following
	 * code: <br>
	 * 
	 * <code>
	 * while(true){<br>
	 *    int a = 1;<br>
	 *    m();<br>
	 * }<br>
	 * void m(){<br>
	 *    int b = 2;<br>
	 * }<br>
	 * </code>
	 * 
	 * the node (<code>int b = 2;</code>) is under the scope of node (<code>while(true)</code>).
	 * 
	 * @param traceNode
	 * @return
	 */
	public boolean isLoopContainsNodeScope(TraceNode traceNode) {
		
		if(this.isLoopCondition()){
			List<TraceNode> parentList = new ArrayList<>();
			TraceNode node = traceNode;
			while(node != null){
				parentList.add(node);
				node = node.getInvocationParent();
			}
//			Scope scope = getControlScope();
			Scope scope = getLoopScope();
			for(TraceNode parent: parentList){
				if(scope.containsNodeScope(parent)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public TraceNode getAbstractionParent(){
		TraceNode invocationParent = getInvocationParent();
		TraceNode loopParent = getLoopParent();
		
		if(invocationParent==null && loopParent==null){
			return null;
		}
		else if(invocationParent!=null && loopParent==null){
			return invocationParent;
		}
		else if(invocationParent==null && loopParent!=null){
			return loopParent;
		}
		else{
			TraceNode abstractionParent = (invocationParent.getOrder()>loopParent.getOrder())?
					invocationParent:loopParent;
			return abstractionParent;
		}
	}
	
	public List<TraceNode> getAbstractChildren(){
		List<TraceNode> abstractChildren = new ArrayList<>();
		
		if(this.loopChildren.isEmpty() && this.invocationChildren.isEmpty()){
			return abstractChildren;
		}
		else if(!this.loopChildren.isEmpty() && this.invocationChildren.isEmpty()){
			return loopChildren;
		}
		else if(this.loopChildren.isEmpty() && !this.invocationChildren.isEmpty()){
			abstractChildren.addAll(this.invocationChildren);
			clearLoopParentsInMethodParent(abstractChildren);
			return abstractChildren;
		}
		else{
			abstractChildren.addAll(this.invocationChildren);
			clearLoopParentsInMethodParent(abstractChildren);
			abstractChildren.addAll(this.loopChildren);
			return abstractChildren;
		}
	}
	
	public boolean isAbstractParent(){
		return getAbstractChildren().size()!=0;
	}

	private void clearLoopParentsInMethodParent(List<TraceNode> abstractChildren) {
		Iterator<TraceNode> iter = abstractChildren.iterator();
		while(iter.hasNext()){
			TraceNode node = iter.next();
			if(isIndirectlyLoopContains(node)){
				iter.remove();
			}
		}
	}

	private boolean isIndirectlyLoopContains(TraceNode node) {
		List<TraceNode> loopParents = new ArrayList<>();
		TraceNode loopParent = node.getLoopParent();
		while(loopParent != null){
			loopParents.add(loopParent);
			loopParent = loopParent.getLoopParent();
		}
		
		for(TraceNode lParent: loopParents){
			if(this.invocationChildren.contains(lParent)){
				return true;
			}
		}
		
		return false;
	}

	public int findTraceLength() {
		TraceNode node = this;
		while(node.getStepInNext() != null){
			if(node.getStepOverNext() != null){
				node = node.getStepOverNext();
			}
			else{
				node = node.getStepInNext();
			}
		}
		
		return node.getOrder();
	}

	public List<TraceNode> getLoopChildren() {
		return loopChildren;
	}

	public void setLoopChildren(List<TraceNode> loopChildren) {
		this.loopChildren = loopChildren;
	}

	public TraceNode getLoopParent() {
		return loopParent;
	}

	public void setLoopParent(TraceNode loopParent) {
		this.loopParent = loopParent;
	}
	
	public void addLoopChild(TraceNode loopChild){
		this.loopChildren.add(loopChild);
	}

	public List<TraceNode> findAllInvocationParents() {
		List<TraceNode> list = new ArrayList<>();
		TraceNode parent = this.getInvocationParent();
		while(parent != null){
			list.add(parent);
			parent = parent.getInvocationParent();
		}
		
		return list;
	}

	public void resetCheckTime() {
		this.checkTime = -1;
		
	}

	public boolean isWrongPathNode() {
		return Settings.wrongPathNodeOrder.contains(new Integer(this.getOrder()));
	}
}
