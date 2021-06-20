package microbat.model.trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;

import microbat.codeanalysis.bytecode.ByteCodeParser;
import microbat.codeanalysis.bytecode.CFG;
import microbat.codeanalysis.bytecode.CFGConstructor;
import microbat.codeanalysis.bytecode.CFGNode;
import microbat.codeanalysis.bytecode.MethodFinderByLine;
import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.model.ControlScope;
import microbat.model.Scope;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
import sav.common.core.utils.CollectionUtils;
import sav.strategies.dto.AppJavaClassPath;

/**
 * This class stands for a trace for an execution
 * @author Yun Lin
 *
 */
public class Trace {
	
	private AppJavaClassPath appJavaClassPath;
	private List<String> includedLibraryClasses;
	private List<String> excludedLibraryClasses;
	private long threadId;
	private boolean isMain;
	private String threadName;

	/**
	 * This variable is to trace whether the variables in different lines are the same
	 * local variable.
	 */
//	private LocalVariableScopes localVariableScopes = new LocalVariableScopes();
	
	public Trace(AppJavaClassPath appJavaClassPath) {
		if(threadName == null) {
			String threadName = Thread.currentThread().getName();
			this.threadName = threadName;
		}
		
		this.setAppJavaClassPath(appJavaClassPath);
		executionList = new ArrayList<>();
	}
	
	public Trace(AppJavaClassPath appJavaClassPath, int stepNums) {
		this.setAppJavaClassPath(appJavaClassPath);
		this.executionList = new ArrayList<>(stepNums);
	}
	
	private List<TraceNode> executionList;
	/**
	 * tracking which steps read/write what variables, and what variables are read/written by which steps.
	 * key is the variable ID, and value is the entry containing all the steps reading/writing the corresponding
	 * variable.
	 */
//	private Map<String, StepVariableRelationEntry> stepVariableTable = new HashMap<>();
	
	public long getThreadId() {
		return threadId;
	}

	public void setThreadId(long threadId) {
		this.threadId = threadId;
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
	
	public TraceNode getLatestNode(){
		int len = size();
		if(len > 0){
			return this.executionList.get(len-1);
		}
		else{
			return null;
		}
	}
	
	private ControlScope parseControlScope(BreakPoint breakPoint, CFG cfg) {
		List<ClassLocation> ranges = new ArrayList<>(1);
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
		
		ControlScope scope = new ControlScope();
		scope.setRangeList(ranges);
		scope.setCondition(breakPoint.isConditional());
		scope.setBranch(breakPoint.isBranch());
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
		fillInControlScope();
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
			
			if(node.isBranch()){
				controlDominator = node;
			}
		}
	}

