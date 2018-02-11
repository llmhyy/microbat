package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.bcel.generic.InstructionHandle;

public class CFGNode {

	private InstructionHandle instructionHandle;
	private List<CFGNode> parents = new ArrayList<>();
	private List<CFGNode> children = new ArrayList<>();
	
	private HashSet<CFGNode> postDominatee = new HashSet<>();
	
	private List<CFGNode> controlDependentees = new ArrayList<>();

	public CFGNode(InstructionHandle insHandle) {
		super();
		this.instructionHandle = insHandle;
	}

	public boolean isBranch(){
//		return this.instructionHandle.getInstruction() instanceof Select
//				|| this.instructionHandle.getInstruction() instanceof IfInstruction;
		return getChildren().size()>1;
	}
	
	public InstructionHandle getInstructionHandle() {
		return instructionHandle;
	}

	public void setInstructionHandle(InstructionHandle insHandle) {
		this.instructionHandle = insHandle;
	}

	public List<CFGNode> getParents() {
		return parents;
	}

	public void setParents(List<CFGNode> parents) {
		this.parents = parents;
	}

	public List<CFGNode> getChildren() {
		return children;
	}
	

	public void setChildren(List<CFGNode> children) {
		this.children = children;
	}

	public void addChild(CFGNode child){
		this.children.add(child);
	}
	
	public void addParent(CFGNode parent){
		this.parents.add(parent);
	}

	@Override
	public String toString() {
		return "CFGNode [insHandle=" + instructionHandle + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CFGNode){
			CFGNode otherNode = (CFGNode)obj;
			return this.instructionHandle.getPosition() == otherNode.getInstructionHandle().getPosition();
		}
		
		return false;
	}

	public HashSet<CFGNode> getPostDominatee() {
		return postDominatee;
	}
	
	public void addPostDominatee(CFGNode node){
		this.postDominatee.add(node);
	}
	
	public List<CFGNode> getControlDependentees() {
		return controlDependentees;
	}

	public void addControlDominatee(CFGNode child) {
		this.controlDependentees.add(child);
		
	}

	public void setPostDominatee(HashSet<CFGNode> originalSet) {
		this.postDominatee = originalSet;
		
	}

	public boolean canReachDominatee(CFGNode target) {
		HashSet<CFGNode> visitedNodes = new HashSet<>();
		return canReachDominatee(target, visitedNodes);
	}

	private boolean canReachDominatee(CFGNode target, HashSet<CFGNode> visitedNodes) {
		for(CFGNode postDominatee: this.getPostDominatee()){
			if(visitedNodes.contains(postDominatee)){
				continue;
			}
			visitedNodes.add(postDominatee);
			
			if(postDominatee.equals(target)){
				return true;
			}
			else if(!postDominatee.equals(this)){
				boolean can = postDominatee.canReachDominatee(target, visitedNodes);
				if(can){
					return true;
				}
			}
		}
		
		return false;
	}
}
