package microbat.model.trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;

import microbat.algorithm.graphdiff.GraphDiff;
import microbat.algorithm.graphdiff.HierarchyGraphDiffer;
import microbat.model.AttributionVar;
import microbat.model.BreakPoint;
import microbat.model.BreakPointValue;
import microbat.model.Scope;
import microbat.model.UserInterestedVariables;
import microbat.model.value.ReferenceValue;
import microbat.model.value.VarValue;
import microbat.model.variable.Variable;
import microbat.util.JavaUtil;
import microbat.util.Settings;
import sav.common.core.Pair;

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
	
//	private List<VarValue> hiddenReadVariables = new ArrayList<>();
//	private List<VarValue> hiddenWrittenVariables = new ArrayList<>();
	
//	private Map<TraceNode, List<String>> dataDominators = new HashMap<>();
//	private Map<TraceNode, List<String>> dataDominatees = new HashMap<>();
	
	private TraceNode controlDominator;
	private List<TraceNode> controlDominatees = new ArrayList<>();
	
	/**
	 * this filed is used as a temporary field during the trace construction.
	 */
	private List<VarValue> returnedVariables = new ArrayList<>();
	
	/**
	 * the order of this node in the whole trace, starting from 1.
	 */
	private int order;
	
	private TraceNode stepInNext;
	private TraceNode stepInPrevious;
	
	private TraceNode stepOverNext;
	private TraceNode stepOverPrevious;
	
	private List<TraceNode> invocationChildren = new ArrayList<>();
	private TraceNode invocationParent;
	
	private List<TraceNode> loopChildren = new ArrayList<>();
	private TraceNode loopParent;
	
	private boolean isException;
	
	private TraceNode invokingMatchNode;
	
	private long runtimePC;
	private Trace trace;
	
	private long timestamp;
	
	private transient double sliceBreakerProbability = 0;
	
	/**
	 * the first element of the pair is the read variable list, the second element is the 
	 * written variable list.
	 */
	private Supplier<Pair<List<VarValue>, List<VarValue>>> RWVarSupplier = () -> {return Pair.of(readVariables, writtenVariables);};
	
	public TraceNode(BreakPoint breakPoint, BreakPointValue programState, int order, Trace trace) {
		super();
		this.breakPoint = breakPoint;
		this.programState = programState;
		this.order = order;
		this.trace = trace;
	}
	
	// TODO: change all this.access to this.get()
	public List<VarValue> findMarkedReadVariable(){
		List<VarValue> markedReadVars = new ArrayList<>();
		for(VarValue readVarValue: this.readVariables){
			if(Settings.interestedVariables.contains(readVarValue)){
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
	
	public String getMethodSign() {
		return this.getBreakPoint().getMethodSign();
	}
	
	public boolean isReturnNode() {
		return this.getBreakPoint().isReturnStatement();
	}
	
	public void addReturnVariable(VarValue var){
		this.returnedVariables.add(var);
	}
	
	public TraceNode getDataDominator(VarValue readVar) {
//		TraceNode dataDominator = null;
//		
//		Map<String, StepVariableRelationEntry> table = this.trace.getStepVariableTable();
//		StepVariableRelationEntry entry = table.get(readVar.getVarID());
//		if(entry!=null){
//			TraceNode latestProducer = findLatestProducer(entry);
//			dataDominator = latestProducer;
//		}
//		
//		return dataDominator;
		
		return this.trace.findDataDependency(this, readVar);
	}
	
//	private TraceNode findLatestProducer(StepVariableRelationEntry entry) {
//		List<TraceNode> producers = entry.getProducers();
//		
//		if(producers == null){
//			return null;
//		}
//		
//		TraceNode latestProducer = null;
//		for(TraceNode producer: producers){
//			if(producer.getOrder()<this.getOrder()){
//				if(latestProducer==null){
//					latestProducer = producer;
//				}
//				else{
//					if(producer.getOrder()>latestProducer.getOrder()){
//						latestProducer = producer;
//					}
//				}
//			}
//		}
//		
//		return latestProducer;
//	}

	public int getReadVarCorrectness(UserInterestedVariables interestedVariables, boolean isUICheck){
		
		for(VarValue var: getReadVariables()){
			if(interestedVariables.contains(var)){
				return TraceNode.READ_VARS_INCORRECT;
			}
			
			List<VarValue> children = var.getAllDescedentChildren();
			for(VarValue child: children){
				if(interestedVariables.contains(child)){
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
			if(interestedVariables.contains(var)){
				return TraceNode.WRITTEN_VARS_INCORRECT;
			}
			
			List<VarValue> children = var.getAllDescedentChildren();
			for(VarValue child: children){
				if(interestedVariables.contains(child)){
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
		if(stepOverNext!=null){
			return stepOverNext;
		}
		else{
			TraceNode n = stepInNext;
			while(n!=null) {
				TraceNode p1 = n.getInvocationParent();
				TraceNode p2 = this.getInvocationParent();
				if(p1==null && p2==null) {
					stepOverNext = n;
					return n;
				}
				else if(p1!=null && p2!=null) {
					if(p1.getOrder()==p2.getOrder()) {
						stepOverNext = n;
						return n;
					}					
				}
				n = n.getStepInNext();
			}
		}
		
		return null;
	}

	public void setStepOverNext(TraceNode stepOverNext) {
		this.stepOverNext = stepOverNext;
	}

	public TraceNode getStepOverPrevious() {
		if(stepOverPrevious!=null){
			return stepOverPrevious;
		}
		else if(stepInPrevious!=null){
			TraceNode n = stepInPrevious;
			while(n!=null) {
				TraceNode p1 = n.getInvocationParent();
				TraceNode p2 = this.getInvocationParent();
				if(p1==null && p2==null) {
					stepOverPrevious = n;
					return n;
				}
				else if(p1!=null && p2!=null) {
					if(p1.getOrder()==p2.getOrder()) {
						stepOverPrevious = n;
						return n;
					}					
				}
				n = n.getStepInPrevious();
			}
		}
		return null;
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
		
		HierarchyGraphDiffer differ = new HierarchyGraphDiffer();
		differ.diff(nodeBefore, nodeAfter, false);
		List<GraphDiff> diffs = differ.getDiffs();
		this.consequences = diffs;
	}
	
	public String getMethodName() {
		return this.getBreakPoint().getMethodName();
	}
	
	public String getShortMethodSignature() {
		return this.getBreakPoint().getShortMethodSignature();
	}

	public Map<TraceNode, VarValue> getDataDominators() {
		Map<TraceNode, VarValue> dataDominators = new HashMap<>();
		for(VarValue readVar: this.getReadVariables()){
			TraceNode dominator = this.trace.findDataDependency(this, readVar);
			if(dominator != null){
				dataDominators.put(dominator, readVar);
			}
		}
		
		return dataDominators;
	}

	public Map<TraceNode, VarValue> getDataDominatee() {
		Map<TraceNode, VarValue> dataDominatees = new HashMap<>();
		for(VarValue writtenVar: this.getWrittenVariables()){
			List<TraceNode> dominatees = this.trace.findDataDependentee(this, writtenVar);
			for(TraceNode dominatee: dominatees){
				dataDominatees.put(dominatee, writtenVar);
			}
		}
		
		return dataDominatees;
	}

	public boolean isException() {
		return isException;
	}

	public void setException(boolean isException) {
		this.isException = isException;
	}
	
	private void populateRWVars() {
		Pair<List<VarValue>, List<VarValue>> pair = this.RWVarSupplier.get();
		this.setReadVariables(pair.first());
		this.setWrittenVariables(pair.second());
	}

	public List<VarValue> getReadVariables() {
		if (this.readVariables.isEmpty()) {
			this.populateRWVars();
		}
		return readVariables;
	}

	public void setReadVariables(List<VarValue> readVariables) {
		this.readVariables = readVariables;
	}
	
	public void addReadVariable(VarValue var){
		this.readVariables.add(var);
	}
	
	public List<VarValue> getWrittenVariables() {
		if (this.writtenVariables.isEmpty()) {
			this.populateRWVars();
		}
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
		for(TraceNode dominator: node.getDataDominators().keySet()){
			if(!dominators.containsKey(dominator.getOrder())){
				dominators.put(dominator.getOrder(), dominator);		
				findDominators(dominator, dominators);				
			}
		}
		
		if(this.controlDominator != null){
			dominators.put(this.controlDominator.getOrder(), this.controlDominator);
		}
		
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
	
	public boolean isBranch(){
		return this.breakPoint.isBranch();
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

	private HashSet<TraceNode> allControlDominatees;
	public List<TraceNode> findAllControlDominatees() {
		if(allControlDominatees==null){
			HashSet<TraceNode> controlDominatees = new HashSet<>();
			findAllControlDominatees(this, controlDominatees);
			allControlDominatees = controlDominatees;
		}
		
		
		return new ArrayList<TraceNode>(allControlDominatees);
	}

//	private void findAllControlDominatees(TraceNode node, HashSet<TraceNode> controlDominatees) {
//		System.out.println("Node: " + node.getOrder());
//		for(TraceNode dominatee: node.getControlDominatees()){
//			if(!controlDominatees.contains(dominatee)){
//				controlDominatees.add(dominatee);
//				findAllControlDominatees(dominatee, controlDominatees);				
//			}
//		}
//	}
	
	private void findAllControlDominatees(TraceNode node, HashSet<TraceNode> controlDominatees) {
		Stack<TraceNode> stack = new Stack<>();
		stack.push(node);
		while (!stack.isEmpty()) {
			TraceNode curNode = stack.pop();
			for(TraceNode dominatee: curNode.getControlDominatees()){
				if(!controlDominatees.contains(dominatee)){
					controlDominatees.add(dominatee);
					stack.push(dominatee);
				}
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
		
//		if(this.loopChildren.isEmpty() && this.invocationChildren.isEmpty()){
////			return abstractChildren;
//		}
//		else if(!this.loopChildren.isEmpty() && this.invocationChildren.isEmpty()){
//			abstractChildren = loopChildren;
//		}
//		else if(this.loopChildren.isEmpty() && !this.invocationChildren.isEmpty()){
//			abstractChildren.addAll(this.invocationChildren);
//			clearLoopParentsInMethodParent(abstractChildren);
////			return abstractChildren;
//		}
//		else{
//			abstractChildren.addAll(this.invocationChildren);
//			clearLoopParentsInMethodParent(abstractChildren);
//			abstractChildren.addAll(this.loopChildren);
////			return abstractChildren;
//		}
		
//		Collections.sort(abstractChildren, new TraceNodeOrderComparator());
		
		abstractChildren.addAll(this.invocationChildren);
		clearLoopParentsInMethodParent(abstractChildren);
		for(TraceNode loopChild: this.loopChildren){
			if(!abstractChildren.contains(loopChild)){
				abstractChildren.add(loopChild);
			}
		}
		
		return abstractChildren;
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

	private List<TraceNode> allInvocationParents = null;
	
	public List<TraceNode> findAllInvocationParents() {
		if(allInvocationParents==null) {
			Set<TraceNode> set = new HashSet<>();
			TraceNode parent = this.getInvocationParent();
			while(parent != null){
				if(set.contains(parent)) {
					break;
				}
				set.add(parent);
				parent = parent.getInvocationParent();
			}
			
			allInvocationParents = new ArrayList<>(set);
		}
		
		return allInvocationParents;
	}

	public void resetCheckTime() {
		this.checkTime = -1;
		
	}

	public boolean isWrongPathNode() {
		return Settings.wrongPathNodeOrder.contains(new Integer(this.getOrder()));
	}
	
	public List<VarValue> getWrongReadVars(UserInterestedVariables interestedVariables) {
		List<VarValue> vars = new ArrayList<>();
		for(VarValue var: getReadVariables()){
			if(interestedVariables.contains(var)){
				vars.add(var);
			}
			
			List<VarValue> children = var.getAllDescedentChildren();
			for(VarValue child: children){
				if(interestedVariables.contains(child)){
					if(!vars.contains(child)){
						vars.add(child);
					}
				}
			}
		}
		
		return vars;
	}

	public boolean containSynonymousReadVar(VarValue readVar) {
		for(VarValue readVariable: getReadVariables()){
			if(readVariable.getVarName().equals(readVar.getVarName())
					&& readVariable.getClass().equals(readVar.getClass())){
				return true;
			}
		}
		return false;
	}
	
	public TraceNode getInvocationMethodOrDominator() {
		TraceNode controlDom = getControlDominator();
		TraceNode invocationParent = getInvocationParent();
		
		if(controlDom!=null && invocationParent!=null) {
			if(controlDom.getOrder()<invocationParent.getOrder()) {
				return invocationParent;
			}
			else {
				return controlDom;
			}
		}
		else if(controlDom!=null && invocationParent==null) {
			return controlDom;
		}
		else if(controlDom==null && invocationParent!=null) {
			return invocationParent;
		}
		
		return null;
	}

	public Trace getTrace() {
		return trace;
	}

	public void setTrace(Trace trace) {
		this.trace = trace;
	}

	public List<VarValue> getReturnedVariables() {
		return returnedVariables;
	}

	public void setReturnedVariables(List<VarValue> returnedVariables) {
		this.returnedVariables = returnedVariables;
	}

	public long getRuntimePC() {
		return runtimePC;
	}

	public void setRuntimePC(long runtimePC) {
		this.runtimePC = runtimePC;
	}

	class CatchClauseFinder extends ASTVisitor{
		int line;
		CompilationUnit cu;
		
		boolean find = false;
		
		public CatchClauseFinder(int line, CompilationUnit cu) {
			super();
			this.line = line;
			this.cu = cu;
		}

		public boolean visit(CatchClause clause){
			int startLine = cu.getLineNumber(clause.getStartPosition());
			int endLine = cu.getLineNumber(clause.getStartPosition()+clause.getLength());
			
			if(startLine<=line && line<=endLine){
				find = true;
			}
			
			return false;
		}
	}
	
	public boolean insideException() {
		CompilationUnit cu = JavaUtil.findCompilationUnitInProject(
				this.getDeclaringCompilationUnitName(), this.getTrace().getAppJavaClassPath());
		
//		if(cu==null){
//			return false;
//		}
		
		CatchClauseFinder finder = new CatchClauseFinder(this.breakPoint.getLineNumber(), cu);
		cu.accept(finder);
		
		return finder.find;
	}
	
	public void setSourceVersion(boolean flag){
		this.breakPoint.setSourceVersion(flag);
	}

	public TraceNode getInvokingMatchNode() {
		return invokingMatchNode;
	}

	public void setInvokingMatchNode(TraceNode invokingMatchNode) {
		this.invokingMatchNode = invokingMatchNode;
	}

	public double getSliceBreakerProbability() {
		return sliceBreakerProbability;
	}

	public void setSliceBreakerProbability(double sliceBreakerProbability) {
		this.sliceBreakerProbability = sliceBreakerProbability;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Supplier<Pair<List<VarValue>, List<VarValue>>> getRWVarSupplier() {
		return RWVarSupplier;
	}

	public void setRWVarSupplier(Supplier<Pair<List<VarValue>, List<VarValue>>> rWVarSupplier) {
		RWVarSupplier = rWVarSupplier;
	}


//	public List<VarValue> getHiddenReadVariables() {
//		return hiddenReadVariables;
//	}
//
//	public void setHiddenReadVariables(List<VarValue> hiddenReadVariables) {
//		this.hiddenReadVariables = hiddenReadVariables;
//	}
//
//	public List<VarValue> getHiddenWrittenVariables() {
//		return hiddenWrittenVariables;
//	}
//
//	public void setHiddenWrittenVariables(List<VarValue> hiddenWrittenVariables) {
//		this.hiddenWrittenVariables = hiddenWrittenVariables;
//	}
}
