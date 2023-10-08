package microbat.debugpilot.pathfinding;

import java.util.Comparator;
import java.util.List;

import org.jgrapht.Graph;
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

public class SuspiciousDijkstraPathFinder extends DijkstraPathFinder {
	
	public SuspiciousDijkstraPathFinder(final PathFinderSettings settings) {
		this(settings.getTrace(), settings.getSlicedTrace());
	}
	
	public SuspiciousDijkstraPathFinder(Trace trace, List<TraceNode> slicedTrace) {
		super(trace, slicedTrace);
	}
	
	@Override
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
					directedGraph.setEdgeWeight(pair, 1.0d/(readVar.getSuspiciousness() + AbstractPathFinder.eps));
					toVisitNodes.add(dataDom);
				}
			}
			
			TraceNode controlDom = node.getControlDominator();
			if (controlDom != null) {
				UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_PATH);
				NodeFeedbacksPair pair = new NodeFeedbacksPair(node, feedback);
				directedGraph.addVertex(controlDom);
				directedGraph.addEdge(node, controlDom, pair);
				directedGraph.setEdgeWeight(pair, 1.0d/(controlDom.getConditionResult().getSuspiciousness() + AbstractPathFinder.eps));
				toVisitNodes.add(controlDom);
			}
		} 
		
		if (directedGraph.vertexSet().size() != this.slicedTrace.size()) {
			throw new RuntimeException(Log.genMsg(this.getClass(), "Graph constructed does not match with sliced trace: " + directedGraph.vertexSet().size() + " : " + this.slicedTrace.size()));
		}
		
		return directedGraph;
	}
}
