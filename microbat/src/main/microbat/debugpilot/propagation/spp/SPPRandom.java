package microbat.debugpilot.propagation.spp;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import debuginfo.NodeFeedbacksPair;
import microbat.debugpilot.settings.PropagatorSettings;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class SPPRandom extends SPP {

	public SPPRandom(PropagatorSettings settings) {
		super(settings);
	}
	
	public SPPRandom(Trace trace, List<TraceNode> slicedTrace, Set<VarValue> wrongVars,
			Collection<NodeFeedbacksPair> feedbackRecords) {
		super(trace, slicedTrace, wrongVars, feedbackRecords);
	}

	@Override
	protected double calForwardFactor(TraceNode node) {
		node.reason = StepExplaination.RANDOM;
		return Math.random();
	}

	@Override
	protected double calBackwardFactor(VarValue var, TraceNode node) {
		node.reason = StepExplaination.RANDOM;
		if (var.isConditionResult()) {
			return Math.random();
		} else if (!this.isComputational(node)) {
			return 1.0d;
		} else {
			return Math.random();
		}
	}
	
	@Override
	protected void calConditionBackwardFactor(final TraceNode node) {
		final TraceNode controlDom = node.getControlDominator();
		if (controlDom != null) {
			controlDom.getConditionResult().addBackwardProbability(Math.random());
		}
	}

}
