package microbat.probability.SPP.propagation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import debuginfo.NodeFeedbacksPair;
import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.bytecode.OpcodeType;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.probability.PropProbability;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;

public class SPP implements ProbabilityPropagator {
	
	protected final Trace trace;
	protected List<TraceNode> slicedTrace;
	
	protected final Set<VarValue> correctVars;
	protected final Set<VarValue> wrongVars;
	
	protected final List<OpcodeType> unmodifiedType = new ArrayList<>();
	protected Collection<NodeFeedbacksPair> feedbackRecords = null;
	
	public SPP(Trace trace, List<TraceNode> slicedTrace, Set<VarValue> correctVars, Set<VarValue> wrongVars, Collection<NodeFeedbacksPair> feedbackRecords) {
		this.trace = trace;
		this.slicedTrace = slicedTrace;
		this.correctVars = correctVars;
		this.wrongVars = wrongVars;
		this.feedbackRecords = feedbackRecords;
		this.constructUnmodifiedOpcodeType();
	}
	
	@Override
	public void propagate() {
		this.fuseFeedbacks();
		this.updateSlicedTrace();
		this.computeComputationalCost();
		this.initProb();
		this.forwardProp();
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
	}
	
	/**
	 * Forward propagation
	 */
	protected void forwardProp() {
		for (TraceNode node : this.slicedTrace) {
			Log.printMsg(getClass(), "Forward: " + node.getOrder());
			if (this.isFeedbackGiven(node)) continue;
			
			// Pass forward probability
			this.inheritForwardProp(node);
			
			// Ignore "this" variable
			List<VarValue> readVars = node.getReadVariables().stream().filter(var -> !var.isThisVariable()).toList();
			List<VarValue> writtenVars = node.getWrittenVariables().stream().filter(var -> !var.isThisVariable()).toList();
			
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
		final double targetMin = 0.0d;
		final double targetMax = 1.0d;
		final double min = Math.min(this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).mapToDouble(var -> var.getForwardProb()).min().orElse(0.0d),
					this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).mapToDouble(var -> var.getForwardProb()).min().orElse(0.0d));
		final double max = Math.max(this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).mapToDouble(var -> var.getForwardProb()).max().orElse(0.0d), 
					this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).mapToDouble(var -> var.getForwardProb()).max().orElse(0.0d));
		this.slicedTrace.stream().flatMap(node  -> node.getReadVariables().stream()).forEach(var -> var.setForwardProb(this.normalize(var.getForwardProb(), min, max, targetMin, targetMax)));
		this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.setForwardProb(this.normalize(var.getForwardProb(), min, max, targetMin, targetMax)));
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
					this.calConditionBackwardProb(node, writtenVar);
				} else {
					List<TraceNode> dataDominatees = this.trace.findDataDependentee(node, writtenVar);
					final double maxProb = dataDominatees.stream().filter(dataDominatee -> this.slicedTrace.contains(dataDominatee))
														 .flatMap(dataDominatee -> dataDominatee.getReadVariables().stream())
														 .filter(var -> var.equals(writtenVar))
														 .mapToDouble(var -> var.getBackwardProb())
														 .max().orElse(PropProbability.UNCERTAIN);
					writtenVar.setBackwardProb(maxProb);	
				}
			}
		}
	}
	
	protected void calConditionBackwardProb(final TraceNode node, final VarValue conditionResult) {
		final double avgWrittenVarsProb = node.getControlDominatees().stream().filter(controlDomatee -> this.slicedTrace.contains(controlDomatee))
				.flatMap(controlDominatee -> controlDominatee.getWrittenVariables().stream())
				.mapToDouble(var -> var.getBackwardProb())
				.average().orElse(PropProbability.UNCERTAIN);
		conditionResult.setBackwardProb(avgWrittenVarsProb);
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
				// User feedback is CORRECT, set every to correct
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
		this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream())
						.forEach(var -> 
							var.setProbability((var.getForwardProb() + (1.0d-var.getBackwardProb()))/2.0d)
						);
		this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream())
						.forEach(var -> 
							var.setProbability((var.getForwardProb() + (1.0-var.getBackwardProb()))/2.0d)
						);
		
		// Normalize to target range
