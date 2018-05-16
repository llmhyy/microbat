package microbat.codeanalysis.bytecode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.GotoInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.JsrInstruction;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Select;

/**
 * Construct CFG based on bytecode of a method.
 * 
 * @author Yun Lin
 *
 */
public class CFGConstructor {
	/**
	 * This field is remained for debugging purpose
	 */
	private Code code;
	
	public CFG buildCFGWithControlDomiance(Code code){
		this.code = code;
		
		CFG cfg = constructCFG(code);
		
		constructPostDomination(cfg);
		constructControlDependency(cfg);
		
		return cfg;
	}
	
	private boolean isNonJumpInstruction(Instruction ins){
		return !(ins instanceof GotoInstruction) && !(ins instanceof ReturnInstruction) && !(ins instanceof ATHROW) &&
				!(ins instanceof JsrInstruction) && !(ins instanceof Select);
	}
	
	@SuppressWarnings("rawtypes")
	public CFG constructCFG(Code code){
		CFG cfg = new CFG();
		CFGNode previousNode = null;
//		System.currentTimeMillis();
		InstructionList list = new InstructionList(code.getCode());
		Iterator iter = list.iterator();
		while(iter.hasNext()){
			InstructionHandle instructionHandle = (InstructionHandle)iter.next();
			CFGNode node = cfg.findOrCreateNewNode(instructionHandle);
			
			if(previousNode != null){
				Instruction ins = previousNode.getInstructionHandle().getInstruction();
				if(isNonJumpInstruction(ins)){
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
				
				InstructionHandle targetHandle = switchIns.getTarget();
				if(targetHandle!=null){
					CFGNode targetNode = cfg.findOrCreateNewNode(targetHandle);
					if(!node.getChildren().contains(targetNode)){
						targetNode.addParent(node);
						node.addChild(targetNode);					
					}					
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
		
		attachTryCatchControlFlow(cfg, code);
		
		setExitNodes(cfg);
		
		return cfg;
	}

	private void attachTryCatchControlFlow(CFG cfg, Code code) {
		CodeException[] exceptions = code.getExceptionTable();
		if(exceptions==null){
			return;
		}
		
		for(CodeException exception: exceptions){
			int start = exception.getStartPC();
			int end = exception.getEndPC();
			int handle = exception.getHandlerPC();
			CFGNode targetNode = cfg.findNode(handle);
			
			for(int i=start; i<=end; i++){
				CFGNode sourceNode = cfg.findNode(i);
				if(sourceNode!=null){
					sourceNode.addChild(targetNode);
					targetNode.addParent(sourceNode);					
				}
			}
		}
	}

	private void setExitNodes(CFG cfg) {
		for(CFGNode node: cfg.getNodeList()){
			if(node.getChildren().isEmpty()){
				cfg.addExitNode(node);
			}
			else{
				if(node.getInstructionHandle().getInstruction() instanceof ReturnInstruction){
					cfg.addExitNode(node);
				}
			}
		}
	}

	public void constructPostDomination(CFG cfg){
		Map<CFGNode, Set<CFGNode>> postDominanceMap = new HashMap<>();
		
		/** connect basic post domination relation */
		for(CFGNode node: cfg.getNodeList()){
			Set<CFGNode> set = new HashSet<>();
			set.add(node);
			postDominanceMap.put(node, set);
		}
		
		/** extend */
		boolean isChange = true;
		while(isChange){
			isChange = false;
			for(int i=cfg.getNodeList().size()-1; i>=0; i--){
				CFGNode node = cfg.getNodeList().get(i);
				Set<CFGNode> intersetion = findIntersetedPostDominator(node.getChildren(), postDominanceMap);
				Set<CFGNode> postDominatorSet = postDominanceMap.get(node);
				
				for(CFGNode newNode: intersetion){
					if(!postDominatorSet.contains(newNode)){
						postDominatorSet.add(newNode);
						isChange = true;
					}
				}
				postDominanceMap.put(node, postDominatorSet);
			}
		}
		
		/** construct post dominatee relation*/
		for(CFGNode node: cfg.getNodeList()){
			Set<CFGNode> postDominators = postDominanceMap.get(node);
			for(CFGNode postDominator: postDominators){
				postDominator.addPostDominatee(node);
			}
		}
		
		System.currentTimeMillis();
	}

	private Set<CFGNode> findIntersetedPostDominator(List<CFGNode> children,
			Map<CFGNode, Set<CFGNode>> postDominanceMap) {
		if(children.isEmpty()){
			return new HashSet<>();
		}
		else if(children.size()==1){
			CFGNode child = children.get(0);
			return postDominanceMap.get(child);
		}
		else{
			CFGNode child = children.get(0);
			Set<CFGNode> set = (Set<CFGNode>) ((HashSet<CFGNode>)postDominanceMap.get(child)).clone();
			
			for(int i=1; i<children.size(); i++){
				CFGNode otherChild = children.get(i);
				Set<CFGNode> candidateSet = postDominanceMap.get(otherChild);
				
				Iterator<CFGNode> setIter = set.iterator();
				while(setIter.hasNext()){
					CFGNode postDominator = setIter.next();
					if(!candidateSet.contains(postDominator)){
						setIter.remove();
					}
				}
			}
			
			return set;
		}
		
	}
	
	/**
	 * This method can only be called after the post domination relation is built in {@code cfg}.
	 * Given a branch node, I traverse its children in first order. All its non-post-dominatees are
	 * its control dependentees.
	 * 
	 * @param cfg
	 */
	public void constructControlDependency(CFG cfg){
		for(CFGNode branchNode: cfg.getNodeList()){
			if(branchNode.isBranch()){
				computeControlDependentees(branchNode, branchNode.getChildren());
			}
		}
	}

	private void computeControlDependentees(CFGNode branchNode, List<CFGNode> list) {
		for(CFGNode child: list){
			if(!child.canReachPostDominatee(branchNode) && !branchNode.getControlDependentees().contains(child)){
				branchNode.addControlDominatee(child);
				computeControlDependentees(branchNode, child.getChildren());
			}
		}
		
	}
}
