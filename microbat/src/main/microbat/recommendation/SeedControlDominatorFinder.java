package microbat.recommendation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

import microbat.codeanalysis.bytecode.CFG;
import microbat.codeanalysis.bytecode.CFGConstructor;
import microbat.codeanalysis.bytecode.CFGNode;
import microbat.codeanalysis.bytecode.MethodNode;
import microbat.model.BreakPoint;

public class SeedControlDominatorFinder {
	public List<BreakPoint> findSeedControlDominators(List<BreakPoint> collectedBreakPoints, Map<MethodNode, 
			List<InstructionHandle>> allSeeds) {
		
		List<BreakPoint> suspiciousBranches = new ArrayList<>();
		
		for(MethodNode node: allSeeds.keySet()){
			CFGConstructor cfgConstructor = new CFGConstructor();
			CFG cfg = cfgConstructor.buildCFGWithControlDomiance(node.getMethod().getCode());
			
			List<InstructionHandle> seedInstructions = allSeeds.get(node);
			
			for(BreakPoint point: collectedBreakPoints){
				boolean isControlDominator = isControlDominateSeed(node, cfg, seedInstructions, point);
				if(isControlDominator){
					suspiciousBranches.add(point);
				}
				
			}
		}
		
		return suspiciousBranches;
	}

	private boolean isControlDominateSeed(MethodNode node, CFG cfg, List<InstructionHandle> seedInstructions,
			BreakPoint point) {
		if(point.getMethodSign()!=null){
			if(point.getMethodSign().equals(node.getMethodSign())){
				if(point.isConditional()){
					List<InstructionHandle> relevantInstructions = findRelevantInstruction(node, point);
					for(InstructionHandle handle: relevantInstructions){
						CFGNode cfgNode = cfg.findNode(handle);
						
						for(InstructionHandle seedHandle: seedInstructions){
							CFGNode tmp = new CFGNode(seedHandle);

							if(cfgNode.getControlDependentees().contains(tmp)){
								return true;
							}
						}
						
					}
					
				}
			}
		}
		
		return false;
	}

	private List<InstructionHandle> findRelevantInstruction(MethodNode node, BreakPoint point) {
		List<InstructionHandle> reList = new ArrayList<>();
		
		InstructionList list = new InstructionList(node.getMethod().getCode().getCode());
		for(InstructionHandle handle: list){
			int line = node.getMethod().getLineNumberTable().getSourceLine(handle.getPosition());
			if(line==point.getLineNumber()){
				reList.add(handle);
			}
		}
		
		return reList;
	}
}
