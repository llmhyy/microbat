package microbat.instrumentation.output.filedb;

import microbat.filedb.store.FileDb;
import microbat.model.trace.ExecutionMetadata;
import microbat.model.trace.Trace;

/**
 * @author LLT
 *
 */
public class TraceFiledbService {
	private FileDb fileDb;

	public TraceFiledbService() {
		fileDb = new FileDb();
	}

	public void storeExecutionMetadata(ExecutionMetadata metadata) {
		fileDb.insert(metadata, ExecutionMetadata.class);
	}

	public void storeTrace(Trace trace) {
		fileDb.insert(trace, Trace.class);
	}
}
