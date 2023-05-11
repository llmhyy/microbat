package microbat.probability.SPP.pathfinding;

import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import debuginfo.NodeFeedbackPair;
import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.ChosenVariableOption;
import microbat.recommendation.UserFeedback;
import microbat.util.UniquePriorityQueue;

public class PathFinder {
	
	private final Trace trace;
	
	private final List<TraceNode> slicedTrace;;
	
	public PathFinder(Trace trace, final List<TraceNode> slicedTrace ) {
		this.trace = trace;
		this.slicedTrace = slicedTrace;
	}
	
	public ActionPath findPath_dijstra(final TraceNode startNode, final TraceNode endNode) {
		Graph<TraceNode, NodeFeedbacksPair> graph = this.constructGraph();
		DijkstraShortestPath<TraceNode, NodeFeedbacksPair> dijstraAlg = new DijkstraShortestPath<>(graph);
		List<NodeFeedbacksPair> path = dijstraAlg.getPath(startNode, endNode).getEdgeList();
		if (path == null) {
			throw new RuntimeException("There are no path from node: " + startNode.getOrder() + " to node: " + endNode.getOrder());
		}
		return new ActionPath(path);
	}
	
	public ActionPath findPathway_greedy(final TraceNode startNode, final TraceNode endNode) {
		ActionPath path = new ActionPath();
		TraceNode currentNode = startNode;
		while(currentNode != null) {
			if (currentNode.equals(endNode)) {
				UserFeedback feedback = new UserFeedback(UserFeedback.ROOTCAUSE);
				path.addPair(currentNode, feedback);
				return path;
			}
			if (currentNode.getOrder() <= endNode.getOrder()) {
				return null;
			}
			UserFeedback feedback = this.giveFeedback(currentNode);
			path.addPair(currentNode, feedback);
			currentNode = this.findNextNode(currentNode, feedback);
		}
		return null;
	}
	
	public ActionPath findPathway_greedy(final TraceNode startNode, final TraceNode endNode, final ActionPath initPath) {
		ActionPath path = new ActionPath(initPath);
		
		UserFeedback lastFeedback = path.peek().getFeedbacks().get(0);
		TraceNode lastNode = path.peek().getNode();
		
		TraceNode currentNode = this.findNextNode(lastNode, lastFeedback);
		while (currentNode != null && currentNode.getOrder() > endNode.getOrder()) {
			if (currentNode.equals(endNode)) {
				UserFeedback feedback = new UserFeedback(UserFeedback.ROOTCAUSE);
				path.addPair(currentNode, feedback);
				break;
			}
			
			UserFeedback feedback = this.giveFeedback(currentNode);
			path.addPair(currentNode, feedback);
			currentNode = this.findNextNode(currentNode, feedback);
		}
		return path;
	}
	
	public UserFeedback giveFeedback(final TraceNode node) {
		UserFeedback feedback = new UserFeedback();
		
		TraceNode controlDom = node.getControlDominator();
		double controlProb = 2.0;
		if (controlDom != null) {
			controlProb = controlDom.getConditionResult().getProbability();
		}
		
		double minReadProb = 2.0;
		VarValue wrongVar = null;
		for (VarValue readVar : node.getReadVariables()) {
			// If the readVar is This variable, then ignore
			if (readVar.isThisVariable()) {
				continue;
			}
			double prob = readVar.getProbability();
			if (prob < minReadProb) {
				minReadProb = prob;
				wrongVar = readVar;
			}
		}
		
		// There are no controlDom and readVar
		if (controlProb == 2.0 && minReadProb == 2.0) {
			feedback.setFeedbackType(UserFeedback.UNCLEAR);
			return feedback;
		}
		if (controlProb <= minReadProb) {
			feedback.setFeedbackType(UserFeedback.WRONG_PATH);
		} else {
			feedback.setFeedbackType(UserFeedback.WRONG_VARIABLE_VALUE);
			feedback.setOption(new ChosenVariableOption(wrongVar, null));
		}
		return feedback;
	}
	
