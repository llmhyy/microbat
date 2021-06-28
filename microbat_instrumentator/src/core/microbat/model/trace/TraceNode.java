package microbat.model.trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import microbat.instrumentation.runtime.InvokingDetail;
import microbat.model.BreakPoint;
import microbat.model.BreakPointValue;
import microbat.model.ControlScope;
import microbat.model.Scope;
import microbat.model.value.VarValue;
import microbat.model.variable.Variable;
import sav.common.core.utils.CollectionUtils;

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
	
	private String invokingMethod = null;
	private InvokingDetail invokingDetail = null;
	
	private BreakPoint breakPoint;
	private BreakPointValue afterStepInState;
	private BreakPointValue afterStepOverState;
	
	private List<VarValue> readVariables;
	private List<VarValue> writtenVariables;
	
	private transient Map<String, VarValue> readVariableMap = new HashMap<>();
	private transient Map<String, VarValue> writtenVariableMap = new HashMap<>();
	
//	private List<VarValue> hiddenReadVariables = new ArrayList<>();
//	private List<VarValue> hiddenWrittenVariables = new ArrayList<>();
	
//	private Map<TraceNode, List<String>> dataDominators = new HashMap<>();
//	private Map<TraceNode, List<String>> dataDominatees = new HashMap<>();
	
	private TraceNode controlDominator;
	private List<TraceNode> controlDominatees = new ArrayList<>();
	
	/**
	 * this filed is used as a temporary field during the trace 
	 * construction for returning value.
	 */
	private List<VarValue> returnedVariables;
	
	/**
	 * this filed is used as a temporary field during the trace 
	 * construction for passing parameter of method invocation.
	 */
	private List<VarValue> passParameters = null;
	
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
	
	private List<TraceNode> loopChildren;
	private TraceNode loopParent;
	
	private boolean isException;
	
	private TraceNode invokingMatchNode;
	
	private long runtimePC;
	private Trace trace;
	
	private long timestamp;
	
	public TraceNode(BreakPoint breakPoint, BreakPointValue programState, int order, Trace trace) {
		this(breakPoint, programState, order, trace, -1, -1, System.currentTimeMillis());
	}
	
	public TraceNode(BreakPoint breakPoint, BreakPointValue programState, int order, Trace trace,
			int initReadVarsSize, int initWrittenVarsSize, long timestamp) {
		this.breakPoint = breakPoint;
		this.order = order;
		this.trace = trace;
		if (initReadVarsSize >= 0) {
			readVariables = new ArrayList<>(initReadVarsSize);
		} else {
			readVariables = new ArrayList<>();
		}
		
		if (initWrittenVarsSize >= 0) {
			writtenVariables = new ArrayList<>(initWrittenVarsSize);
		} else {
			writtenVariables = new ArrayList<>();
		}
		this.timestamp = timestamp;
	}
	
	public String getMethodSign() {
		return this.getBreakPoint().getMethodSign();
	}
	
	public boolean isReturnNode() {
		return this.getBreakPoint().isReturnStatement();
	}
	
	public void addReturnVariable(VarValue var){
		if (returnedVariables == null) {
			returnedVariables = new ArrayList<>(5);
		}
		this.returnedVariables.add(var);
	}
	
