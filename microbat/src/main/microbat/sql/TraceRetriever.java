package microbat.sql;

import java.util.List;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import sav.common.core.Pair;

public interface TraceRetriever {
	List<Trace> getTraces(String runId);
	Pair<List<VarValue>, List<VarValue>> loadRWVars(TraceNode step, String traceId);
}
