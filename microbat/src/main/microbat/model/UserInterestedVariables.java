package microbat.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class UserInterestedVariables {
	
	private List<AttributionVar> roots = new ArrayList<>();
	
	//key: varID, value: checkTime 
	private Map<VarValue, Integer> varIDs = new HashMap<>();
	
	public boolean contains(VarValue varID){
		return this.varIDs.keySet().contains(varID);
	}
	
	public AttributionVar add(int checkTime, VarValue value){
		
		this.varIDs.put(value, checkTime);
		
		AttributionVar var = new AttributionVar(checkTime, value);
		Map<String, AttributionVar> vars = findAllValidateAttributionVar();
		if(!vars.containsKey(var.getVarID())){
			roots.add(var);
			return var;
		}
		else{
			AttributionVar recordVar = vars.get(var.getVarID());
			recordVar.setCheckTime(checkTime);
			return recordVar;
		}
		
	}

	public void remove(VarValue varID) {
		this.varIDs.remove(varID);
	}
	
	public List<AttributionVar> getRoots() {
		return roots;
	}

	public void setRoots(List<AttributionVar> roots) {
		this.roots = roots;
	}

	public void updateAttributionTrees() {
		Map<String, AttributionVar> vars = findAllValidateAttributionVar();
		
		List<AttributionVar> roots = new ArrayList<>();
		for(AttributionVar var: vars.values()){
			if(var.getParents().isEmpty()){
				roots.add(var);
			}
		}
		this.roots = roots;
	}
	
	private Map<String, AttributionVar> findAllValidateAttributionVar(){
		Map<String, AttributionVar> vars = new HashMap<>();
		for(AttributionVar var: roots){
			collectVars(vars, var);
		}
		
		for(VarValue varID: varIDs.keySet()){
			if(!vars.containsKey(varID.getVarID())){
				AttributionVar aVar = new AttributionVar(varIDs.get(varID), varID);
				vars.put(varID.getVarID(), aVar);
			}
		}
		
		Iterator<String> iter = vars.keySet().iterator();
		while(iter.hasNext()){
			String varID = iter.next();
			if(varIDs.get(vars.get(varID).getValue()) == null){
				AttributionVar aVar = vars.get(varID);
				for(AttributionVar parent: aVar.getParents()){
					parent.getChildren().remove(aVar);
				}
				for(AttributionVar child: aVar.getChildren()){
					child.getParents().remove(aVar);
				}
				
				iter.remove();
			}
		}
		
		return vars;
	}

	private void collectVars(Map<String, AttributionVar> vars, AttributionVar var) {
		vars.put(var.getVarID(), var);
		
		for(AttributionVar child: var.getChildren()){
			collectVars(vars, child);
		}
	}

	/**
	 * When deciding the focus variable, I adopt the following policy: <br><br>
	 *	(1) If the user has select some read variables as wrong variables, the focus variable will be an arbitrary one;<br><br>
	 *	(2) If not, the focus variable will be the newest root attribution variables.<br><br>
	 * @param readVars
	 * @return
	 */
	public AttributionVar findFocusVar(Trace trace, TraceNode currentNode, List<AttributionVar> readVars) {
		if(readVars.isEmpty()){
			if(!roots.isEmpty()){
				AttributionVar bestRoot = findBestRoot(trace, currentNode);
				
				return bestRoot;				
			}
			else{
				return null;
			}
		}
		else{
			AttributionVar readVar = readVars.get(0);
			
			Collections.sort(roots, new Comparator<AttributionVar>(){

				@Override
				public int compare(AttributionVar o1, AttributionVar o2) {
					return o2.getCheckTime()-o1.getCheckTime();
				}
				
			});
			
			AttributionVar var = null;
			for(AttributionVar root: roots){
				var = root.findChild(readVar.getVarID());
				if(var != null){
					return var;
				}
			}
		}
		
		return null;
	}

	private AttributionVar findBestRoot(Trace trace, TraceNode currentNode) {
		AttributionVar bestRoot = null;
		int distance = -1;
		for(AttributionVar root: roots){
			if(bestRoot == null){
				bestRoot = root;
				distance = calculateDistanceWithCurrentNode(trace, currentNode, bestRoot);
			}
			else{
				int newDistance = calculateDistanceWithCurrentNode(trace, currentNode, root);
				if(newDistance < distance){
					bestRoot = root;
					distance = newDistance;
				}
			}
		}
		
		return bestRoot;
	}

	private int calculateDistanceWithCurrentNode(Trace trace, TraceNode currentNode, AttributionVar bestRoot) {
//		String varID = bestRoot.getVarID();
//		StepVariableRelationEntry entry = trace.getStepVariableTable().get(varID);
		TraceNode producer = trace.findProducer(bestRoot.getValue(), currentNode);
		
		return Math.abs(producer.getOrder()-currentNode.getOrder());
	}
	
	public UserInterestedVariables clone(){
		Map<VarValue, Integer> clonedVarIDs = new HashMap<>();
		
		for(VarValue key: varIDs.keySet()){
			clonedVarIDs.put(key, varIDs.get(key));
		}
		
		UserInterestedVariables variables = new UserInterestedVariables();
		variables.varIDs = clonedVarIDs;
		
		List<AttributionVar> clonedRoots = this.cloneAllAttributionVar();
		variables.cloneAttributionTrees(clonedRoots, this.roots);
		
		List<AttributionVar> clonedRootTree = new ArrayList<>();
		for(AttributionVar possibleRoot: clonedRoots){
			if(possibleRoot.getParents().isEmpty()){
				clonedRootTree.add(possibleRoot);
			}
		}
		variables.roots = clonedRootTree;
		
		return variables;
	}
	
	public List<AttributionVar> cloneAllAttributionVar(){
		List<AttributionVar> list = new ArrayList<>();
		for(AttributionVar var: this.roots){
			traverse(var, list);
		}
		return list;
	}

	private void traverse(AttributionVar var0, List<AttributionVar> list) {
		AttributionVar var = new AttributionVar(var0.getCheckTime(), var0.getValue());
		list.add(var);
		for(AttributionVar child: var0.getChildren()){
			traverse(child, list);
		}
	}

	private void cloneAttributionTrees(List<AttributionVar> clonedRoots, List<AttributionVar> roots) {
		for(AttributionVar root: roots){
			traverseClone(root, clonedRoots);
		}
		
	}

	private void traverseClone(AttributionVar root, List<AttributionVar> clonedRoots) {
		AttributionVar clonedParent = findCorrespondVar(clonedRoots, root);
		for(AttributionVar child: root.getChildren()){
			AttributionVar clonedChild = findCorrespondVar(clonedRoots, child);
			
			clonedParent.addChild(clonedChild);
			clonedChild.addParent(clonedParent);
			
			traverseClone(child, clonedRoots);
		}
		
	}

	private AttributionVar findCorrespondVar(List<AttributionVar> clonedRoots,
			AttributionVar inputVar) {
		for(AttributionVar var: clonedRoots){
			if(var.getVarID().equals(inputVar.getVarID())){
				return var;
			}
		}
		return null;
	}

	public void clear() {
		this.varIDs.clear();
		this.roots.clear();
	}
}
