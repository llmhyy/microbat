package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.bcel.Const;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.Select;

import microbat.instrumentation.cfgcoverage.graph.IGraphNode;

public class CFGNode implements IGraphNode<CFGNode>{

	private int idx; // index of instruction in instructionList
	private int lineNo; // optional
	private InstructionHandle instructionHandle;
	private List<CFGNode> parents = new ArrayList<>();
	private List<CFGNode> children = new ArrayList<>();
	
	private HashSet<CFGNode> postDominatee = new HashSet<>();
	
	private List<CFGNode> controlDependentees = new ArrayList<>();
	
	private BlockNode blockNode;

	public CFGNode(InstructionHandle insHandle) {
		super();
		this.instructionHandle = insHandle;
	}

	public boolean isBranch(){
		return getChildren().size()>1;
	}
	
	public boolean isConditional(){
		return this.instructionHandle.getInstruction() instanceof Select
				|| this.instructionHandle.getInstruction() instanceof IfInstruction;
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
		return getDisplayString();
//		return "CFGNode [insHandle=" + instructionHandle + "]";
	}
	
	public String getDisplayString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("node[%d,%s,line %d]", idx, Const.getOpcodeName(instructionHandle.getInstruction().getOpcode()), lineNo));
		if (!children.isEmpty()) {
			sb.append(", branches={");
			for (int i = 0; i < children.size();) {
				CFGNode child = children.get(i++);
				sb.append(String.format("node[%d,%s,line %d]", child.idx,
						Const.getOpcodeName(child.instructionHandle.getInstruction().getOpcode()), child.lineNo));
				if (i < children.size()) {
					sb.append(",");
				}
			}
			sb.append("}");
		}
		return sb.toString();
	}

	@Override
	public int hashCode(){
		return this.instructionHandle.getPosition();
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
	
	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public boolean canReachPostDominatee(CFGNode target) {
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

	public BlockNode getBlockNode() {
		return blockNode;
	}

	public void setBlockNode(BlockNode blockNode) {
		this.blockNode = blockNode;
	}

	public int getLineNo() {
		return lineNo;
	}

	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}
	
}
