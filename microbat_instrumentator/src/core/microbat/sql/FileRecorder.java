/**
 * 
 */
package microbat.sql;

import java.io.IOException;
import java.util.List;

import microbat.instrumentation.Agent;
import microbat.instrumentation.AgentLogger;
import microbat.instrumentation.AgentParams;
import microbat.instrumentation.output.RunningInfo;
import microbat.model.trace.Trace;

/**
 * @author knightsong
 *
 */
public class FileRecorder implements TraceRecorder {
	
	AgentParams agentParams;
	public FileRecorder(AgentParams agentParams) {
		this.agentParams= agentParams;
	}

	/* 
	 * @see microbat.sql.TraceRecorder#store(microbat.model.trace.Trace)
	 */
	@Override
	public void store(List<Trace> traceList) {
		
//		Trace trace = traceList.get(0);
		
		int collectedSteps = traceList.get(0).getExecutionList().size();
		int expectedSteps = agentParams.getExpectedSteps();
		RunningInfo result = new RunningInfo(Agent.getProgramMsg(), traceList, collectedSteps, expectedSteps);
		try {
			result.saveToFile(agentParams.getDumpFile(), false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		AgentLogger.debug(result.toString());
		
	}

}
