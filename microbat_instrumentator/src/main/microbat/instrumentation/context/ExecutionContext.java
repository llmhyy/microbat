package microbat.instrumentation.context;

import java.util.ArrayList;
import java.util.List;

import microbat.model.trace.StepVariableRelationEntry;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.trace.VariableDefinitions;
import microbat.model.value.VarValue;
import microbat.model.variable.ArrayElementVar;
import microbat.model.variable.FieldVar;
import microbat.model.variable.Variable;
import sav.strategies.dto.AppJavaClassPath;

/**
 * 
 * @author LLT
 *
 */
public class ExecutionContext {
	private AppJavaClassPath appJavaClassPath;
	
	public ExecutionContext(AppJavaClassPath appJavaClassPath) {
		this.appJavaClassPath = appJavaClassPath;
	}
	 
	public AppJavaClassPath getAppJavaClassPath() {
		return appJavaClassPath;
	}

	public Trace newTrace() {
		return new Trace(appJavaClassPath);
	}
	
	public void buildDataRelation(TraceNode currentNode, VarValue value, String rw){
		Variable var = value.getVariable();
		if(currentNode==null){
			return;
		}
		
		Trace trace = currentNode.getTrace();
		String order = trace.findDefiningNodeOrder(rw, currentNode, var, VariableDefinitions.USE_LAST);
		
		if(order.equals("0")){
			if(var instanceof FieldVar || var instanceof ArrayElementVar){
				if(!value.getParents().isEmpty()){
					/**
					 * use the first defining step of the parent.
					 */
					order = trace.findDefiningNodeOrder(rw, currentNode, 
							value.getParents().get(0).getVariable(), VariableDefinitions.USE_FIRST);
				}
				
			}
		}
		
		String varID = var.getVarID() + ":" + order;
		var.setVarID(varID);
		if(var.getAliasVarID()!=null){
			var.setAliasVarID(var.getAliasVarID()+":"+order);			
		}
		
		StepVariableRelationEntry entry = trace.getStepVariableTable().get(varID);
		if(entry == null){
			entry = new StepVariableRelationEntry(varID);
			if(!order.equals("0")){
				TraceNode producer = trace.getTraceNode(Integer.valueOf(order));
				entry.addProducer(producer);
				trace.getStepVariableTable().put(varID, entry);
			}
		}
		if(rw.equals(Variable.READ)){
			entry.addConsumer(currentNode);
		}
		else if(rw.equals(Variable.WRITTEN)){
			entry.addProducer(currentNode);
		}
		trace.getStepVariableTable().put(varID, entry);
	}
	
	private TraceNode findInvokingMatchNode(TraceNode latestNode, String invokingMethodSig) {
		Trace trace = latestNode.getTrace();
		List<TraceNode> candidates = new ArrayList<>();
		if (latestNode.getInvocationParent() != null) {
			candidates = latestNode.getInvocationParent().getInvocationChildren();
		} else {
			candidates = trace.getTopMethodLevelNodes();
		}

		for (int i = candidates.size() - 1; i >= 0; i--) {
			TraceNode prevOver = candidates.get(i);
			if (prevOver.getOrder() != latestNode.getOrder()) {
				String prevOverInvocation = prevOver.getInvokingMethod();
				if (prevOverInvocation != null && prevOverInvocation.equals(invokingMethodSig)) {

					if (prevOver.getInvokingMatchNode() == null) {
						return prevOver;
					}
				}
			}
		}
		System.currentTimeMillis();
		return null;
	}

	public void handleInvocationLayerChanged(TraceNode latestNode, TraceNode caller) {
		TraceNode olderCaller = latestNode.getInvocationParent();
		if(olderCaller!=null){
			olderCaller.getInvocationChildren().remove(latestNode);
			latestNode.setInvocationParent(caller);
		}		
	}
}
