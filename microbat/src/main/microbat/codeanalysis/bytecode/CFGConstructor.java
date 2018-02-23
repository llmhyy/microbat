package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Select;

import microbat.model.variable.ArrayElementVar;
import microbat.model.variable.FieldVar;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
import microbat.util.JavaUtil;

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
		
//		constructDataDependency(cfg);
		
		return cfg;
	}
	
	public void constructDataDependency(CFG cfg) {
		for(CFGNode node: cfg.getNodeList()){
			node.parseReadWrittenVariable(code);
			node.intializeGenSet();
		}
		
		System.currentTimeMillis();
		
		boolean change = true;
		while(change){
			change = false;
			
			for(CFGNode node: cfg.getNodeList()){
				List<CFGNode> oldOutSet = clone(node.getOutSet());
				
				List<CFGNode> inSet = new ArrayList<>();
				for(CFGNode parent: node.getParents()){
					union(inSet, parent.getOutSet());
				}
				union(inSet, node.getOutSet());
				union(inSet, node.getGenSet());
				
				List<CFGNode> newOutSet = kill(inSet, node);
				node.setOutSet(newOutSet);
				
				if(!equal(oldOutSet, newOutSet)){
					change = true;
				}
			}
		}
		
		for(CFGNode useNode: cfg.getNodeList()){
			for(CFGNode defNode: useNode.getOutSet()){
				boolean isRDChain = isRDChain(defNode, useNode);
				if(isRDChain){
					useNode.addDefineNode(defNode);
					defNode.addUseNode(useNode);
				}
			}
		}
	}
	
	private boolean isRDChain(CFGNode defNode, CFGNode useNode) {
		for(Variable var1: defNode.getWrittenVars()){
			for(Variable var2: useNode.getReadVars()){
				if(isSame(var1, var2)){
					return true;
				}
			}
		}
		return false;
	}

	private void union(List<CFGNode> inSet, List<CFGNode> genSet) {
		for(CFGNode genNode: genSet){
			if(!inSet.contains(genNode)){
				inSet.add(genNode);
			}
		}
	}

	private boolean equal(List<CFGNode> oldOutSet, List<CFGNode> newOutSet) {
		if(oldOutSet.size()!=newOutSet.size()){
			return false;
		}
		
		int count = 0;
		for(CFGNode n: oldOutSet){
			for(CFGNode m: newOutSet){
				if(n.equals(m)){
					count++;
					break;
				}
			}
		}
		
		return count==oldOutSet.size();
	}

	private List<CFGNode> kill(List<CFGNode> inSet, CFGNode node) {
		Iterator<CFGNode> iter = inSet.iterator();
		while(iter.hasNext()){
			CFGNode inNode = iter.next();
			for(Variable var1: inNode.getWrittenVars()){
				for(Variable var2: node.getWrittenVars()){
					if(isSame(var1, var2) && !node.getGenSet().contains(inNode)){
						iter.remove();
					}
				}
			}
		}
		
		return inSet;
	}

	private boolean isSame(Variable var1, Variable var2) {
		if(var1 instanceof FieldVar && var2 instanceof FieldVar){
			FieldVar fVar1 = (FieldVar)var1;
			FieldVar fVar2 = (FieldVar)var2;
			return fVar1.getDeclaringType().equals(fVar2.getDeclaringType()) &&
					fVar1.getName().equals(fVar2.getName());
		}
		else if(var1 instanceof LocalVar && var2 instanceof LocalVar){
			LocalVar lVar1 = (LocalVar)var1;
			LocalVar lVar2 = (LocalVar)var2;
			return lVar1.getByteCodeIndex()==lVar2.getByteCodeIndex();
		}
		else if(var1 instanceof ArrayElementVar && var2 instanceof ArrayElementVar){
			ArrayElementVar aVar1 = (ArrayElementVar)var1;
			ArrayElementVar aVar2 = (ArrayElementVar)var2;
			return aVar1.getType().equals(aVar2.getType());
		}
		
		return false;
	}

	private List<CFGNode> clone(List<CFGNode> outSet) {
		List<CFGNode> list = new ArrayList<>();
		for(CFGNode node: outSet){
			list.add(node);
		}
		return list;
	}

	@SuppressWarnings("rawtypes")
	public CFG constructCFG(Code code){
		CFG cfg = new CFG(code);
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
	private void extendPostDominatee0(CFG cfg) {
		boolean isClose = false;
		while(!isClose){
			isClose = true;
			
			for(CFGNode node: cfg.getNodeList()){
				HashSet<CFGNode> originalSet = (HashSet<CFGNode>) node.getPostDominatee().clone();
				int originalSize = originalSet.size();
				
				long t5 = System.currentTimeMillis();
				for(CFGNode postDominatee: node.getPostDominatee()){
					originalSet.addAll(postDominatee.getPostDominatee());
					int newSize = node.getPostDominatee().size();
					
					boolean isAppendNew =  originalSize != newSize;
					isClose = isClose && !isAppendNew;
				}
				long t6 = System.currentTimeMillis();
				System.out.println("time for travese post dominatee: " + (t6-t5));
				
				node.setPostDominatee(originalSet);
			}
			
			
			for(CFGNode nodei: cfg.getNodeList()){
				if(nodei.isBranch()){
					for(CFGNode nodej: cfg.getNodeList()){
						if(!nodei.equals(nodej)){
							boolean isExpend = checkBranchDomination0(nodei, nodej);
							isClose = isClose && !isExpend;
						}
					}
					
				}
				
			}
		}
	}
	

	private boolean checkBranchDomination0(CFGNode branchNode, CFGNode postDominator) {
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
	
	private void extendPostDominatee(CFG cfg) {
		boolean isClose = false;
		
		while(!isClose){
			isClose = true;
			for(CFGNode nodei: cfg.getNodeList()){
				if(nodei.isBranch()){
					for(CFGNode nodej: cfg.getNodeList()){
						if(!nodei.equals(nodej)){
							boolean isAppend = checkBranchDomination(nodei, nodej);
							isClose = isClose && !isAppend;
						}
					}
				}
			}
		}
	}
	
	private boolean checkBranchDomination(CFGNode branchNode, CFGNode postDominator) {
		boolean isExpend = false;
		if(allBranchTargetsReachedByDominatees(branchNode, postDominator)){
			if(!postDominator.getPostDominatee().contains(branchNode)){
				isExpend = true;
				postDominator.getPostDominatee().add(branchNode);
			}
		}
		return isExpend;
	}
	
	private boolean allBranchTargetsReachedByDominatees(CFGNode branchNode, CFGNode postDominator) {
		for(CFGNode target: branchNode.getChildren()){
			if(!postDominator.canReachDominatee(target)){
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
		for(CFGNode branchNode: cfg.getNodeList()){
			if(branchNode.isBranch()){
				computeControlDependentees(branchNode, branchNode.getChildren());
			}
		}
	}

	private void computeControlDependentees(CFGNode branchNode, List<CFGNode> list) {
		for(CFGNode child: list){
			if(!child.canReachDominatee(branchNode) && !branchNode.getControlDependentees().contains(child)){
				branchNode.addControlDominatee(child);
				computeControlDependentees(branchNode, child.getChildren());
			}
		}
		
	}
}
