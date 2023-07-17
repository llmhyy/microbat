package microbat.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;

/**
 * TraceUtil provides helper function
 * to analyze the trace
 * 
 * @author WYK
 *
 */
public class TraceUtil {
	
	/**
	 * Perform dynamic slicing on the given trace based on the
	 * given target variables. <br><br>
	 * 
	 * It serves as a filter filtering out the node that does
	 * not contribute to the target variables <br><br>
	 * 
	 * Procedures: <br>
	 * 1. It will first search the nodes containing the
	 * target variables starting from the end. <br>
	 * 2. For each node containing the target variables,
	 * perform breath first search based on data and control
	 * relation to search the related node until it cannot
	 * go further. <br><br>
	 * 
	 * Note that this function will ignore the throwing exception
	 * node.
	 * 
	 * @param trace Trace of target program
	 * @param targets Target variables 
	 * @return List of node that contribute the result target variables
	 */
	public static List<TraceNode> dyanmicSlice(final Trace trace, final Collection<VarValue> targets) {
		UniquePriorityQueue<TraceNode> toVisitNodes = new UniquePriorityQueue<>(new Comparator<TraceNode>() {
			@Override
			public int compare(TraceNode t1, TraceNode t2) {
				return t2.getOrder() - t1.getOrder();
			}
		});
		
		List<VarValue> targetsCopy = new ArrayList<>();
		targetsCopy.addAll(targets);
		
		// Search in reversed order because output usually appear at the end
		for (int order = trace.getLatestNode().getOrder(); order>=1; order--) {
			TraceNode node = trace.getTraceNode(order);
			
			// The throwing exception is not considered
			if (node.isThrowingException()) {
				continue;
			}
			
			// Store to visit node
			Iterator<VarValue> iter = targetsCopy.iterator();
			while(iter.hasNext()) {
				VarValue output = iter.next();
				if (node.isReadVariablesContains(output.getVarID()) ||
					node.isWrittenVariablesContains(output.getVarID())) {
					toVisitNodes.add(node);
					iter.remove();
				}
			}
			
			// Early stopping: when all output has been found
			if (targetsCopy.isEmpty()) {
				break;
			}
		}
		
		// Perform dynamic slicing base starting from output trace node
		Set<TraceNode> slicingSet = new HashSet<>();
		while (!toVisitNodes.isEmpty()) {
			TraceNode node = toVisitNodes.poll();
			
			// Add data dominator
			for (VarValue readVar : node.getReadVariables()) {
				TraceNode dataDom = trace.findDataDependency(node, readVar);
				if (dataDom != null) {
					toVisitNodes.add(dataDom);
				}
			}
			
			// Add control dominator
			TraceNode controlDom = node.getControlDominator();
			if (controlDom != null) {
				toVisitNodes.add(controlDom);
			}
			
			slicingSet.add(node);
		}
		
		// Sort the result
		List<TraceNode> result = new ArrayList<>(slicingSet);
		Collections.sort(result, new Comparator<TraceNode>() {
			@Override
			public int compare(TraceNode node1, TraceNode node2) {
				return node1.getOrder() - node2.getOrder();
			}
		});
		
		return result;
	}
	
	public static List<TraceNode> dynamicSlic(final Trace trace, final TraceNode targetNode) {
		UniquePriorityQueue<TraceNode> toVisitNodes = new UniquePriorityQueue<>(new Comparator<TraceNode>() {
			@Override
			public int compare(TraceNode t1, TraceNode t2) {
				return t2.getOrder() - t1.getOrder();
			}
		});
		toVisitNodes.add(targetNode);
		
		// Perform dynamic slicing base starting from output trace node
		Set<TraceNode> slicingSet = new HashSet<>();
		while (!toVisitNodes.isEmpty()) {
			TraceNode node = toVisitNodes.poll();
			
			// Add data dominator
			for (VarValue readVar : node.getReadVariables()) {
				TraceNode dataDom = trace.findDataDependency(node, readVar);
				if (dataDom != null) {
					toVisitNodes.add(dataDom);
				}
			}
			
			// Add control dominator
			TraceNode controlDom = node.getControlDominator();
			if (controlDom != null) {
				toVisitNodes.add(controlDom);
			}
			
			slicingSet.add(node);
		}
		
		// Sort the result
		List<TraceNode> result = new ArrayList<>(slicingSet);
		Collections.sort(result, new Comparator<TraceNode>() {
			@Override
			public int compare(TraceNode node1, TraceNode node2) {
				return node1.getOrder() - node2.getOrder();
			}
		});
		
		return result;
	}
	
