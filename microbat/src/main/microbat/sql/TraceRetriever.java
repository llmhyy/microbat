package microbat.sql;

import java.util.List;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public interface TraceRetriever {
	List<Trace> getLatestTraces();
	void loadRWVars(TraceNode step, String traceId);
}
