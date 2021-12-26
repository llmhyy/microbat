package microbat.model.trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;

import microbat.codeanalysis.ast.LocalVariableScopes;
import microbat.model.AttributionVar;
import microbat.model.BreakPoint;
import microbat.model.Scope;
import microbat.model.value.VarValue;
import microbat.model.value.VirtualValue;
import microbat.model.variable.Variable;
import microbat.util.JavaUtil;
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
	private boolean isMain;
	private String threadName;
	private String traceId;
	
	/**
	 * This variable is to trace whether the variables in different lines are the same
	 * local variable.
	 */
	private LocalVariableScopes localVariableScopes = new LocalVariableScopes();
	
	public Trace(AppJavaClassPath appJavaClassPath, String traceId) {
		this.setAppJavaClassPath(appJavaClassPath);
		this.traceId = traceId;
	}
	
	/**
	 * This variable indicate the time of user ask for recommendation, in addition, the check time is also used
	 * to specify the time of a variable marked as "incorrect". Note that, newer variables has more importance
	 * in the trace.
	 */
	private int checkTime = 1;
	
	private List<TraceNode> executionList = new ArrayList<>();
	/**
	 * tracking which steps read/write what variables, and what variables are read/written by which steps.
	 * key is the variable ID, and value is the entry containing all the steps reading/writing the corresponding
	 * variable.
	 */
	@Deprecated
	private Map<String, StepVariableRelationEntry> stepVariableTable = new HashMap<>();
	
	/**
	 * the time used to construct the trace, which is used for evaluation.
	 */
	private int constructTime = 0;
	
	private long threadId;

	public void resetCheckTime(){
		this.checkTime = -1;
		for(TraceNode node: getExecutionList()){
			node.resetCheckTime();
		}
	}
	
	public List<TraceNode> getExecutionList() {
		return executionList;
	}

	public void setExecutionList(List<TraceNode> exectionList) {
		this.executionList = exectionList;
	}
	
	public void addTraceNode(TraceNode node){
		this.executionList.add(node);
	}
	
	public int size(){
		return this.executionList.size();
	}
	
	public List<TraceNode> getTopMethodLevelNodes(){
		List<TraceNode> topList = new ArrayList<>();
		for(TraceNode node: this.executionList){
			if(node.getInvocationParent() == null){
				topList.add(node);
			}
		}
		
		return topList;
	}
	
	public List<TraceNode> getTopLoopLevelNodes(){
		List<TraceNode> topList = new ArrayList<>();
		for(TraceNode node: this.executionList){
			if(node.getLoopParent() == null){
				topList.add(node);
			}
		}
		
		return topList;
	}
	
	public List<TraceNode> getTopAbstractionLevelNodes(){
		List<TraceNode> topList = new ArrayList<>();
		for(TraceNode node: this.executionList){
			if(node.getAbstractionParent() == null){
				topList.add(node);
			}
		}
		
		return topList;
	}
	
	public TraceNode getLatestNode(){
		int len = size();
		if(len > 0){
			return this.executionList.get(len-1);
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
	
	public String getId() {
		return traceId;
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
		
		for(int i=observingIndex+1; i<executionList.size(); i++){
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
		TraceNode node = executionList.get(i);
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
//			if(expression.matches("id=(\\w|\\W)+:\\d+")){
			if(expression.matches("id=(\\w|\\W)+")){
				String id = expression.replace("id=", "");
				for(VarValue readVar: node.getReadVariables()){
					if(readVar.getVarID().contains(id)){
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
		for(int i=0; i<this.executionList.size(); i++){
			TraceNode node = this.executionList.get(i);
			node.conductStateDiff();
		}
		
	}
	
	/**
	 * Get the step where the read variable is defined. If we cannot find such a 
	 * step, we find the step defining its (grand)parent of the read variable. 
	 * @param readVar
	 * @return
	 */
	public TraceNode findDataDependency(TraceNode checkingNode, VarValue readVar) {
		return findProducer(readVar, checkingNode);
	}
	
	public List<TraceNode> findDataDependentee(TraceNode traceNode, VarValue writtenVar) {
		return findConsumer(writtenVar, traceNode);
	}
	
	private List<TraceNode> findConsumer(VarValue writtenVar, TraceNode startNode) {
		List<TraceNode> consumers = new ArrayList<TraceNode>();
		
		String varID = Variable.truncateSimpleID(writtenVar.getVarID());
		String headID = Variable.truncateSimpleID(writtenVar.getAliasVarID());
		
		for(int i=startNode.getOrder()+1; i>=this.getExecutionList().size(); i++) {
			TraceNode node = this.getTraceNode(i);
			for(VarValue readVar: node.getReadVariables()) {
				
				String rVarID = Variable.truncateSimpleID(readVar.getVarID());
				String rHeadID = Variable.truncateSimpleID(readVar.getAliasVarID());
				
				if(rVarID != null && rVarID.equals(varID)) {
					consumers.add(node);						
				}
				
				if(rHeadID != null && rHeadID.equals(headID)) {
					consumers.add(node);
				}
				
				VarValue childValue = readVar.findVarValue(varID, headID);
				if(childValue != null) {
					consumers.add(node);
				}
				
			}
		}
		
		return consumers;
	}

	public List<TraceNode> findNextReadingTraceNodes(VarValue value, int startOrder){
		String varID = value.getAliasVarID();
		varID = Variable.truncateSimpleID(varID);
		
		List<TraceNode> list = new ArrayList<>();
		for(int i=startOrder; i<this.executionList.size(); i++){
			TraceNode node = this.executionList.get(i);
			for(VarValue readVar: node.getReadVariables()){
				if(readVar.getAliasVarID()!=null){
					String readVarID = Variable.truncateSimpleID(readVar.getAliasVarID());
					if(readVarID.equals(varID)){
						list.add(node);						
					}
				}
				
			}
		}
		
		System.currentTimeMillis();
		return list;
	}
	
	public List<TraceNode> findPrevReadingTraceNodes(VarValue value, int startOrder){
		List<TraceNode> list = new ArrayList<>();
		for(int i=startOrder-2; i>0; i--){
			TraceNode node = this.executionList.get(i);
			if(node.getReadVariables().contains(value)){
				list.add(node);
			}
		}
		
		return list;
	}
	
	public void constructLoopParentRelation(){
		Stack<TraceNode> loopParentStack = new Stack<>();
		System.currentTimeMillis();
		for(TraceNode node: this.executionList){
			
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
				if(loopParent.getLoopScope().containsNodeScope(node) 
						&& loopParentHaveNotLoopChildOfSomeInvocationParentOfNode(loopParent, node)){
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

	public void constructDomianceRelation(){
//		constructDataDomianceRelation();
//		constructControlDomianceRelation0();
		constructControlDomianceRelation();
	}
	
	class CatchClauseFinder extends ASTVisitor{
		CompilationUnit cu;
		int lineNumber;
		
		public CatchClauseFinder(CompilationUnit cu, int lineNumber) {
			super();
			this.cu = cu;
			this.lineNumber = lineNumber;
		}

		CatchClause containingClause;
		
		public boolean visit(CatchClause clause) {
			int startLine = cu.getLineNumber(clause.getStartPosition());
			int endLine = cu.getLineNumber(clause.getStartPosition()+clause.getLength());
			
			if(startLine<=lineNumber && lineNumber<=endLine) {
				containingClause = clause;
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	private void constructControlDomianceRelation() {
		TraceNode controlDominator = null;
		for(TraceNode node: this.executionList){
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
			
			//add try-catch flow
			//testAndAppendTryCatchControlFlow(node);
			
			if(node.isConditional()){
				controlDominator = node;
			}
		}
	}

	private Map<String, CatchClause> catchClauseMap = new HashMap<>();
	
	private void testAndAppendTryCatchControlFlow(TraceNode node) {
		String key = node.getDeclaringCompilationUnitName()+":"+node.getLineNumber();
		CatchClause clause = catchClauseMap.get(key);
		CompilationUnit cu = JavaUtil.findCompilationUnitInProject(node.getDeclaringCompilationUnitName(), this.appJavaClassPath);
		if(clause==null){
			if(cu == null) {
				cu = JavaUtil.findCompiltionUnitBySourcePath(node.getBreakPoint().getFullJavaFilePath(), 
						node.getDeclaringCompilationUnitName());
			}
			 
			CatchClauseFinder finder = new CatchClauseFinder(cu, node.getLineNumber());
			cu.accept(finder);
			catchClauseMap.put(key, finder.containingClause);
		}
		
		
		if(clause!=null) {
			TraceNode existingControlDom = node.getControlDominator();
			if(existingControlDom!=null) {
				if (!isClauseContainScope(node, cu, clause, existingControlDom)) {
					addTryCatchControlFlow(node);
				}
			}
			else {
				addTryCatchControlFlow(node);
			}
			
		}
	}

	private boolean isClauseContainScope(TraceNode node, CompilationUnit cu, CatchClause clause, TraceNode existingControlDom) {
		if(node.getDeclaringCompilationUnitName().equals(existingControlDom.getDeclaringCompilationUnitName())) {
			int startLine = cu.getLineNumber(clause.getStartPosition());
			int endLine = cu.getLineNumber(clause.getStartPosition()+clause.getLength());
			
			if(startLine<=existingControlDom.getLineNumber() && existingControlDom.getLineNumber()<=endLine) {
				return true;
			}
		}
		
		return false;
	}

	private void addTryCatchControlFlow(TraceNode node) {
		TraceNode previousNode = node.getStepInPrevious();
		while(previousNode!=null) {
			if(previousNode.isException()) {
				break;
			}
			previousNode = previousNode.getStepInPrevious();
		}
		
		if(previousNode!=null) {
			node.setControlDominator(previousNode);
			previousNode.addControlDominatee(node);
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

	@Deprecated
	public Map<String, StepVariableRelationEntry> getStepVariableTable() {
		return stepVariableTable;
	}

//	private Map<String, TraceNode> latestNodeDefiningVariableMap = new HashMap<>();
	
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
//			latestNodeDefiningVariableMap.put(varID, currentNode);
//			if(aliasVarID!=null){
//				latestNodeDefiningVariableMap.put(aliasVarID, currentNode);				
//			}
		}
		else if(accessType.equals(Variable.READ)){
//			TraceNode node1 = latestNodeDefiningVariableMap.get(varID);
//			TraceNode node2 = latestNodeDefiningVariableMap.get(aliasVarID);
//			
//			int order = 0;
//			if(node1!=null && node2==null){
//				order = node1.getOrder();
//			}
//			else if(node1==null && node2!=null){
//				order = node2.getOrder();
//			}
//			else if(node1!=null && node2!=null){
//				order = (node1.getOrder()>node2.getOrder())?node1.getOrder():node2.getOrder();
////				order = node2.getOrder();
//			}
//			TraceNode node = this.findProducer(varValue, startNode)
//			definingOrder = String.valueOf(order);
			
			definingOrder = null;
		}
		
		return definingOrder;
	}

	public TraceNode findLatestNodeDefiningVariable(String varID, int startingOrder){
		for(int i=startingOrder-2; i>=0; i--){
			TraceNode node = executionList.get(i);
			int count = 0;
			for(VarValue var: node.getWrittenVariables()){
				count++;
				if(count>100){
					break;
				}
				
				String writtenVarID = var.getVarID();
				String simpleVarID = Variable.truncateSimpleID(writtenVarID);
				String simpleAliasID = Variable.truncateSimpleID(var.getAliasVarID());
				if(simpleVarID.equals(varID)){
					return node;
				}
				
				if(simpleAliasID!=null && simpleAliasID.equals(varID)){
					return node;
				}
			}
		}
		
		return null;
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
			TraceNode node = this.executionList.get(i-1);
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
		for(TraceNode node: this.executionList){
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
		for(TraceNode node: this.executionList){
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
//					System.out.println(node.getOrder() + "(" + var.getVarID() + "):" + node.getSuspicousScore(var));
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
			TraceNode node = this.executionList.get(i);
			boolean isInSameLoop = isInSameLoop(node, controlLoopDominator);
			if(isInSameLoop){
				range.add(node);
			}
			else{
				break;
			}
		}
		
		TraceNode lastLoopNode = allControlDominatees.get(allControlDominatees.size()-1);
		for(int i=lastLoopNode.getOrder()-1+1; i<executionList.size(); i++){
			TraceNode node = this.executionList.get(i);
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
		for(TraceNode node: this.executionList){
			if(node.getWittenVarCorrectness(Settings.interestedVariables, false)==TraceNode.WRITTEN_VARS_INCORRECT
					|| node.getReadVarCorrectness(Settings.interestedVariables, false)==TraceNode.READ_VARS_INCORRECT){
				return node;
			}
		}
		return null;
	}
	
	public TraceNode findProducer(VarValue varValue, TraceNode startNode) {
		
		String varID = Variable.truncateSimpleID(varValue.getVarID());
		String headID = Variable.truncateSimpleID(varValue.getAliasVarID());
		
		for(int i=startNode.getOrder()-1; i>=1; i--) {
			TraceNode node = this.getTraceNode(i);
			for(VarValue writtenValue: node.getWrittenVariables()) {
				
				String wVarID = Variable.truncateSimpleID(writtenValue.getVarID());
				String wHeadID = Variable.truncateSimpleID(writtenValue.getAliasVarID());
				
				if(wVarID != null && wVarID.equals(varID)) {
					return node;						
				}
				
				if(wHeadID != null && wHeadID.equals(headID)) {
					return node;
				}
				
				VarValue childValue = writtenValue.findVarValue(varID, headID);
				if(childValue != null) {
					return node;
				}
				
			}
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
		for(TraceNode node: this.executionList){
			if(!node.isAllReadWrittenVarCorrect(false) || node.isWrongPathNode()){
				return node;
			}
		}
		return null;
	}

	public Map<String, List<Integer>> getExecutedLocation(){
		Map<String, List<Integer>> locationMap = new HashMap<>();
		for(TraceNode node: this.executionList){
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

	public LocalVariableScopes getLocalVariableScopes() {
		return localVariableScopes;
	}

	public void setLocalVariableScopes(LocalVariableScopes localVariableScopes) {
		this.localVariableScopes = localVariableScopes;
	}
	
	public TraceNode getTraceNode(int order){
		return this.executionList.get(order-1);
	}

	public List<BreakPoint> allLocations() {
		List<BreakPoint> locations = new ArrayList<>();
		for(TraceNode node: this.executionList){
			if(!locations.contains(node.getBreakPoint())){
				locations.add(node.getBreakPoint());
			}
		}
		
		return locations;
	}

	public void setSourceVersion(boolean isBuggy) {
		for(TraceNode node: this.executionList){
			node.setSourceVersion(isBuggy);
		}
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

	/**
	 * @return the threadId
	 */
	public long getThreadId() {
		return threadId;
	}

	/**
	 * @param threadId the threadId to set
	 */
	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public boolean isMain() {
		return isMain;
	}

	public void setMain(boolean isMain) {
		this.isMain = isMain;
	}

	
}
