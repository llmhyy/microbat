package microbat.baseline;

import java.util.List;

import microbat.baseline.probpropagation.PropProbability;
import microbat.evaluation.model.TraceNodePair;
import microbat.model.trace.Trace;
import microbat.model.value.VarValue;

public class AutoFeedback {
	/* 
	 * Emulate the feedback that a user will provide
	 * based on the correct trace that is provided
	 */
	public boolean feedback(Trace buggyTrace, TraceNodePair pair) {
		if (pair.isExactSame()) {
			// not the right node
			pair.getOriginalNode().setProbability(PropProbability.HIGH);
			return false;
		} else {
			List<VarValue> wrongRead = pair.findSingleWrongReadVar(buggyTrace);
			if (wrongRead.size() == 0) {
				// no wrongly read variable --> likely to be root cause
				return true;
			}
			List<VarValue> wrongWrite = pair.findSingleWrongWrittenVarID(buggyTrace);
			wrongRead.addAll(wrongWrite);
			for (VarValue v : wrongWrite) {
				v.setProbability(PropProbability.HIGH);
			}
			return false;
		}
	}
}
