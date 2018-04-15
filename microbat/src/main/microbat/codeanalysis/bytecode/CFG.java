package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.generic.InstructionHandle;

public class CFG {
	private List<CFGNode> nodeList = new ArrayList<>();
	private CFGNode startNode;
	private List<CFGNode> exitList = new ArrayList<>();
	
	private Code code;
	
	public CFG(Code code) {
		super();
		this.code = code;
	}

	public int getLineNumber(CFGNode node){
		return this.code.getLineNumberTable().getSourceLine(node.getInstructionHandle().getPosition());
	}
	
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
		return nodeList;
	}
	
	public void addNode(CFGNode node){
		if(!this.nodeList.contains(node)){
			this.nodeList.add(node);			
		}
	}
	
	public boolean contains(CFGNode node){
		return this.nodeList.contains(node);
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
			this.nodeList.add(node);
		}
		
		return node;
	}
	
	public CFGNode findNode(InstructionHandle handle){
		for(CFGNode node: this.nodeList){
			if(node.getInstructionHandle().getPosition() == handle.getPosition()){
				return node;
			}
		}
		return null;
	}
	
	public CFGNode findNode(int offset){
		for(CFGNode node: this.nodeList){
			if(node.getInstructionHandle().getPosition() == offset){
				return node;
			}
		}
		return null;
	}

	public int size() {
		return nodeList.size();
	}

	public int getEndLine() {
		int max = -1;
		for(LineNumber ln: code.getLineNumberTable().getLineNumberTable()){
			int line = ln.getLineNumber();
			if(max==-1){
				max = line;
			}
			else{
				max = (max > line)? max : line;
			}
		}
		
		return max;
	}
}
