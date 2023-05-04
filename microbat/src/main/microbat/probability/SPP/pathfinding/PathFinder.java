package microbat.probability.SPP.pathfinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import debuginfo.NodeFeedbackPair;
import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.ChosenVariableOption;
import microbat.recommendation.UserFeedback;

public class PathFinder {
	
	private final Trace trace;
	
	public PathFinder(Trace trace ) {
		this.trace = trace;
	}
	
	public ActionPath findPath_dijstra(final TraceNode startNode, final TraceNode endNode) {
		TraceDijstraAlgorithm algorithm = new TraceDijstraAlgorithm(startNode, endNode, this.trace);
		return algorithm.findShortestPath();
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
	
//	public UserFeedback giveFeedback(final TraceNode node) {
//		/*
//		 * Compare the probability of control and data correctness
//		 * Do the slicing based on the lowest one
//		 */
//		
//		UserFeedback feedback = new UserFeedback();
//		
//		TraceNode controlDom = node.getControlDominator();
//		double controlProb = 2.0;
//		if (controlDom != null) {
//			VarValue conditionResult = controlDom.getConditionResult();
//			
//			// If the condition result is confirmed to be wrong,
//			// then give feedback directly
//			if (this.wrongVars.contains(conditionResult)) {
//				feedback.setFeedbackType(UserFeedback.WRONG_PATH);
//				return feedback; 
//			}
//			
//			// Ignore if the condition result confirmed to be correct
//			if (!this.correctVars.contains(conditionResult)) {
//				controlProb = controlDom.getConditionResult().getProbability();
//			} 
//		}
//		
//		double minReadProb = 2.0;
//		VarValue wrongVar = null;
//		for (VarValue readVar : node.getReadVariables()) {
//			
//			// If the readVar is confirmed to be wrong,
//			// then give feedback directly
//			if (this.wrongVars.contains(readVar)) {
//				feedback.setFeedbackType(UserFeedback.WRONG_VARIABLE_VALUE);
//				feedback.setOption(new ChosenVariableOption(readVar, null));
//				return feedback;
//			}
//			
//			// If the readVar is This variable, then ignore
//			if (readVar.isThisVariable()) {
//				continue;
//			}
//			
//			// If the readVar is confirmed to be correct, then ignore
//			if (!this.correctVars.contains(readVar)) {
//				double prob = readVar.getProbability();
//				if (prob < minReadProb) {
//					minReadProb = prob;
//					wrongVar = readVar;
//				}
//			}
//		}
//		
//		// There are no controlDom and readVar
//		if (controlProb == 2.0 && minReadProb == 2.0) {
//			feedback.setFeedbackType(UserFeedback.UNCLEAR);
//			return feedback;
//		}
//		
//		if (controlProb <= minReadProb) {
//			feedback.setFeedbackType(UserFeedback.WRONG_PATH);
//		} else {
//			feedback.setFeedbackType(UserFeedback.WRONG_VARIABLE_VALUE);
//			feedback.setOption(new ChosenVariableOption(wrongVar, null));
//		}
//		
//		return feedback;
//	}
	
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