	public static TraceNode findNextNode(final TraceNode node, final UserFeedback feedback, final Trace trace) {
		TraceNode nextNode = null;
		if (feedback.getFeedbackType() == UserFeedback.WRONG_PATH) {
			nextNode = node.getControlDominator();
		} else if (feedback.getFeedbackType() == UserFeedback.WRONG_VARIABLE_VALUE) {
			VarValue wrongVar = feedback.getOption().getReadVar();
			nextNode = trace.findDataDependency(node, wrongVar);
		}
		return nextNode;
	}
	
	public static Set<TraceNode> findAllNextNodes(final TraceNode node, final Collection<UserFeedback> feedbacks, final Trace trace) {
		if (node == null) throw new IllegalArgumentException("node should not be null");
		if (trace == null) throw new IllegalArgumentException("trace should not be null");
		
		Set<TraceNode> nextNodes = new HashSet<>();
		for (UserFeedback feedback : feedbacks) {
			TraceNode nextNode = TraceUtil.findNextNode(node, feedback, trace);
			if (nextNode != null) {
				nextNodes.add(nextNode);
			}
		}
		return nextNodes;
	}
	/**
	 * Find all the nodes that affected by the give node using BFS
	 * @param node Given starting node
	 * @param trace Trace needed to check the relation
	 * @return Set of nodes affected by the given node
	 */
	public static Set<TraceNode> cropTrace(final TraceNode node, final Trace trace) {
		Set<TraceNode> croppedTrace = new HashSet<>();
		croppedTrace.add(node);
		
		Queue<TraceNode> toVisitNodes = new LinkedList<>();
		toVisitNodes.add(node);
		
		while(!toVisitNodes.isEmpty()) {
			TraceNode currentNode = toVisitNodes.poll();
			croppedTrace.add(currentNode);
			
			List<TraceNode> controlDominatees = currentNode.getControlDominatees();
			controlDominatees.removeIf(controlDominatee -> croppedTrace.contains(controlDominatee));
			toVisitNodes.addAll(controlDominatees);
			
			for (VarValue writtenVar : currentNode.getWrittenVariables()) {
				List<TraceNode> dataDominatees = trace.findDataDependentee(currentNode, writtenVar);
				dataDominatees.removeIf(dataDominatee -> croppedTrace.contains(dataDominatee));
				toVisitNodes.addAll(dataDominatees);
			}
		}
		
		return croppedTrace;
	}
	
	public static int relationDistance(final TraceNode node1, final TraceNode node2, final Trace trace, final int disLimit) {
		if (node1.equals(node2)) {
			return 0;
		}
		Set<TraceNode> nextNodes = TraceUtil.extractRelatedNodes(node1, trace);
		for (int distance=1; distance<=disLimit; distance++) {
			if (nextNodes.contains(node2)) {
				return distance;
			}
			Set<TraceNode> candidatesNodes = new HashSet<>();
			for (TraceNode node : nextNodes) {
				candidatesNodes.addAll(TraceUtil.extractRelatedNodes(node, trace));
			}
		}
		return -1;
	}
	
	public static Set<TraceNode> extractRelatedNodes(final TraceNode node, final Trace trace) {
		Set<TraceNode> relatedNodes = new HashSet<>();
		
		// Control Dominator
		final TraceNode controlDom = node.getControlDominator();
		if (controlDom != null) {
			relatedNodes.add(controlDom);
		}
		
		// Data Dominator
		for (VarValue readVar : node.getReadVariables()) {
			final TraceNode dataDom = trace.findDataDependency(node, readVar);
			if (dataDom != null) {
				relatedNodes.add(dataDom);
			}
		}
		
		// Control Dominatees
		relatedNodes.addAll(node.getControlDominatees());
		
		// Data Dominatees
		for (VarValue writtenVar : node.getWrittenVariables()) {
			relatedNodes.addAll(trace.findDataDependentee(node, writtenVar));
		}
		
		return relatedNodes;
	}
	
	public static List<VarValue> filterThisVar(final Collection<VarValue> vars) {
		return vars.stream().filter(var -> var.isThisVariable()).toList();
	}
} 
