package microbat.debugpilot.propagation.spp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import debuginfo.NodeFeedbacksPair;
import microbat.debugpilot.propagation.probability.PropProbability;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.pyserver.BackwardModelClient;
import microbat.recommendation.ChosenVariableOption;
import microbat.recommendation.UserFeedback;

public class SPPCF extends SPPH {

	protected final BackwardModelClient client;
	
	public SPPCF(Trace trace, List<TraceNode> slicedTrace, Set<VarValue> correctVars, Set<VarValue> wrongVars,
			Collection<NodeFeedbacksPair> feedbackRecords) {
		super(trace, slicedTrace, correctVars, wrongVars, feedbackRecords);
		this.client = new BackwardModelClient("127.0.0.4", 8084);
	}

	@Override
	protected void backwardProp() {
		try {
			this.client.conntectServer();
			this.client.sendFeedbackVectors(this.feedbackRecords);
			super.backwardProp();
			this.client.notifyStop();
			this.client.disconnectServer();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
		}
	}
	
	@Override
	protected double calForwardFactor(TraceNode node) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected double calBackwardFactor(VarValue var, TraceNode node) {
		if (this.feedbackRecords.isEmpty()) {
			return super.calBackwardFactor(var, node);
		} else if (var.isConditionResult()) {
			return this.calFactor(node, var);
		} else if (!this.isComputational(node)) {
			return 1.0d;
		} else {
			return this.calFactor(node, var);
		}
	}
	
//	@Override
//	protected void calConditionBackwardFactor(final TraceNode node) {
//		final TraceNode controlDom = node.getControlDominator();
//		if (controlDom != null) {
//			if (this.feedbackRecords.isEmpty()) {
//				super.calConditionBackwardFactor(node);
//			} else {
//				final VarValue conditionResult = controlDom.getConditionResult();
//				List<ModelPrediction> predictions = this.askForPredictions(node, conditionResult);
//				this.storeReason(node, conditionResult, predictions);
//				final boolean isAllUncertain = predictions.stream().allMatch(prediction -> prediction.equals(ModelPrediction.UNCERTAIN));
//				if (isAllUncertain) {
//					super.calConditionBackwardFactor(node);
//				} else {
//					
//				}
//			}
//		}
//	}
	
	protected double calFactor(final TraceNode node, final VarValue var) {
		try {
			this.client.notifyContinuoue();
			this.client.sendContextFeature(node);
			this.client.sendVariableVector(var);
			this.client.sendVariableName(var);
			final String response = this.client.recieveModelPredictionString();
			List<ModelPrediction> predictions = ModelPrediction.parseStringList(response);
			this.storeReason(node, var, predictions);
			final boolean isAllUncertain = predictions.stream().allMatch(prediction -> prediction.equals(ModelPrediction.UNCERTAIN));
			if (isAllUncertain) {
				return super.calBackwardFactor(var, node);
			} else {
				return predictions.stream().filter(prediction -> !prediction.equals(ModelPrediction.UNCERTAIN))
						.mapToDouble(prediction -> prediction.equals(ModelPrediction.SUSPICION) ? 1.0d : 0.0d)
						.average().orElse(-1.0d);
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
		}
	}
	
//	protected double calFactor(final VarValue var, final TraceNode node, List<ModelPrediction> predictions) {
//		final boolean isAllUncertain = predictions.stream().allMatch(prediction -> prediction.equals(ModelPrediction.UNCERTAIN));
//		if (isAllUncertain) {
//			return super.calBackwardFactor(var, node);
//		} else {
//			return predictions.stream().filter(prediction -> !prediction.equals(ModelPrediction.UNCERTAIN))
//					.mapToDouble(prediction -> prediction.equals(ModelPrediction.SUSPICION) ? 1.0d : 0.0d)
//					.average().orElse(-1.0d);
//		}
//	}
//	
//	protected double calHeuristicFactor(final TraceNode node, final VarValue var) {
//		if (var.isConditionResult()) {
//			return node.getWrittenVariables().stream().mapToDouble(writtenVar -> writtenVar.getBackwardProb()).average().orElse(PropProbability.UNCERTAIN);
//		}
//		
//		final double totalCost = node.getReadVariables().stream().mapToDouble(readVar -> readVar.computationalCost).sum();
//		if (totalCost == 0) {
//			return (1 - node.computationCost) * (1 / node.getReadVariables().size());
//		} else {
//			return (1 - node.computationCost) * (var.computationalCost / totalCost);
//		}
//	}
	
	protected void storeReason(final TraceNode node, final VarValue varValue, final List<ModelPrediction> predictions) {
		final String reason = this.genReason(predictions);
		if (varValue.isConditionResult()) {
			NodeFeedbacksPair pair = new NodeFeedbacksPair(node, new UserFeedback(UserFeedback.WRONG_PATH));
			node.storeReason(pair, reason);
		} else {
			ChosenVariableOption option = new ChosenVariableOption(varValue, null);
			UserFeedback feedback = new UserFeedback(option, UserFeedback.WRONG_VARIABLE_VALUE);
			NodeFeedbacksPair pair = new NodeFeedbacksPair(node, feedback);
			node.storeReason(pair, reason);
		}
	}
	
	protected String genReason(final List<ModelPrediction> predictions) {
		List<Integer> suspicionIdxes = IntStream.range(0, predictions.size())
                .filter(i -> predictions.get(i) == ModelPrediction.SUSPICION)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		List<Integer> nonSupspicionIdxes = IntStream.range(0, predictions.size())
                .filter(i -> predictions.get(i) == ModelPrediction.NOT_SUSPICION)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		
		if (suspicionIdxes.isEmpty() && nonSupspicionIdxes.isEmpty()) {
			return StepExplaination.COST;
		} else if (nonSupspicionIdxes.isEmpty()) {
			final NodeFeedbacksPair pair = this.feedbackRecords.get(suspicionIdxes.get(0));
			return StepExplaination.REF(pair.getNode().getOrder());
		} else if (suspicionIdxes.isEmpty()) {
			final NodeFeedbacksPair pair = this.feedbackRecords.get(nonSupspicionIdxes.get(0));
			return StepExplaination.REF(pair.getNode().getOrder());
		} else {
			final NodeFeedbacksPair pair_1 = this.feedbackRecords.get(suspicionIdxes.get(0));
			final NodeFeedbacksPair pair_2 = this.feedbackRecords.get(nonSupspicionIdxes.get(0));
			return StepExplaination.REF(pair_1.getNode().getOrder(), pair_2.getNode().getOrder());
		}
	}
	
	

}
