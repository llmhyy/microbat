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
	public RunningInfo Read(PrecheckInfo precheckInfo,String dumpFile) {
		return RunningInfo.readFromFile(dumpFile);
	}

	/* (non-Javadoc)
	 * @see microbat.trace.TraceReader#ReadTraces(microbat.instrumentation.precheck.PrecheckInfo, java.lang.String)
	 */
	@Override
	public List<Trace> ReadTraces(PrecheckInfo info, String drumpFile) {
		// TODO Auto-generated method stub
		return null;
	}


}
