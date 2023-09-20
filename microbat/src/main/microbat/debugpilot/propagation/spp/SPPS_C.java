package microbat.debugpilot.propagation.spp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.bytecode.OpcodeType;
import microbat.debugpilot.NodeFeedbacksPair;
import microbat.debugpilot.propagation.ProbabilityPropagator;
import microbat.debugpilot.settings.PropagatorSettings;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;
import microbat.vectorization.vector.NodeVector;

public class SPPS_C implements ProbabilityPropagator {
	
	protected final Trace trace;
	protected List<TraceNode> slicedTrace;
	
	protected final Set<VarValue> wrongVars = new HashSet<>();
	
	protected final List<OpcodeType> unmodifiedType = new ArrayList<>();
	protected List<NodeFeedbacksPair> feedbackRecords = new ArrayList<>();
	
	public SPPS_C(final PropagatorSettings settings) {
		this(settings.getTrace(), settings.getSlicedTrace(), settings.getWrongVars(), settings.getFeedbacks());
	}
	
	public SPPS_C(final Trace trace, final List<TraceNode> sliceTraceNodes, final Collection<VarValue> wrongVars, final Collection<NodeFeedbacksPair> feedbacksPair) {
		this.trace = trace;
		this.slicedTrace = sliceTraceNodes;
		this.wrongVars.addAll(wrongVars);
		this.feedbackRecords.addAll(feedbacksPair);
		this.constructUnmodifiedOpcodeType();
	}
	
	@Override
	public void propagate() {
		this.initConfirmed();
		this.initSuspiciousScore();
		this.calComputationalSuspiciousScore();
		this.calSuspiciousScoreVariable();
		this.normalizeVariableSuspicious();

	}
	
	protected void genRandomSuspiciousScore() {
		this.slicedTrace.stream().forEach(node -> node.computationCost = Math.random());
	}
	protected void initConfirmed() {
		this.slicedTrace.stream().forEach(node -> node.confirmed = false);
	}
	
	protected void initSuspiciousScore() {
		this.slicedTrace.stream().forEach(node -> node.computationCost = 0.0d);
	}
	
	protected void calComputationalSuspiciousScore() {
		final long totalNodeCost = this.slicedTrace.stream().mapToLong(node -> this.countModifyOperation(node)).sum();
		if (totalNodeCost != 0) {
			this.slicedTrace.stream().forEach(node -> {
				node.computationCost += this.countModifyOperation(node)/ (double) totalNodeCost;
			});
		}
	}

	protected void calFeedbackSuspiciousScore() {
		StepComparision comparision = new StepComparision();
		for (TraceNode node : this.slicedTrace) {
			double score = 1.0d;
			final NodeVector nodeVector = new NodeVector(node);
			for (NodeFeedbacksPair nodeFeedbacksPair : this.feedbackRecords) {
				final NodeVector feedbackNodeVector = new NodeVector(nodeFeedbacksPair.getNode());
				final double sim = comparision.calCosSimilarity(nodeVector.getVector(), feedbackNodeVector.getVector());
				score *= (1-Math.pow(sim, 4));
			}
			node.computationCost += score;
		}
	}
	
	protected void calSuspiciousScoreVariable() {
		final double eps = 1e-10;
		this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).forEach(var -> var.computationalCost = eps);
		this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.computationalCost = eps);
		
		for (TraceNode node : this.slicedTrace) {
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
			
			double cumulatedScore = readVars.stream().mapToDouble(var -> var.computationalCost * var.computationalCost).sum();
			cumulatedScore = Math.sqrt(cumulatedScore);
			cumulatedScore += node.computationCost;
			if (node.getControlDominator()!= null) {
				cumulatedScore += node.getControlDominator().getConditionResult().computationalCost;
			}
			final double cumulatedScore_ = cumulatedScore;
			node.getWrittenVariables().stream().forEach(var -> var.computationalCost = cumulatedScore_);
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
	
	protected void normalizeVariableSuspicious() {
		final double maxSuspicious = Math.max(
				this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).mapToDouble(var -> var.computationalCost).max().orElse(0.0d),
				this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).mapToDouble(var -> var.computationalCost).max().orElse(0.0d)
		);
		if (maxSuspicious != 0.0d) {
			this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).forEach(var -> var.computationalCost /= maxSuspicious);
			this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.computationalCost /= maxSuspicious);
		}
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
}
