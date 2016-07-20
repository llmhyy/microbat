package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.InstructionHandle;

public class CFGNode {

	private InstructionHandle instructionHandle;
	private List<CFGNode> parents = new ArrayList<>();
	private List<CFGNode> children = new ArrayList<>();
	
	private List<CFGNode> postDominatee = new ArrayList<>();
	
	private List<CFGNode> controlDependentees = new ArrayList<>();

	public CFGNode(InstructionHandle insHandle) {
		super();
		this.instructionHandle = insHandle;
	}

	public boolean isBranch(){
		return this.instructionHandle.getInstruction() instanceof BranchInstruction;
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

	public List<CFGNode> getPostDominatee() {
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
}
