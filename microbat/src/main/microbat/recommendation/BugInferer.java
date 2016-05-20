package microbat.recommendation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import microbat.model.trace.PathInstance;
import microbat.model.trace.PathInstance.SourceLine;
import microbat.model.trace.PotentialCorrectPattern;
import microbat.model.trace.PotentialCorrectPatternList;
import microbat.model.trace.TraceNode;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;

/**
 * This class is used to infer bug type if the bug is found based on loop inference.
 * @author "linyun"
 *
 */
public class BugInferer {
	public Bug infer(TraceNode currentNode, StepRecommender recommender){
		Bug bug;
		
		TraceNode startNode = recommender.getLoopRange().findCorrespondingStartNode(currentNode);
		//Branch bug
		if(startNode == null){
			startNode = findIterationHead(currentNode, Settings.potentialCorrectPatterns, recommender);
			if(startNode != null){
				PathInstance path = new PathInstance(startNode, currentNode);
				List<PathInstance> labelPaths = Settings.potentialCorrectPatterns.findSimilarIterationPath(path);
				List<TraceNode> diffNodes = computePathDiff(path, labelPaths);
				bug = new BranchMistakeBug(diffNodes);
			}
			else{
				bug = new UnknownBug();
			}
		}
		//Missing corner case bug
		else{
			PathInstance path = new PathInstance(startNode, currentNode);
			PotentialCorrectPattern pattern = Settings.potentialCorrectPatterns.getPattern(path);
			
			bug = new MissingCornerCaseBug(currentNode.getReadVariables(), pattern);
		}
		
		return bug;
	}

	private List<TraceNode> computePathDiff(PathInstance path, List<PathInstance> labelPaths) {
		List<TraceNode> list = new ArrayList<>();
		for(PathInstance labelPath: labelPaths){
			List<TraceNode> pathDiff = pathDiff(path, labelPath);
			
			for(TraceNode node: pathDiff){
				if(!list.contains(node)){
					list.add(node);
				}
			}
		}
		
		return list;
	}

	@SuppressWarnings("unchecked")
	private List<TraceNode> pathDiff(PathInstance path, PathInstance labelPath) {
		SourceLine[] lines = path.getLineTrace().toArray(new SourceLine[0]);
		SourceLine[] labelLines = labelPath.getLineTrace().toArray(new SourceLine[0]);
		
		Object[] commonLines = MicroBatUtil.generateCommonNodeList(lines, labelLines);
		ArrayList<Object> commonList = new ArrayList<>();
		for(Object obj: commonLines){
			commonList.add(obj);
		}
		
		ArrayList<SourceLine> clonedLines = (ArrayList<SourceLine>) path.getLineTrace().clone();
		Iterator<SourceLine> iter = clonedLines.iterator();
		while(iter.hasNext()){
			SourceLine line = iter.next();
			if(commonList.contains(line)){
				iter.remove();
				commonList.remove(line);
			}
		}
		
		List<TraceNode> diffList = new ArrayList<>();
		TraceNode startNode = path.getStartNode();
		while(startNode.getOrder() <= path.getEndNode().getOrder()){
			SourceLine line = path.new SourceLine(startNode.getClassCanonicalName(), startNode.getLineNumber(),
					startNode.isLoopCondition());
			if(clonedLines.contains(line)){
				diffList.add(startNode);
			}
			startNode = startNode.getStepInNext();
		}
		
		return diffList;
	}

	private TraceNode findIterationHead(TraceNode currentNode, PotentialCorrectPatternList potentialCorrectPatterns,
			StepRecommender recommender) {
		SourceLine line = new PathInstance().new SourceLine(currentNode.getClassCanonicalName(), 
				currentNode.getLineNumber(), currentNode.isLoopCondition());
		List<PotentialCorrectPattern> patterns = potentialCorrectPatterns.findPatternsHavingSameEndNodeWith(line);
		for(PotentialCorrectPattern pattern: patterns){
			TraceNode startNode = recommender.findNextSuspiciousNodeByPattern(pattern, currentNode);
			if(startNode != null){
				return startNode;
			}
		}
		
		return null;
	}
}
