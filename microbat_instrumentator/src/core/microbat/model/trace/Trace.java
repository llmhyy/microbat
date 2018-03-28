package microbat.model.trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;

import microbat.codeanalysis.ast.LocalVariableScopes;
import microbat.codeanalysis.bytecode.ByteCodeParser;
import microbat.codeanalysis.bytecode.CFG;
import microbat.codeanalysis.bytecode.CFGConstructor;
import microbat.codeanalysis.bytecode.CFGNode;
import microbat.codeanalysis.bytecode.MethodFinderByLine;
import microbat.model.AttributionVar;
import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.model.ControlScope;
import microbat.model.Scope;
import microbat.model.value.VarValue;
import microbat.model.value.VirtualValue;
import microbat.model.variable.Variable;
import microbat.util.Settings;
import sav.strategies.dto.AppJavaClassPath;

/**
 * This class stands for a trace for an execution
 * @author Yun Lin
 *
 */
public class Trace {
	
	private int observingIndex = -1;
	private AppJavaClassPath appJavaClassPath;
	private List<String> includedLibraryClasses = new ArrayList<>();
	private List<String> excludedLibraryClasses = new ArrayList<>();
	/**
	 * This variable is to trace whether the variables in different lines are the same
	 * local variable.
	 */
	private LocalVariableScopes localVariableScopes = new LocalVariableScopes();
	
	public Trace(AppJavaClassPath appJavaClassPath) {
		this.setAppJavaClassPath(appJavaClassPath);
	}
	
	/**
	 * This variable indicate the time of user ask for recommendation, in addition, the check time is also used
	 * to specify the time of a variable marked as "incorrect". Note that, newer variables has more importance
	 * in the trace.
	 */
	private int checkTime = 1;
	
	private List<TraceNode> exectionList = new ArrayList<>();
	/**
	 * tracking which steps read/write what variables, and what variables are read/written by which steps.
	 * key is the variable ID, and value is the entry containing all the steps reading/writing the corresponding
	 * variable.
	 */
	private Map<String, StepVariableRelationEntry> stepVariableTable = new HashMap<>();
	
	/**
	 * the time used to construct the trace, which is used for evaluation.
	 */
	private int constructTime = 0;
	
	private boolean isMultiThread = false;

	public void resetCheckTime(){
		this.checkTime = -1;
		for(TraceNode node: getExecutionList()){
			node.resetCheckTime();
		}
	}
	
	public List<TraceNode> getExecutionList() {
		return exectionList;
	}

	public void setExectionList(List<TraceNode> exectionList) {
		this.exectionList = exectionList;
	}
	
	public void addTraceNode(TraceNode node){
		this.exectionList.add(node);
	}
	
	public int size(){
		return this.exectionList.size();
	}
	
	public List<TraceNode> getTopMethodLevelNodes(){
		List<TraceNode> topList = new ArrayList<>();
		for(TraceNode node: this.exectionList){
			if(node.getInvocationParent() == null){
				topList.add(node);
			}
		}
		
		return topList;
	}
	
	public List<TraceNode> getTopLoopLevelNodes(){
		List<TraceNode> topList = new ArrayList<>();
		for(TraceNode node: this.exectionList){
			if(node.getLoopParent() == null){
				topList.add(node);
			}
		}
		
		return topList;
	}
	
	public List<TraceNode> getTopAbstractionLevelNodes(){
		List<TraceNode> topList = new ArrayList<>();
		for(TraceNode node: this.exectionList){
			if(node.getAbstractionParent() == null){
				topList.add(node);
			}
		}
		
		return topList;
	}
	
	public TraceNode getLatestNode(){
		int len = size();
		if(len > 0){
			return this.exectionList.get(len-1);
		}
		else{
			return null;
		}
	}
	
	public void resetObservingIndex(){
		this.observingIndex = -1;
	}
	
	public int getObservingIndex() {
		return observingIndex;
	}

