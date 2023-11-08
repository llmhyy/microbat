/*
 * package microbat.debugpilot.pathfinding;
 * 
 * import java.util.Objects;
 * 
 * import microbat.debugpilot.NodeFeedbacksPair; import microbat.log.Log; import
 * microbat.model.trace.Trace; import microbat.model.trace.TraceNode; import
 * microbat.recommendation.UserFeedback; import microbat.util.TraceUtil;
 * 
 * public class FeedbackPathUtil {
 * 
 * public static FeedbackPath_ concat(final FeedbackPath_ path1, final
 * FeedbackPath_ path2) { if (path1 == null && path2 == null) return null; if
 * (path1 == null) return path2; if (path2 == null) return path1; if
 * (path1.isEmpty()) return path2; if (path2.isEmpty()) return path1;
 * 
 * FeedbackPath_ path = new FeedbackPath_(path1); TraceNode lastNode =
 * path1.getLastFeedback().getNode(); TraceNode nextNode =
 * path2.get(0).getNode(); if (lastNode.equals(nextNode)) { path.pop(); }
 * 
 * final Trace trace = lastNode.getTrace(); if
 * (path1.getLastFeedback().getFeedbacks().size() > 1) { final NodeFeedbacksPair
 * pair = path.pop(); final TraceNode node = pair.getNode(); NodeFeedbacksPair
 * newPair = null; for (UserFeedback feedback : pair.getFeedbacks()) { TraceNode
 * targetNextNode = TraceUtil.findNextNode(node, feedback, trace); if
 * (targetNextNode.equals(nextNode)) { newPair = new NodeFeedbacksPair(node,
 * feedback); break; } } if (newPair == null) { throw new
 * RuntimeException(Log.genMsg(FeedbackPath_.class,
 * "Concatinating two path that is not connected")); } path.addPair(newPair); }
 * 
 * for (NodeFeedbacksPair pair : path2) { path.addPair(pair); }
 * 
 * return path; }
 * 
 * 
 * public static FeedbackPath_ splicePathAtIndex(final FeedbackPath_ sourcePath,
 * final FeedbackPath_ splicePath, final int idx) {
 * Objects.requireNonNull(splicePath, Log.genMsg(FeedbackPathUtil.class,
 * "splicePath cannot be null")); if (sourcePath == null || sourcePath.isEmpty()
 * || idx==0) return splicePath; if (idx > sourcePath.getLength()) return
 * FeedbackPathUtil.concat(sourcePath, splicePath); FeedbackPath_ resultPath =
 * FeedbackPathUtil.concat(sourcePath.subPath(0, idx), splicePath); return
 * resultPath; }
 * 
 * public static boolean samePathBeforeNode(final FeedbackPath_ path1, final
 * FeedbackPath_ path2, final TraceNode targetNode) { if
 * (!path1.contains(targetNode) || !path2.contains(targetNode)) { throw new
 * IllegalArgumentException(Log.genMsg(FeedbackPathUtil.class,
 * "Given target node: " + targetNode.getOrder() +
 * " does not appear in both path")); }
 * 
 * final int minPathLength = Math.min(path1.getLength(), path2.getLength()); for
 * (int i=0; i<minPathLength; i++) { final NodeFeedbacksPair pair1 =
 * path1.get(i); final NodeFeedbacksPair pair2 = path2.get(i); if
 * (pair1.getNode().equals(targetNode) && pair2.getNode().equals(targetNode)) {
 * return true; // It is allowed to have different feedback on target node }
 * else if (!pair1.getNode().equals(targetNode) &&
 * !pair2.getNode().equals(targetNode)) { if (!pair1.equals(pair2)) { return
 * false; } } else { return false; } } throw new
 * RuntimeException(Log.genMsg(FeedbackPathUtil.class,
 * "Program should not execute this line of code")); }
 * 
 * }
 */