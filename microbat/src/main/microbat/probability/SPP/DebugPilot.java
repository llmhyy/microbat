package microbat.probability.SPP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.probability.PropProbability;
import microbat.probability.SPP.pathfinding.ActionPath;
import microbat.probability.SPP.pathfinding.PathFinder;
import microbat.probability.SPP.propagation.ProbabilityPropagator;
import microbat.probability.SPP.propagation.PropInfer;
import microbat.probability.SPP.propagation.SPP;
import microbat.recommendation.ChosenVariableOption;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;

/**
 * StepwisePropagator is used to propagate
 * the probability of correctness
 * 
 * @author David
 *
 */
public class DebugPilot {
	
	/**
	 * Execution trace of target program
	 */
	private final Trace trace;
	
	/**
	 * List of input variables which assumed to be correct
	 */
	private Set<VarValue> correctVars = new HashSet<>();
	
	/**
	 * List of outputs variables which assumed to be wrong
	 */
	private Set<VarValue> wrongVars = new HashSet<>();
	
	/**
	 * List of executed trace node after dynamic slicing
	 */
	private List<TraceNode> slicedTrace = null;

	private Collection<NodeFeedbacksPair> feedbackRecords = new ArrayList<>();
	
	private final TraceNode outputNode;
	
	private TraceNode rootCause = null;
	
	private ActionPath path = null;
	
	private final boolean useBaseline;
	
	/**
	 * Constructor
	 * @param trace Execution trace for target program
	 */
	public DebugPilot(Trace trace) {
		this(trace, false);
	}
	
	public DebugPilot(final Trace trace, final boolean useBaseline) {
		this.trace = trace;
		this.outputNode = null;
		this.useBaseline = useBaseline;
	}
	
	/**
	 * Constructor
	 * @param trace Execution trace for target program
	 * @param inputs Input variables which assumed to be correct
	 * @param outputs Output variables which assumed to be wrong
	 */
	public DebugPilot(Trace trace, List<VarValue> inputs, List<VarValue> outputs, TraceNode outputNode) {
		this(trace, inputs, outputs, outputNode, false);
	}
	
	public DebugPilot(Trace trace, List<VarValue> inputs, List<VarValue> outputs, TraceNode outputNode, final boolean useBaseline) {
		this.trace = trace;
		this.correctVars.addAll(inputs);
		this.wrongVars.addAll(outputs);
		this.slicedTrace = TraceUtil.dyanmicSlice(trace, outputNode);
		this.outputNode = outputNode;
		this.useBaseline = useBaseline;
	}
	
	
	public void addCorrectVar(VarValue correctVar) {
		this.correctVars.add(correctVar);
	}
	
	public void addWrongVar(VarValue wrongVar) {
		this.wrongVars.add(wrongVar);
	}
	/**
	 * Add input variables
	 * @param inputs Input variables
	 */
	public void addCorrectVars(Collection<VarValue> inputs) {
		this.correctVars.addAll(inputs);
	}
	
	/**
	 * Add output variables
	 * @param outputs Output variables
	 */
	public void addWrongVars(Collection<VarValue> outputs) {
		this.wrongVars.addAll(outputs);
	}
	
	public void propagate() {
		ProbabilityPropagator propagator = null;
		if (this.useBaseline) {
			propagator = new PropInfer(this.trace, this.slicedTrace, this.correctVars, this.wrongVars, this.feedbackRecords);
		} else {
			propagator = new SPP(this.trace, this.slicedTrace, this.correctVars, this.wrongVars, this.feedbackRecords);
		}
		propagator.propagate();
	}
	
	public UserFeedback giveFeedback(final TraceNode node) {
		for (NodeFeedbacksPair action : this.path) {
			if (action.getNode().equals(node)) {
				return action.getFirstFeedback();
			}
		}
		DebugPilot.printMsg("This node is not contained in path");
		throw new RuntimeException("Given node is not in the path");
	}
	
	public void locateRootCause(final TraceNode currentNode) {
		this.rootCause = this.proposeRootCause(currentNode);
		if (this.rootCause == null) {
			DebugPilot.printMsg("Cannot locate root cause");
		} else {
			DebugPilot.printMsg("Proposed root cause: " + this.rootCause.getOrder());
		}
	}
	
	public void constructPath() {
		ActionPath mustFollowPath = new ActionPath(this.feedbackRecords);
		this.path = this.suggestPath(this.outputNode, this.rootCause, mustFollowPath);
		if (this.path == null) {
			DebugPilot.printMsg("Failed to construct the path ...");
			throw new RuntimeException("Failed to construct the path ...");
		} else {
			DebugPilot.printMsg("Suggested path ...");
			for (NodeFeedbacksPair pair : this.path) {
				DebugPilot.printMsg(pair.toString());
			}
		}
	}
	
	public ActionPath getPath() {
		return this.path;
	}
	
	public ActionPath suggestPath(final TraceNode startNode, final TraceNode endNode) {
		if (startNode.getOrder() < endNode.getOrder()) {
			UserFeedback feedback = this.giveGreedyFeedback(endNode);
			NodeFeedbacksPair pair = new NodeFeedbacksPair(startNode, feedback);
			return new ActionPath(pair);
		}
		PathFinder finder = new PathFinder(this.trace, this.slicedTrace);
		System.out.println("Find path by greedy ...");
		ActionPath path = finder.findPathway_greedy(startNode, endNode);
		if (path == null) {
			System.out.println("Find path by Dijstra ...");
			path = finder.findPath_dijstra(startNode, endNode);
		}
		return path;
	}
	
