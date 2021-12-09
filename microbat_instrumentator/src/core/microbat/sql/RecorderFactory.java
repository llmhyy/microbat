package microbat.sql;

public class RecorderFactory {
	private final int recordThreshold;
	private final TraceRecorder recorderInstance;
	
	public RecorderFactory(int recordThreshold, TraceRecorder recorderInstance) {
		this.recordThreshold = recordThreshold;
		this.recorderInstance = recorderInstance;
	}
	
	public IntervalRecorder createRecorder() {
		return new IntervalRecorder(recordThreshold, recorderInstance);
	}
}