//		final double targetMin = 0.0d;
//		final double targetMax = 1.0d;
//		final double min = Math.min(this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).mapToDouble(var -> var.getProbability()).min().orElse(0.0d),
//					this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).mapToDouble(var -> var.getProbability()).min().orElse(0.0d));
//		final double max = Math.max(this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).mapToDouble(var -> var.getProbability()).max().orElse(0.0d), 
//					this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).mapToDouble(var -> var.getProbability()).max().orElse(0.0d));
//		this.slicedTrace.stream().flatMap(node  -> node.getReadVariables().stream()).forEach(var -> var.setProbability(this.normalize(var.getProbability(), min, max, targetMin, targetMax)));
//		this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.setProbability(this.normalize(var.getProbability(), min, max, targetMin, targetMax)));
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
	
	protected double calForwardFactor(final TraceNode node) {
		return Math.random();
	}
	
	protected double calBackwardFactor(final VarValue var, final TraceNode node) {
		return Math.random();
	}
	
	protected boolean isComputational(final TraceNode node) {
		return this.countModifyOperation(node) != 0;
	}
	
	protected boolean isTested(final TraceNode node) {
		return false;
	}
	
	protected double normalize(final double probability, final double min, final double max, final double targetMin, final double targetMax) {
		return (probability - min) / (max - min) * (targetMax - targetMin) + targetMin;
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
	
	protected void updateSlicedTrace() {
		Set<TraceNode> relatedNodes = new HashSet<>();
		relatedNodes.addAll(this.slicedTrace);
		for (NodeFeedbacksPair pair : this.feedbackRecords) {
			final TraceNode node = pair.getNode();
			relatedNodes.retainAll(TraceUtil.dyanmicSlice(this.trace, node));
		}
		List<TraceNode> newSlicedNodes = new ArrayList<>();
		newSlicedNodes.addAll(relatedNodes);
		newSlicedNodes.sort(new Comparator<TraceNode>() {
			@Override
			public int compare(TraceNode t1, TraceNode t2) {
				return t1.getOrder() - t2.getOrder();
			}
		});
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
			// Ignore "this" variable
			List<VarValue> readVars = node.getReadVariables().stream().filter(var -> !var.isThisVariable()).toList();
			
			if (readVars.isEmpty()) {
				continue;
			}
			
			// Inherit computation cost from data dominator
			for (VarValue readVar : node.getReadVariables()) {
				final VarValue dataDomVar = this.findDataDomVar(readVar, node);
				if (dataDomVar != null) {
					readVar.computationalCost = dataDomVar.computationalCost;
				}
			}
			
			final double cumulatedCost = readVars.stream().mapToDouble(var -> var.computationalCost).sum();
			final double optCost = node.computationCost;
			final double cost = Double.isInfinite(cumulatedCost+optCost) ? Double.MAX_VALUE : cumulatedCost+optCost;
			
			node.getWrittenVariables().stream().filter(var -> !var.isThisVariable()).forEach(var -> var.computationalCost = cost);
			maxVarCost = Math.max(cost,  maxVarCost);
		}
		
		// Normalize by maximum computational cost
		final double maxVarCost_ = maxVarCost;
		this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).forEach(var -> var.computationalCost = maxVarCost_ == 0 ? 0 : var.computationalCost / maxVarCost_);
		this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.computationalCost = maxVarCost_ == 0 ? 0 : var.computationalCost / maxVarCost_);
	}

	private void constructUnmodifiedOpcodeType() {
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

	@Override
	public void updateFeedbacks(Collection<NodeFeedbacksPair> pairs) {
		this.feedbackRecords = pairs;
	}
}
