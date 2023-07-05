package microbat.probability.SPP.propagation;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class EmptyPropagator implements ProbabilityPropagator  {

	public EmptyPropagator() {

	}
	
	@Override
	public void propagate() {
	}

	@Override
	public void updateFeedbacks(Collection<NodeFeedbacksPair> pairs) {

	}

}
