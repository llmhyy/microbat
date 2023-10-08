package microbat.debugpilot.pathfinding;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import microbat.debugpilot.NodeFeedbacksPair;
import microbat.debugpilot.settings.PathFinderSettings;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.ChosenVariableOption;
import microbat.recommendation.UserFeedback;
import microbat.util.UniquePriorityQueue;

public abstract class DijkstraPathFinder extends AbstractPathFinder {

	public DijkstraPathFinder(final PathFinderSettings settings) {
		this(settings.getTrace(), settings.getSlicedTrace());
	}
	
	public DijkstraPathFinder(Trace trace, List<TraceNode> slicedTrace) {
		super(trace, slicedTrace);
	}
	
	// Do not allow public to construct finder without parameter
	protected DijkstraPathFinder() {
		super(null, null);
	}
	
	@Override
	public FeedbackPath findPath(TraceNode startNode, TraceNode endNode) {
		Objects.requireNonNull(startNode, Log.genMsg(getClass(), "start node should not be null"));
		Objects.requireNonNull(endNode, Log.genMsg(getClass(), "endNode should not be null"));
		
		// Handle the case that startNode is the endNode
		if (startNode.equals(endNode)) {
			FeedbackPath path = new FeedbackPath();
			path.addPair(endNode, new UserFeedback(UserFeedback.ROOTCAUSE));
			return path;
		}
		
		// Construct a graph for library algorithm to use
		Graph<TraceNode, NodeFeedbacksPair> graph = this.constructGraph();
		
		// Run Dijkstra algorithm
		DijkstraShortestPath<TraceNode, NodeFeedbacksPair> dijstraAlg = new DijkstraShortestPath<>(graph);
		GraphPath<TraceNode, NodeFeedbacksPair> result = dijstraAlg.getPath(startNode, endNode);
		if (result == null) {
			return null;
		}
		List<NodeFeedbacksPair> path = result.getEdgeList();
		UserFeedback feedback = new UserFeedback(UserFeedback.ROOTCAUSE);
		NodeFeedbacksPair pair = new NodeFeedbacksPair(endNode, feedback);
		path.add(pair);
		return new FeedbackPath(path);
	}
	
	/**
	 * Construct a graph for algorithm runner
	 * @return Graph
	 */
	protected Graph<TraceNode, NodeFeedbacksPair> constructGraph() {
		Graph<TraceNode, NodeFeedbacksPair> directedGraph = new DirectedWeightedMultigraph<TraceNode, NodeFeedbacksPair>(NodeFeedbacksPair.class);
		
		final TraceNode lastNode = this.slicedTrace.get(this.slicedTrace.size()-1);
		UniquePriorityQueue<TraceNode> toVisitNodes = new UniquePriorityQueue<>(new Comparator<TraceNode>() {
			@Override
			public int compare(TraceNode t1, TraceNode t2) {
				return t2.getOrder() - t1.getOrder();
			}
		});
		toVisitNodes.add(lastNode);
		
		// Building the graph in the same way as dynamic slicing
		while (!toVisitNodes.isEmpty()) {
			final TraceNode node = toVisitNodes.poll();
			directedGraph.addVertex(node);
			for (VarValue readVar : node.getReadVariables()) {
				final TraceNode dataDom = this.trace.findDataDependency(node, readVar);
				if (dataDom != null) {
					UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_VARIABLE_VALUE);
					feedback.setOption(new ChosenVariableOption(readVar, null));
					NodeFeedbacksPair pair = new NodeFeedbacksPair(node, feedback);
					directedGraph.addVertex(dataDom);
					directedGraph.addEdge(node, dataDom, pair);
					directedGraph.setEdgeWeight(pair, this.getCost(readVar));
					toVisitNodes.add(dataDom);
				}
			}
			
			TraceNode controlDom = node.getControlDominator();
			if (controlDom != null) {
				UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_PATH);
				NodeFeedbacksPair pair = new NodeFeedbacksPair(node, feedback);
				directedGraph.addVertex(controlDom);
				directedGraph.addEdge(node, controlDom, pair);
				directedGraph.setEdgeWeight(pair, this.getCost(controlDom.getConditionResult()));
				toVisitNodes.add(controlDom);
			}
		} 
		
		if (directedGraph.vertexSet().size() != this.slicedTrace.size()) {
			throw new RuntimeException(Log.genMsg(this.getClass(), "Graph constructed does not match with sliced trace: " + directedGraph.vertexSet().size() + " : " + this.slicedTrace.size()));
		}
		
		return directedGraph;
	}
	
	/**
	 * Get the cost of choosing the given varValue as next step. Greedy method will choose the lowest one
	 * @param varValue Target variable
	 * @return Cost for choosing the give variable
	 */
	protected abstract double getCost(final VarValue varValue);
}
