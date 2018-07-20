package microbat.instrumentation.cfgcoverage.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.RETURN;

import microbat.codeanalysis.bytecode.CFG;
import microbat.codeanalysis.bytecode.CFGNode;
import microbat.instrumentation.cfgcoverage.InstrumentationUtils;
import microbat.instrumentation.cfgcoverage.graph.CFGInstance.UniqueNodeId;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode.Type;
import microbat.model.ClassLocation;
import sav.common.core.SavRtException;
import sav.common.core.utils.CollectionUtils;
import sav.strategies.dto.AppJavaClassPath;

/**
 * @author LLT
 *
 */
public class CoverageGraphConstructor {
	private CFGRepository cfgRepository = new CFGRepository();
	
	public CoverageSFlowGraph buildCoverageGraph(AppJavaClassPath appClasspath, ClassLocation targetMethod,
			int cdgLayer, List<String> inclusiveMethodIds) {
		CFGInstance cfg = buildProgramFlowGraph(appClasspath, targetMethod, 1, cdgLayer, inclusiveMethodIds);
		breakCircle(cfg);
		CoverageSFlowGraph coverageGraph = new CoverageSFlowGraph(cfg, cdgLayer);
		Stack<CFGNode> stack = new Stack<>();
		stack.push(cfg.getCfg().getStartNode());
		Map<Integer, CoverageSFNode> nodeMap = new HashMap<>();
		
		while (!stack.isEmpty()) {
			CFGNode node = stack.pop();
			if (nodeMap.containsKey(node.getIdx())) {
				continue;
			}
			Type type = Type.BLOCK_NODE;
			if (node instanceof CFGAliasNode) {
				type = Type.ALIAS_NODE;
			} else if (node.getInstructionHandle().getInstruction() instanceof InvokeInstruction) {
				type = Type.INVOKE_NODE;
			} else if (node.isBranch()) {
				type = Type.CONDITION_NODE;
			}
			CoverageSFNode blockNode = new CoverageSFNode(type, node, cfg);
			switch (type) {
			case BLOCK_NODE:
				blockNode.addContentNode(node.getIdx());
				CFGNode curNode = CollectionUtils.getLast(node.getChildren());
				if (curNode == null) {
					continue;
				}
				while (curNode != null) {
					if ((CollectionUtils.getSize(curNode.getChildren()) > 1)
							|| (CollectionUtils.getSize(curNode.getParents()) != 1)
							|| (curNode.getInstructionHandle().getInstruction() instanceof InvokeInstruction)
							|| (curNode.isBranch())
							|| curNode instanceof CFGAliasNode) {
						stack.push(curNode);
						break;
					}
					blockNode.addContentNode(curNode.getIdx());
					curNode = CollectionUtils.getLast(curNode.getChildren());
				}
				break;
			case CONDITION_NODE:
			case INVOKE_NODE:
				for (CFGNode branch : node.getChildren()) {
					stack.push(branch);
				}
				break;
			case ALIAS_NODE:
				blockNode.setAliasId(((CFGAliasNode) node).getAliasNodeId());
				break;
			default:
				break;
			}
			blockNode.setBlockScope();
			coverageGraph.addNode(blockNode);
			nodeMap.put(node.getIdx(), blockNode);
			if (coverageGraph.getStartNode() == null) {
				coverageGraph.setStartNode(blockNode);
			}
		}
		
		/* set endNodeId */
		for (CoverageSFNode node : coverageGraph.getNodeList()) {
			if (node.getType() == Type.ALIAS_NODE) {
				CoverageSFNode orgCoverageNode = nodeMap.get(node.getStartIdx());
				node.setEndIdx(orgCoverageNode.getEndIdx());
				node.setEndNodeId(orgCoverageNode.getEndNodeId());
			} else {
				CFGNode endCfgNode = cfg.getNodeList().get(node.getEndIdx());
				node.setEndNodeId(cfg.getUnitCfgNodeId(endCfgNode));
			}
		}
		
		/* create graph edges */
		for (CoverageSFNode node : coverageGraph.getNodeList()) {
			if (node.getType() == Type.ALIAS_NODE) {
				Branch outLoopBranch = node.getAliasId().getOutLoopBranch();
				if (outLoopBranch != null) {
					node.addBranch(nodeMap.get(outLoopBranch.getToNodeIdx()));
				}
				continue;
			}
			CFGNode endCfgNode = cfg.getNodeList().get(node.getEndIdx());
			for (CFGNode branch : endCfgNode.getChildren()) {
				CoverageSFNode blockNode = nodeMap.get(branch.getIdx());
				if (blockNode == null) {
					throw new SavRtException("blockNode should not be null!!");
				}
				node.addBranch(blockNode);
			}
		}
		return coverageGraph;
	}

	/**
	 * List stack: to trace back nodes when traversing the graph.
	 * int[] visited: the order of visited node (also its index on stack), if node has not been visited, the value is -1.
	 * this array is used to keep the path from entry node to current node.
	 * using DFS to traverse the graph, 
	 */
	private void breakCircle(CFGInstance cfgInstance) {
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
				stack.add(branch);
			}
		}
	}

	private CFGInstance buildProgramFlowGraph(AppJavaClassPath appClasspath, ClassLocation targetMethod, int layer, int maxLayer, List<String> inclusiveMethodIds) {
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
					if (inclusiveMethodIds.contains(methodId)) {
						ClassLocation invokeMethod = new ClassLocation(invkClassName, invkMethodName, -1);
						CFGInstance subCfg = buildProgramFlowGraph(appClasspath, invokeMethod, layer + 1, maxLayer, inclusiveMethodIds);
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
	
}
