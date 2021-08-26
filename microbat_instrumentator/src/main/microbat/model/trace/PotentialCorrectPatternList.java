package microbat.model.trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.model.trace.PathInstance.SourceLine;
import microbat.model.value.VarValue;
import microbat.model.variable.Variable;
import microbat.util.Settings;
import microbat.util.VariableUtil;

public class PotentialCorrectPatternList {
	
	private Map<String, PotentialCorrectPattern> patterns = new HashMap<>();

	public Map<String, PotentialCorrectPattern> getPatterns() {
		return patterns;
	}

	public void setPatterns(Map<String, PotentialCorrectPattern> patterns) {
		this.patterns = patterns;
	}

	public void addPathForPattern(PathInstance path){
		
		if(PotentialCorrectPattern.checkValidLabelInstance(path)){
			String pathKey = path.getPatternKey();
			
			PotentialCorrectPattern pattern = patterns.get(pathKey);
			if(pattern == null){
				pattern = new PotentialCorrectPattern();
				patterns.put(pathKey, pattern);
			}
			
			pattern.addPathInstance(path);
		}
		
	}

	public boolean containsPattern(PathInstance path) {
		return patterns.containsKey(path.getPatternKey());
	}

	public PotentialCorrectPattern getPattern(PathInstance path) {
		return patterns.get(path.getPatternKey());
	}

	public void clear() {
		this.patterns.clear();
	}

	public List<PathInstance> findSimilarIterationPath(PathInstance path) {
		List<PathInstance> list = new ArrayList<>();
		for(PotentialCorrectPattern pattern: this.patterns.values()){
			PathInstance labelPath = pattern.getLabelInstance();
			
			SourceLine patternStartLine = path.new SourceLine(labelPath.getStartNode().getClassCanonicalName(), 
					labelPath.getStartNode().getLineNumber(), labelPath.getStartNode().isLoopCondition());
			SourceLine patternEndLine = path.new SourceLine(labelPath.getEndNode().getClassCanonicalName(), 
					labelPath.getEndNode().getLineNumber(), labelPath.getEndNode().isLoopCondition());
			
			SourceLine pathStartLine = path.new SourceLine(path.getStartNode().getClassCanonicalName(), 
					path.getStartNode().getLineNumber(), path.getStartNode().isLoopCondition());
			SourceLine pathEndLine = path.new SourceLine(path.getEndNode().getClassCanonicalName(), 
					path.getEndNode().getLineNumber(), path.getEndNode().isLoopCondition());
			
			if(patternStartLine.equals(pathStartLine) &&
					patternEndLine.equals(pathEndLine)){
				list.add(labelPath);
			}
		}
		
		return list;
	}

	public List<PotentialCorrectPattern> findPatternsHavingSameEndNodeWith(SourceLine line) {
		List<PotentialCorrectPattern> patterns = new ArrayList<>();
		for(PotentialCorrectPattern pattern: this.patterns.values()){
			PathInstance labelPath = pattern.getLabelInstance();
			TraceNode node = labelPath.getEndNode();
			SourceLine endLine = new PathInstance().new SourceLine(node.getClassCanonicalName(), 
					node.getLineNumber(), node.isLoopCondition());
			
			if(line.equals(endLine)){
				patterns.add(pattern);
			}
		}
		
		return patterns;
	}

	public PotentialCorrectPatternList clone(){
		PotentialCorrectPatternList clonedPatterns = new PotentialCorrectPatternList();
		
		for(String key: patterns.keySet()){
			PotentialCorrectPattern pattern = patterns.get(key);
			PotentialCorrectPattern clonedPattern = pattern.clone();
			
			clonedPatterns.getPatterns().put(key, clonedPattern);
		}
		
		return clonedPatterns;
	}

	/**
	 * Given existing suspicious node, I try to infer a possible wrong-value variable, 
	 * <code>var</code>, on this suspicious node. If there exists pattern which can cover 
	 * the path consisting of the data dominant node of suspicious node on <code>var</code>,
	 * Then the dominance node will be returned as the next suspicious node.<br>
	 * <br>
	 * Such an inference is based on abduction. A path covered by a pattern means that a 
	 * similar path has been feedbacked as non-buggy. Therefore, we skip such kind of path
	 * to speed up jumping on the trace. 
	 * 
	 * @param existingSusiciousNode
	 * @return
	 */
	public TraceNode inferNextSuspiciousNode(TraceNode existingSusiciousNode) {
		SourceLine line = new PathInstance().new SourceLine(existingSusiciousNode.getClassCanonicalName(), 
				existingSusiciousNode.getLineNumber(), existingSusiciousNode.isLoopCondition());
		List<PotentialCorrectPattern> patterns = findPatternsHavingSameEndNodeWith(line);
		
		for(PotentialCorrectPattern pattern: patterns){
			PathInstance pathInstance = pattern.getLabelInstance();
			Variable causingVar = pathInstance.findCausingVar();
			
			TraceNode dataDominator = findDataDominatorOnCertainVariable(existingSusiciousNode, causingVar);
			
			if(dataDominator != null && 
					pathInstance.getStartNode().getLineNumber()==dataDominator.getLineNumber()){
				
				PathInstance newIns = new PathInstance(dataDominator, existingSusiciousNode);
				if(Settings.potentialCorrectPatterns.containsPattern(newIns)){
					return dataDominator;					
				}
			}
		}
		
		return null;
	}
	
	private TraceNode findDataDominatorOnCertainVariable(TraceNode existingSusiciousNode, Variable causingVar){
		for(TraceNode dominator: existingSusiciousNode.getDataDominators().keySet()){
			for(VarValue writtenVar: dominator.getWrittenVariables()){
				Variable writtenVariable = writtenVar.getVariable();
				
				if(VariableUtil.isEquivalentVariable(causingVar, writtenVariable)){
					return dominator;					
				}
			}
		}
		
		return null;
	}
}
