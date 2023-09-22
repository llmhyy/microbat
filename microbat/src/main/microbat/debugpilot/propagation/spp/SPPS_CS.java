package microbat.debugpilot.propagation.spp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import microbat.debugpilot.NodeFeedbacksPair;
import microbat.debugpilot.settings.PropagatorSettings;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;

public class SPPS_CS extends SPPS_C {

	protected final Map<BreakPoint, Integer> breakPointCFScore = new HashMap<>();
	protected final Map<BreakPoint, Integer> breakPointCSScore = new HashMap<>();
	protected final Map<BreakPoint, Integer> breakPointNFScore = new HashMap<>();
	
	protected final double eps = 1e-10;
	
	public SPPS_CS(final PropagatorSettings settings) {
		this(settings.getTrace(), settings.getSlicedTrace(), settings.getWrongVars(), settings.getFeedbacks());
	}
	
	public SPPS_CS(final Trace trace, final List<TraceNode> sliceTraceNodes, final Collection<VarValue> wrongVars, final Collection<NodeFeedbacksPair> feedbacksPair) {
		super(trace, sliceTraceNodes, wrongVars, feedbacksPair);
	}
	
	@Override
	public void propagate() {
		this.initConfirmed();
		this.initSuspiciousScore();
		this.calComputationalSuspiciousScore();
		this.calSpectrumSuspiciousScore();
		this.calSuspiciousScoreVariable();
		this.normalizeVariableSuspicious();
	}
	
	protected void calSpectrumSuspiciousScore() {
		this.updateBreakPointScore();
		this.slicedTrace.stream().forEach(node -> {
			node.computationCost += this.calBreakPointScore(node.getBreakPoint());
		});
	}
	
	protected void updateBreakPointScore() {
		this.breakPointCFScore.clear();
		this.breakPointCSScore.clear();
		this.breakPointNFScore.clear();
		
		Set<TraceNode> nodeCoveredByWrongFeedback = new HashSet<>();
		for (NodeFeedbacksPair feedbackPair : this.feedbackRecords) {
			if (feedbackPair.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE) || feedbackPair.getFeedbackType().equals(UserFeedback.CORRECT_VARIABLE_VALUE)) {
				final TraceNode node = feedbackPair.getNode();
				for (VarValue readVar : node.getReadVariables()) {
					final TraceNode dataDominator = this.trace.findDataDependency(node, readVar);
					if (dataDominator == null) {
						continue;
					}
					
					List<TraceNode> relatedNodes = TraceUtil.dynamicSlice(trace, dataDominator);
					nodeCoveredByWrongFeedback.addAll(relatedNodes);
					List<BreakPoint> breakPoints =  relatedNodes.stream().map(relateNode -> relateNode.getBreakPoint()).distinct().toList();
					if (feedbackPair.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
						for (BreakPoint breakPoint : breakPoints) {
							this.breakPointCFScore.compute(breakPoint, (k, v) -> (v == null) ? 1 : v + 1);
						}						
					} else {
						for (BreakPoint breakPoint : breakPoints) {
							this.breakPointCSScore.compute(breakPoint, (k, v) -> (v == null) ? 1 : v + 1);
						}
					}
				}
				
				final TraceNode controlDom = node.getControlDominator();
				if (controlDom != null) {
					List<TraceNode> relatedNodes = TraceUtil.dynamicSlice(trace, controlDom);
					List<BreakPoint> breakPoints =  relatedNodes.stream().map(relateNode -> relateNode.getBreakPoint()).distinct().toList();
					for (BreakPoint breakPoint : breakPoints) {
						this.breakPointCSScore.compute(breakPoint, (k, v) -> (v == null) ? 1 : v + 1);
					}
				}
			} else if (feedbackPair.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
				final TraceNode controlDominator = feedbackPair.getNode().getControlDominator();
				if (controlDominator != null) {
					List<TraceNode> relatedNodes = TraceUtil.dynamicSlice(trace, controlDominator);
					nodeCoveredByWrongFeedback.addAll(relatedNodes);
					List<BreakPoint> breakPoints =  relatedNodes.stream().map(relateNode -> relateNode.getBreakPoint()).distinct().toList();
					for (BreakPoint breakPoint : breakPoints) {
						this.breakPointCFScore.compute(breakPoint, (k, v) -> (v == null) ? 1 : v + 1);
					}
				}
			}
		}
		
		Set<TraceNode> nodeUnCoveredByWrongFeedback = new HashSet<>();
		nodeUnCoveredByWrongFeedback.addAll(this.slicedTrace);
		nodeUnCoveredByWrongFeedback.removeAll(nodeCoveredByWrongFeedback);
		
		List<BreakPoint> breakPoints =  nodeUnCoveredByWrongFeedback.stream().map(relateNode -> relateNode.getBreakPoint()).distinct().toList();
		for (BreakPoint breakPoint : breakPoints) {
			this.breakPointNFScore.compute(breakPoint, (k, v) -> (v == null) ? 1 : v + 1);
		}
	}
	
	protected double calBreakPointScore(final BreakPoint breakPoint) {
		final int cf = this.breakPointCFScore.getOrDefault(breakPoint, 0);
		final int cs = this.breakPointCSScore.getOrDefault(breakPoint, 0);
		final int nf = this.breakPointNFScore.getOrDefault(breakPoint, 0);
		
		return cf / ((double) cf + cs + nf + this.eps);
	}

}
