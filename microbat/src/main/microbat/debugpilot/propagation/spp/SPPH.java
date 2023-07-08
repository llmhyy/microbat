package microbat.debugpilot.propagation.spp;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class SPPH extends SPP {

	public SPPH(Trace trace, List<TraceNode> slicedTrace, Set<VarValue> correctVars, Set<VarValue> wrongVars,
			Collection<NodeFeedbacksPair> feedbackRecords) {
		super(trace, slicedTrace, correctVars, wrongVars, feedbackRecords);
	}

	@Override
	protected double calForwardFactor(final TraceNode node) {
		return 1 - node.computationCost;
	}
	
	@Override
	protected double calBackwardFactor(final VarValue var, final TraceNode node) {
		final double totalCost = node.getReadVariables().stream().mapToDouble(readVar -> readVar.computationalCost).sum();
		if (totalCost == 0) {
			return (1 - node.computationCost) * (1 / node.getReadVariables().size());
		} else {
			return (1 - node.computationCost) * (var.computationalCost / totalCost);
		}
	}
}