	public ActionPath suggestPath(final TraceNode startNode, final TraceNode endNode, final ActionPath mustFollowPath) {
		if (startNode.getOrder() < endNode.getOrder()) {
			System.out.println("Fail to propose a valid root cause, Now give feedback based on probability");
			NodeFeedbacksPair latestAction = mustFollowPath.peek();
			TraceNode latestNode = TraceUtil.findNextNode(latestAction.getNode(), latestAction.getFirstFeedback(), trace);
			ActionPath path = new ActionPath(mustFollowPath);
			UserFeedback feedback =  this.giveGreedyFeedback(latestNode);
			NodeFeedbacksPair pair = new NodeFeedbacksPair(latestNode, feedback);
			path.addPair(pair);
			return path;
		}
		
		// If there are no user path provided, the find path from the error node
		if (mustFollowPath == null || mustFollowPath.isEmpty()) {
			return this.suggestPath(startNode, endNode);
		}
		
		// If mustFollowPath is provided,
		// then find path starting from last node of the user path
		NodeFeedbacksPair latestAction = mustFollowPath.peek();
		TraceNode latestNode = TraceUtil.findNextNode(latestAction.getNode(), latestAction.getFirstFeedback(), trace);
		if (latestNode == null) {
			throw new RuntimeException("[SPP] There are invalid next node based on the feedback");
		}
		
		ActionPath consecutive_path = this.suggestPath(latestNode, endNode);
		if (consecutive_path == null) {
			// Fail to construct path, give feedback directly based on greedy approach
			ActionPath path = new ActionPath(mustFollowPath);
			UserFeedback feedback =  this.giveGreedyFeedback(latestNode);
			NodeFeedbacksPair pair = new NodeFeedbacksPair(latestNode, feedback);
			path.addPair(pair);
			return path;
		}
		
		// Concatenate two path together
		ActionPath path = ActionPath.concat(mustFollowPath, consecutive_path);
		return path;
	}
	
	/**
	 * Propose the root cause node. <br><br>
	 * 
	 * It will compare the drop of correctness
	 * probability from read variables to the
	 * written variables. <br><br>
	 * 
	 * The one with the maximum drop will be the
	 * root cause. <br><br>
	 * 
	 * @return Root cause node
	 */
	public TraceNode proposeRootCause(final TraceNode currentNode) {
		TraceNode rootCause = null;
		double maxDrop = -1.0;
		for (TraceNode node : this.slicedTrace) {
			
			if (this.isFeedbackGiven(node) || this.outputNode.equals(node) || node.getOrder() > currentNode.getOrder()) {
				continue;
			}
			
			List<VarValue> readVars = node.getReadVariables().stream().filter(var -> !var.isThisVariable()).collect(Collectors.toList());
			List<VarValue> writtenVars = node.getWrittenVariables().stream().filter(var -> !var .isThisVariable()).collect(Collectors.toList());
			
			/*
			 * We need to handle:
			 * 1. Node without any variable
			 * 2. Node with only control dominator
			 * 3. Node with only read variables
			 * 4. Node with only written variables
			 * 5. Node with written variable and control dominator
			 * 6. Node with both read and written variables
			 * 
			 * It will ignore the node that already have feedback
			 */
			double drop = 0.0;
			if (writtenVars.isEmpty() && readVars.isEmpty() && node.getControlDominator() == null) {
				// Case 1
				continue;
			} else if (writtenVars.isEmpty() && readVars.isEmpty() && node.getControlDominator() != null) {
				// Case 2
				drop = PropProbability.UNCERTAIN - node.getControlDominator().getConditionResult().getProbability();
			} else if (writtenVars.isEmpty()) {
				// Case 3
				double prob = readVars.stream().mapToDouble(var -> var.getProbability()).average().orElse(0.5);
				drop = PropProbability.UNCERTAIN - prob;
			} else if (readVars.isEmpty()) {
				// Case 4
				double prob = writtenVars.stream().mapToDouble(var -> var.getProbability()).average().orElse(0.5);
				drop = PropProbability.UNCERTAIN - prob;
			} else if (!writtenVars.isEmpty() && readVars.isEmpty() && node.getControlDominator() != null){
				// Case 5
				drop = PropProbability.UNCERTAIN - node.getControlDominator().getConditionResult().getProbability();
			} else {
				// Case 6
				double readProb = readVars.stream().mapToDouble(var -> var.getProbability()).min().orElse(0.5);
				double writtenProb = writtenVars.stream().mapToDouble(var -> var.getProbability()).min().orElse(0.5);
				drop = readProb - writtenProb;
			}
			
			node.setDrop(drop);
			if (drop < 0) {
				// Case that the read variable is wrong but the written variable is correct
				// Ignore it by now
//				System.out.println("Warning: Trace node " + node.getOrder() + " has negative drop");
				continue;
			} else {
				if (drop > maxDrop) {
					maxDrop = drop;
					rootCause = node;
				}
			}
		}
		return rootCause;
	}
	
	public void updateFeedbacks(Collection<NodeFeedbacksPair> feedbacks) {
		this.feedbackRecords.clear();
		this.feedbackRecords.addAll(feedbacks);
	}
	
//	public UserFeedback giveFeedback(final TraceNode node) {
//		PathFinder finder = new PathFinder(this.trace, this.slicedTrace);
//		return finder.giveFeedback(node);
//	}
	
	private boolean isFeedbackGiven(final TraceNode node) {
		for (NodeFeedbacksPair pair : this.feedbackRecords) {
			if (node.equals(pair.getNode())) {
				return true;
			}
		}
		return false;
	}
	
	public static void printMsg(final String message) {
		System.out.println("[SPP] " + message);
	}
	
	private UserFeedback giveGreedyFeedback(final TraceNode node) {
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
}
