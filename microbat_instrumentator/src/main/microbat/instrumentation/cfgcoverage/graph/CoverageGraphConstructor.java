package microbat.instrumentation.cfgcoverage.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.bcel.generic.InvokeInstruction;

import microbat.codeanalysis.bytecode.CFGNode;
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
	private CFGUtility cfgUtility = new CFGUtility();
	
	public CoverageSFlowGraph buildCoverageGraph(AppJavaClassPath appClasspath, ClassLocation targetMethod,
			int cdgLayer) {
		CFGInstance cfg = cfgUtility.buildProgramFlowGraph(appClasspath, targetMethod, cdgLayer);
		cfgUtility.breakCircle(cfg);
		return buildCoverageGraph(cfg);
	}
	
	public CoverageSFlowGraph buildCoverageGraph(CFGInstance cfg) {
		CoverageSFlowGraph coverageGraph = new CoverageSFlowGraph(cfg, cfg.getCfgExtensionLayer());
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
					break;
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
		for (CoverageSFNode node : coverageGraph.getNodeList()) {
			if (CollectionUtils.isEmpty(node.getBranches())) {
				coverageGraph.addExitNode(node);
			}
		}
		return coverageGraph;
	}
	
}
