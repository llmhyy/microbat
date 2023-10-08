package microbat.debugpilot.propagation.spp;

import java.util.Collection;
import java.util.List;

import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.debugpilot.NodeFeedbacksPair;
import microbat.debugpilot.settings.PropagatorSettings;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class SPP_C extends SPP {
	
	public SPP_C(final PropagatorSettings settings) {
		this(settings.getTrace(), settings.getSlicedTrace(), settings.getFeedbacks());
	}
	
	public SPP_C(final Trace trace, final List<TraceNode> sliceTraceNodes, final Collection<NodeFeedbacksPair> feedbacksPairs) {
		super(trace, sliceTraceNodes, feedbacksPairs);
	}
	
	@Override
	public void propagate() {
		this.initConfirmed();
		this.initSuspiciousScore();
		this.calComputationalSuspiciousScore();
		this.calSuspiciousScoreVariable();
		this.normalizeVariableSuspicious();
	}

	protected void initConfirmed() {
		this.slicedTrace.stream().forEach(node -> node.confirmed = false);
	}
	
	protected void initSuspiciousScore() {
		this.slicedTrace.stream().forEach(node -> node.setSuspicousness(0.0d));
	}
	
	protected void calComputationalSuspiciousScore() {
		final long totalNodeCost = this.slicedTrace.stream().mapToLong(node -> this.countModifyOperation(node)).sum();
		if (totalNodeCost != 0) {
			this.slicedTrace.stream().forEach(node -> {
				final double computationSuspiciousness = this.countModifyOperation(node) / (double) totalNodeCost;
				node.addSuspiciousness(computationSuspiciousness);
			});
		}
	}
	
	protected void calSuspiciousScoreVariable() {
		final double eps = 1e-10;
		this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).forEach(var -> var.setSuspiciousness(eps));
		this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.setSuspiciousness(eps));
		
		for (TraceNode node : this.slicedTrace) {
			List<VarValue> readVars = node.getReadVariables();
			
			if (readVars.isEmpty()) {
				continue;
			}
			
			// Inherit computation cost from data dominator
			for (VarValue readVar : readVars) {
				final VarValue dataDomVar = this.findDataDomVar(readVar, node);
				if (dataDomVar != null) {
					readVar.setSuspiciousness(dataDomVar.getSuspiciousness());
				}
			}
			
			double cumulatedScore = readVars.stream().mapToDouble(var -> var.getSuspiciousness() * var.getSuspiciousness()).sum();
			cumulatedScore = Math.sqrt(cumulatedScore);
			cumulatedScore += node.getSuspicousness();
			if (node.getControlDominator()!= null) {
				cumulatedScore += node.getControlDominator().getConditionResult().getSuspiciousness();
			}
			final double cumulatedScore_ = cumulatedScore;
			node.getWrittenVariables().stream().forEach(var -> var.setSuspiciousness(cumulatedScore_));
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
				this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).mapToDouble(var -> var.getSuspiciousness()).max().orElse(0.0d),
				this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).mapToDouble(var -> var.getSuspiciousness()).max().orElse(0.0d)
		);
		if (maxSuspicious != 0.0d) {
			this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).forEach(var -> var.setSuspiciousness(var.getSuspiciousness() / maxSuspicious));
			this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.setSuspiciousness(var.getSuspiciousness() / maxSuspicious));
		}
	}
	
	protected int countModifyOperation(final TraceNode node) {
		ByteCodeList byteCodeList = new ByteCodeList(node.getBytecode());
		int count = 0;
		for (ByteCode byteCode : byteCodeList) {
			if (!byteCode.isComputational()) {
				count+=1;
			}
		}
		return count;
	}
}
