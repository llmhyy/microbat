/**
 * 
 */
package microbat.sql;

import microbat.model.trace.Trace;

/**
 * @author knightsong
 *
 */
public interface TraceRecorder {
void store(Trace trace);
}
