//package microbat.probability.SPP.pathfinding;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Queue;
//import java.util.Set;
//
//import debuginfo.NodeFeedbackPair;
//import debuginfo.NodeFeedbacksPair;
//import microbat.model.trace.Trace;
//import microbat.model.trace.TraceNode;
//import microbat.model.value.VarValue;
//import microbat.recommendation.ChosenVariableOption;
//import microbat.recommendation.UserFeedback;
//import microbat.util.TraceUtil;
//
//public class TraceDijstraAlgorithm {
//	
//	final TraceNode startNode;
//	final TraceNode endNode;
//	final Trace trace;
//	final Collection<TraceNode> croppedRegion;
//	
//	final Set<DijstraNode> graph = new HashSet<>();
//	final Map<DijstraNode, TraceNode> mapping = new HashMap<>();
//	
//	public TraceDijstraAlgorithm(TraceNode startNode, TraceNode endNode, Trace trace) {
//		this.startNode = startNode;
//		this.endNode = endNode;
//		this.trace = trace;
//		this.croppedRegion = TraceUtil.cropTrace(endNode, trace);
//	}
//	
//	public ActionPath findShortestPath() {
//		this.init();
//		this.runAlgorithm();
//		
//		List<NodeFeedbacksPair> actions = new ArrayList<>();
//		TraceNode currentNode = endNode;
//		actions.add(new NodeFeedbacksPair(this.endNode, new UserFeedback(UserFeedback.ROOTCAUSE)));
//		while (!currentNode.equals(startNode)) {
//			NodeFeedbacksPair action = currentNode.getPrevAction();
//			actions.add(action);
//			currentNode = action.getNode();
//		}
//		Collections.reverse(actions);
//		
// 		return new ActionPath(actions);
//	}
//	
//	private void runAlgorithm() {
//		DijstraNode currentNode = this.getNextNode();
//		while (currentNode != null) {
//			currentNode.setVisisted(true);
//			
//			final TraceNode node = currentNode.getTraceNode();
//			
//			final TraceNode controlDom = node.getControlDominator();
//			if (controlDom != null) {
//				if (this.isInsideRegion(controlDom) && this.graph.contains(controlDom)) {
//					DijstraNode dNode = (DijstraNode) controlDom;
//					VarValue conditionResult = controlDom.getConditionResult();
//					final double distance = currentNode.getDistance() + this.calProb(conditionResult);
//					if (distance < dNode.getDistance()) {
//						dNode.setDistance(distance);
//						UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_PATH);
//						dNode.setPrevAction(new NodeFeedbacksPair(node, feedback));
//					}
//				}
//			}
//			
//			for (VarValue readVar : node.getReadVariables()) {
//				TraceNode dataDom = this.trace.findDataDependency(node, readVar);
//				if (this.isInsideRegion(dataDom) && this.graph.contains(dataDom)) {
//					VarValue dataDomVar = null;
//					for (VarValue writtenVar : dataDom.getWrittenVariables()) {
//						if (writtenVar.id_equals(readVar)) {
//							dataDomVar = writtenVar;
//							break;
//						}
//					}
//					
//					DijstraNode dNode = (DijstraNode) dataDom;
//					final double distance = node.getDistance() + this.calProb(dataDomVar);
//					if (distance < dNode.getDistance()) {
//						dNode.setDistance(distance);
//						UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_VARIABLE_VALUE);
//						feedback.setOption(new ChosenVariableOption(readVar, null));
//						dNode.setPrevAction(new NodeFeedbacksPair(node, feedback));
//					}
//				}
//			}
//			
//			currentNode = this.getNextNode();
//		}
////		DijstraNode currentdNode = this.getNextNode();
////		while (currentdNode!=null) {
////			currentdNode.setVisisted(true);
////			
////			final TraceNode traceNode = this.getTraceNode(currentdNode);
////			
////			final TraceNode controlDom = traceNode.getControlDominator();
////			if (controlDom != null) {
////				if (this.isInsideRegion(traceNode)) {
////					DijstraNode dNode = controlDom.getConditionResult();
////					final double distance = currentdNode.getDistance() + this.calProb(controlDom.getConditionResult());
////					if (distance < dNode.getDistance()) {
////						dNode.setDistance(distance);
////						UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_PATH);
////						dNode.setPrevAction(new NodeFeedbackPair(controlDom, feedback));
////					}
////				}
////			}
////			
////			for (VarValue readVar : traceNode.getReadVariables()) {
////				TraceNode dataDom = this.trace.findDataDependency(traceNode, readVar);
////				if (this.isInsideRegion(dataDom)) {
////					VarValue dataDomVar = null;
////					for (VarValue writtenVar : dataDom.getWrittenVariables()) {
////						if (readVar.equals(writtenVar)) {
////							dataDomVar = writtenVar;
////							break;
////						}
////					}
////					
////					DijstraNode dNode = (DijstraNode) dataDomVar;
////					final double distance = currentdNode.getDistance() + this.calProb(dataDomVar);
////					if (distance < dNode.getDistance()) {
////						dNode.setDistance(distance);
////						UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_VARIABLE_VALUE);
////						feedback.setOption(new ChosenVariableOption(readVar, null));
////						dNode.setPrevAction(new NodeFeedbackPair(dataDom, feedback));
////					}
////				}
////			}
////			
////			currentdNode = this.getNextNode();
////		}
//	}
//	
//	private void init() {
//		
//		this.graph.clear();
//		
//		this.startNode.init(true);
//		
//		Queue<TraceNode> queue = new LinkedList<>();
//		queue.offer(this.startNode);
//		this.graph.add(this.startNode);
//
//		Set<TraceNode> visitedNodes = new HashSet<>();
//		visitedNodes.add(this.startNode);
//		
//		while(!queue.isEmpty()) {
//			TraceNode node = queue.poll();
//			visitedNodes.add(node);
//			
//			TraceNode controlDom = node.getControlDominator();
//			if (controlDom != null) {
//				if (this.isInsideRegion(controlDom)) {
//					DijstraNode controlDNode = (DijstraNode) controlDom;
//					controlDNode.init(false);
//					this.graph.add(controlDNode);
//					
//					if (!queue.contains(controlDom) && !visitedNodes.contains(controlDom)) {
//						queue.add(controlDom);
//					}
//				} 
//			}
//			
//			for (VarValue readVar : node.getReadVariables()) {
//				TraceNode dataDom = this.trace.findDataDependency(node, readVar);
//				if (this.isInsideRegion(dataDom)) {
//					DijstraNode dNode = (DijstraNode) dataDom;
//					dNode.init(false);
//					this.graph.add(dataDom);
//					
//					if (!queue.contains(dataDom) && !visitedNodes.contains(dataDom)) {
//						queue.add(dataDom);
//					}
//				}
//			}
//		} 
////		this.graph.clear();
////		
////		// Init starting node
////		for (DijstraNode node : startNode.getWrittenVariables()) {
////			node.init(true);
////			this.mapping.put(node, startNode);
////		}
////		
////		for (DijstraNode node : startNode.getReadVariables()) {
////			node.init(true);
////			this.mapping.put(node, startNode);
////		}
////		
////		Queue<TraceNode> queue = new LinkedList<>();
////		queue.offer(this.startNode);
////		
////		while(!queue.isEmpty()) {
////			TraceNode node = queue.poll();
////			
////			// Control Slicing
////			TraceNode controlDom = node.getControlDominator();
////			if (controlDom != null) {
////				if (this.isInsideRegion(controlDom)) {
////					DijstraNode dNode = controlDom.getConditionResult();
////					dNode.init(false);
////					
////					if (!queue.contains(controlDom)) {
////						queue.offer(controlDom);
////					}
////					
////					this.graph.add(dNode);
////					this.mapping.put(dNode, controlDom);
////				}
////			}
////			
////			// Data Slicing
////			for (VarValue readVar : node.getReadVariables()) {
////				DijstraNode dNode = (DijstraNode) readVar;
////				dNode.init(false);
////				this.graph.add(dNode);
////				this.mapping.put(dNode, node);
////				TraceNode dataDom = this.trace.findDataDependency(node, readVar);
////				if (this.isInsideRegion(dataDom)) {
////					
////					if(!queue.contains(dataDom)) {
////						queue.offer(dataDom);
////					}
////					
//////					this.mapping.put(dNode, dataDom);
////				}
////			}
//		
//	}
//	
//	private NodeFeedbackPair getActionTo(final TraceNode node) {
//		NodeFeedbackPair action = null;
////		double minDistance = Double.MAX_VALUE;
////		for (DijstraNode dNode : node.getWrittenVariables()) {
////			double distance = dNode.getDistance();
////			if (distance < minDistance) {
////				minDistance = distance;
////				action = dNode.getPrevAction();
////			}
////		}
//		return action;
//	}
//	
//	private boolean isInsideRegion(final TraceNode node) {
//		return this.croppedRegion.contains(node);
//	}
//	
//	private DijstraNode getNextNode() {
//		double minDistance = Double.MAX_VALUE;
//		DijstraNode nextNode = null;
//		for (DijstraNode node : this.graph) {
//			final double distance = node.getDistance();
//			if (distance < minDistance && !node.isVisited()) {
//				nextNode = node;
//				minDistance = distance;
//			}
//		}
//		return nextNode;
//	}
//	
//	private TraceNode getTraceNode(DijstraNode dNode) {
//		return this.mapping.get(dNode);
//	}
//	
//	private double calProb(VarValue var) {
//		return (var.getForwardProb() + var.getBackwardProb()) / 2;
//	}
//}
