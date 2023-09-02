package microbat.debugpilot.propagation.spp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.poi.util.SystemOutLogger;

import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.bytecode.OpcodeType;
import microbat.debugpilot.NodeFeedbacksPair;
import microbat.debugpilot.propagation.ProbabilityPropagator;
import microbat.debugpilot.propagation.probability.PropProbability;
import microbat.debugpilot.settings.PropagatorSettings;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;

public abstract class SPP implements ProbabilityPropagator {
	
	protected final Trace trace;
	protected List<TraceNode> slicedTrace;
	
	protected final Set<VarValue> correctVars;
	protected final Set<VarValue> wrongVars;
	
	protected final List<OpcodeType> unmodifiedType = new ArrayList<>();
	protected List<NodeFeedbacksPair> feedbackRecords = new ArrayList<>();
	
	public SPP(final PropagatorSettings settings) {
		this(settings.getTrace(), settings.getSlicedTrace(), settings.getWrongVars(), settings.getFeedbacks());
	}
	
	public SPP(Trace trace, List<TraceNode> slicedTrace, Set<VarValue> wrongVars, Collection<NodeFeedbacksPair> feedbackRecords) {
		this.trace = trace;
		this.slicedTrace = slicedTrace;
		this.correctVars = new HashSet<>();
		this.wrongVars = wrongVars;
		this.feedbackRecords.addAll(feedbackRecords);
		this.constructUnmodifiedOpcodeType();
	}
	
	@Override
	public void propagate() {
		this.fuseFeedbacks();
		this.computeComputationalCost();
		this.initProb();
		this.backwardProp();
		this.combineProb();
	}
	
	/**
	 * Initialize forward and backward probability
	 */
	protected void initProb() {
		// Initialize all variables to UNCERTAIN
		this.trace.getExecutionList().stream().flatMap(node -> node.getReadVariables().stream()).forEach(var -> var.setAllProbability(PropProbability.UNCERTAIN));
		this.trace.getExecutionList().stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.setAllProbability(PropProbability.UNCERTAIN));
		
		// Initialize inputs to be CORRECT
		this.trace.getExecutionList().stream()
									 .flatMap(node -> node.getReadVariables().stream())
									 .filter(var -> this.isCorrect(var))
									 .forEach(var -> var.setForwardProb(PropProbability.ONE));
		this.trace.getExecutionList().stream()
									 .flatMap(node -> node.getWrittenVariables().stream())
									 .filter(var -> this.isCorrect(var))
									 .forEach(var -> var.setForwardProb(PropProbability.ONE));
		
