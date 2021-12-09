/**
 * 
 */
package microbat.sql;

import java.util.List;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

/**
 * @author knightsong
 *
 */
public interface TraceRecorder {
	void store(List<Trace> trace);

	void insertSteps(String traceId, List<TraceNode> traces);
}
