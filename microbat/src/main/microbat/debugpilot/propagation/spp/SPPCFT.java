package microbat.debugpilot.propagation.spp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.debugpilot.NodeFeedbacksPair;
import microbat.debugpilot.settings.PropagatorSettings;
import microbat.model.BreakPoint;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;

public class SPPCFT extends SPPCF {
	
	protected Map<BreakPoint, Integer> trustDegreeTable;
	
	public SPPCFT(PropagatorSettings settings) {
		super(settings);
		this.trustDegreeTable = new HashMap<>();
	}
	
	@Override
	public void propagate() {
		this.fuseFeedbacks();
		this.updateTrustDegree();
		this.calUntrustScore();
		this.backwardProp();
		this.combineProb();
	}
	
	protected void calUntrustScore() {
		final long totalNodeCost = this.slicedTrace.stream().mapToLong(node -> this.countModifyOperation(node)).sum();
		this.slicedTrace.stream().forEach(node -> node.computationCost = totalNodeCost == 0 ? 0 : this.countModifyOperation(node) / (double) totalNodeCost);
		this.slicedTrace.stream().forEach(node -> node.computationCost = node.computationCost / Math.exp(this.trustDegreeTable.get(node.getBreakPoint())));
		
		
		// Initialize all variable computational cost to 0
		this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).forEach(var -> var.computationalCost = 0.0d);
		this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.computationalCost = 0.0d);
		
		double maxVarCost = 0.0d;
		for (TraceNode node : this.slicedTrace) {
			node.confirmed = false;
			
			List<VarValue> readVars = node.getReadVariables();
			if (readVars.isEmpty()) {
				continue;
			}
			
			// Inherit computation cost from data dominator
			for (VarValue readVar : readVars) {
				final VarValue dataDomVar = this.findDataDomVar(readVar, node);
				if (dataDomVar != null) {
					readVar.computationalCost = dataDomVar.computationalCost;
				}
			}
			
			final double cumulatedCost = readVars.stream().mapToDouble(var -> var.computationalCost).sum();
			final double optCost = node.computationCost;
			final double cost = Double.isInfinite(cumulatedCost+optCost) ? Double.MAX_VALUE : cumulatedCost+optCost;
			
			node.getWrittenVariables().stream().forEach(var -> var.computationalCost = cost);
			maxVarCost = Math.max(cost,  maxVarCost);
		}
		
		// Normalize by maximum computational cost
		final double maxVarCost_ = maxVarCost;
		this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).forEach(var -> var.computationalCost = maxVarCost_ == 0 ? 0 : var.computationalCost / maxVarCost_);
		this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.computationalCost = maxVarCost_ == 0 ? 0 : var.computationalCost / maxVarCost_);
	
	}
	
	protected double calUntrustStepScore(final TraceNode node) {
		double n = this.countModifyOperation(node);
		double t = this.trustDegreeTable.get(node.getBreakPoint());
		return n / (t+1.0d);
	}
	
	protected void updateTrustDegree() {
		for (TraceNode node : this.slicedTrace) {
			this.trustDegreeTable.put(node.getBreakPoint(), 0);
		}
		
		// We assume that the feedback records only contain the wrong branch and wrong data feedback
		for (NodeFeedbacksPair feedbacksPair : this.feedbackRecords) {
			if (feedbacksPair.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
				final TraceNode node = feedbacksPair.getNode();
				final List<VarValue> wrongVars = feedbacksPair.getFeedbacks().stream().map(feedback -> feedback.getOption().getReadVar()).toList();
				for (VarValue readVar : node.getReadVariables()) {
					// We only care about the variable that is correct
					if (wrongVars.contains(readVar)) {
						continue;
					}
					
					// If there are no data dominator, there is no need to continue as well
					final TraceNode dataDominator = this.trace.findDataDependency(node, readVar);
					if (dataDominator == null) {
						continue;
					}
					
					List<TraceNode> trustedNodes = TraceUtil.dynamicSlice(trace, dataDominator);
					List<BreakPoint> trustedBreakPoints = trustedNodes.stream()
							.filter(trustedNode -> this.slicedTrace.contains(trustedNode))
							.map(trustedNode -> trustedNode.getBreakPoint())
							.distinct().toList();
					
					for (BreakPoint trustedBreakPoint : trustedBreakPoints) {
						final int degree = this.trustDegreeTable.get(trustedBreakPoint);
						this.trustDegreeTable.put(trustedBreakPoint, degree+1);
					}
				}
			}
		}
		
		this.trustDegreeTable.forEach((key, value) -> {
            System.out.println("Line No: " + key.getLineNumber() + ", Trusted degree: " + value);
        });
	}
}
