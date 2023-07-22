package microbat.pyserver;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.model.variable.Variable;
import microbat.recommendation.UserFeedback;
import microbat.vectorization.vector.ContextVector;
import microbat.vectorization.vector.FeedbackVector;
import microbat.vectorization.vector.NodeVector;
import microbat.vectorization.vector.VariableVector;

public abstract class RLModelClient extends Client {
	
	public RLModelClient(String host, int port) {
		this(host, port, false);
	}
	
	public RLModelClient(String host, int post, boolean verbose) {
		super(host, post, verbose);
	}

	public void sendNodeFeature(final TraceNode node) throws IOException, InterruptedException {
		final NodeVector vector = new NodeVector(node);
		this.sendMsg(vector.toString());
	}
	
	public void sendVariableVector(final VarValue var ) throws IOException, InterruptedException {
		VariableVector vector;
		if (var == null) {
			vector = new VariableVector();
		} else if (var.isConditionResult()) {
			vector = new VariableVector();
		} else {
			vector = new VariableVector(var);
		}
		this.sendMsg(vector.toString());
	}
	
	public void sendVariableName(final VarValue var) throws IOException, InterruptedException {
		if (var == null) {
			this.sendMsg(" ");
		} else {
			this.sendMsg(var.getVarName());
		}
	}

	
	public void sendContextFeature(final TraceNode node) throws IOException, InterruptedException {
		final Trace trace = node.getTrace();
		// Send node order
		this.sendNodeOrder(node);
		// Send target node feature
		this.notifyContinuoue();
		this.sendNodeFeature(node);
		// Send data dominator
		for (int idx=0; idx<ContextVector.NUM_DATA_DOMS; idx++) {
			this.notifyContinuoue();
			if (idx<node.getReadVariables().size()) {
				final VarValue readVar = node.getReadVariables().get(idx);
				final TraceNode dataDom = trace.findDataDependency(node, readVar);
				if (dataDom != null) {
					this.sendNodeFeature(node);
				} else {
					this.sendEmptyNodeFeature();
				}
			} else {
				this.sendEmptyNodeFeature();
			}
		}
		
		// Send control dominator
		final TraceNode controlDom = node.getControlDominator();
		this.notifyContinuoue();
		if (controlDom == null) {
			this.sendEmptyNodeFeature();
		} else {
			this.sendNodeFeature(controlDom);
		}
		
		// Send data dominatees
		for (int idx=0; idx<ContextVector.NUM_DATA_DOMEES; idx++) {
			this.notifyContinuoue();
			if (idx<node.getWrittenVariables().size()) {
				final VarValue writtenVar = node.getWrittenVariables().get(idx);
				List<TraceNode> dataDominatees = trace.findDataDependentee(node, writtenVar);
				if (dataDominatees.isEmpty()) {
					this.sendEmptyNodeFeature();
				} else {
					final TraceNode dataDominatee = dataDominatees.get(0);
					this.sendNodeFeature(dataDominatee);
				}
			} else {
				this.sendEmptyNodeFeature();
			}
		}
		
		// Send control dominatees
		List<TraceNode> controlDominatees = node.getControlDominatees();
		for (int idx=0; idx<ContextVector.NUM_CONTROL_DOMEES; idx++) {
			this.notifyContinuoue();
			if (idx<controlDominatees.size()) {
				final TraceNode controlDominatee = controlDominatees.get(idx);
				this.sendNodeFeature(controlDominatee);
			} else {
				this.sendEmptyNodeFeature();
			}
		}
		this.notifyStop();
	}
	
	public void sendNodeOrder(final int nodeOrder) throws IOException, InterruptedException {
		this.sendMsg(String.valueOf(nodeOrder));
	}
	
	public void sendNodeOrder(final TraceNode node) throws IOException, InterruptedException {
		final int order = node.getOrder();
		this.sendMsg(String.valueOf(order));
	}
	
	public void sendEmptyNodeFeature() throws IOException, InterruptedException {
		final NodeVector vector = new NodeVector();
		this.sendMsg(vector.toString());
	}
	
	public double receiveFactor() throws IOException {
		String message = this.receiveMsg();
		return Double.valueOf(message);
	}

	public String recieveModelPredictionString() throws IOException {
		return this.receiveMsg();
	}
	
	public double receiveAlpha() throws IOException {
		final String message = this.receiveMsg();
		return Double.valueOf(message);
	}
	
	public void sendFeedbackVectors(final Collection<NodeFeedbacksPair> pairs) throws IOException, InterruptedException {
		for (NodeFeedbacksPair pair : pairs) {
			this.notifyContinuoue();
			this.sendFeedbackVector(pair);
		}
		this.notifyStop();
	}
	
	protected void sendFeedbackVector(final NodeFeedbacksPair pair) throws IOException, InterruptedException {
		final TraceNode node = pair.getNode();
		for (UserFeedback feedback : pair.getFeedbacks()) {
			this.sendFeedbackVector(node, feedback);
		}
	}
	
	protected void sendFeedbackVector(final TraceNode node, final UserFeedback feedback) throws IOException, InterruptedException {
		this.sendContextFeature(node);
		FeedbackVector feedbackVector = new FeedbackVector(feedback);
		this.sendMsg(feedbackVector.toString());
		
		VarValue var;
		if (feedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
			var = feedback.getOption().getReadVar();
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
			var = node.getControlDominator().getConditionResult();
		} else {
			var = null;
		}
		this.sendVariableVector(var);
		this.sendVariableName(var);
	}

}