	public void setObservingIndex(int observingIndex) {
		this.observingIndex = observingIndex;
	}
	
	public int searchBackwardTraceNode(String expression){
		int resultIndex = -1;
		
		for(int i=observingIndex-1; i>=0; i--){
			resultIndex = searchTraceNode(expression, i);
			if(resultIndex != -1){
				break;
			}
		}
		
		if(resultIndex != -1){
			this.observingIndex = resultIndex;
		}
		return resultIndex;
	}

	public int searchForwardTraceNode(String expression){
		int resultIndex = -1;
		
		for(int i=observingIndex+1; i<exectionList.size(); i++){
			resultIndex = searchTraceNode(expression, i);
			if(resultIndex != -1){
				break;
			}
		}
		
		if(resultIndex != -1){
			this.observingIndex = resultIndex;			
		}
		return resultIndex;
	}

	private int searchTraceNode(String expression, int i) {
		int resultIndex = -1;
		TraceNode node = exectionList.get(i);
		BreakPoint breakPoint = node.getBreakPoint();
		String className = breakPoint.getDeclaringCompilationUnitName();
		int lineNumber = breakPoint.getLineNumber();
		
		String simpleClassName = className.substring(className.lastIndexOf(".")+1, className.length());
		
		try{
			int order = Integer.valueOf(expression);
			if(node.getOrder()==order){
				resultIndex = i;
			}
		}
		catch(Exception e){
			if(expression.matches("id=(\\w|\\W)+:\\d+")){
				String id = expression.replace("id=", "");
				for(VarValue readVar: node.getReadVariables()){
					if(readVar.getVarID().equals(id)){
						resultIndex = i;
					}
					else if(readVar.getAliasVarID()!=null && readVar.getAliasVarID().equals(id)){
						resultIndex = i;
					}
				}
			}
			else{
				String exp = combineTraceNodeExpression(className, lineNumber);
				if(exp.equals(expression)){
					resultIndex = i;
				}
				else if(simpleClassName.equals(expression)){
					if (resultIndex==-1) {
						resultIndex = i;					
					}
				}
			}
		}
		
		return resultIndex;
	}
	
	public static String combineTraceNodeExpression(String className, int lineNumber){
		className = className.substring(className.lastIndexOf(".")+1, className.length());
		
		String exp = className + " line:" + lineNumber;
		return exp;
	}

	public void conductStateDiff() {
		for(int i=0; i<this.exectionList.size(); i++){
			TraceNode node = this.exectionList.get(i);
			node.conductStateDiff();
		}
		
	}
	
	/**
	 * Get the step where the read variable is defined. If we cannot find such a 
	 * step, we find the step defining its (grand)parent of the read variable. 
	 * @param readVar
	 * @return
	 */
	public TraceNode findDataDominator(TraceNode checkingNode, VarValue readVar) {
		List<TraceNode> producers = new ArrayList<>();
		String varID = readVar.getVarID();
		StepVariableRelationEntry entry = getStepVariableTable().get(varID);
		if(entry != null){
			producers.addAll(entry.getProducers());
		}
		
		if(readVar.getAliasVarID()!=null){
			StepVariableRelationEntry entry0 = getStepVariableTable().get(readVar.getAliasVarID());
			if(entry0 != null){
				producers.addAll(entry0.getProducers());				
			}
		}
		
		if(producers.isEmpty()) {
			return null;
		}
		else {
			if(producers.size()==1){
				return producers.get(0);				
			}
			else{
				int distance = 0;
				TraceNode producer = null;
				for(TraceNode n: producers){
					if(producer==null){
						int dis = checkingNode.getOrder() - n.getOrder();
						if(dis>0){
							producer = n;							
						}
					}
					else{
						int dis = checkingNode.getOrder() - n.getOrder();
						if(dis>0 && dis<distance){
							producer = n;							
						}
					}
				}
				return producer;
			}
			
		}
		
	}
	
