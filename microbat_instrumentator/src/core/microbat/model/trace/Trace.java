package microbat.model.trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	/**
	 * This variable is to trace whether the variables in different lines are the same
	 * local variable.
	 */
//	private LocalVariableScopes localVariableScopes = new LocalVariableScopes();
	
	public Trace(AppJavaClassPath appJavaClassPath) {
		this.setAppJavaClassPath(appJavaClassPath);
		exectionList = new ArrayList<>();
	}
	
	public Trace(AppJavaClassPath appJavaClassPath, int stepNums) {
		this.setAppJavaClassPath(appJavaClassPath);
		this.exectionList = new ArrayList<>(stepNums);
	}
	
	private List<TraceNode> exectionList;
	/**
	 * tracking which steps read/write what variables, and what variables are read/written by which steps.
	 * key is the variable ID, and value is the entry containing all the steps reading/writing the corresponding
	 * variable.
	 */
	private Map<String, StepVariableRelationEntry> stepVariableTable = new HashMap<>();
	
	private boolean isMultiThread = false;

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
	
	public TraceNode getLatestNode(){
		int len = size();
		if(len > 0){
			return this.exectionList.get(len-1);
		}
		else{
			return null;
		}
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
			Variable var) {
		String varID = var.getVarID(); 
		String aliasVarID = var.getAliasVarID();
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
}
