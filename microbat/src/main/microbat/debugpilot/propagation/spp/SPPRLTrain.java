package microbat.debugpilot.propagation.spp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import debuginfo.NodeFeedbacksPair;
import microbat.debugpilot.propagation.probability.PropProbability;
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
	protected double alpha = -1.0d;
	
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
		if (this.feedbackRecords.isEmpty()) {
			return this.calHeuristicForwardFactor(node);
		} else {
			try {
				this.forwardTrainClient.notifyContinuoue();
				this.forwardTrainClient.sendContextFeature(node);
				final double f_rl = this.forwardTrainClient.receiveFactor();
				final double alpha = this.forwardTrainClient.receiveAlpha();
				final double f_heuristic = this.calHeuristicForwardFactor(node);
				return alpha * f_rl + (1-alpha) * f_heuristic;
			} catch (IOException | InterruptedException e) {
				throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
			}
		}
	}
	
	@Override
	protected double calBackwardFactor(VarValue var, TraceNode node) {
		if (this.feedbackRecords.isEmpty()) {
			return this.calHeuristicBackwardFactor(node, var);
		} else {
			try {
				this.backwardTrainClient.notifyContinuoue();
				this.backwardTrainClient.sendContextFeature(node);
				this.backwardTrainClient.sendVariableVector(var);
				this.backwardTrainClient.sendVariableName(var);
				List<ModelPrediction> predictions = ModelPrediction.parseStringList(this.backwardTrainClient.recieveModelPredictionString());
			
				final boolean isAllUncertain = predictions.stream().allMatch(prediction -> prediction.equals(ModelPrediction.UNCERTAIN));
				if (isAllUncertain) {
					return this.calHeuristicBackwardFactor(node, var);
				} else {
					return predictions.stream().filter(prediction -> !prediction.equals(ModelPrediction.UNCERTAIN))
							.mapToDouble(prediction -> prediction.equals(ModelPrediction.SUSPICION) ? 1.0d : 0.0d)
							.average().orElse(this.calHeuristicBackwardFactor(node, var));
				}
	
				// The only case left will be there are multiple SUSPICION and NOT_SUSPICION, then we calculate the average value
//				final double f_rl = this.backwardTrainClient.receiveFactor();
//				final double alpha = this.backwardTrainClient.receiveAlpha();
//				final double f_heuristic = this.calHeuristicBackwardFactor(node, var);
//				return alpha * f_rl + (1-alpha) * f_heuristic;;
			} catch (IOException | InterruptedException e) {
				throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
			}
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
	
	
	protected double calHeuristicForwardFactor(final TraceNode node) {
		return 1 - node.computationCost;
	}
	
	protected double calHeuristicBackwardFactor(final TraceNode node, final VarValue var) {
		if (var.isConditionResult()) {
			return node.getWrittenVariables().stream().mapToDouble(writtenVar -> writtenVar.getBackwardProb()).average().orElse(PropProbability.UNCERTAIN);
		}
		
		final double totalCost = node.getReadVariables().stream().mapToDouble(readVar -> readVar.computationalCost).sum();
		if (totalCost == 0) {
			return (1 - node.computationCost) * (1 / node.getReadVariables().size());
		} else {
			return (1 - node.computationCost) * (var.computationalCost / totalCost);
		}
	}
	
}