	public void constructLoopParentRelation(){
		Stack<TraceNode> loopParentStack = new Stack<>();
		System.currentTimeMillis();
		for(TraceNode node: this.exectionList){
			
			/**
			 * if out of the scope the loop parent, pop
			 * this step decide the influential loop parent.
			 */
			if(!loopParentStack.isEmpty()){
				TraceNode currentLoopParent = loopParentStack.peek();
				while(!isLoopParentContainDirectlyOrIndirectly(currentLoopParent, node)
						                                                                 /**for recursive case*/
						|| (node.getLineNumber() == currentLoopParent.getLineNumber() && loopParentHaveNotLoopChildOfSomeInvocationParentOfNode(currentLoopParent, node))
						|| (node.getOrder()==currentLoopParent.getOrder()+1 && !currentLoopParent.getLoopScope().containsNodeScope(node))){
					
					loopParentStack.pop();
					if(loopParentStack.isEmpty()){
						break;
					}
					currentLoopParent = loopParentStack.peek(); 
				}
			}
			
			/**
			 * connect loop parent-child relation
			 * this step decide the direct loop child for the influential loop parent in the peek of the stack
			 */
			if(!loopParentStack.isEmpty()){
				TraceNode loopParent = loopParentStack.peek();
				if(loopParent.getLoopScope().containsNodeScope(node) && loopParentHaveNotLoopChildOfSomeInvocationParentOfNode(loopParent, node)){
					loopParent.addLoopChild(node);
					node.setLoopParent(loopParent);					
				}
			}
			
			/**
			 * if a node is a loop condition, push
			 */
			if(node.isLoopCondition()){
				loopParentStack.push(node);
			}
		}
	}

	private boolean loopParentHaveNotLoopChildOfSomeInvocationParentOfNode(TraceNode currentLoopParent, TraceNode node) {
		List<TraceNode> invocationParents = node.findAllInvocationParents();
		for(TraceNode parent: invocationParents){
			if(currentLoopParent.getLoopChildren().contains(parent)){
				return false;
			}
		}
		
		return true;
	}

	private boolean isLoopParentContainDirectlyOrIndirectly(TraceNode currentLoopParent, TraceNode node) {
		List<TraceNode> invocationParentList = new ArrayList<>();
		TraceNode invocationParent = node;
		while(invocationParent != null){
			invocationParentList.add(invocationParent);
			invocationParent = invocationParent.getInvocationParent();
		}
		
		for(TraceNode iParent: invocationParentList){
			if(currentLoopParent.getLoopScope().containsNodeScope(iParent)){
				return true;
			}
		}
		
		return false;
	}

	private Map<BreakPoint, ControlScope> controlScopeMap = new HashMap<>();
	private Map<String, CFG> methodCFGMap = new HashMap<>();
	
