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
import microbat.sql.GraphTraceRetriever;
import microbat.sql.TraceRetrieverImpl;
import microbat.sql.TraceRetriever;
import sav.commons.testdata.calculator.CalculatorTest1;

public class GraphTraceReader implements TraceReader {
	private String runId;

	public GraphTraceReader(String runId) {
		this.runId = runId;
	}

	/*
	 * @see microbat.trace.TraceReader#Read()
	 */
	@Override
	public RunningInfo read(PrecheckInfo precheckInfo, String drumpFile) {
		List<Trace> traces = new GraphTraceRetriever(this.runId).getTraces(runId);
		
		int collectedSteps = traces.isEmpty() ? 0 : 
			traces.stream().mapToInt(trace -> trace.size()).sum();
		int expectedSteps = precheckInfo.getStepTotal();
		
		return new RunningInfo(precheckInfo.getProgramMsg(), traces, expectedSteps, collectedSteps);
	}

}

