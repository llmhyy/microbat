package microbat.debugpilot.propagation.spp;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class SPPRandom extends SPP {

	public SPPRandom(Trace trace, List<TraceNode> slicedTrace, Set<VarValue> correctVars, Set<VarValue> wrongVars,
			Collection<NodeFeedbacksPair> feedbackRecords) {
		super(trace, slicedTrace, correctVars, wrongVars, feedbackRecords);
	}

	@Override
	protected double calForwardFactor(TraceNode node) {
		node.reason = StepExplaination.RANDOM;
		return Math.random();
	}

	@Override
	protected double calBackwardFactor(VarValue var, TraceNode node) {
		node.reason = StepExplaination.RANDOM;
		if (!this.isComputational(node)) {
			return 1.0d;
		} else {
			return Math.random();
		}
	}

}