	private ControlScope parseControlScope(BreakPoint breakPoint) {
		
		ControlScope scope = controlScopeMap.get(breakPoint);
		if(scope!=null){
			breakPoint.setConditional(scope.isCondition());
			breakPoint.setBranch(scope.isBranch());
			return scope;
		}
		
		List<ClassLocation> ranges = new ArrayList<>();
		
		CFG cfg = methodCFGMap.get(breakPoint.getMethodSign());
		if(cfg==null){
			MethodFinderByLine finder = new MethodFinderByLine(breakPoint);
			ByteCodeParser.parse(breakPoint.getClassCanonicalName(), finder, appJavaClassPath);
			Method method = finder.getMethod();
			CFGConstructor cfgConstructor = new CFGConstructor();
			cfg = cfgConstructor.buildCFGWithControlDomiance(method.getCode());
			cfg.setMethod(method);
			methodCFGMap.put(breakPoint.getMethodSign(), cfg);
		}
		
		
		List<InstructionHandle> correspondingList = findCorrepondingIns(breakPoint, cfg, cfg.getMethod());
		
		for(InstructionHandle ins: correspondingList){
			CFGNode cfgNode = cfg.findNode(ins);
			if(!breakPoint.isConditional()){
				breakPoint.setConditional(cfgNode.isConditional());				
			}
			
			if(!breakPoint.isBranch()){
				breakPoint.setBranch(cfgNode.isBranch());				
			}
			
			List<ClassLocation> controlScope0 = findControlledLines(cfgNode.getControlDependentees(), cfg.getMethod(), breakPoint);
			ranges.addAll(controlScope0);
		}
		
		ClassLocation own = new ClassLocation(breakPoint.getClassCanonicalName(), breakPoint.getMethodSign(), breakPoint.getLineNumber());
		if(!ranges.contains(own)){
			ranges.add(own);
		}
		
		scope = new ControlScope();
		scope.setRangeList(ranges);
		scope.setCondition(breakPoint.isConditional());
		scope.setBranch(breakPoint.isBranch());
		controlScopeMap.put(breakPoint, scope);
		
		return scope;
	}
	
	private List<InstructionHandle> findCorrepondingIns(BreakPoint breakPoint, CFG cfg, Method method) {
		List<InstructionHandle> list = new ArrayList<>();
		for(CFGNode node: cfg.getNodeList()){
			int line = method.getLineNumberTable().getSourceLine(node.getInstructionHandle().getPosition());
			if(line==breakPoint.getLineNumber()){
				list.add(node.getInstructionHandle());
			}
		}
		return list;
	}

	private List<ClassLocation> findControlledLines(List<CFGNode> controlDependentees, Method method,
			BreakPoint breakPoint) {
		List<ClassLocation> controlLines = new ArrayList<>();
		for(CFGNode node: controlDependentees){
			int line = method.getLineNumberTable().getSourceLine(node.getInstructionHandle().getPosition());
			ClassLocation location = new ClassLocation(breakPoint.getClassCanonicalName(), breakPoint.getMethodSign(), line);
			
			if(!controlLines.contains(location)){
				controlLines.add(location);
			}
		}
		return controlLines;
	}

	public void constructControlDomianceRelation() {
		TraceNode controlDominator = null;
		for(TraceNode node: this.exectionList){
			ControlScope scope = parseControlScope(node.getBreakPoint());		
			System.currentTimeMillis();
			node.setControlScope(scope);
			
			if(controlDominator != null){
				
				if(isContainedInScope(node, controlDominator.getControlScope())){
					controlDominator.addControlDominatee(node);
					node.setControlDominator(controlDominator);
				}
				/** which means the {@code controlDominator} is no longer effective now */
				else{
					controlDominator = findContainingControlDominator(node, controlDominator);
					
					if(controlDominator != null){
						controlDominator.addControlDominatee(node);
						node.setControlDominator(controlDominator);
					}
				}
			}
			
			if(node.isBranch()){
				controlDominator = node;
			}
		}
	}


	private TraceNode findContainingControlDominator(TraceNode node, TraceNode controlDominator) {
		TraceNode superControlDominator = controlDominator.getControlDominator();
		while(superControlDominator != null){
			if(isContainedInScope(node, superControlDominator.getControlScope())){
				return superControlDominator;
			}
			superControlDominator = superControlDominator.getControlDominator();
		}
		
		return null;
	}

