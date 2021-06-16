/**
 * 
 */
package microbat.trace;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import microbat.instrumentation.output.RunningInfo;
import microbat.instrumentation.precheck.PrecheckInfo;
import microbat.model.trace.Trace;
import microbat.sql.DbService;
import microbat.sql.SqliteTraceRetriever;
import microbat.sql.TraceRetriever;
import sav.commons.testdata.calculator.CalculatorTest1;

/**
 * @author knightsong
 *
 */
public class SqliteTraceReader implements TraceReader {
	private TraceRetriever traceRetriever;
	private String runId;

	public SqliteTraceReader(String runId) {
		Connection conn = null;
		try {
			conn = DbService.getConnection();
		} catch (SQLException e) {
			// should not fail here, since SqliteTrace is selected
			e.printStackTrace();
		}
		this.traceRetriever = new SqliteTraceRetriever(conn);
		this.runId = runId;
	}

	/*
	 * @see microbat.trace.TraceReader#Read()
	 */
	@Override
	public RunningInfo read(PrecheckInfo precheckInfo, String drumpFile) {
		
		List<Trace> traces = this.traceRetriever.getTraces(runId);
		
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
