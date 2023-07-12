package microbat.debugpilot.pathfinding;

import debuginfo.NodeFeedbacksPair;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;

public class ActionPathUtil {
	
	public static ActionPath concat(final ActionPath path1, final ActionPath path2, final Trace trace) {
		if (path1 == null && path2 == null) return null;
		if (path1 == null) return path2;
		if (path2 == null) return path1;
		if (path1.isEmpty()) return path2;
		if (path2.isEmpty()) return path1;
		
		ActionPath path = new ActionPath(path1);
		TraceNode lastNode = path1.peek().getNode();
		TraceNode nextNode = path2.get(0).getNode();
		if (lastNode.equals(nextNode)) {
			path.pop();
		} 
		
		if (path1.peek().getFeedbacks().size() > 1) {
			final NodeFeedbacksPair pair = path1.pop();
			final TraceNode node = pair.getNode();
			NodeFeedbacksPair newPair = null;
			for (UserFeedback feedback : pair.getFeedbacks()) {
				TraceNode targetNextNode = TraceUtil.findNextNode(node, feedback, trace);
				if (targetNextNode.equals(nextNode)) {
					newPair = new NodeFeedbacksPair(node, feedback);
					break;
				}
			}
			if (newPair == null) {
				throw new RuntimeException(Log.genMsg(ActionPath.class, "Concatinating two path that is not connected"));
			}
			path.addPair(newPair);
		}
		
		for (NodeFeedbacksPair pair : path2) {
			path.addPair(pair);
		}
		
		return path;
	}
	
	public static boolean samePathBeforeNode(final ActionPath path1, final ActionPath path2, final TraceNode targetNode) {
		if (!path1.contains(targetNode) || !path2.contains(targetNode)) {
			throw new IllegalArgumentException(Log.genMsg(ActionPathUtil.class, "Given target node: " + targetNode.getOrder() + " does not appear in both path"));
		}
		
		final int minPathLength = Math.min(path1.getLength(), path2.getLength());
		for (int i=0; i<minPathLength; i++) {
			final NodeFeedbacksPair pair1 = path1.get(i);
			final NodeFeedbacksPair pair2 = path2.get(i);
			if (pair1.getNode().equals(targetNode) && pair2.getNode().equals(targetNode)) {
				return true; // It is allowed to have different feedback on target node
			} else if (!pair1.getNode().equals(targetNode) && !pair2.getNode().equals(targetNode)) {
				if (!pair1.equals(pair2)) {
					return false;
				}
			} else {
				return false;
			}
		}
		throw new RuntimeException(Log.genMsg(ActionPathUtil.class, "Program should not execute this line of code"));
	}
	
 }
