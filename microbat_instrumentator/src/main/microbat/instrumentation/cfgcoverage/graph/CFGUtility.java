package microbat.instrumentation.cfgcoverage.graph;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.RETURN;

import microbat.codeanalysis.bytecode.CFG;
import microbat.codeanalysis.bytecode.CFGNode;
import microbat.instrumentation.cfgcoverage.InstrumentationUtils;
import microbat.instrumentation.cfgcoverage.graph.CFGInstance.UniqueNodeId;
import microbat.instrumentation.cfgcoverage.graph.CoverageGraphConstructor.CFGInclusiveMethodChecker;
import microbat.model.ClassLocation;
import sav.strategies.dto.AppJavaClassPath;

public class CFGUtility {
	private CFGRepository cfgRepository = new CFGRepository();

	public CFGInstance buildProgramFlowGraph(AppJavaClassPath appClasspath, ClassLocation targetMethod, int layer,
			int maxLayer, CFGInclusiveMethodChecker checker) {
		CFGInstance cfg = cfgRepository.createCfgInstance(targetMethod, appClasspath);
		if (layer != maxLayer) {
			for (CFGNode node : cfg.getNodeList()) {
				if (node.getInstructionHandle().getInstruction() instanceof InvokeInstruction) {
					ConstantPoolGen cpg = new ConstantPoolGen(cfg.getCfg().getMethod().getConstantPool());
					InvokeInstruction methodInsn = (InvokeInstruction) node.getInstructionHandle().getInstruction();
					String invkClassName = methodInsn.getClassName(cpg);
					String invkMethodName = InstrumentationUtils.getMethodWithSignature(methodInsn.getMethodName(cpg),
							methodInsn.getSignature(cpg));
					String methodId = InstrumentationUtils.getMethodId(invkClassName, invkMethodName);
					if (checker.accept(methodId)) {
						ClassLocation invokeMethod = new ClassLocation(invkClassName, invkMethodName, -1);
						CFGInstance subCfg = buildProgramFlowGraph(appClasspath, invokeMethod, layer + 1, maxLayer, checker);
						glueCfg(cfg, node, subCfg);
					}
				}
			}
		}
		return cfg;
	}
	
	private void glueCfg(CFGInstance cfgInstance, CFGNode node, CFGInstance subCfgInstance) {
		CFG cfg = cfgInstance.getCfg();
		CFG subCfg = subCfgInstance.getCfg();
		if (subCfg == null) {
			return;
		}
		
		boolean hasExceptionBranch = false;
		for (CFGNode exitNode : subCfg.getExitList()) {
			if (exitNode.getInstructionHandle().getInstruction() instanceof ATHROW) {
				cfg.getExitList().add(exitNode);
				hasExceptionBranch = true;
			} 
		}
		if (!hasExceptionBranch) {
			return; // no need to attach
		}
		/* 
		 * turn invokeNode --> nextNode
		 * to: invokeNode --> startNode of subCFG -- --> returnNodes of subCfg --> nextNode
		 * */
		List<CFGNode> nextNodes = node.getChildren();
		node.getChildren().clear();
		node.addChild(subCfg.getStartNode());
		for (CFGNode exitNode : subCfg.getExitList()) {
			if (exitNode.getInstructionHandle().getInstruction() instanceof RETURN) {
				for (CFGNode nextNode : nextNodes) {
					exitNode.addChild(nextNode);
				}
			}
		}
		for (CFGNode subCfgNode : subCfgInstance.getNodeList()) {
			cfg.addNode(subCfgNode);
			UniqueNodeId unitCfgNodeId = subCfgInstance.getUnitCfgNodeIds().get(subCfgNode.getIdx());
			subCfgNode.setIdx(cfgInstance.getNodeList().size());
			cfgInstance.getNodeList().add(subCfgNode);
			cfgInstance.getUnitCfgNodeIds().add(unitCfgNodeId);
		}
	}
	
	/**
	 * List stack: to trace back nodes when traversing the graph.
	 * int[] visited: the order of visited node (also its index on stack), if node has not been visited, the value is -1.
	 * this array is used to keep the path from entry node to current node.
	 * using DFS to traverse the graph, 
	 */
	public void breakCircle(CFGInstance cfgInstance) {
		CFG cfg = cfgInstance.getCfg();
		int[] visited = new int[cfgInstance.getNodeList().size()];
		for (int i = 0; i < visited.length; i++) {
			visited[i] = -1;
		}
		List<CFGNode> stack = new ArrayList<CFGNode>();
		stack.add(cfg.getStartNode());
		while (!stack.isEmpty()) {
			CFGNode curNode = stack.get(stack.size() - 1);
			int nodeIdx = curNode.getIdx();
			int stackPos = visited[nodeIdx];
			/* this means after visit all branches of a node, we are traversing back to the previous one (curNode) */
			if (stackPos == stack.size() - 1) {
				stack.remove(stack.size() - 1);
				visited[nodeIdx] = -1;
				continue;
			}
			/* visited --> backward edge, curNode is the loopHeader */
			if (stackPos >= 0) {
				CFGNode backwardEdgeStartNode = stack.get(stack.size() - 2);	
				CFGAliasNode aliasNode = new CFGAliasNode(backwardEdgeStartNode, curNode);
				aliasNode.addParent(backwardEdgeStartNode);
				backwardEdgeStartNode.getChildren().remove(curNode);
				backwardEdgeStartNode.addChild(aliasNode); 
				/* find outloop edge */
				Branch outloopEdge = null;
				boolean stop = false;
				for (int i = stackPos; i < stack.size() && !stop; i++) {
					CFGNode node = stack.get(i);
					if (node.isConditional()) {
						for (CFGNode branch : node.getChildren()) {
							if (!stack.get(i + 1).equals(branch)) {
								outloopEdge = new Branch(node.getIdx(), branch.getIdx());
								stop = true;
								break;
							}
						}
					}
				}
				aliasNode.getAliasNodeId().setOutLoopBranch(outloopEdge);
				/* get back one step and discover another path from there */
				stack.remove(stack.size() - 1);
				continue;
			}
			/* not visited --> visit */
			visited[nodeIdx] = stack.size() - 1;
			for (CFGNode branch : curNode.getChildren()) {
				if (!(branch instanceof CFGAliasNode)) {
					stack.add(branch);
				}
			}
		}
	}
	
	public Map<CFGNode, List<CFGNode>> buildControlDependencyMap(CFG cfg) {
		GraphControlDependencyCalculator controlDependencyCalcul = new GraphControlDependencyCalculator();
		return controlDependencyCalcul.buildControlDependencyMap(cfg);
	}
	
}
