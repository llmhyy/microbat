/**
 * 
 */
package microbat.sql;

import java.io.IOException;

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
	public void store(Trace trace) {
		RunningInfo result = new RunningInfo();
		result.setProgramMsg(Agent.getProgramMsg());
		result.setTrace(trace);
		result.setCollectedSteps(trace.getExecutionList().size());
		result.setExpectedSteps(agentParams.getExpectedSteps());
		try {
			result.saveToFile(agentParams.getDumpFile(), false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		AgentLogger.debug(result.toString());
		
	}

}