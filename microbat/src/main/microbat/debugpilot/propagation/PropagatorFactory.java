package microbat.debugpilot.propagation;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import debuginfo.NodeFeedbacksPair;
import microbat.debugpilot.propagation.BP.PropInfer;
import microbat.debugpilot.propagation.spp.SPPH;
import microbat.debugpilot.propagation.spp.SPPRL;
import microbat.debugpilot.propagation.spp.SPPRLTrain;
import microbat.debugpilot.propagation.spp.SPPRandom;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class PropagatorFactory {
	
	private PropagatorFactory() {}
	
	public static ProbabilityPropagator getPropagator(final PropagatorType type, Trace trace, List<TraceNode> slicedTrace, Set<VarValue> correctVars, Set<VarValue> wrongVars,
			Collection<NodeFeedbacksPair> feedbackRecords) {
		switch(type) {
		case Heuristic_Cost:
			return new SPPH(trace, slicedTrace, correctVars, wrongVars, feedbackRecords);
		case ProfInfer:
			return new PropInfer(trace, slicedTrace, correctVars, wrongVars, feedbackRecords);
		case Heuristic_Random:
			return new SPPRandom(trace, slicedTrace, correctVars, wrongVars, feedbackRecords);
		case RL:
			return new SPPRL(trace, slicedTrace, correctVars, wrongVars, feedbackRecords);
		case RL_TRAIN:
			return new SPPRLTrain(trace, slicedTrace, correctVars, wrongVars, feedbackRecords);
		case None:
			return new EmptyPropagator();
		default:
			// Should not enter here
			throw new RuntimeException(Log.genMsg(PropagatorFactory.class, "Undefined propagator type: " + type));
		}
	}
}
