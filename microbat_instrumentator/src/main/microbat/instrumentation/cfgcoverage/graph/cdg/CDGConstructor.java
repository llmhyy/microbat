package microbat.instrumentation.cfgcoverage.graph.cdg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
		CDG cdg = new CDG();
		Map<Integer, CDGNode> cdgNodes = new HashMap<>();
		for (CoverageSFNode node : graph.getNodeList()) {
			if (CollectionUtils.getSize(node.getChildren()) > 1) {
				CDGNode cdgNode = new CDGNode(node);
				cdgNodes.put(node.getCvgIdx(), cdgNode);
			}
		}
		boolean[] visited = new boolean[graph.size()];
		boolean[] parentAssigned = new boolean[graph.size()];
		/* DFS */
		Stack<CoverageSFNode> stack = new Stack<>();
		stack.add(graph.getStartNode());
		while (!stack.isEmpty()) {
			CoverageSFNode curNode = stack.peek(); 
			
			if (visited[curNode.getCvgIdx()]) { // all branch visited.
				List<CoverageSFNode> dependentees = controlDependencyMap.get(curNode);
				if (!CollectionUtils.isEmpty(dependentees)) { // is conditional Node
					CDGNode cdgNode = cdgNodes.get(curNode.getCvgIdx());
					for (CoverageSFNode dependentee : dependentees) {
						if (!parentAssigned[dependentee.getCvgIdx()]) {
							CDGNode dependenteeCdgNode = cdgNodes.get(dependentee.getCvgIdx());
							if (dependenteeCdgNode == null) {
								cdgNode.addContent(dependentee);
							} else{
								cdgNode.setChild(dependenteeCdgNode);
							}
							parentAssigned[dependentee.getCvgIdx()] = true;
						}
					}
				}
				stack.pop();
				continue;
			}
			/* visit */
			for (CoverageSFNode child : CollectionUtils.nullToEmpty(curNode.getChildren())) {
				stack.push(child);
			}
			visited[curNode.getCvgIdx()] = true;
		}
		
		for (CoverageSFNode node : graph.getNodeList()) {
			CDGNode cdgNode = cdgNodes.get(node.getCvgIdx());
			if (!parentAssigned[node.getCvgIdx()]) {
				if (cdgNode == null) {
					cdg.addContent(node);
				} else{
					cdg.addStartNode(cdgNode);
				}
			} else if (cdgNode != null){
				if (cdgNode.getChildren().isEmpty()) {
					cdg.addExitNode(cdgNode);
				}
			}
		}
		return cdg;
	}
	
}
