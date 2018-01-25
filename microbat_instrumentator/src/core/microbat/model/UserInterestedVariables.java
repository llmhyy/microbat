package microbat.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import microbat.model.trace.StepVariableRelationEntry;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public class UserInterestedVariables {
	
	private List<AttributionVar> roots = new ArrayList<>();
	
	private Map<String, Integer> varIDs = new HashMap<>();
	
	public boolean contains(String varID){
		return this.varIDs.keySet().contains(varID);
	}
	
	public AttributionVar add(String varID, int checkTime){
		
		this.varIDs.put(varID, checkTime);
		
		AttributionVar var = new AttributionVar(varID, checkTime);
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

	public void remove(String varID) {
		this.varIDs.remove(varID);
	}
	
	public Set<String> getVarIDs(){
		return this.varIDs.keySet();
	}
	
	public double getVarScore(String varID){
		return varIDs.get(varID);
	}
	
	public String getNewestVarID(){
		String newestID = null;
		for(String varID: this.varIDs.keySet()){
			if(newestID == null){
				newestID = varID;
			}
			else{
				if(this.varIDs.get(newestID) < this.varIDs.get(varID)){
					newestID = varID;
				}
			}
		}
		return newestID;
	}

	public List<AttributionVar> getRoots() {
		return roots;
	}

	public void setRoots(List<AttributionVar> roots) {
		this.roots = roots;
	}

	public AttributionVar findOrCreateVar(String varID, int checkTime) {
		
		for(AttributionVar rootVar: this.roots){
			AttributionVar var = rootVar.findChild(varID);
			if(var != null){
				var.setCheckTime(checkTime);
				return var;
			}
		}
		
		AttributionVar var = new AttributionVar(varID, checkTime);
		roots.add(var);
		
		return var;
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
		
		for(String varID: varIDs.keySet()){
			if(!vars.containsKey(varID)){
				AttributionVar aVar = new AttributionVar(varID, varIDs.get(varID));
				vars.put(varID, aVar);
			}
		}
		
		Iterator<String> iter = vars.keySet().iterator();
		while(iter.hasNext()){
			String varID = iter.next();
			
			if(varIDs.get(varID) == null){
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
		String varID = bestRoot.getVarID();
		StepVariableRelationEntry entry = trace.getStepVariableTable().get(varID);
		TraceNode producer = entry.getProducers().get(0);
		
		return Math.abs(producer.getOrder()-currentNode.getOrder());
	}
	
	public UserInterestedVariables clone(){
		Map<String, Integer> clonedVarIDs = new HashMap<>();
		
		for(String key: varIDs.keySet()){
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
		AttributionVar var = new AttributionVar(var0.getVarID(), var0.getCheckTime());
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
