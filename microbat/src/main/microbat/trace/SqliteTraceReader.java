/**
 * 
 */
package microbat.trace;

import java.sql.SQLException;
import java.util.List;

import microbat.instrumentation.output.RunningInfo;
import microbat.instrumentation.precheck.PrecheckInfo;
import microbat.model.trace.Trace;
import microbat.sql.TraceRetriever01;

/**
 * @author knightsong
 *
 */
public class SqliteTraceReader implements TraceReader {

	public SqliteTraceReader() {

	}

	/*
	 * @see microbat.trace.TraceReader#Read()
	 */
	@Override
	public RunningInfo Read(PrecheckInfo precheckInfo, String drumpFile) {
		TraceRetriever01 traceRetriever01 = new TraceRetriever01(drumpFile);
		RunningInfo info = new RunningInfo();
		Trace trace = null;
		try {
			trace = traceRetriever01.retrieveTrace(traceRetriever01.getLatestTraces(precheckInfo.getProgramMsg()))
					.get(0);
			info.setTrace(trace);
			info.setCollectedSteps(trace.getExecutionList().size());
			info.setExpectedSteps(precheckInfo.getStepTotal());
			info.setProgramMsg(precheckInfo.getProgramMsg());

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return info;
	}

	@Override
	public List<Trace> ReadTraces(PrecheckInfo info, String drumpFile) {
		TraceRetriever01 traceRetriever01 = new TraceRetriever01(drumpFile);
		List<Trace> traces = null;
		try {
			traces= traceRetriever01.retrieveTrace(traceRetriever01.getLatestTraces(info.getProgramMsg()));

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return traces;
	}

}
