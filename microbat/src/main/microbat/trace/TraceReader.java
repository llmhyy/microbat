/**
 * 
 */
package microbat.trace;

import microbat.instrumentation.output.RunningInfo;
import microbat.instrumentation.precheck.PrecheckInfo;

/**
 * @author SongXuezhi
 */
public interface TraceReader {
	/**
	 * @author SongXuezhi Please overwrite the method to read trace from any source
	 * @return Trace
	 */
	RunningInfo read(PrecheckInfo info, String drumpFile);
}
