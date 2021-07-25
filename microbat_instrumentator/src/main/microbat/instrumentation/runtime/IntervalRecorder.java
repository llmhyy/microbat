package microbat.instrumentation.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import microbat.model.trace.TraceNode;
import microbat.sql.SqliteRecorder;
import microbat.sql.Recorder;

public class IntervalRecorder {
	private static final int THRESHOLD = 1000;
	private final long threadId;
	private List<TraceNode> cache; 

	public IntervalRecorder(long threadId) {
		this.threadId = threadId;
		this.cache = new ArrayList<>();
	}
	
	public void add(TraceNode node) {
		if (cache.size() >= THRESHOLD) {
			// store traces
			CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
				Recorder.create(ExecutionTracer.agentParams).storeTraceNodes(this.threadId, this.cache);
			});
			cf.thenRun(() -> {
				System.out.println("inserted nodes");
			});
			this.cache = new ArrayList<>();
		}
		this.cache.add(node);
	}
}
