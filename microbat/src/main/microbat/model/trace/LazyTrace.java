package microbat.model.trace;

import java.sql.SQLException;
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
import microbat.sql.TraceRetriever;
import microbat.util.JavaUtil;
import microbat.util.Settings;
import sav.strategies.dto.AppJavaClassPath;

/**
 * This class wraps the Trace class in order to load trace values lazily
 * @author dingyuchen
 *
 */
public class LazyTrace extends Trace{
	private TraceRetriever retriever;
	
	public LazyTrace(AppJavaClassPath appJavaClassPath) {
		super(appJavaClassPath);
		// TODO Auto-generated constructor stub
	}
	
	public List<TraceNode> getExecutionList() {
		if (super.getExecutionList() == null) {
			List<TraceNode> steps;
			try {
				steps = retriever.getSteps(this);
			} catch (SQLException e) {
				e.printStackTrace();
				steps = new ArrayList<>();
			}
			super.setExectionList(steps);
		}
		return super.getExecutionList();
	}

	public List<TraceNode> getTopMethodLevelNodes(){
		List<TraceNode> topList = new ArrayList<>();
		for(TraceNode node: this.getExecutionList()){
			if(node.getInvocationParent() == null){
				topList.add(node);
			}
		}
		
		return topList;
	}
	
	public List<TraceNode> getTopLoopLevelNodes(){
		List<TraceNode> topList = new ArrayList<>();
		for(TraceNode node: this.getExecutionList()){
			if(node.getLoopParent() == null){
				topList.add(node);
			}
		}
		
		return topList;
	}
	
	public static String combineTraceNodeExpression(String className, int lineNumber){
		className = className.substring(className.lastIndexOf(".")+1, className.length());
		
		String exp = className + " line:" + lineNumber;
		return exp;
	}

	public void conductStateDiff() {
		for(int i=0; i<this.getExecutionList().size(); i++){
			TraceNode node = this.getExecutionList().get(i);
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
		for(int i=startOrder; i<this.exectionList.size(); i++){
			TraceNode node = this.exectionList.get(i);
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
			TraceNode node = this.exectionList.get(i);
			if(node.getReadVariables().contains(value)){
				list.add(node);
			}
		}
		
		return list;
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
		for(TraceNode node: this.exectionList){
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

}
