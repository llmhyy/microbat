package microbat.instrumentation.cfgcoverage.graph;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
import microbat.instrumentation.utils.ApplicationUtility;
import microbat.instrumentation.utils.CollectionUtils;
import microbat.model.ClassLocation;
import sav.common.core.utils.ClassUtils;
import sav.strategies.dto.AppJavaClassPath;

public class CFGUtility {
	private CFGRepository cfgCreator = new CFGRepository();
	private List<String> inclusiveMethodIds;
	
	public CFGInstance buildProgramFlowGraph(AppJavaClassPath appClasspath, ClassLocation targetMethod,
			int cfgExtensionLayer) {
		CFGInstance cfg = buildProgramFlowGraph(appClasspath, targetMethod, 1, cfgExtensionLayer);
		cfg.setCfgExtensionLayer(cfgExtensionLayer);
		return cfg;
	}

	private CFGInstance buildProgramFlowGraph(AppJavaClassPath appClasspath, ClassLocation targetMethod, int layer,
			int maxLayer) {
		String methodId = InstrumentationUtils.getMethodId(targetMethod.getClassCanonicalName(), targetMethod.getMethodSign());
		if (!CollectionUtils.isEmpty(inclusiveMethodIds) && !inclusiveMethodIds.contains(methodId)) {
			return new CFGInstance(null, methodId, Collections.<CFGNode>emptyList());
		}
		
		CFGInstance cfg = cfgCreator.createCfgInstance(targetMethod, appClasspath);
		List<String> appBinFolders = ApplicationUtility.lookupAppBinaryFolders(appClasspath);
		if (layer < maxLayer) {
			List<CFGNode> invokeNodes = new ArrayList<>();
			for (CFGNode node : cfg.getNodeList()) {
				if (node.getInstructionHandle().getInstruction() instanceof InvokeInstruction) {
					invokeNodes.add(node);
				}
			}
			for (CFGNode node : invokeNodes) {
				ConstantPoolGen cpg = new ConstantPoolGen(cfg.getCfg().getMethod().getConstantPool());
				InvokeInstruction methodInsn = (InvokeInstruction) node.getInstructionHandle().getInstruction();
				String invkClassName = methodInsn.getClassName(cpg);
				String invkMethodName = InstrumentationUtils.getMethodWithSignature(methodInsn.getMethodName(cpg),
						methodInsn.getSignature(cpg));
				if (isApplicationClass(appBinFolders, invkClassName)) {
					ClassLocation invokeMethod = new ClassLocation(invkClassName, invkMethodName, -1);
					CFGInstance subCfg = buildProgramFlowGraph(appClasspath, invokeMethod, layer + 1, maxLayer);
					glueCfg(cfg, node, subCfg);
				}
			}
		}
		return cfg;
	}
	
	private boolean isApplicationClass(List<String> appBinFolders, String className) {
		for (String appBinFolder : appBinFolders) {
			String classFilePath = ClassUtils.getClassFilePath(appBinFolder, className);
			File classFile = new File(classFilePath);
			if (classFile.exists()) {
				return true;
			}
		}
		return false;
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
		List<CFGNode> nextNodes = new ArrayList<>(node.getChildren());
		node.getChildren().clear();
		node.addChild(subCfg.getStartNode());
		for (CFGNode exitNode : subCfg.getExitList()) {
			if (exitNode.getInstructionHandle().getInstruction() instanceof RETURN) {
				for (CFGNode nextNode : nextNodes) {
					exitNode.addChild(nextNode);
					nextNode.getParents().remove(node);
					nextNode.addParent(exitNode);
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
				CFGAliasNode aliasNode = new CFGAliasNode(curNode);
				aliasNode.addParent(backwardEdgeStartNode);
				backwardEdgeStartNode.getChildren().remove(curNode);
				curNode.getParents().remove(backwardEdgeStartNode);
				backwardEdgeStartNode.addChild(aliasNode); 
				
				cfgInstance.addAliasNode(aliasNode);
				
				/* find outloop edge */
				boolean stop = false;
				CFGNode outloopNode = null;
				for (int i = stackPos; i < stack.size() && !stop; i++) {
					CFGNode node = stack.get(i);
					if (node.isConditional()) {
						/* find next visited node on stack */
						CFGNode firstNodeOfCurrentBranch = null;
						for (int j = i + 1; j < stack.size() ;j++) {
							CFGNode sNode = stack.get(j);
							if (visited[sNode.getIdx()] >= 0) {
								firstNodeOfCurrentBranch = sNode;
								break;
							}
						}
						for (CFGNode branch : node.getChildren()) {
							if (!firstNodeOfCurrentBranch.equals(branch)) {
								outloopNode = branch;
								stop = true;
								break;
							}
						}
					}
				}
				aliasNode.addChild(outloopNode);
				outloopNode.addParent(aliasNode);
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
	
	public void setInclusiveMethodIds(List<String> inclusiveMethodIds) {
		this.inclusiveMethodIds = inclusiveMethodIds;
	}
}
