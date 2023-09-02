package microbat.debugpilot.propagation.spp;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import microbat.baseline.constraints.VariableConstraintA1Test;
import microbat.debugpilot.NodeFeedbacksPair;
import microbat.debugpilot.settings.DebugPilotSettings;
import microbat.debugpilot.settings.PropagatorSettings;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class SPPH extends SPP {

	public SPPH(final PropagatorSettings settings) {
		super(settings);
	}
	
	public SPPH(Trace trace, List<TraceNode> slicedTrace, Set<VarValue> wrongVars,
			Collection<NodeFeedbacksPair> feedbackRecords) {
		super(trace, slicedTrace, wrongVars, feedbackRecords);
	}

	@Override
	protected double calForwardFactor(final TraceNode node) {
		return 1 - node.computationCost;
	}
	
//	@Override
//	protected double calBackwardFactor(final VarValue var, final TraceNode node) {
//		node.reason = StepExplaination.COST;
//		return this.calHeuristicFactor(var, node);
//	}
//	
//	protected double calHeuristicFactor(final VarValue var, final TraceNode node) {
//		double totalCost = node.getReadVariables().stream().mapToDouble(readVar -> readVar.computationalCost).sum();
//		totalCost += node.getControlDominator() == null ? 0.0d : node.getControlDominator().getConditionResult().computationalCost;
//		double factor = 1 - node.computationCost;
//		if (totalCost == 0.0d) {
//			factor *= 0;
//		} else {
//			factor *= var.computationalCost / totalCost;
//		}
//		return factor;
//	}

	@Override
	protected double calBackwardFactor(final VarValue var, final TraceNode node) {
		node.reason = StepExplaination.COST;
		if (var.isConditionResult()) {
			return 1.0d;
		} else {
			return this.calHeuristicFactor(var, node);
		}
	}
	
	protected double calHeuristicFactor(final VarValue var, final TraceNode node) {
		double totalCost = node.getReadVariables().stream().mapToDouble(readVar -> readVar.computationalCost).sum();
		double factor = 1 - node.computationCost;
		if (totalCost == 0.0d) {
			factor *= 0;
		} else {
			factor *= var.computationalCost / totalCost;
		}
		return factor;
	}
}
