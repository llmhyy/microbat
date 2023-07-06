package microbat.probability.SPP.propagation;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import debuginfo.NodeFeedbacksPair;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.probability.PropProbability;

public class SPPRL extends SPPRLTrain {
	
	public SPPRL(Trace trace, List<TraceNode> slicedTrace, Set<VarValue> correctVars, Set<VarValue> wrongVars,
			Collection<NodeFeedbacksPair> feedbackRecords) {
		super(trace, slicedTrace, correctVars, wrongVars, feedbackRecords);
	}
	
	@Override
	protected void forwardProp() {
		try {
			super.forwardProp();
			this.forwardClient.disconnectServer();
		} catch (IOException e) {
			throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
		}
	}
	
	@Override
	protected void backwardProp() {
		try {
			super.backwardProp();
			this.backwardClient.disconnectServer();
		} catch (IOException e) {
			throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
		}
	}
	

}
