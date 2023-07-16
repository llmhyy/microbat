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
import microbat.pyserver.BackwardModelClient;
import microbat.pyserver.ForwardModelClient;

public class SPPRL extends SPP {
	
	protected final ForwardModelClient forwardClient;
	protected final BackwardModelClient backwardClient;
	
	public SPPRL(Trace trace, List<TraceNode> slicedTrace, Set<VarValue> correctVars, Set<VarValue> wrongVars,
			Collection<NodeFeedbacksPair> feedbackRecords) {
		super(trace, slicedTrace, correctVars, wrongVars, feedbackRecords);
		this.forwardClient = new ForwardModelClient();
		this.backwardClient = new BackwardModelClient();
	}
	
	@Override
	protected void forwardProp() {
		try {
			this.forwardClient.conntectServer();
			this.forwardClient.sendFeedbackVectors(this.feedbackRecords);
			super.forwardProp();
			this.forwardClient.notifyStop();
			this.forwardClient.disconnectServer();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
		}
	}
	
	@Override
	protected void backwardProp() {
		try {
			this.backwardClient.conntectServer();
			this.backwardClient.sendFeedbackVectors(this.feedbackRecords);
			super.backwardProp();
			this.backwardClient.notifyStop();
			this.backwardClient.disconnectServer();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
		}
	}

	@Override
	protected double calForwardFactor(TraceNode node) {
		try {
			this.forwardClient.notifyContinuoue();
			this.forwardClient.sendContextFeature(node);
			double factor = this.forwardClient.receiveFactor();
			return factor;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
		}
	}

	@Override
	protected double calBackwardFactor(VarValue var, TraceNode node) {
		try {
			this.backwardClient.notifyContinuoue();
			this.backwardClient.sendContextFeature(node);
			this.backwardClient.sendVariableVector(var);
			this.backwardClient.sendVariableName(var);
			double factor = this.backwardClient.receiveFactor();
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
			try {
				this.backwardClient.notifyContinuoue();
				this.backwardClient.sendContextFeature(node);
				this.backwardClient.sendVariableVector(controlDomVar);
				this.backwardClient.sendVariableName(controlDomVar);
				double factor = this.backwardClient.receiveFactor();
				controlDomVar.addBackwardProbability(factor);
			} catch (IOException | InterruptedException e) {
				throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
			}
		}
	}
	

}