	/**
	 * I will consider the invocation parents of {@code node} as well
	 * @param node
	 * @param conditionScope
	 * @return
	 */
	private boolean isContainedInScope(TraceNode node, Scope conditionScope) {
		if(conditionScope==null){
			return false;
		}
		
		List<TraceNode> testingSet = new ArrayList<>();
		testingSet.add(node);
		List<TraceNode> invocationParents = node.findAllInvocationParents();
		testingSet.addAll(invocationParents);
		
		for(TraceNode n: testingSet){
			if(conditionScope.containsNodeScope(n)){
				return true;
			}
		}
		
		return false;
	}

//	@Deprecated
//	private void constructControlDomianceRelation0() {
//		if(this.exectionList.size()>1){
//			for(int i=this.exectionList.size()-1; i>=1; i--){
//				TraceNode dominatee = this.exectionList.get(i);
//				List<TraceNode> controlDominators = findControlDominators(dominatee.getOrder());
//				
//				for(TraceNode controlDominator: controlDominators){
//					dominatee.addControlDominator(controlDominator);
//					controlDominator.addControlDominatee(dominatee);
//				}
//			}			
//		}
//		
//	}

//	private List<TraceNode> findControlDominators(int startOrder) {
//		
//		List<TraceNode> controlDominators = new ArrayList<>();
//
//		TraceNode dominatee = this.exectionList.get(startOrder-1);
//		for(int i=startOrder-1-1; i>=0; i--){
//			TraceNode node = this.exectionList.get(i);
//			
//			if(node.isConditional()){
//				Scope conditionScope = node.getConditionScope();
//				if(conditionScope != null){
//					if(conditionScope.containsNodeScope(dominatee)){
//						controlDominators.add(node);
//						return controlDominators;
//					}
//					else if(conditionScope.hasJumpStatement()){
//						controlDominators.add(node);
//					}
//				}
//				
//			}
//			
//			if(node.equals(dominatee.getInvocationParent())){
//				dominatee = dominatee.getInvocationParent();
//			}
//		}
//		
//		return controlDominators;
//	}

//	private void constructDataDomianceRelation() {
//		for(String varID: this.stepVariableTable.keySet()){
//			
//			StepVariableRelationEntry entry = this.stepVariableTable.get(varID);
//			List<TraceNode> producers = entry.getProducers();
//			List<TraceNode> consumers = entry.getConsumers();
//			
//			if(producers.isEmpty()){
//				//System.err.println("there is no producer for variable " + entry.getAliasVariables());
//			}
//			
////			if(producers.size() > 1){
////				System.err.println("there are more than one producer for variable " + entry.getAliasVariables());
////			}
//
//			if(!producers.isEmpty()){
//				TraceNode producer = producers.get(0);
//				List<String> varList = new ArrayList<>();
//				varList.add(varID);
//				for(TraceNode consumer: consumers){
//					producer.addDataDominatee(consumer, varList);
//					consumer.addDataDominator(producer, varList);
//				}
//				
//			}
//		}
//		
//	}

	public Map<String, StepVariableRelationEntry> getStepVariableTable() {
		return stepVariableTable;
	}

	public TraceNode findLastestExceptionNode() {
		for(int i=0; i<exectionList.size(); i++){
			TraceNode lastestNode = exectionList.get(exectionList.size()-1-i);
			if(lastestNode.isException()){
				return lastestNode;
			}
		}
		
		return null;
	}
	
	private Map<String, TraceNode> latestNodeDefiningVariableMap = new HashMap<>();
	
