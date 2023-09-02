package microbat.debugpilot.rootcausefinder;

import java.util.Collection;
import java.util.List;

import microbat.debugpilot.NodeFeedbacksPair;
import microbat.debugpilot.propagation.probability.PropProbability;
import microbat.debugpilot.settings.RootCauseLocatorSettings;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class SPPRootCauseLocator extends AbstractRootCauseLocator {

	public SPPRootCauseLocator(final RootCauseLocatorSettings settings) {
		this(settings.getSliceTrace(), settings.getFeedbacks(), settings.getOutputNode());
	}
	
	public SPPRootCauseLocator(List<TraceNode> sliceTrace, Collection<NodeFeedbacksPair> feedbacks, TraceNode outputNode) {
		super(sliceTrace, feedbacks, outputNode);
	}

	@Override
	public TraceNode locateRootCause() {
		TraceNode rootCause = null;
		double maxDrop = -1.0d;
		for (TraceNode node : this.slicedTrace) {
			if (this.isFeedbackGivenTo(node) || node.equals(outputNode)) continue;
			double drop = this.calDrop(node);
//			drop *= node.getControlDominator() == null ? 1.0d : node.getControlDominator().getConditionResult().getProbability();
			node.setDrop(maxDrop);
			if (drop < 0) continue;
			if (drop > maxDrop) {
				maxDrop = drop;
				rootCause = node;
			}
		}
		if (rootCause == null) rootCause = this.slicedTrace.get(0);
		return rootCause;
	}
	
	protected double calDrop(final TraceNode node) {
		/*
		 * We need to handle:
		 * 1. Node without any variable
		 * 2. Node with only control dominator
		 * 3. Node with only read variables
		 * 4. Node with only written variables
		 * 5. Node with written variable and control dominator
		 * 6. Node with both read and written variables
		 * 
		 * It will ignore the node that already have feedback
		 */
		List<VarValue> readVars = node.getReadVariables();
		List<VarValue> writtenVars = node.getWrittenVariables();
		double drop = 0.0;
		if (writtenVars.isEmpty() && readVars.isEmpty() && node.getControlDominator() == null) {
			// Case 1
			drop = 0.0;
		} else if (writtenVars.isEmpty() && readVars.isEmpty() && node.getControlDominator() != null) {
			// Case 2
			drop = PropProbability.UNCERTAIN - node.getControlDominator().getConditionResult().getProbability();
		} else if (writtenVars.isEmpty()) {
			// Case 3
			double prob = readVars.stream().mapToDouble(var -> var.getProbability()).average().orElse(0.5);
			drop = PropProbability.UNCERTAIN - prob;
		} else if (readVars.isEmpty()) {
			// Case 4
			double prob = writtenVars.stream().mapToDouble(var -> var.getProbability()).average().orElse(0.5);
			drop = PropProbability.UNCERTAIN - prob;
		} else if (!writtenVars.isEmpty() && readVars.isEmpty() && node.getControlDominator() != null){
			// Case 5
			drop = PropProbability.UNCERTAIN - node.getControlDominator().getConditionResult().getProbability();
		} else {
			// Case 6
			double readProb = readVars.stream().mapToDouble(var -> var.getProbability()).min().orElse(0.5);
			double writtenProb = writtenVars.stream().mapToDouble(var -> var.getProbability()).min().orElse(0.5);
			drop = readProb - writtenProb;
		}
		return drop;
	}

}
