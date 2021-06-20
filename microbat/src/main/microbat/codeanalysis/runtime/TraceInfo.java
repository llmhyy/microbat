package microbat.codeanalysis.runtime;

import java.util.List;
import java.util.Optional;

import microbat.model.trace.Trace;

/**
 * Wrapper class around trace lists and main trace,
 * Use with Optional to enforce error handling empty trace
 * @author dingyuchen
 *
 */
public class TraceInfo {
	private List<Trace>	traces;
	private Trace mainTrace;
	
	public TraceInfo() {
		new TraceInfo(Optional.empty());
	}
	
	public TraceInfo(List<Trace> traces) {
		if (traces == null || traces.isEmpty()) {
			this.traces = null;
		} else {
			this.traces = traces;
			this.mainTrace = getMain(traces);
		}
	}
	
	public TraceInfo(Optional<List<Trace>> traces) {
		new TraceInfo(traces.orElse(null));
	}
	
	private static Trace getMain(List<Trace> traces) {
		return traces.stream()
					.filter(trace -> trace.isMain())
					.findFirst().orElse(null);
	}
	
	public Optional<List<Trace>> getTraces() {
		return Optional.ofNullable(this.traces);
	}
	
	public Optional<Trace> getMainTrace() {
		return Optional.ofNullable(this.mainTrace);
	}
}