	/**
	 * if we are finding defining step of a read variable, v, the defining step is the latest
	 * step defining v.
	 * 
	 * if we are finding defining step of a written variable:
	 * (1) if it is not a field/index of an object/array, the defining step is the latest step.
	 * (2) if it is a sub-value, sv, let the latest step be s1, the defining step of the sub-value sv is 
	 * (2-1) s1 if the variable id of sv is never defined before 
	 * (2-2) s2, s2 is the latest step defining sv.
	 * 
	 * @param accessType
	 * @param currentNode
	 * @param isSubValue
	 * @param varID
	 * @param aliasVarID
	 * @return
	 */
	public String findDefiningNodeOrder(String accessType, TraceNode currentNode,
			String varID, String aliasVarID) {
		varID = Variable.truncateSimpleID(varID);
		aliasVarID = Variable.truncateSimpleID(aliasVarID);
		String definingOrder = "0";
		if(accessType.equals(Variable.WRITTEN)){
			definingOrder = String.valueOf(currentNode.getOrder());
			latestNodeDefiningVariableMap.put(varID, currentNode);
			if(aliasVarID!=null){
				latestNodeDefiningVariableMap.put(aliasVarID, currentNode);				
			}
		}
		else if(accessType.equals(Variable.READ)){
			TraceNode node1 = latestNodeDefiningVariableMap.get(varID);
			TraceNode node2 = latestNodeDefiningVariableMap.get(aliasVarID);
			
			int order = 0;
			if(node1!=null && node2==null){
				order = node1.getOrder();
			}
			else if(node1==null && node2!=null){
				order = node2.getOrder();
			}
			else if(node1!=null && node2!=null){
				order = (node1.getOrder()>node2.getOrder())?node1.getOrder():node2.getOrder();
//				order = node2.getOrder();
			}
			
			definingOrder = String.valueOf(order);
		}
		
		return definingOrder;
	}
	
	public TraceNode findLastestNodeDefiningPrimitiveVariable(String varID, int limitOrder){
		for(int i=limitOrder-2; i>=0; i--){
			TraceNode node = exectionList.get(i);
			for(VarValue var: node.getWrittenVariables()){
				String writtenVarID = var.getVarID();
				if(writtenVarID.contains(":")){
					String simpleVarID = writtenVarID.substring(0, writtenVarID.indexOf(":"));
					if(simpleVarID.equals(varID)){
						return node;
					}
				}
			}
		}
		
		return null;
	}

	public TraceNode findLastestNodeDefiningPrimitiveVariable(String varID) {
		TraceNode node = findLastestNodeDefiningPrimitiveVariable(varID, exectionList.size());
		return node;
		
//		for(int i=exectionList.size()-2; i>=0; i--){
//			TraceNode node = exectionList.get(i);
//			for(VarValue var: node.getWrittenVariables()){
//				String writtenVarID = var.getVarID();
//				if(writtenVarID.contains(":")){
//					String simpleVarID = writtenVarID.substring(0, writtenVarID.indexOf(":"));
//					if(simpleVarID.equals(varID)){
//						return node;
//					}
//				}
//			}
//		}
//		
//		return null;
	}

	/**
	 * Note that, if a variable is a primitive type, I cannot retrieve its heap address, therefore, I use the static approach
	 * to uniquely identify a variable, i.e., variable ID. Please refer to {@link microbat.model.variable.Variable#varID} for details.
	 * <br>
	 * <br>
	 * However, in order to save the parsing efficiency, the ID of variables of primitive types does not have the suffix of ":order".
	 * That's why I need to do the mapping from state variables to read/written variables.
	 * 
	 * @param varID
	 * @param order
	 * @return
	 */
	public String findTrueIDFromStateVariable(String varID, int order) {
		for(int i=order; i>=1; i--){
			TraceNode node = this.exectionList.get(i-1);
			String trueID = findTrueID(node.getWrittenVariables(), varID); 
			
			if(trueID != null){
				return trueID;
			}
			else{
				if(i != order){
					trueID = findTrueID(node.getReadVariables(), varID);
					if(trueID != null){
						return trueID;
					}
				}					
			}
		}
		return null;
	}
	
	private String findTrueID(List<VarValue> readOrWriteVars, String varID){
		for(VarValue var: readOrWriteVars){
			if(!(var instanceof VirtualValue)){
				String ID = var.getVarID();
				String concanateID = ID.substring(0, ID.indexOf(":"));
				if(concanateID.equals(varID)){
					return ID;
				}				
			}
		}
		
		return null;
	}

	public int getCheckTime() {
		return checkTime;
	}

	public void setCheckTime(int checkTime) {
		this.checkTime = checkTime;
	}
	
