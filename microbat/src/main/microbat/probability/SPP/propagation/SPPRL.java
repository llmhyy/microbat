package microbat.probability.SPP.propagation;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import debuginfo.NodeFeedbacksPair;
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
	protected double calForwardFactor(final TraceNode node) {
		try {
			this.sendNodeFeatures(node, this.forwardClient);
			this.forwardClient.notifyVectorEnd();
			double factor = this.forwardClient.recieveFactor();
			return factor;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return -1.0d;
		}
	}
	
	@Override
	protected double calBackwardFactor(final VarValue var, final TraceNode node) {
		try {
			this.backwardClient.sendVarFeature(var);
			this.sendNodeFeatures(node, this.backwardClient);
			this.backwardClient.notifyVectorEnd();
			double factor = this.backwardClient.recieveFactor();
			return factor;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return -1.0d;
		}
	}
	
	protected void sendNodeFeatures(final TraceNode node, RLModelClient client) throws IOException, InterruptedException {
		List<VarValue> readVars = node.getReadVariables().stream().filter(var -> !var.isThisVariable()).toList();
		List<VarValue> writtenVars = node.getWrittenVariables().stream().filter(var -> !var.isThisVariable()).toList();
		
		// Target node feature
		client.sendNodeFeature(node);
		
		// Data dominators feature
		for (int idx=0; idx<this.numDataDoms; idx++) {
			if (idx<readVars.size()) {
				final VarValue readVar = readVars.get(idx);
				final TraceNode dataDom = this.trace.findDataDependency(node, readVar);
				client.sendNodeFeature(dataDom);
			} else {
				client.sendEmptyNodeFeature();;
			}
		}
		
		// Control dominator feature
		final TraceNode controlDom = node.getControlDominator();
		if (controlDom == null) {
			client.sendEmptyNodeFeature();
		} else {
			client.sendNodeFeature(controlDom);
		}
		
		// Data dominatees features
		for (int idx=0; idx<this.numDataDomees; idx++) {
			if (idx<writtenVars.size()) {
				final VarValue writtenVar = writtenVars.get(idx);
				final List<TraceNode> dataDomees = this.trace.findDataDependentee(node, writtenVar);
				if (dataDomees.isEmpty()) {
					client.sendEmptyNodeFeature();
				} else {
					client.sendNodeFeature(dataDomees.get(0));
				}
			} else {
				client.sendEmptyNodeFeature();
			}
		}
		
		// Control dominatees features
		final List<TraceNode> controlDomatees = node.getControlDominatees();
		for (int idx=0; idx<this.numControlDomees; idx++) {
			if (idx<controlDomatees.size()) {
				final TraceNode controlDomee = controlDomatees.get(idx);
				client.sendNodeFeature(controlDomee);
			} else {
				client.sendEmptyNodeFeature();
			}
		}
	}
	
	@Override
	protected void calConditionBackwardProb(final TraceNode node, final VarValue conditionResult) {
		final double avgProb = conditionResult.getConditionBackwardProb().stream().mapToDouble(Double::doubleValue).average().orElse(PropProbability.UNCERTAIN);
		conditionResult.setBackwardProb(avgProb);
	}
	
	@Override
	protected void forwardProp() {
		super.forwardProp();
		try {
			this.forwardClient.notifyPropEnd();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
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
		
		try {
			this.backwardClient.notifyPropEnd();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
