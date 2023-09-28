package microbat.debugpilot.propagation.spp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.debugpilot.NodeFeedbacksPair;
import microbat.debugpilot.settings.PropagatorSettings;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;

public class SPPS_CB extends SPPS_C {
	
	protected final int bytecode_size = 256;
	
	protected final int[] bytecode_cf;
	protected final int[] bytecode_cs;
	protected final int[] bytecode_nf;
	
	protected final double eps = 1e-10;
	
	public SPPS_CB(final PropagatorSettings settings) {
		this(settings.getTrace(), settings.getSlicedTrace(), settings.getWrongVars(), settings.getFeedbacks());
	}
	
	public SPPS_CB(final Trace trace, final List<TraceNode> sliceTraceNodes, final Collection<VarValue> wrongVars, final Collection<NodeFeedbacksPair> feedbacksPair) {
		super(trace, sliceTraceNodes, wrongVars, feedbacksPair);
		
		this.bytecode_cf = new int[this.bytecode_size];
		this.bytecode_cs = new int[this.bytecode_size];
		this.bytecode_nf = new int[this.bytecode_size];
	}
	
	@Override
	public void propagate() {
		this.initConfirmed();
		this.initSuspiciousScore();
//		this.calComputationalSuspiciousScore();
		this.calBytecodeSuspiciousScore();
		this.calSuspiciousScoreVariable();
		this.normalizeVariableSuspicious();
	}
	
	protected void calBytecodeSuspiciousScore() {
		this.updateBytecodeScore();
		for (TraceNode node : this.slicedTrace) {
			double score = 0.0d;
			ByteCodeList byteCodeList = new ByteCodeList(node.getBytecode());
			for (ByteCode byteCode : byteCodeList) {
				score += this.calBytecodeScore(byteCode);
			}
			node.computationCost += 1+score;
		}
	}
	
	protected void updateBytecodeScore() {
		Arrays.fill(this.bytecode_cf, 0);
		Arrays.fill(this.bytecode_cs, 0);
		Arrays.fill(this.bytecode_nf, 0);
		
		Set<TraceNode> nodeCoveredByWrongFeedback = new HashSet<>();
		for (NodeFeedbacksPair feedbackPair : this.feedbackRecords) {
			if (feedbackPair.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
				final TraceNode node = feedbackPair.getNode();
				final List<VarValue> wrongVars = feedbackPair.getFeedbacks().stream().map(feedback -> feedback.getOption().getReadVar()).toList();
				for (VarValue readVar : node.getReadVariables()) {
					final TraceNode dataDominator = this.trace.findDataDependency(node, readVar);
					if (dataDominator == null) {
						continue;
					}
					
					List<TraceNode> relatedNodes = TraceUtil.dynamicSlice(trace, dataDominator);
					
					if (wrongVars.contains(readVar)) {
						// Variable is wrong
						nodeCoveredByWrongFeedback.addAll(relatedNodes);
						for (TraceNode relatedNode : this.filteredSameLineOfCode(relatedNodes)) {
							ByteCodeList byteCodeList = new ByteCodeList(relatedNode.getBytecode());
							for (ByteCode byteCode : byteCodeList) {
								this.bytecode_cf[byteCode.getOpcode()] += 1;
							}
						}
					} else {
						// Variable is correct
						for (TraceNode relatedNode : relatedNodes) {
							ByteCodeList byteCodeList = new ByteCodeList(relatedNode.getBytecode());
							for (ByteCode byteCode : byteCodeList) {
								this.bytecode_cs[byteCode.getOpcode()] += 1;
							}
						}
					}
				}
				
				final TraceNode controlDom = node.getControlDominator();
				if (controlDom != null) {
					List<TraceNode> relatedNodes = this.filteredSameLineOfCode(TraceUtil.dynamicSlice(trace, controlDom));
					for (TraceNode relatedNode : relatedNodes) {
						ByteCodeList byteCodeList = new ByteCodeList(relatedNode.getBytecode());
						for (ByteCode byteCode : byteCodeList) {
							this.bytecode_cs[byteCode.getOpcode()] += 1;
						}
					}
				}
			} else if (feedbackPair.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
				final TraceNode controlDominator = feedbackPair.getNode().getControlDominator();
				if (controlDominator != null) {
					List<TraceNode> relatedNodes = TraceUtil.dynamicSlice(trace, controlDominator);
					nodeCoveredByWrongFeedback.addAll(relatedNodes);
					for (TraceNode relatedNode : this.filteredSameLineOfCode(relatedNodes)) {
						ByteCodeList byteCodeList = new ByteCodeList(relatedNode.getBytecode());
						for (ByteCode byteCode : byteCodeList) {
							this.bytecode_cf[byteCode.getOpcode()] += 1;
						}
					}
				}
			}
		}
		
		Set<TraceNode> nodeUnCoveredByWrongFeedback = new HashSet<>();
		nodeUnCoveredByWrongFeedback.addAll(this.slicedTrace);
		nodeUnCoveredByWrongFeedback.removeAll(nodeCoveredByWrongFeedback);
		
		for (TraceNode relatedNode : this.filteredSameLineOfCode(nodeUnCoveredByWrongFeedback)) {
			ByteCodeList byteCodeList = new ByteCodeList(relatedNode.getBytecode());
			for (ByteCode byteCode : byteCodeList) {
				this.bytecode_nf[byteCode.getOpcode()] += 1;
			}
		}
	}
	
	protected List<TraceNode> filteredSameLineOfCode(final Collection<TraceNode> nodes) {
		List<TraceNode> filteredNodes = new ArrayList<>();
		Set<BreakPoint> existingBreakPoints = new HashSet<>();
		
		for (TraceNode node : nodes) {
			if (!existingBreakPoints.contains(node.getBreakPoint())) {
				existingBreakPoints.add(node.getBreakPoint());
				filteredNodes.add(node);
			}
		}
		return filteredNodes;
	}
	
	protected double calBytecodeScore(final ByteCode byteCode) {
		final int cf = this.bytecode_cf[byteCode.getOpcode()];
		final int cs = this.bytecode_cs[byteCode.getOpcode()];
		final int nf = this.bytecode_nf[byteCode.getOpcode()];
		
		return cf / ((double) cf + cs + nf + this.eps);
	}
	
}
