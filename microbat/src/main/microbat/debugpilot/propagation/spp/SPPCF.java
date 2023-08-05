package microbat.debugpilot.propagation.spp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import debuginfo.NodeFeedbacksPair;
import microbat.debugpilot.settings.PropagatorSettings;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.ChosenVariableOption;
import microbat.recommendation.UserFeedback;

public class SPPCF extends SPPH {

	protected final StepComparision comparision = new StepComparision();
	
	public SPPCF(final PropagatorSettings settings) {
		super(settings);
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
	
	protected double calFactor(final TraceNode node, final VarValue var) {
		List<ModelPrediction> predictions = this.getPredictions(node, var);
		this.storeReason(node, var, predictions);
		final boolean isAllUncertain = predictions.stream().allMatch(prediction -> prediction.equals(ModelPrediction.UNCERTAIN));
		if (isAllUncertain) {
			return super.calBackwardFactor(var, node);
		} else {
			return predictions.stream().filter(prediction -> !prediction.equals(ModelPrediction.UNCERTAIN))
					.mapToDouble(prediction -> prediction.equals(ModelPrediction.SUSPICION) ? 1.0d : 0.0d)
					.average().orElse(-1.0d);
		}
	}
	
	protected List<ModelPrediction> getPredictions(final TraceNode node, final VarValue var) {
		List<ModelPrediction> predictions = new ArrayList<>();
		for (NodeFeedbacksPair pair : this.feedbackRecords) {
			final TraceNode feedbackNode = pair.getNode();
			for (UserFeedback feedback : pair.getFeedbacks()) {
				final VarValue feedbackVar = this.extractWrongVar(feedbackNode, feedback);
				final boolean nodeSimilar = this.comparision.isSimiliar(node, feedbackNode);
				final boolean varSimilar = this.comparision.isSimiliar(var, feedbackVar);
				if (nodeSimilar && varSimilar) {
					predictions.add(ModelPrediction.SUSPICION);
				} else if (nodeSimilar && !varSimilar) {
					predictions.add(ModelPrediction.NOT_SUSPICION);
				} else {
					predictions.add(ModelPrediction.UNCERTAIN);
				}
			}
		}
		return predictions;
	}
	
	protected VarValue extractWrongVar(final TraceNode node, final UserFeedback feedback) {
		VarValue wrongVar;
		if (feedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
			wrongVar = feedback.getOption().getReadVar();
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
			wrongVar = node.getControlDominator().getConditionResult();
		} else {
			wrongVar = null;
		}
		return wrongVar;
	}
	
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
