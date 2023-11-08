package microbat.debugpilot.pathfinding;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import microbat.debugpilot.settings.PathFinderSettings;
import microbat.debugpilot.userfeedback.DPUserFeedback;
import microbat.debugpilot.userfeedback.DPUserFeedbackType;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
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
			path.add(new DPUserFeedback(DPUserFeedbackType.ROOT_CAUSE, endNode));
			return path;
		}
		
		// Construct a graph for library algorithm to use
		Graph<TraceNode, DPUserFeedback> graph = this.constructGraph();
		
		// Run Dijkstra algorithm
		DijkstraShortestPath<TraceNode, DPUserFeedback> dijstraAlg = new DijkstraShortestPath<>(graph);
		GraphPath<TraceNode, DPUserFeedback> result = dijstraAlg.getPath(startNode, endNode);
		if (result == null) {
			return null;
		}
		List<DPUserFeedback> path = result.getEdgeList();
		DPUserFeedback feedback = new DPUserFeedback(DPUserFeedbackType.ROOT_CAUSE, endNode);
//		UserFeedback feedback = new UserFeedback(UserFeedback.ROOTCAUSE);
//		NodeFeedbacksPair pair = new NodeFeedbacksPair(endNode, feedback);
		path.add(feedback);
		return new FeedbackPath(path);
	}
	
	/**
	 * Construct a graph for algorithm runner
	 * @return Graph
	 */
	protected Graph<TraceNode, DPUserFeedback> constructGraph() {
		Graph<TraceNode, DPUserFeedback> directedGraph = new DirectedWeightedMultigraph<TraceNode, DPUserFeedback>(DPUserFeedback.class);
		
		final TraceNode lastNode = this.slicedTrace.get(this.slicedTrace.size()-1);
		UniquePriorityQueue<TraceNode> toVisitNodes = new UniquePriorityQueue<>(new Comparator<TraceNode>() {
			@Override
			public int compare(TraceNode t1, TraceNode t2) {
				return t2.getOrder() - t1.getOrder();
			}
		});
		toVisitNodes.addAll(this.slicedTrace);
		
		// Building the graph in the same way as dynamic slicing
		while (!toVisitNodes.isEmpty()) {
			final TraceNode node = toVisitNodes.poll();
			directedGraph.addVertex(node);
			for (VarValue readVar : node.getReadVariables()) {
				final TraceNode dataDom = this.trace.findDataDependency(node, readVar);
				if (dataDom != null) {
					DPUserFeedback feedback = new DPUserFeedback(DPUserFeedbackType.WRONG_VARIABLE, node);
					feedback.addWrongVar(readVar);
//					UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_VARIABLE_VALUE);
//					feedback.setOption(new ChosenVariableOption(readVar, null));
//					NodeFeedbacksPair pair = new NodeFeedbacksPair(node, feedback);
					directedGraph.addVertex(dataDom);
					directedGraph.addEdge(node, dataDom, feedback);
					directedGraph.setEdgeWeight(feedback, this.getCost(readVar));
					toVisitNodes.add(dataDom);
				}
			}
			
			TraceNode controlDom = node.getControlDominator();
			if (controlDom != null) {
				DPUserFeedback feedback = new DPUserFeedback(DPUserFeedbackType.WRONG_PATH, node);
//				UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_PATH);
//				NodeFeedbacksPair pair = new NodeFeedbacksPair(node, feedback);
				directedGraph.addVertex(controlDom);
				directedGraph.addEdge(node, controlDom, feedback);
				directedGraph.setEdgeWeight(feedback, this.getCost(controlDom.getConditionResult()));
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
