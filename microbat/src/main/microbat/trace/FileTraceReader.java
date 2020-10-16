/**
 * 
 */
package microbat.trace;

import microbat.instrumentation.output.RunningInfo;
import microbat.instrumentation.precheck.PrecheckInfo;
import microbat.model.trace.Trace;

/**
 * @author knightsong
 *
 */
public class FileTraceReader implements TraceReader {

	
	@Override
	public RunningInfo Read(PrecheckInfo precheckInfo,String dumpFile) {
		return RunningInfo.readFromFile(dumpFile);
	}


}
