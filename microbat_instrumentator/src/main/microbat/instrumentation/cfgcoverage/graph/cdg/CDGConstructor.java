package microbat.instrumentation.cfgcoverage.graph.cdg;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import microbat.instrumentation.cfgcoverage.graph.GraphControlDependencyCalculator;
import sav.common.core.utils.CollectionUtils;

public class CDGConstructor {
	private GraphControlDependencyCalculator controlDependencyCalcul = new GraphControlDependencyCalculator();
	
	public CDG construct(CoverageSFlowGraph graph) {
		Map<CoverageSFNode, List<CoverageSFNode>> controlDependencyMap = controlDependencyCalcul
															.buildControlDependencyMap(graph);
		CDG cdg = new CDG(graph);
		Map<Integer, CDGNode> cdgNodeMap = new HashMap<>();
		Set<Integer> visited = new HashSet<>();
		Stack<CDGNodeHolder> condNodeStack = new Stack<>();
		Stack<CoverageSFNode> visitStack = new Stack<>();
		visitStack.push(graph.getStartNode());
		CDGNodeHolder curDominatorNode = new CDGNodeHolder(cdgNodeMap);
		while (!visitStack.empty()) {
			CoverageSFNode visitingNode = visitStack.peek();
			if (!visited.contains(visitingNode.getCvgIdx())) {
				if (curDominatorNode.controlDominate(visitingNode)) {
					curDominatorNode.addBranch(visitingNode);
				}
				if (visitingNode.isConditionalNode()) {
					curDominatorNode = new CDGNodeHolder(cdgNodeMap, visitingNode, controlDependencyMap);
					condNodeStack.push(curDominatorNode);
					visited.add(visitingNode.getCvgIdx());
				} else {
					visitStack.pop();
				}
				for (CoverageSFNode child : CollectionUtils.nullToEmpty(visitingNode.getChildren())) {
					visitStack.push(child);
				}
			} else {
				if (!condNodeStack.isEmpty() && condNodeStack.peek().dominator.getCfgNode() == visitingNode) {
					condNodeStack.pop();
				}
				if (condNodeStack.isEmpty()) {
					curDominatorNode = new CDGNodeHolder(cdgNodeMap);
				} else {
					curDominatorNode = condNodeStack.peek();
				}
				visitStack.pop();
			}
		}
		
		for (CoverageSFNode node : graph.getNodeList()) {
			CDGNode cdgNode = cdgNodeMap.get(node.getCvgIdx());
			if (cdgNode == null) {
				cdgNode = new CDGNode(node);
				cdgNodeMap.put(node.getCvgIdx(), cdgNode);
			}
			cdg.addNode(cdgNode);
			if (cdgNode.getChildren().isEmpty()) {
				cdg.addExitNode(cdgNode);
			}
			if (cdgNode.getParent().isEmpty()) {
				cdg.addStartNode(cdgNode);
			}
		}
		cdg.setCoverageSFNodeToCDGNodeMap(cdgNodeMap);
		return cdg;
	}
	
	private static class CDGNodeHolder {
		Map<Integer, CDGNode> cdgNodeMap;
		List<CoverageSFNode> dominatorDependencies = Collections.emptyList();
		CDGNode dominator;
		
		CDGNodeHolder(Map<Integer, CDGNode> cdgNodeMap) {
			this.cdgNodeMap = cdgNodeMap;
		}
		
		CDGNodeHolder(Map<Integer, CDGNode> cdgNodeMap, CoverageSFNode dominatorNode,
				Map<CoverageSFNode, List<CoverageSFNode>> controlDependencyMap) {
			this(cdgNodeMap);
			this.dominator = getCorrespondingCDGNode(dominatorNode);
			this.dominatorDependencies = controlDependencyMap.get(dominatorNode);
		}

		public void addBranch(CoverageSFNode controlDependentNode) {
			CDGNode correspondingCdgNode = getCorrespondingCDGNode(controlDependentNode);
			dominator.setChild(correspondingCdgNode);
		}

		private CDGNode getCorrespondingCDGNode(CoverageSFNode cfgNode) {
			CDGNode correspondingCdgNode = cdgNodeMap.get(cfgNode.getCvgIdx());
			if (correspondingCdgNode == null) {
				correspondingCdgNode = new CDGNode(cfgNode);
				cdgNodeMap.put(cfgNode.getCvgIdx(), correspondingCdgNode);
			}
			return correspondingCdgNode;
		}

		public boolean controlDominate(CoverageSFNode visitingNode) {
			return dominatorDependencies.contains(visitingNode);
		}
		
	}
}
