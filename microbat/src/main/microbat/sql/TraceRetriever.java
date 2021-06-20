package microbat.sql;

import java.sql.SQLException;
import java.util.List;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public interface TraceRetriever {
	List<Trace> getTraces(String runId);
	List<TraceNode> getSteps(Trace trace) throws SQLException;
	void loadRWVars(TraceNode step, String traceId);
}