//	public TraceNode getDataDominator(VarValue readVar) {
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
//	}
	
	private TraceNode findLatestProducer(StepVariableRelationEntry entry) {
		List<TraceNode> producers = entry.getProducers();
		
		if(producers == null){
			return null;
		}
		
		TraceNode latestProducer = null;
		for(TraceNode producer: producers){
			if(producer.getOrder()<this.getOrder()){
				if(latestProducer==null){
					latestProducer = producer;
				}
				else{
					if(producer.getOrder()>latestProducer.getOrder()){
						latestProducer = producer;
					}
				}
			}
		}
		
		return latestProducer;
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
				
				if(p2!=null){
					int index = p2.getInvocationChildren().size()-1;
					TraceNode lastChild = p2.getInvocationChildren().get(index);
					if(n.getOrder()>lastChild.getOrder()){
						break;						
					}
				}
				
				if(p1!=null && p2!=null) {
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
				
				if(p2!=null && n.getOrder()<=p2.getOrder()){
					break;
				}
				
				if(p1!=null && p2!=null) {
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

	public String getMethodName() {
		return this.getBreakPoint().getMethodName();
	}
	
	public String getShortMethodSignature() {
		return this.getBreakPoint().getShortMethodSignature();
	}

//	public Map<TraceNode, VarValue> getDataDominators() {
//		Map<TraceNode, VarValue> dataDominators = new HashMap<>();
//		Map<String, StepVariableRelationEntry> table = this.trace.getStepVariableTable();
//		for(VarValue readVar: this.getReadVariables()){
//			StepVariableRelationEntry entry = table.get(readVar.getVariable().getVarID());
//			if(entry != null){
//				TraceNode dominator = findLatestProducer(entry);
//				if(dominator != null){
//					dataDominators.put(dominator, readVar);
//				}
//			}
//		}
//		
//		return dataDominators;
//	}

//	public Map<TraceNode, VarValue> getDataDominatee() {
//		Map<TraceNode, VarValue> dataDominatees = new HashMap<>();
//		Map<String, StepVariableRelationEntry> table = this.trace.getStepVariableTable();
//		for(VarValue writtenVar: this.getWrittenVariables()){
//			StepVariableRelationEntry entry = table.get(writtenVar.getVariable());
//			if(entry != null){
//				for(TraceNode consumer: entry.getConsumers()){
//					dataDominatees.put(consumer, writtenVar);
//				}
//			}
//		}
//		
//		return dataDominatees;
//	}

//	public void addDataDominator(TraceNode node, List<String> variables){
//		List<String> varIDs = this.dataDominators.get(node);
//		if(varIDs == null){
//			this.dataDominators.put(node, variables);
//		}
//		else{
//			varIDs.addAll(variables);
//		}
//	}
//	
//	public void addDataDominatee(TraceNode node, List<String> variables){
//		List<String> varIDs = this.dataDominatees.get(node);
//		if(varIDs == null){
//			this.dataDominatees.put(node, variables);
//		}
//		else{
//			varIDs.addAll(variables);
//		}
//	}

	public boolean isException() {
		return isException;
	}

	public void setException(boolean isException) {
		this.isException = isException;
	}
	
	public boolean containReadVariable(VarValue readVar){
		if(!readVariableMap.isEmpty()){
			return this.readVariableMap.containsKey(readVar.getVarID());			
		}
		else{
			return this.readVariables.contains(readVar);
		}
	}

	public Collection<VarValue> getReadVariables() {
		if(this.readVariables==null || this.readVariables.size() < this.readVariableMap.size()){
			this.readVariables = new ArrayList<>(this.readVariableMap.values());
		}
		return this.readVariables;
	}
	
	public Collection<VarValue> getWrittenVariables() {
		if(this.writtenVariables==null || this.writtenVariables.size() < this.writtenVariableMap.size()){
			this.writtenVariables = new ArrayList<>(writtenVariableMap.values());			
		}
		
		return this.writtenVariables;
	}

	public void setReadVariables(List<VarValue> readVariables) {
		this.readVariables = readVariables;
	}
	
	public void addReadVariable(VarValue var){
//		VarValue readVar = find(this.readVariables, var);
//		if(readVar != null){
//			readVar.setStringValue(var.getStringValue());
//		}
//		else{
//			this.readVariables.add(var);			
//		}
		
		this.readVariableMap.put(var.getVarID(), var);
	}
	
	public void addWrittenVariable(VarValue var){
//		VarValue writtenVar = find(this.writtenVariables, var);
//		if(writtenVar != null){
//			writtenVar.setStringValue(var.getStringValue());
//		}
//		else{
//			this.writtenVariables.add(var);			
//		}
		
		this.writtenVariableMap.put(var.getVarID(), var);
	}
	
	private VarValue find(List<VarValue> variables, VarValue var) {
		for(VarValue existingVar: variables){
			String simpleID = Variable.truncateSimpleID(existingVar.getVarID());
			if(simpleID.equals(var.getVarID())){
				return existingVar;
			}
		}
		return null;
	}

	

	public void setWrittenVariables(List<VarValue> writtenVariables) {
		this.writtenVariables = writtenVariables;
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
	
//	public Map<Integer, TraceNode> findAllDominators() {
//		Map<Integer, TraceNode> dominators = new HashMap<>();
//		
//		findDominators(this, dominators);
//		
//		return dominators;
//	}
//
//	private void findDominators(TraceNode node, Map<Integer, TraceNode> dominators) {
//		for(TraceNode dominator: node.getDataDominators().keySet()){
//			if(!dominators.containsKey(dominator.getOrder())){
//				dominators.put(dominator.getOrder(), dominator);		
//				findDominators(dominator, dominators);				
//			}
//		}
//		
//		if(this.controlDominator != null){
//			dominators.put(this.controlDominator.getOrder(), this.controlDominator);
//		}
//		
//	}
//	
//	public Map<Integer, TraceNode> findAllDominatees() {
//		Map<Integer, TraceNode> dominatees = new HashMap<>();
//		
//		findDominatees(this, dominatees);
//		
//		return dominatees;
//	}
//
//	private void findDominatees(TraceNode node, Map<Integer, TraceNode> dominatees) {
//		for(TraceNode dominatee: node.getDataDominatee().keySet()){
//			if(!dominatees.containsKey(dominatee.getOrder())){
//				if(dominatee.getOrder() == 1){
//					System.currentTimeMillis();
//				}
//				
//				dominatees.put(dominatee.getOrder(), dominatee);		
//				findDominatees(dominatee, dominatees);				
//			}
//		}
//		
//		for(TraceNode controlDominatee: node.getControlDominatees()){
//			if(!dominatees.containsKey(controlDominatee.getOrder())){
//				if(controlDominatee.getOrder() == 1){
//					System.currentTimeMillis();
//				}
//				
//				dominatees.put(controlDominatee.getOrder(), controlDominatee);
//				findDominatees(controlDominatee, dominatees);				
//			}
//		}
//	}
	
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

	private List<TraceNode> allControlDominatees;
	public List<TraceNode> findAllControlDominatees() {
		if(allControlDominatees==null){
			List<TraceNode> controlDominatees = new ArrayList<>();
			findAllControlDominatees(this, controlDominatees);
			allControlDominatees = controlDominatees;
		}
		return allControlDominatees;
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
		
		if(this.getOrder()==5){
			System.currentTimeMillis();
		}
		
		abstractChildren.addAll(this.invocationChildren);
		clearLoopParentsInMethodParent(abstractChildren);
		for(TraceNode loopChild: getLoopChildren()){
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
		return CollectionUtils.nullToEmpty(loopChildren);
	}

	public TraceNode getLoopParent() {
		return loopParent;
	}

	public void setLoopParent(TraceNode loopParent) {
		this.loopParent = loopParent;
	}
	
	public void addLoopChild(TraceNode loopChild){
		loopChildren = CollectionUtils.initIfEmpty(loopChildren);
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

	public Trace getTrace() {
		return trace;
	}

	public void setTrace(Trace trace) {
		this.trace = trace;
	}

	public List<VarValue> getReturnedVariables() {
		return CollectionUtils.nullToEmpty(returnedVariables);
	}

	public long getRuntimePC() {
		return runtimePC;
	}

	public void setRuntimePC(long runtimePC) {
		this.runtimePC = runtimePC;
	}

	public String getInvokingMethod() {
		return invokingMethod;
	}

	public void setInvokingMethod(String invokingMethod) {
		this.invokingMethod = invokingMethod;
	}

	public InvokingDetail getInvokingDetail() {
		return invokingDetail;
	}

	public void setInvokingDetail(InvokingDetail invokingDetail) {
		this.invokingDetail = invokingDetail;
	}

	public List<VarValue> getPassParameters() {
		return CollectionUtils.nullToEmpty(passParameters);
	}

	public void setPassParameters(List<VarValue> passParameters) {
		this.passParameters = passParameters;
	}

	public void setControlScope(ControlScope scope) {
		this.breakPoint.setControlScope(scope);;
	}

	public boolean isBranch() {
		return this.breakPoint.isBranch();
	}

	public TraceNode getInvokingMatchNode() {
		return invokingMatchNode;
	}

	public void setInvokingMatchNode(TraceNode invokingMatchNode) {
		this.invokingMatchNode = invokingMatchNode;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
