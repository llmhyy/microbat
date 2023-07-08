package microbat.debugpilot.propagation.spp;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import debuginfo.NodeFeedbacksPair;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.pyserver.BackwardModelTrainClient;
import microbat.pyserver.ForwardModelTrainClient;
import sav.common.core.Pair;

public class SPPRLTrain extends SPP {
	
	protected final ForwardModelTrainClient forwardTrainClient;
	protected final BackwardModelTrainClient backwardTrainClient;
	
	public SPPRLTrain(Trace trace, List<TraceNode> slicedTrace, Set<VarValue> correctVars, Set<VarValue> wrongVars,
			Collection<NodeFeedbacksPair> feedbackRecords) {
		super(trace, slicedTrace, correctVars, wrongVars, feedbackRecords);
		this.forwardTrainClient = new ForwardModelTrainClient();
		this.backwardTrainClient = new BackwardModelTrainClient();
	}

	@Override
	protected void forwardProp() {
		try {
			this.forwardTrainClient.conntectServer();
			this.forwardTrainClient.notifyInferenceMode();
			this.forwardTrainClient.sendFeedbackVectors(this.feedbackRecords);
			super.forwardProp();
			this.forwardTrainClient.notifyStop();
			this.forwardTrainClient.disconnectServer();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
		}
	}
	
	@Override
	protected void backwardProp() {
		try {
			this.backwardTrainClient.conntectServer();
			this.backwardTrainClient.notifyInferenceMode();
			this.backwardTrainClient.sendFeedbackVectors(this.feedbackRecords);
			super.backwardProp();
			this.backwardTrainClient.notifyStop();
			this.backwardTrainClient.disconnectServer();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
		}
	}
	
	@Override
	protected double calForwardFactor(TraceNode node) {
		try {
			this.forwardTrainClient.notifyContinuoue();
			this.forwardTrainClient.sendContextFeature(node);
			double factor = this.forwardTrainClient.recieveFactor();
			return factor;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
		}
	}
	
	@Override
	protected double calBackwardFactor(VarValue var, TraceNode node) {
		try {
			this.backwardTrainClient.notifyContinuoue();
			this.backwardTrainClient.sendContextFeature(node);
			this.backwardTrainClient.sendVariableVector(var);
			this.backwardTrainClient.sendVariableName(var);
			double factor = this.backwardTrainClient.recieveFactor();
			return factor;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
		}
	}
	
	@Override
	protected void calConditionBackwardFactor(final TraceNode node) {
		final TraceNode controlDom = node.getControlDominator();
		if (controlDom != null) {
			final VarValue controlDomVar = controlDom.getConditionResult();
			final double factor = this.calBackwardFactor(controlDomVar, node);
			controlDomVar.addBackwardProbability(factor);
		}
	}
}