	private Graph<TraceNode, NodeFeedbacksPair> constructGraph() {
		Graph<TraceNode, NodeFeedbacksPair> directedGraph = new DirectedWeightedMultigraph<TraceNode, NodeFeedbacksPair>(NodeFeedbacksPair.class);
		
		final TraceNode lastNode = this.slicedTrace.get(this.slicedTrace.size()-1);
		UniquePriorityQueue<TraceNode> toVisitNodes = new UniquePriorityQueue<>(new Comparator<TraceNode>() {
			@Override
			public int compare(TraceNode t1, TraceNode t2) {
				return t2.getOrder() - t1.getOrder();
			}
		});
		toVisitNodes.add(lastNode);
		
		while (!toVisitNodes.isEmpty()) {
			final TraceNode node = toVisitNodes.poll();
			directedGraph.addVertex(node);
			for (VarValue readVar : node.getReadVariables()) {
				if (readVar.isThisVariable()) {
					continue;
				}
				final TraceNode dataDom = this.trace.findDataDependency(node, readVar);
				if (dataDom != null) {
					UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_VARIABLE_VALUE);
					feedback.setOption(new ChosenVariableOption(readVar, null));
					NodeFeedbacksPair pair = new NodeFeedbacksPair(node, feedback);
					directedGraph.addVertex(dataDom);
					directedGraph.addEdge(node, dataDom, pair);
					directedGraph.setEdgeWeight(pair, readVar.getProbability());
					toVisitNodes.add(dataDom);
				}
			}
			
			TraceNode controlDom = node.getControlDominator();
			if (controlDom != null) {
				UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_PATH);
				NodeFeedbacksPair pair = new NodeFeedbacksPair(node, feedback);
				directedGraph.addVertex(controlDom);
				directedGraph.addEdge(node, controlDom, pair);
				directedGraph.setEdgeWeight(pair, controlDom.getConditionResult().getProbability());
				toVisitNodes.add(controlDom);
			}
		} 
		
		return directedGraph;
	}
	
	private TraceNode findNextNode(final TraceNode node, final UserFeedback feedback) {
		TraceNode nextNode = null;
		if (feedback.getFeedbackType() == UserFeedback.WRONG_PATH) {
			nextNode = node.getControlDominator();
		} else if (feedback.getFeedbackType() == UserFeedback.WRONG_VARIABLE_VALUE) {
			VarValue wrongVar = feedback.getOption().getReadVar();
			nextNode = this.trace.findDataDependency(node, wrongVar);
		}
		return nextNode;
	}
	
	public List<ActionPath> findAllPathway(final TraceNode startNode, final TraceNode endNode, Collection<VarValue> correctVars, Collection<VarValue> wrongVars) {
		List<ActionPath> output = new ArrayList<>();
		
		// Setup for BFS
		Queue<ActionPath> paths = new LinkedList<>();
		ActionPath path = new ActionPath();
		path.addPair(startNode, null);
		paths.offer(path);
		
		// Start BFS
		while (!paths.isEmpty()) {
			path = paths.poll();
			NodeFeedbacksPair pair = path.peek();
			TraceNode lastNode = pair.getNode();
			
			if (lastNode.equals(endNode)) {
				path.setLastAction(new UserFeedback(UserFeedback.ROOTCAUSE));
				output.add(path);
				continue;
			}
			
			// Check if there any wrong variable
			boolean haveWrongVariable = false;
			for (VarValue readVar : lastNode.getReadVariables()) {
				if (wrongVars.contains(readVar)) {
					// If the read variable is said to be wrong, then path will only pass this variable
					UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_VARIABLE_VALUE);
					feedback.setOption(new ChosenVariableOption(readVar, null));
					path.setLastAction(feedback);
					ActionPath newPath = new ActionPath(path);
					path.setLastAction(null);
					TraceNode nextNode = this.findNextNode(lastNode, feedback);
					if (nextNode == null) {
						continue;
					}
					
					// Skip nextNode if it already beyond the endNode
					if (nextNode.getOrder() < endNode.getOrder()) {
						continue;
					}
					
					newPath.addPair(nextNode, null);
					paths.offer(newPath);
					
					haveWrongVariable = true;
					continue;
				}
			}
			
			if (haveWrongVariable) {
				continue;
			}
			
			TraceNode controlDom = lastNode.getControlDominator();
			if (controlDom != null) {
				if (controlDom.getOrder() >= endNode.getOrder()) {
					VarValue controlDomVar = controlDom.getConditionResult();
					// Skip if the controlDom is correct
					if (!correctVars.contains(controlDomVar)) {
						path.setLastAction(new UserFeedback(UserFeedback.WRONG_PATH));
						ActionPath newPath = new ActionPath(path);
						newPath.addPair(controlDom, null);
						paths.offer(newPath);
						path.setLastAction(null);
						
						// Skip the read variables if the condition is wrong
						if (wrongVars.contains(controlDomVar)) {
							continue;
						}
					}
				}
			}
			
			// Search all read variable to go
			for (VarValue readVar : lastNode.getReadVariables()) {
				// Skip if the variables are said to be correct
				if (correctVars.contains(readVar)) {
					continue;
				}
				
				// Skip if it is the This variable
				if (readVar.isThisVariable()) {
					continue;
				}
				
				UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_VARIABLE_VALUE);
				feedback.setOption(new ChosenVariableOption(readVar, null));
				path.setLastAction(feedback);
				ActionPath newPath = new ActionPath(path);
				path.setLastAction(null);
				TraceNode nextNode = this.findNextNode(lastNode, feedback);
				
				if (nextNode == null) {
					continue;
				}
				
				if (nextNode.getOrder()<endNode.getOrder()) {
					continue;
				}
				
				newPath.addPair(nextNode, null);
				paths.offer(newPath);
			}
		}
		
		return output;
	}
	
}
