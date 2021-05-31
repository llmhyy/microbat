package microbat.sql;

import java.util.List;
import microbat.model.trace.Trace;

public interface TraceRetriever {
	List<Trace> getLatestTraces();
}
