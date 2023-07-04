package microbat.probability.SPP.propagation;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import debuginfo.NodeFeedbacksPair;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.probability.PropProbability;

public class SPPRL extends SPP {

	protected final ForwardModelClient forwardClient;
	protected final BackwardModelClient backwardClient;
	
	protected final int numDataDoms = 5;
	protected final int numDataDomees = 3;
	protected final int numControlDomees = 2;
	
	public SPPRL(Trace trace, List<TraceNode> slicedTrace, Set<VarValue> correctVars, Set<VarValue> wrongVars,
			Collection<NodeFeedbacksPair> feedbackRecords) {
		super(trace, slicedTrace, correctVars, wrongVars, feedbackRecords);
		this.forwardClient = new ForwardModelClient();
		this.backwardClient = new BackwardModelClient();
	}
	
	public void connectServer() throws UnknownHostException, IOException {
		this.forwardClient.conntectServer();
		this.backwardClient.conntectServer();
	}
	
	public void dissconnectServer() throws IOException {
		this.forwardClient.disconnectServer();
		this.backwardClient.disconnectServer();
	}
	
	@Override
	public void propagate() {
		this.fuseFeedbacks();
		this.initProb();
		this.forwardProp();
		this.backwardProp();
		this.combineProb();
	}
	
	@Override
	protected void forwardProp() {
		try {
			this.forwardClient.conntectServer();
			this.forwardClient.sendFeedbackVectors(this.feedbackRecords);
			super.forwardProp();
			this.forwardClient.notifyStop();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
		}
	}
	
	@Override
	protected double calForwardFactor(final TraceNode node) {
		try {
			this.forwardClient.notifyContinuoue();
			this.forwardClient.sendContextFeature(node);
			double factor = this.forwardClient.recieveFactor();
//			Log.printMsg("Node: " + node.getOrder() + " forward factor: " + factor, this.getClass());
			return factor;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
		}
	}
	
	public void sendRewardToBackServer(final float reward) {
		try {
			this.backwardClient.sendReward(reward);
			this.backwardClient.disconnectServer();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
		}
	}
	
	public void sendRewardToForwardServer(final float reward) {
		try {
			this.forwardClient.sendReward(reward);
			this.forwardClient.disconnectServer();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
		}
	}
	
	public void sendReward(final float reward) {
		this.sendRewardToForwardServer(reward);
		this.sendRewardToBackServer(reward);
	}
	
	@Override
	protected void backwardProp() {
		try {
			this.backwardClient.conntectServer();
			this.backwardClient.sendFeedbackVectors(this.feedbackRecords);
			for (int order = this.slicedTrace.size()-1; order>=0; order--) {
				final TraceNode node = this.slicedTrace.get(order);
				if (this.isFeedbackGiven(node)) continue;
				
				// Inherit backward probability
				this.inheritBackwardProp(node);
				
				// Ignore "this" variable
				List<VarValue> readVars = node.getReadVariables().stream().filter(var -> !var.isThisVariable()).toList();
				List<VarValue> writtenVars = node.getWrittenVariables().stream().filter(var -> !var.isThisVariable()).toList();
				
				// Skip if read or written variables is missing
				if (readVars.isEmpty() || writtenVars.isEmpty()) {
					continue;
				}
			
				final double avgProb = writtenVars.stream().mapToDouble(var -> var.getBackwardProb()).average().orElse(0.0d);
				for (VarValue readVar : readVars) {
					if (this.isCorrect(readVar)) {
						readVar.setBackwardProb(PropProbability.ZERO);
					} else if (this.isWrong(readVar)) {
						readVar.setBackwardProb(PropProbability.ONE);
					} else {
						double factor = -1.0d;
						if (!this.isComputational(node) || this.isTested(node)) {
							factor = 1.0d;
						} else {
							factor = this.calBackwardFactor(readVar, node);
						}
						final double resultProb = avgProb * factor;
						readVar.setBackwardProb(resultProb);
					}	
				}
				
				// Propagate to control dominator as well
				final TraceNode controlDom = node.getControlDominator();
				if (controlDom != null) {
					final double prob = this.calBackwardFactor(controlDom.getConditionResult(), node);
					controlDom.getConditionResult().addBackwardProbability(prob);
				}
			}
			
			// Normalize to target range
			final double targetMin = 0.0d;
			final double targetMax = 1.0d;
			final double min = Math.min(this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).mapToDouble(var -> var.getBackwardProb()).min().orElse(0.0d),
						this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).mapToDouble(var -> var.getBackwardProb()).min().orElse(0.0d));
			final double max = Math.max(this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).mapToDouble(var -> var.getBackwardProb()).max().orElse(0.0d), 
						this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).mapToDouble(var -> var.getBackwardProb()).max().orElse(0.0d));
			this.slicedTrace.stream().flatMap(node  -> node.getReadVariables().stream()).forEach(var -> var.setBackwardProb(this.normalize(var.getBackwardProb(), min, max, targetMin, targetMax)));
			this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.setBackwardProb(this.normalize(var.getBackwardProb(), min, max, targetMin, targetMax)));

			this.backwardClient.notifyStop();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
		}
	}
	
	@Override
	protected double calBackwardFactor(final VarValue var, final TraceNode node) {
		try {
			this.backwardClient.notifyContinuoue();
			this.backwardClient.sendContextFeature(node);
			this.backwardClient.sendVariableVector(var);
			this.backwardClient.sendVariableName(var);
			double factor = this.backwardClient.recieveFactor();
			return factor;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
		}
	}

	
	@Override
	protected void calConditionBackwardProb(final TraceNode node, final VarValue conditionResult) {
		List<Double> probs = conditionResult.getConditionBackwardProb();
		if (probs == null) {
			conditionResult.setBackwardProb(PropProbability.UNCERTAIN);
		} else {
			final double avgProb = probs.stream().mapToDouble(Double::doubleValue).average().orElse(PropProbability.UNCERTAIN);
			conditionResult.setBackwardProb(avgProb);
		}
	}
}
