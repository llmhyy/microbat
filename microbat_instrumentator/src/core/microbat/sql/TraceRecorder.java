/**
 * 
 */
package microbat.sql;

import java.util.List;

import microbat.model.trace.Trace;

/**
 * @author knightsong
 *
 */
public interface TraceRecorder {
	void store(List<Trace> trace);
}