	public void clearAllSuspiciousness(){
		for(TraceNode node: this.exectionList){
			node.getSuspicousScoreMap().clear();
		}
	}

//	public TraceNode findSuspiciousControlDominator(TraceNode buggyNode, String feedback) {
//		
//		List<TraceNode> dominators;
//		if(feedback.equals(UserFeedback.WRONG_PATH)){
//			dominators = buggyNode.getControlDominators();
//		}
//		else{
//			dominators = new ArrayList<>(buggyNode.getDataDominator().keySet());
//		}
//		
//		if(dominators.isEmpty()){
//			return buggyNode;
//		}
//		else{
//			for(TraceNode controlDominator: dominators){
//				if(!controlDominator.hasChecked()){
//					return controlDominator;
//				}
//			}
//			return dominators.get(0);
//		}
//	}
	
	
	public TraceNode findMostSupiciousNode(AttributionVar var) {
		TraceNode suspiciousNode = null;
		for(TraceNode node: this.exectionList){
			if(suspiciousNode == null){
				suspiciousNode = node;
			}
			else{
				Double score1 = node.getSuspicousScore(var);
				score1 = (score1 == null) ? 0 : score1;
				Double score2 = suspiciousNode.getSuspicousScore(var);
				score2 = (score2 == null) ? 0 : score2;
				if(score1 > score2){
					suspiciousNode = node;
				}
				
//				if(node.getOrder()==203 || node.getOrder()==194){
//					AgentLogger.debug(node.getOrder() + "(" + var.getVarID() + "):" + node.getSuspicousScore(var));
//				}
			}
		}
		
		return suspiciousNode;
	}

	public LoopSequence findLoopRangeOf(TraceNode currentNode) {
		
		TraceNode controlDominator = currentNode.findContainingLoopControlDominator();
		
		if(controlDominator != null){
			List<TraceNode> allControlDominatees = controlDominator.findAllControlDominatees();
			Collections.sort(allControlDominatees, new TraceNodeOrderComparator());
			
			List<TraceNode> range = extendLoopRange(allControlDominatees, controlDominator);
			Collections.sort(range, new TraceNodeOrderComparator());
			
			LoopSequence loopSequence = new LoopSequence(range.get(0).getOrder(), 
					range.get(range.size()-1).getOrder());
			
			return loopSequence;
		}
		
		return null;
	}

	/**
	 * extend from one single iteration to all the iterations of the loop.
	 * @param allControlDominatees
	 * @param controlLoopDominator
	 * @return
	 */
	private List<TraceNode> extendLoopRange(List<TraceNode> allControlDominatees, TraceNode controlLoopDominator) {

		List<TraceNode> range = new ArrayList<>();
		for(int i=controlLoopDominator.getOrder()-2; i>=0; i--){
			TraceNode node = this.exectionList.get(i);
			boolean isInSameLoop = isInSameLoop(node, controlLoopDominator);
			if(isInSameLoop){
				range.add(node);
			}
			else{
				break;
			}
		}
		
		TraceNode lastLoopNode = allControlDominatees.get(allControlDominatees.size()-1);
		for(int i=lastLoopNode.getOrder()-1+1; i<exectionList.size(); i++){
			TraceNode node = this.exectionList.get(i);
			boolean isInSameLoop = isInSameLoop(node, controlLoopDominator);
			if(isInSameLoop){
				range.add(node);
			}
			else{
				break;
			}
		}
		
		range.addAll(allControlDominatees);
		return range;
	}

	/**
	 * check by seeing whether the control loop dominator of <code>node</code> has the same location
	 * with the parameter <code>controlLoopDominator</code>
	 * @param node
	 * @param controlLoopDominator
	 * @return
	 */
	private boolean isInSameLoop(TraceNode node, TraceNode controlLoopDominator) {
		
		TraceNode testNode = node;
		while(testNode != null && !testNode.hasSameLocation(controlLoopDominator)){
			testNode = testNode.findContainingLoopControlDominator();
		}
		
		return testNode != null;
	}