		// Initialize outputs to be WRONG
		this.trace.getExecutionList().stream()
									 .flatMap(node -> node.getReadVariables().stream())
									 .filter(var -> this.isWrong(var))
									 .forEach(var -> var.setBackwardProb(PropProbability.ONE));
		this.trace.getExecutionList().stream()
									 .flatMap(node -> node.getWrittenVariables().stream())
									 .filter(var -> this.isWrong(var))
									 .forEach(var -> var.setBackwardProb(PropProbability.ONE));
	}
	
	/**
	 * Backward propagation
	 */
	protected void backwardProp() {
		
		// Clear the backward probability
		this.slicedTrace.stream().filter(node -> node.isBranch()).forEach(node -> node.getConditionResult().clearBackwardProbs());
		
		this.feedbackRecords.stream().forEach(nodeFeedbacksPair -> this.updateProbByFeedback(nodeFeedbacksPair));
		
		for (int order = this.slicedTrace.size()-1; order>=0; order--) {
			final TraceNode node = this.slicedTrace.get(order);
			
			if (this.isFeedbackGiven(node)) {
				node.reason = "User Confirmed";
				continue;
			}
			
			if (node.getOrder() == 540) {
				System.out.println();
			}
			// Inherit backward probability
			this.inheritBackwardProp(node);
			
			List<VarValue> readVars = node.getReadVariables();
			List<VarValue> writtenVars = node.getWrittenVariables();
			
			// Skip if written variables is missing
			if (writtenVars.isEmpty()) {
				continue;
			}
			
			final double avgProb = writtenVars.stream().mapToDouble(var -> var.getBackwardProb()).average().orElse(0.0d);
			for (VarValue readVar : readVars) {
				if (this.isCorrect(readVar)) {
					readVar.setBackwardProb(PropProbability.ZERO);
				} else if (this.isWrong(readVar)) {
					readVar.setBackwardProb(PropProbability.ONE);
				} else {
					final double factor = this.calBackwardFactor(readVar, node);
					final double resultProb = avgProb * factor;
					readVar.setBackwardProb(resultProb);
				}
			}
			
			final TraceNode controlDom = node.getControlDominator();
			if (controlDom != null) {
				final VarValue conditionResult = controlDom.getConditionResult();
				final double factor = this.calBackwardFactor(controlDom.getConditionResult(), node);				
				final double resultProb = avgProb * factor;
				conditionResult.addBackwardProbability(resultProb);
				// If we are sure control dominator is wrong (factor == 1.0), 
				// then the read variables are set to unknown
				if (factor == 1.0d && node.isCertain()) {
					node.getReadVariables().stream().forEach(var -> var.setBackwardProb(1 - PropProbability.UNCERTAIN));
				}
			}
		}
		
		// Normalize to target range
		this.normalizeBackwardProb(0.0d, 1.0d);
	}
	
	/**
	 * Forward propagation
	 */
	protected void forwardProp() {
		for (TraceNode node : this.slicedTrace) {
			if (this.isFeedbackGiven(node)) continue;
			
			// Pass forward probability
			this.inheritForwardProp(node);
			
			// Ignore "this" variable
//			List<VarValue> readVars = node.getReadVariables().stream().filter(var -> !var.isThisVariable()).toList();
//			List<VarValue> writtenVars = node.getWrittenVariables().stream().filter(var -> !var.isThisVariable()).toList();
			List<VarValue> readVars = node.getReadVariables();
			List<VarValue> writtenVars = node.getWrittenVariables();
			
			// Skip if read or written variable is missing
			if (readVars.isEmpty() || writtenVars.isEmpty()) {
				continue;
			}
			
			final double avgProb = readVars.stream().mapToDouble(var -> var.getForwardProb()).average().orElse(0.0d);
			double discount = -1.0d;
			if (!this.isComputational(node) || this.isTested(node)) {
				discount = 1.0d;
			} else {
				discount = this.calForwardFactor(node);
			}
			
			final double resultProp = avgProb * discount;
			for (VarValue writtenVar : writtenVars) {
				if (this.isCorrect(writtenVar)) {
					writtenVar.setForwardProb(PropProbability.ONE);
				} else if (this.isWrong(writtenVar)) {
					writtenVar.setForwardProb(PropProbability.ZERO);
				} else {
					writtenVar.setForwardProb(resultProp);
				}
			}
		}

		// Normalize to target range
		this.normalizeBackwardProb(0.0d, 1.0d);
	}
	
	protected void inheritForwardProp(final TraceNode node) {
		for (VarValue readVar : node.getReadVariables()) {
			if (this.isCorrect(readVar)) {
				readVar.setForwardProb(PropProbability.ONE);
			} else if (this.isWrong(readVar)) {
				readVar.setForwardProb(PropProbability.ZERO);
			} else {
				VarValue dataDomVar = this.findDataDomVar(readVar, node);
				readVar.setForwardProb(dataDomVar == null ? PropProbability.UNCERTAIN : dataDomVar.getForwardProb());
			}
		}
	}
	
	protected void inheritBackwardProp(final TraceNode node) {
		for (VarValue writtenVar : node.getWrittenVariables()) {
			if (this.isCorrect(writtenVar)) {
				writtenVar.setBackwardProb(PropProbability.ZERO);
			} else if (this.isWrong(writtenVar)) {
				writtenVar.setBackwardProb(PropProbability.ONE);
			} else {
				if (node.isBranch() && writtenVar.isConditionResult()) {
					if (node.getOrder() == 129) {
						System.out.println();
					}
					writtenVar.setBackwardProb(writtenVar.getConditionBackwardProb() == null ? 
							PropProbability.UNCERTAIN :
							writtenVar.getConditionBackwardProb().stream().mapToDouble(Double::doubleValue).average().orElse(PropProbability.UNCERTAIN));
				} else {
					List<TraceNode> dataDominatees = this.trace.findDataDependentee(node, writtenVar);
					final double maxProb = dataDominatees.stream().filter(dataDominatee -> this.slicedTrace.contains(dataDominatee))
														 .flatMap(dataDominatee -> dataDominatee.getReadVariables().stream())
														 .filter(var -> var.equals(writtenVar))
														 .mapToDouble(var -> var.getBackwardProb())
														 .average().orElse(PropProbability.UNCERTAIN);
					writtenVar.setBackwardProb(maxProb);	
				}
			}
		}
	}
	
	/**
	 * Convert collected user feedbacks into probability
	 */
	protected void fuseFeedbacks() {
		for (NodeFeedbacksPair feedbacksPair : this.feedbackRecords) {
			final TraceNode node = feedbacksPair.getNode();
			final String feedbackType = feedbacksPair.getFeedbackType();
			final TraceNode controlDom = node.getControlDominator();
			if (feedbackType.equals(UserFeedback.CORRECT)) {
				// User feedback is CORRECT, set every variable to correct
				this.correctVars.addAll(node.getReadVariables());
				this.correctVars.addAll(node.getWrittenVariables());
				if (controlDom != null) {
					this.correctVars.add(controlDom.getConditionResult());
				}
			} else if (feedbackType.equals(UserFeedback.WRONG_PATH)) {
				// User feedback is WRONG_PATH, set control dominator variable to be wrong
				this.wrongVars.add(controlDom.getConditionResult());
			} else if (feedbackType.equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
				// User feedback is WRONG_VARIABLE, set selected variable to wrong and set control dominator to correct
				List<VarValue> selectedWrongVars = feedbacksPair.getFeedbacks().stream().map(feedback -> feedback.getOption().getReadVar()).toList();
				this.wrongVars.addAll(selectedWrongVars);
				this.wrongVars.addAll(node.getWrittenVariables());
				if (controlDom != null) {
					this.correctVars.add(controlDom.getConditionResult());
				}
			}
		}
	}
	
	protected void combineProb() {
		Stream.concat(
				this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()), 
				this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()))
			.forEach(var -> var.setProbability(1 - var.getBackwardProb()));
	}
	
	protected boolean isCorrect(final VarValue var) {
		return this.correctVars.contains(var);
	}
	
	protected boolean isWrong(final VarValue var) {
		return this.wrongVars.contains(var);
	}
	
	protected boolean isFeedbackGiven(final TraceNode node) {
		for (NodeFeedbacksPair pair : this.feedbackRecords) {
			if (node.equals(pair.getNode())) {
				return true;
			}
		}
		return false;
	}
	
	protected abstract double calForwardFactor(final TraceNode node);
	
	protected abstract double calBackwardFactor(final VarValue var, final TraceNode node);
	
	protected void calConditionBackwardFactor(final TraceNode node) {
		final TraceNode controlDom = node.getControlDominator();
		if (controlDom != null) {
			for (VarValue writtenVar : node.getWrittenVariables()) {
				controlDom.getConditionResult().addBackwardProbability(writtenVar.getBackwardProb());
			}
		}
	}
	
	protected boolean isComputational(final TraceNode node) {
		return this.countModifyOperation(node) != 0;
	}
	
	protected boolean isTested(final TraceNode node) {
		return false;
	}
	
	protected double normalize(final double probability, final double min, final double max, final double targetMin, final double targetMax) {
		// Handle the case that duplicated variable
		if (probability < min) {
			return min;
		} else if (probability > max) {
			return max;
		} else {
			return (probability - min) / (max - min) * (targetMax - targetMin) + targetMin;			
		}
	}
	
	protected VarValue findDataDomVar(final VarValue var, final TraceNode node) {
		TraceNode dataDominator = this.trace.findDataDependency(node, var);
		if (dataDominator != null) {
			for (VarValue writtenVar : dataDominator.getWrittenVariables()) {
				if (writtenVar.equals(var)) {
					return writtenVar;
				}
			}
		}
		return null;
	}
	
	protected int countModifyOperation(final TraceNode node) {
		ByteCodeList byteCodeList = new ByteCodeList(node.getBytecode());
		int count = 0;
		for (ByteCode byteCode : byteCodeList) {
			if (!this.unmodifiedType.contains(byteCode.getOpcodeType())) {
				count+=1;
			}
		}
		return count;
	}
	
	protected void computeComputationalCost() {
		
		// Calculate computation cost for each step 
		final long totalNodeCost = this.slicedTrace.stream().mapToLong(node -> this.countModifyOperation(node)).sum();
		this.slicedTrace.stream().forEach(node -> node.computationCost = totalNodeCost == 0 ? 0 : this.countModifyOperation(node) / (double) totalNodeCost);
		
		// Initialize all variable computational cost to 0
		this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).forEach(var -> var.computationalCost = 0.0d);
		this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.computationalCost = 0.0d);
		
		// Calculate computational cost for each variable
		double maxVarCost = 0.0d;
		for (TraceNode node : this.slicedTrace) {

//			List<VarValue> readVars = node.getReadVariables().stream().filter(var -> !var.isThisVariable()).toList();
			List<VarValue> readVars = node.getReadVariables();
			
			if (readVars.isEmpty()) {
				continue;
			}
			
			// Inherit computation cost from data dominator
			for (VarValue readVar : readVars) {
				final VarValue dataDomVar = this.findDataDomVar(readVar, node);
				if (dataDomVar != null) {
					readVar.computationalCost = dataDomVar.computationalCost;
				}
			}
			
			final double cumulatedCost = readVars.stream().mapToDouble(var -> var.computationalCost).sum();
			final double optCost = node.computationCost;
			final double cost = Double.isInfinite(cumulatedCost+optCost) ? Double.MAX_VALUE : cumulatedCost+optCost;
			
			node.getWrittenVariables().stream().forEach(var -> var.computationalCost = cost);
//			node.getWrittenVariables().stream().filter(var -> !var.isThisVariable()).forEach(var -> var.computationalCost = cost);
			maxVarCost = Math.max(cost,  maxVarCost);
		}
		
		// Normalize by maximum computational cost
		final double maxVarCost_ = maxVarCost;
		this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).forEach(var -> var.computationalCost = maxVarCost_ == 0 ? 0 : var.computationalCost / maxVarCost_);
		this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.computationalCost = maxVarCost_ == 0 ? 0 : var.computationalCost / maxVarCost_);
	}

	protected void constructUnmodifiedOpcodeType() {
		this.unmodifiedType.add(OpcodeType.LOAD_CONSTANT);
		this.unmodifiedType.add(OpcodeType.LOAD_FROM_ARRAY);
		this.unmodifiedType.add(OpcodeType.LOAD_VARIABLE);
		this.unmodifiedType.add(OpcodeType.STORE_INTO_ARRAY);
		this.unmodifiedType.add(OpcodeType.STORE_VARIABLE);
		this.unmodifiedType.add(OpcodeType.RETURN);
		this.unmodifiedType.add(OpcodeType.GET_FIELD);
		this.unmodifiedType.add(OpcodeType.GET_STATIC_FIELD);
		this.unmodifiedType.add(OpcodeType.PUT_FIELD);
		this.unmodifiedType.add(OpcodeType.PUT_STATIC_FIELD);
		this.unmodifiedType.add(OpcodeType.INVOKE);
	}
	
	protected void normalizeForwardProb(final double targetMin, final double targetMax) {
		final double min = Math.min(this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).mapToDouble(var -> var.getForwardProb()).min().orElse(0.0d),
					this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).mapToDouble(var -> var.getForwardProb()).min().orElse(0.0d));
		final double max = Math.max(this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).mapToDouble(var -> var.getForwardProb()).max().orElse(0.0d), 
					this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).mapToDouble(var -> var.getForwardProb()).max().orElse(0.0d));
		
		if (Double.compare(min, max) != 0) {
			this.slicedTrace.stream().flatMap(node  -> node.getReadVariables().stream()).forEach(var -> var.setForwardProb(this.normalize(var.getForwardProb(), min, max, targetMin, targetMax)));
			this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.setForwardProb(this.normalize(var.getForwardProb(), min, max, targetMin, targetMax)));
		}
	}
	
	protected void normalizeBackwardProb(final double targetMin, final double targetMax) {
		final double min = Stream.concat(this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()), this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()))
				.mapToDouble(var -> var.getBackwardProb()).min().orElse(0.0d);
		final double max = Stream.concat(this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()), this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()))
				.mapToDouble(var -> var.getBackwardProb()).max().orElse(0.0d);
		if (Double.compare(min, max) != 0 ) {
			this.slicedTrace.stream().flatMap(node  -> node.getReadVariables().stream()).forEach(var -> var.setBackwardProb(this.normalize(var.getBackwardProb(), min, max, targetMin, targetMax)));
			this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.setBackwardProb(this.normalize(var.getBackwardProb(), min, max, targetMin, targetMax)));
		}
	}
	
	protected void normalizeProb(final double targetMin, final double targetMax) {
		final double min = Math.min(this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).mapToDouble(var -> var.getProbability()).min().orElse(0.0d),
				this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).mapToDouble(var -> var.getProbability()).min().orElse(0.0d));
		final double max = Math.max(this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).mapToDouble(var -> var.getProbability()).max().orElse(0.0d), 
				this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).mapToDouble(var -> var.getProbability()).max().orElse(0.0d));
		if (Double.compare(min, max) == 0) {
			this.slicedTrace.stream().flatMap(node  -> node.getReadVariables().stream()).forEach(var -> var.setProbability(this.normalize(var.getProbability(), min, max, targetMin, targetMax)));
			this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.setProbability(this.normalize(var.getProbability(), min, max, targetMin, targetMax)));			
		}
	}
	
	protected void updateProbByFeedback(final NodeFeedbacksPair pair) {
		final TraceNode node = pair.getNode();
		if (pair.getFeedbackType().equals(UserFeedback.CORRECT)) {
			node.getWrittenVariables().forEach(var -> var.setProbability(PropProbability.ONE));
			node.getReadVariables().forEach(var -> var.setProbability(PropProbability.ONE));
		} else if (pair.getFeedbackType().equals(UserFeedback.WRONG_PATH) || pair.getFeedbackType().equals(UserFeedback.UNCLEAR)) {
			node.getWrittenVariables().forEach(var -> var.setProbability(PropProbability.UNCERTAIN));
			node.getReadVariables().forEach(var -> var.setProbability(PropProbability.UNCERTAIN));
		} else if (pair.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
			node.getWrittenVariables().forEach(var -> var.setProbability(PropProbability.ZERO));
			List<VarValue> wrongVars = pair.getFeedbacks().stream().map(feedback -> feedback.getOption().getReadVar()).toList();
			for (VarValue readVar : node.getReadVariables()) {
				if (wrongVars.contains(readVar)) {
					readVar.setProbability(PropProbability.ZERO);
				} else {
					readVar.setProbability(PropProbability.ONE);
				}
			}
		}
		
	}
}
