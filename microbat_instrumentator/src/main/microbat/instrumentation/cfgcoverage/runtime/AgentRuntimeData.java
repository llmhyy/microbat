package microbat.instrumentation.cfgcoverage.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;

public class AgentRuntimeData {
	public static volatile CoverageSFlowGraph coverageFlowGraph;
	public static volatile Map<Integer, List<MethodExecutionData>> methodExecsOnASingleTcMap = new HashMap<>();
	public static volatile Map<Long, Integer> currentTestIdxMap = new HashMap<>();
	public static volatile Map<TracerKey, List<ICoverageTracer>> tracerMap = new HashMap<>();
	
	
	public static synchronized void register(ICoverageTracer tracer, long threadId, int testIdx) {
		TracerKey tracerKey = TracerKey.of(threadId, testIdx);
		List<ICoverageTracer> tracers = tracerMap.get(tracerKey);
		if (tracers == null) {
			tracers = new ArrayList<>();
			tracerMap.put(tracerKey, tracers);
		}
		tracers.add(tracer);
	}
	
	public static synchronized void unregister(long threadId, Integer testIdx) {
		TracerKey tracerKey = TracerKey.of(threadId, testIdx);
		List<ICoverageTracer> tracers = tracerMap.remove(tracerKey);
		if (tracers != null) {
			for (ICoverageTracer tracer : tracers) {
				tracer.shutDown();
			}
 		}
	}
	
	private static class TracerKey {
		long threadId;
		int testIdx;
		
		public static TracerKey of(long threadId, Integer testIdx) {
			TracerKey key = new TracerKey();
			key.threadId = threadId;
			key.testIdx = testIdx;
			return key;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + testIdx;
			result = prime * result + (int) (threadId ^ (threadId >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof TracerKey))
				return false;
			TracerKey other = (TracerKey) obj;
			if (testIdx != other.testIdx)
				return false;
			if (threadId != other.threadId)
				return false;
			return true;
		}
	}
}
