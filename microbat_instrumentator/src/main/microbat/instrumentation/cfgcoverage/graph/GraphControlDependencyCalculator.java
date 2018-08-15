package microbat.instrumentation.cfgcoverage.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sav.common.core.utils.CollectionUtils;

/**
 * 
 * @author lyly
 * Adopt code in Yun Lin's CFGConstructor, just parameterize Graph type.
 */
public class GraphControlDependencyCalculator {
	
	public <T extends IGraphNode<T>> Map<T, List<T>> buildControlDependencyMap(IGraph<T> graph) {
		Map<T, Set<T>> postDominanceMap = constructPostDomination(graph);
		return constructControlDependency(graph, postDominanceMap);
	}
	
	public <T extends IGraphNode<T>>Map<T, Set<T>> constructPostDomination(IGraph<T> graph){
		Map<T, Set<T>> postDominanceMap = new HashMap<>();
		
		/** connect basic post domination relation */
		for(T node: graph.getNodeList()){
			Set<T> set = new HashSet<>();
			set.add(node);
			postDominanceMap.put(node, set);
		}
		
		/** extend */
		Boolean isChange = true;
		while(isChange){
			isChange = false;
			Set<T> visitedBlocks = new HashSet<>();
			for(T exitNode: graph.getExitList()){
				propagatePostDominator(postDominanceMap, exitNode, isChange, visitedBlocks);
			}
		}
		
		return postDominanceMap;
	}

	private <T extends IGraphNode<T>> void propagatePostDominator(Map<T, Set<T>> postDominanceMap, T node, 
			Boolean isChange, Set<T> visitedBlocks) {
		visitedBlocks.add(node);
		
		Set<T> intersetion = findIntersetedPostDominator((List<T>) node.getChildren(), postDominanceMap);
		Set<T> postDominatorSet = postDominanceMap.get(node);
		
		for(T newNode: intersetion){
			if(!postDominatorSet.contains(newNode)){
				postDominatorSet.add(newNode);
				isChange = true;
			}
		}
		postDominanceMap.put(node, postDominatorSet);
		
		for(T parent: node.getParents()){
			if(!visitedBlocks.contains(parent)){
				propagatePostDominator(postDominanceMap, parent, isChange, visitedBlocks);				
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends IGraphNode<T>> Set<T> findIntersetedPostDominator(List<T> children,
			Map<T, Set<T>> postDominanceMap) {
		if(children.isEmpty()){
			return new HashSet<>();
		}
		else if(children.size()==1){
			T child = children.get(0);
			return postDominanceMap.get(child);
		}
		else{
			T child = children.get(0);
			Set<T> set = (Set<T>) ((HashSet<T>)postDominanceMap.get(child)).clone();
			
			for(int i=1; i<children.size(); i++){
				T otherChild = children.get(i);
				Set<T> candidateSet = postDominanceMap.get(otherChild);
				
				Iterator<T> setIter = set.iterator();
				while(setIter.hasNext()){
					T postDominator = setIter.next();
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
	private <T extends IGraphNode<T>> Map<T, List<T>> constructControlDependency(IGraph<T> graph,
			Map<T, Set<T>> postDominanceMap) {
		Map<T, List<T>> controlDependencyMap = new HashMap<>();
		List<T> branchNodes = new ArrayList<>();
		for (T branchNode : graph.getNodeList()) {
			if (CollectionUtils.getSize(branchNode.getChildren()) > 1) {
				branchNodes.add(branchNode);
			}
		}
		for (T branchNode : branchNodes) {
			controlDependencyMap.put(branchNode, new ArrayList<T>());
		}
		
		for (T branchNode : branchNodes) {
			computeControlDependentees(branchNode, (List<T>) branchNode.getChildren(), graph, postDominanceMap, controlDependencyMap);
		}
		return controlDependencyMap;
	}

	private <T extends IGraphNode<T>> void computeControlDependentees(T branchNode, List<T> children, IGraph<T> graph,
			Map<T, Set<T>> postDominanceMap, Map<T, List<T>> controlDependencyMap) {
		List<T> dependentees = controlDependencyMap.get(branchNode);
		for (T child : CollectionUtils.nullToEmpty(children)) {
			if(!dependentees.contains(child)){
				if (!postDominanceMap.get(child).contains(branchNode)) {
					dependentees.add(child);
					computeControlDependentees(branchNode, (List<T>) child.getChildren(), graph, postDominanceMap, controlDependencyMap);
				}
			}
		}
		
	}
}
