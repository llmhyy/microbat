/**
 * 
 */
package microbat.trace;

import microbat.instrumentation.output.RunningInfo;
import microbat.instrumentation.precheck.PrecheckInfo;

/**
 * @author knightsong
 *
 */
public class FileTraceReader implements TraceReader {

	
	@Override
	public RunningInfo read(PrecheckInfo precheckInfo,String dumpFile) {
		return RunningInfo.readFromFile(dumpFile);
	}


}
