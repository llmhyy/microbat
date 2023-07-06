package microbat.probability.SPP.propagation;

import java.util.Collection;

import debuginfo.NodeFeedbacksPair;

public interface ProbabilityPropagator {
	public void propagate();
	public void updateFeedbacks(final Collection<NodeFeedbacksPair> pairs);
}
