/**
 * 
 */
package microbat.sql;

import java.util.List;

import microbat.instrumentation.Agent;
import microbat.instrumentation.AgentParams;
import microbat.instrumentation.output.TraceOutputWriter;
import microbat.instrumentation.output.tcp.TcpConnector;
import microbat.model.trace.Trace;

/**
 * 
 *
 */
public class SocketRecorder implements TraceRecorder{
	AgentParams agentParams;
	public SocketRecorder(AgentParams agentParams) {
		this.agentParams=agentParams;
	}
	/* 
	 * @see microbat.sql.TraceRecorder#store(microbat.model.trace.Trace)
	 */
	@Override
	public void store(List<Trace> traceList) {
		TcpConnector tcpConnector = new TcpConnector(agentParams.getTcpPort());
		TraceOutputWriter traceWriter;
		try {
			traceWriter = tcpConnector.connect();
			traceWriter.writeString(Agent.getProgramMsg());
			traceWriter.writeTrace(traceList);
			traceWriter.flush();
			Thread.sleep(10000l);
		} catch (Exception e) {
			e.printStackTrace();
		}
		tcpConnector.close();
	}

}
