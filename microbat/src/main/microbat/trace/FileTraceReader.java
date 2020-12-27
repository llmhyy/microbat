/**
 * 
 */
package microbat.trace;

import java.util.List;

import microbat.instrumentation.output.RunningInfo;
import microbat.instrumentation.precheck.PrecheckInfo;
import microbat.model.trace.Trace;

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
