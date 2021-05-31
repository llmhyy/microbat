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
import microbat.sql.TraceRetriever;
import sav.commons.testdata.calculator.CalculatorTest1;

/**
 * @author knightsong
 *
 */
public class SqliteTraceReader implements TraceReader {
	private TraceRetriever traceRetriever;

	public SqliteTraceReader() {
		this.traceRetriever = new SqliteTraceRetriever();
	}

	/*
	 * @see microbat.trace.TraceReader#Read()
	 */
	@Override
	public RunningInfo read(PrecheckInfo precheckInfo, String drumpFile) {
		
		List<Trace> traces = this.traceRetriever.getLatestTraces();
		
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
		
		return info;
	}

}
