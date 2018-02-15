package microbat.agent;

import java.io.BufferedReader;

import microbat.instrumentation.trace.InstrConstants;
import microbat.model.trace.Trace;
import sav.common.core.utils.SingleTimer;
import sav.strategies.vm.interprocess.ServerOutputReader;
import sav.strategies.vm.interprocess.socket.SocketAgentVmRunner;

public class TraceAgentRunner extends SocketAgentVmRunner {
	private TraceOutputReader traceReader;
	
	public TraceAgentRunner(String agentJar) {
		this(agentJar, new TraceOutputReader());
	}

	public TraceAgentRunner(String agentJar, TraceOutputReader traceReader) {
		super(null, traceReader, agentJar);
		this.traceReader = traceReader;
		setAgentOptionSeparator(InstrConstants.AGENT_OPTION_SEPARATOR);
		setAgentParamsSeparator(InstrConstants.AGENT_PARAMS_SEPARATOR);
	}

	public Trace getTrace() {
		return traceReader.getTrace();
	}

	private static class TraceOutputReader extends ServerOutputReader {
		private Trace trace;
		
		@Override
		public boolean isMatched(String line) {
			return InstrConstants.INSTRUMENT_RESULT.equals(line);
		}

		@Override
		protected void readData(BufferedReader br) {
			// TODO Auto-generated method stub

		}
		
		public Trace getTrace() {
			SingleTimer timer = SingleTimer.start("read output");
			while (isWaiting()) {
				if (timer.getExecutionTime() > 1000l) {
					System.out.println("timeout!");
					return null;
				}
			}
			waiting();
			return trace;
		}

	}
}
