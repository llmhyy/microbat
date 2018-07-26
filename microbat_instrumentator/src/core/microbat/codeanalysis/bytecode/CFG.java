package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;

import microbat.instrumentation.cfgcoverage.graph.IGraph;

public class CFG implements IGraph<CFGNode>{
	private Method method;
	
	private Map<Integer, CFGNode> nodeList = new HashMap<>();
	private CFGNode startNode;
	private List<CFGNode> exitList = new ArrayList<>();

	public CFGNode getStartNode() {
		return startNode;
	}

	public List<CFGNode> getExitList() {
		return exitList;
	}

	public void addExitNode(CFGNode node){
		this.exitList.add(node);
	}

	public void setStartNode(CFGNode startNode) {
		this.startNode = startNode;
	}


	public List<CFGNode> getNodeList() {
		return new ArrayList<>(nodeList.values());
	}
	
	public void addNode(CFGNode node){
		if(!this.nodeList.keySet().contains(node)){
			this.nodeList.put(node.getInstructionHandle().getPosition(), node);			
		}
	}
	
	public boolean contains(CFGNode node){
		return this.nodeList.keySet().contains(node.getInstructionHandle().getPosition());
	}
	
	/**
	 * find a node based on the instruction handle in CFG, if failed, I
	 * create a new node and add it into CFG.
	 * 
	 * @param handle
	 * @return
	 */
	public CFGNode findOrCreateNewNode(InstructionHandle handle){
		CFGNode node = findNode(handle);
		if(node == null){
			node = new CFGNode(handle);
			this.nodeList.put(handle.getPosition(), node);
		}
		
		return node;
	}
	
	public CFGNode findNode(InstructionHandle handle){
		CFGNode node = this.nodeList.get(handle.getPosition());
		return node;
	}
	
	public CFGNode findNode(int offset){
		CFGNode node = this.nodeList.get(offset);
		return node;
	}

	public int size() {
		return nodeList.size();
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

}