	public TraceNode getEarliestNodeWithWrongVar() {
		for(TraceNode node: this.exectionList){
			if(node.getWittenVarCorrectness(Settings.interestedVariables, false)==TraceNode.WRITTEN_VARS_INCORRECT
					|| node.getReadVarCorrectness(Settings.interestedVariables, false)==TraceNode.READ_VARS_INCORRECT){
				return node;
			}
		}
		return null;
	}
	
	public TraceNode getLatestProducerBySimpleVarIDForm(int startOrder, String simpleVarID){
		int latestOrder = -1;
		String latestVarID = null;
		for(String varID: this.stepVariableTable.keySet()){
			String orderString = varID.substring(varID.indexOf(":")+1);
			int order = Integer.valueOf(orderString);
			
			String simVarID = Variable.truncateSimpleID(varID);
			if(order > startOrder || !simpleVarID.equals(simVarID)){
				continue;
			}
			
			if(latestOrder == -1){
				latestOrder = order;
				latestVarID = varID;
			}
			else{
				if(latestOrder > order){
					latestOrder = order;
					latestVarID = varID;
				}
			}
		}
		
		if(latestVarID != null){
			return getProducer(latestVarID);			
		}
		return null;
	}

	public TraceNode getProducer(String varID) {
		StepVariableRelationEntry entry = this.stepVariableTable.get(varID);
		
		if(entry == null){
			System.err.println("the variable with ID " + varID + " is not explicitly read or written");
			return null;
		}
		
		if(!entry.getProducers().isEmpty()){
			return entry.getProducers().get(0);
		}
		
		return null;
	}

	public int getConstructTime() {
		return constructTime;
	}

	public void setConstructTime(int constructTime) {
		this.constructTime = constructTime;
	}

	public TraceNode getLatestWrongNode() {
		for(TraceNode node: this.exectionList){
			if(!node.isAllReadWrittenVarCorrect(false) || node.isWrongPathNode()){
				return node;
			}
		}
		return null;
	}

	public Map<String, List<Integer>> getExecutedLocation(){
		Map<String, List<Integer>> locationMap = new HashMap<>();
		for(TraceNode node: this.exectionList){
			List<Integer> lines = locationMap.get(node.getDeclaringCompilationUnitName());
			Integer line = node.getLineNumber();
			if(lines == null){
				lines = new ArrayList<>();
			}
			
			if(!lines.contains(line)){
				lines.add(line);
			}
			
			locationMap.put(node.getDeclaringCompilationUnitName(), lines);
		}
		
		return locationMap;
	}

	public AppJavaClassPath getAppJavaClassPath() {
		return appJavaClassPath;
	}

	public void setAppJavaClassPath(AppJavaClassPath appJavaClassPath) {
		this.appJavaClassPath = appJavaClassPath;
	}

//	public LocalVariableScopes getLocalVariableScopes() {
//		return localVariableScopes;
//	}
//
//	public void setLocalVariableScopes(LocalVariableScopes localVariableScopes) {
//		this.localVariableScopes = localVariableScopes;
//	}
	
	public TraceNode getTraceNode(int order){
		return this.exectionList.get(order-1);
	}

	public boolean isMultiThread() {
		return isMultiThread;
	}

	public void setMultiThread(boolean isMultiThread) {
		this.isMultiThread = isMultiThread;
	}
	
	public List<String> getIncludedLibraryClasses() {
		return includedLibraryClasses;
	}

	public void setIncludedLibraryClasses(List<String> includedLibraryClasses) {
		this.includedLibraryClasses = includedLibraryClasses;
	}

	public List<String> getExcludedLibraryClasses() {
		return excludedLibraryClasses;
	}

	public void setExcludedLibraryClasses(List<String> excludedLibraryClasses) {
		this.excludedLibraryClasses = excludedLibraryClasses;
	}
}
