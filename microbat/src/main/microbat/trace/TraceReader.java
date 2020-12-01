/**
 * 
 */
package microbat.trace;

import java.util.List;

import microbat.instrumentation.output.RunningInfo;
import microbat.instrumentation.precheck.PrecheckInfo;
import microbat.model.trace.Trace;

/**
 * @author SongXuezhi
 */
public interface TraceReader {
	/**
	 * @author SongXuezhi
	 * Please overwrite the method to read trace from any source
	 * @return Trace
	 */
	RunningInfo Read(PrecheckInfo info,String drumpFile);
	List<Trace> ReadTraces(PrecheckInfo info,String drumpFile);
}
