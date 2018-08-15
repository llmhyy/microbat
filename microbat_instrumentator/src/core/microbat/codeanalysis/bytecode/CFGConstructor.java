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
		
		BlockGraph bGraph = constructPostDomination(cfg);
		constructControlDependency(cfg, bGraph);
		
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
		
		int idx = 0;
		for (InstructionHandle insnHandler : list) {
			cfg.findNode(insnHandler).setIdx(idx++);
		}
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
				if (sourceNode != null && sourceNode != targetNode) {
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

	public BlockGraph constructPostDomination(CFG cfg){
		
		BlockGraph bGraph = BlockGraph.createBlockGraph(cfg);
		
		Map<BlockNode, Set<BlockNode>> postDominanceMap = new HashMap<>();
		
		/** connect basic post domination relation */
		for(BlockNode node: bGraph.getList()){
			Set<BlockNode> set = new HashSet<>();
			set.add(node);
			postDominanceMap.put(node, set);
		}
		
		/** extend */
		Boolean isChange = true;
		int iteration = 0;
		while(isChange){
			isChange = false;
			iteration++;
			Set<BlockNode> visitedBlocks = new HashSet<>();
			for(BlockNode exitNode: bGraph.getExitNodeList()){
				propagatePostDominator(postDominanceMap, exitNode, isChange, visitedBlocks);
			}
		}
		
		bGraph.setPostDominanceMap(postDominanceMap);
		return bGraph;
		
//		/** map relation back to CFG node*/
//		for(BlockNode block: bGraph.getList()){
//			for(CFGNode cfgNode: block.getContents()){
//				cfgNode.getPostDominatee().addAll(block.getContents());
//			}
//		}
//		
//		/** construct post dominatee relation*/
//		for(BlockNode block: bGraph.getList()){
//			Set<BlockNode> postDominators = postDominanceMap.get(block);
//			for(BlockNode postDominator: postDominators){
//				for(CFGNode cfgNode: postDominator.getContents()){
//					cfgNode.getPostDominatee().addAll(block.getContents());
//				}
//			}
//		}
		
//		System.currentTimeMillis();
	}

	private void propagatePostDominator(Map<BlockNode, Set<BlockNode>> postDominanceMap, BlockNode node, 
			Boolean isChange, Set<BlockNode> visitedBlocks) {
		visitedBlocks.add(node);
		
		Set<BlockNode> intersetion = findIntersetedPostDominator(node.getChildren(), postDominanceMap);
		Set<BlockNode> postDominatorSet = postDominanceMap.get(node);
		
		for(BlockNode newNode: intersetion){
			if(!postDominatorSet.contains(newNode)){
				postDominatorSet.add(newNode);
				isChange = true;
			}
		}
		postDominanceMap.put(node, postDominatorSet);
		
		for(BlockNode parent: node.getParents()){
			if(!visitedBlocks.contains(parent)){
				propagatePostDominator(postDominanceMap, parent, isChange, visitedBlocks);				
			}
		}
	}

	private Set<BlockNode> findIntersetedPostDominator(List<BlockNode> children,
			Map<BlockNode, Set<BlockNode>> postDominanceMap) {
		if(children.isEmpty()){
			return new HashSet<>();
		}
		else if(children.size()==1){
			BlockNode child = children.get(0);
			return postDominanceMap.get(child);
		}
		else{
			BlockNode child = children.get(0);
			Set<BlockNode> set = (Set<BlockNode>) ((HashSet<BlockNode>)postDominanceMap.get(child)).clone();
			
			for(int i=1; i<children.size(); i++){
				BlockNode otherChild = children.get(i);
				Set<BlockNode> candidateSet = postDominanceMap.get(otherChild);
				
				Iterator<BlockNode> setIter = set.iterator();
				while(setIter.hasNext()){
					BlockNode postDominator = setIter.next();
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
	 * @param bGraph 
	 */
	public void constructControlDependency(CFG cfg, BlockGraph bGraph){
		for(CFGNode branchNode: cfg.getNodeList()){
			if(branchNode.isBranch()){
				computeControlDependentees(branchNode, branchNode.getChildren(), bGraph);
			}
		}
	}

	private void computeControlDependentees(CFGNode branchNode, List<CFGNode> list, BlockGraph bGraph) {
		for(CFGNode child: list){
			if(!branchNode.getControlDependentees().contains(child)){
				
				boolean isChildPostDominateBranchNode = isChildPostDominateBranchNode(child, branchNode, bGraph);
				if(!isChildPostDominateBranchNode){
					branchNode.addControlDominatee(child);
					computeControlDependentees(branchNode, child.getChildren(), bGraph);					
				}
			}
		}
		
	}

	private boolean isChildPostDominateBranchNode(CFGNode child, CFGNode branchNode, BlockGraph bGraph) {
		BlockNode childBlock = child.getBlockNode();
		BlockNode branchBlock = branchNode.getBlockNode();
		
		Set<BlockNode> postDominators = bGraph.getPostDominanceMap().get(childBlock);
		
		return postDominators.contains(branchBlock);
	}
}