	private void fillInControlScope() {
		Map<BreakPoint, List<TraceNode>> breakpointMap = new HashMap<>();
		for (TraceNode node : executionList) {
			CollectionUtils.getListInitIfEmpty(breakpointMap, node.getBreakPoint()).add(node);
		}
		Map<String, Set<String>> classMethodMap = new HashMap<>();
		Map<String, List<BreakPoint>> methodSignMap = new HashMap<>();
		for (BreakPoint bkp : breakpointMap.keySet()) {
			CollectionUtils.getListInitIfEmpty(methodSignMap, bkp.getMethodSign()).add(bkp);
			CollectionUtils.getSetInitIfEmpty(classMethodMap, bkp.getClassCanonicalName()).add(bkp.getMethodSign());
		}
		for (String classCanonicalName : classMethodMap.keySet()) {
			for (String methodSig : classMethodMap.get(classCanonicalName)) {
				List<BreakPoint> bkpList = methodSignMap.get(methodSig);
				BreakPoint breakPoint = bkpList.get(0);
				MethodFinderByLine finder = new MethodFinderByLine(breakPoint);
				ByteCodeParser.parse(breakPoint.getClassCanonicalName(), finder, appJavaClassPath);
				Method method = finder.getMethod();
				CFGConstructor cfgConstructor = new CFGConstructor();
				CFG cfg = cfgConstructor.buildCFGWithControlDomiance(method.getCode());
				cfg.setMethod(method);
				for (BreakPoint bkp : bkpList) {
					ControlScope scope = parseControlScope(bkp, cfg);		
					for (TraceNode node : breakpointMap.get(bkp)) {
						node.getBreakPoint().setConditional(scope.isCondition());
						node.getBreakPoint().setBranch(scope.isBranch());
						node.setControlScope(scope);
					}
				}
			}
			Repository.clearCache();
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

//	public Map<String, StepVariableRelationEntry> getStepVariableTable() {
//		return stepVariableTable;
//	}

	public TraceNode findLastestExceptionNode() {
		for(int i=0; i<executionList.size(); i++){
			TraceNode lastestNode = executionList.get(executionList.size()-1-i);
			if(lastestNode.isException()){
				return lastestNode;
			}
		}
		
		return null;
	}
	
	private VariableDefinitions variableDefs = new VariableDefinitions();
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
	 * firstOrLast indicates that, when there are multiple defining steps for a variable, whether we use 
	 * the first one or the last one. The default is using last one.
	 * 
	 * @param accessType
	 * @param currentNode
	 * @param isSubValue
	 * @param varID
	 * @param aliasVarID
	 * @return
	 */
	public String findDefiningNodeOrder(String accessType, TraceNode currentNode,
			Variable var, int defStepSelection) {
		String varID = var.getVarID(); 
		String aliasVarID = var.getAliasVarID();
		varID = Variable.truncateSimpleID(varID);
		aliasVarID = Variable.truncateSimpleID(aliasVarID);
		String definingOrder = "0";
		if(accessType.equals(Variable.WRITTEN)){
			definingOrder = String.valueOf(currentNode.getOrder());
			variableDefs.put(varID, currentNode);
			if(aliasVarID!=null){
				variableDefs.put(aliasVarID, currentNode);				
			}
		}
		else if(accessType.equals(Variable.READ)){
			TraceNode node1 = variableDefs.get(varID, currentNode, defStepSelection);
			TraceNode node2 = variableDefs.get(aliasVarID, currentNode, defStepSelection);
			
			int order = 0;
			if(var instanceof LocalVar){
				if(node1!=null && node2==null){
					order = node1.getOrder();
				}
				else if(node1==null && node2!=null){
					order = node2.getOrder();
				}
				else if(node1!=null && node2!=null){
					if(node2.getInvocationParent()==null && currentNode.getInvocationParent()==null){
						order = (node1.getOrder()>node2.getOrder())?node1.getOrder():node2.getOrder();						
					}
					else if(node2.getInvocationParent()!=null && currentNode.getInvocationParent()!=null
							&& node2.getInvocationParent().equals(currentNode.getInvocationParent())){
						order = (node1.getOrder()>node2.getOrder())?node1.getOrder():node2.getOrder();
					}
					else{
						order = node1.getOrder();
					}
				}
			}
			else{
				if(node1!=null){
					order = node1.getOrder();
				}
			}
			
			definingOrder = String.valueOf(order);
		}
		
		return definingOrder;
	}
	
//	public TraceNode getProducer(String varID) {
//		StepVariableRelationEntry entry = this.stepVariableTable.get(varID);
//		
//		if(entry == null){
//			System.err.println("the variable with ID " + varID + " is not explicitly read or written");
//			return null;
//		}
//		
//		if(!entry.getProducers().isEmpty()){
//			return entry.getProducers().get(0);
//		}
//		
//		return null;
//	}

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

//	public LocalVariableScopes getLocalVariableScopes() {
//		return localVariableScopes;
//	}
//
//	public void setLocalVariableScopes(LocalVariableScopes localVariableScopes) {
//		this.localVariableScopes = localVariableScopes;
//	}
	
	public TraceNode getTraceNode(int order){
		return this.executionList.get(order-1);
	}

	public List<String> getIncludedLibraryClasses() {
		return CollectionUtils.nullToEmpty(includedLibraryClasses);
	}

	public void setIncludedLibraryClasses(List<String> includedLibraryClasses) {
		this.includedLibraryClasses = includedLibraryClasses;
	}

	public List<String> getExcludedLibraryClasses() {
		return CollectionUtils.nullToEmpty(excludedLibraryClasses);
	}

	public void setExcludedLibraryClasses(List<String> excludedLibraryClasses) {
		this.excludedLibraryClasses = excludedLibraryClasses;
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
