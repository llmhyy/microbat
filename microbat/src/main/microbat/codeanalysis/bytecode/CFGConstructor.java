package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

public class CFGConstructor {
	
	public CFG buildCFGWithControlDomiance(Code code){
		CFG cfg = constructCFG(code);
		
		System.currentTimeMillis();
		
		constructPostDomination(cfg);
		constructControlDependency(cfg);
		
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
				node.addParent(previousNode);
				previousNode.addChild(node);
			}
			else{
				cfg.setStartNode(node);
			}
			
			if(instructionHandle.getInstruction() instanceof BranchInstruction){
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
			for(CFGNode parent: node.getParents()){
				if(!parent.isBranch()){
					node.addPostDominatee(parent);
				}
			}
		}
		
		/** extend */
		extendPostDominatee(cfg);
	}

	private void extendPostDominatee(CFG cfg) {
		boolean isClose = false;
		while(!isClose){
			isClose = true;
			
			for(CFGNode node: cfg.getNodeList()){
				List<CFGNode> totalAppendedList = new ArrayList<>();
				
				for(CFGNode postDominatee: node.getPostDominatee()){
					List<CFGNode> appendedList = checkAppendedPostDominatee(node, postDominatee);
					totalAppendedList.addAll(appendedList);
					isClose = isClose && appendedList.isEmpty();
				}
				
				node.getPostDominatee().addAll(totalAppendedList);
			}
			
			
			for(CFGNode nodei: cfg.getNodeList()){
				for(CFGNode nodej: cfg.getNodeList()){
					if(!nodei.equals(nodej) && nodei.isBranch()){
						boolean isExpend = checkBranchDomination(nodei, nodej);
						isClose = isClose && !isExpend;
					}
				}
			}
		}
	}
	
	/**
	 * return true if {code postDominatee} has some dominatee not contained in this code,
	 * and vice versa.
	 * 
	 * @param postDominatee
	 * @return
	 */
	private List<CFGNode> checkAppendedPostDominatee(CFGNode node, CFGNode postDominatee){
		List<CFGNode> appendedList = new ArrayList<>();
		for(CFGNode pDominatee: postDominatee.getPostDominatee()){
			if(!node.getPostDominatee().contains(pDominatee)){
				if(!appendedList.contains(pDominatee)){
					appendedList.add(pDominatee);					
				}
			}
		}
		return appendedList;
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
