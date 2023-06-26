package microbat.probability.SPP.propagation;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class SPPRL extends SPP {

	public SPPRL(Trace trace, List<TraceNode> slicedTrace, Set<VarValue> correctVars, Set<VarValue> wrongVars,
			Collection<NodeFeedbacksPair> feedbackRecords) {
		super(trace, slicedTrace, correctVars, wrongVars, feedbackRecords);
	}
	
	@Override
	public void propagate() {
		this.fuseFeedbacks();
		this.initProb();
		this.forwardProp();
		this.backwardProp();
		this.combineProb();
	}
	
}
