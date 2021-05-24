/**
 * 
 */
package microbat.trace;

import java.sql.SQLException;
import java.util.List;

import microbat.instrumentation.output.RunningInfo;
import microbat.instrumentation.precheck.PrecheckInfo;
import microbat.model.trace.Trace;
import microbat.sql.SqliteTraceRetriever;

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
	public RunningInfo read(PrecheckInfo precheckInfo, String drumpFile) {
		
		SqliteTraceRetriever traceRetriever01 = new SqliteTraceRetriever();
		List<Trace> traces = null;
		try {
			List<String> latestTraces = traceRetriever01.getLatestTraces(precheckInfo.getProgramMsg());
			traces= traceRetriever01.retrieveTrace(latestTraces);

		} catch (SQLException e) {
			e.printStackTrace();
		}
//		return traces;
		
		RunningInfo info = new RunningInfo();
		info.setTraceList(traces);
		if (traces.size() == 0) {
			info.setCollectedSteps(0);
		} else {
			info.setCollectedSteps(traces.get(0).getExecutionList().size());
		}
//		info.setCollectedSteps(traces.get(0).getExecutionList().size());
		info.setExpectedSteps(precheckInfo.getStepTotal());
		info.setProgramMsg(precheckInfo.getProgramMsg());
		
//		TraceRetriever01 traceRetriever01 = new TraceRetriever01(drumpFile);
//		Trace trace = null;
//		try {
//			trace = traceRetriever01.retrieveTrace(traceRetriever01.getLatestTraces(precheckInfo.getProgramMsg()))
//					.get(0);
//
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
		return info;
	}

//	@Override
//	public List<Trace> ReadTraces(PrecheckInfo info, String drumpFile) {
//		
//	}

}
