package microbat.codeanalysis.bytecode;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.Select;

import microbat.util.JavaUtil;

public class CFGConstructor {
	
	public CFG buildCFGWithControlDomiance(Code code){
		CFG cfg = constructCFG(code);
		
//		System.currentTimeMillis();
		
		constructPostDomination(cfg);
		constructControlDependency(cfg);
		
//		LineNumberTable table = code.getLineNumberTable();
		
		return cfg;
	}
	
	@SuppressWarnings("rawtypes")
	public CFG constructCFG(Code code){
		CFG cfg = new CFG();
		CFGNode previousNode = null;
		
		InstructionList list = new InstructionList(code.getCode());
		Iterator iter = list.iterator();
		while(iter.hasNext()){
			InstructionHandle instructionHandle = (InstructionHandle)iter.next();
			CFGNode node = cfg.findOrCreateNewNode(instructionHandle);
			
			if(previousNode != null){
				Instruction ins = previousNode.getInstructionHandle().getInstruction();
				if(JavaUtil.isNonJumpInstruction(ins)){
					node.addParent(previousNode);
					previousNode.addChild(node);					
				}
				
			}
			else{
				cfg.setStartNode(node);
			}
			
			if(instructionHandle.getInstruction() instanceof Select){
				Select switchIns = (Select)instructionHandle.getInstruction();
				InstructionHandle[] targets = switchIns.getTargets();
				
				for(InstructionHandle targetHandle: targets){
					CFGNode targetNode = cfg.findOrCreateNewNode(targetHandle);
					targetNode.addParent(node);
					node.addChild(targetNode);
				}
			}
			else if(instructionHandle.getInstruction() instanceof BranchInstruction){
				BranchInstruction bIns = (BranchInstruction)instructionHandle.getInstruction();
				InstructionHandle target = bIns.getTarget();
				
				CFGNode targetNode = cfg.findOrCreateNewNode(target);
				targetNode.addParent(node);
				node.addChild(targetNode);
			}
			
			previousNode = node;
		}
		
		setExitNodes(cfg);
		
		return cfg;
	}

	private void setExitNodes(CFG cfg) {
		for(CFGNode node: cfg.getNodeList()){
			if(node.getChildren().isEmpty()){
				cfg.addExitNode(node);
			}
		}
	}

	public void constructPostDomination(CFG cfg){
		/** connect basic post domination relation */
		for(CFGNode node: cfg.getNodeList()){
			node.addPostDominatee(node);
			for(CFGNode parent: node.getParents()){
				if(!parent.isBranch()){
					node.addPostDominatee(parent);
					for(CFGNode postDominatee: parent.getPostDominatee()){
						node.addPostDominatee(postDominatee);
					}
				}
			}
		}
		
		/** extend */
		extendPostDominatee(cfg);
	}

	@SuppressWarnings("unchecked")
	private void extendPostDominatee(CFG cfg) {
		boolean isClose = false;
		while(!isClose){
			isClose = true;
			
			for(CFGNode node: cfg.getNodeList()){
				HashSet<CFGNode> originalSet = (HashSet<CFGNode>) node.getPostDominatee().clone();
				int originalSize = originalSet.size();
				
				for(CFGNode postDominatee: node.getPostDominatee()){
					originalSet.addAll(postDominatee.getPostDominatee());
					int newSize = node.getPostDominatee().size();
					
					boolean isAppendNew =  originalSize != newSize;
					isClose = isClose && !isAppendNew;
				}
				
				node.setPostDominatee(originalSet);
			}
			
			
			for(CFGNode nodei: cfg.getNodeList()){
				if(nodei.isBranch()){
					for(CFGNode nodej: cfg.getNodeList()){
						if(!nodei.equals(nodej)){
							boolean isExpend = checkBranchDomination(nodei, nodej);
							isClose = isClose && !isExpend;
						}
					}
					
				}
				
			}
		}
	}
	

	private boolean checkBranchDomination(CFGNode branchNode, CFGNode postDominator) {
		boolean isExpend = false;
		if(allBranchTargetsIncludedInDominatees(branchNode, postDominator)){
			if(!postDominator.getPostDominatee().contains(branchNode)){
				isExpend = true;
				postDominator.getPostDominatee().add(branchNode);
			}
		}
		return isExpend;
	}

	private boolean allBranchTargetsIncludedInDominatees(CFGNode branchNode, CFGNode postDominator) {
		for(CFGNode target: branchNode.getChildren()){
			if(!postDominator.getPostDominatee().contains(target)){
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * This method can only be called after the post domination relation is built in {@code cfg}.
	 * Given a branch node, I traverse its children in first order. All its non-post-dominatees are
	 * its control dependentees.
	 * 
	 * @param cfg
	 */
	public void constructControlDependency(CFG cfg){
		for(CFGNode node: cfg.getNodeList()){
			if(node.isBranch()){
				computeControlDependentees(node, node.getChildren());
			}
		}
	}

	private void computeControlDependentees(CFGNode node, List<CFGNode> list) {
		for(CFGNode child: list){
			if(!child.getPostDominatee().contains(node) && !node.getControlDependentees().contains(child)){
				node.addControlDominatee(child);
				computeControlDependentees(node, child.getChildren());
			}
		}
		
	}
}
