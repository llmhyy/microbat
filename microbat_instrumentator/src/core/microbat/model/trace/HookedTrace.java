package microbat.model.trace;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import microbat.sql.IntervalRecorder;
import sav.strategies.dto.AppJavaClassPath;

/**
 * This class manages the tracenodes and queues the node for storage if the threshold is reached
 *
 */
public class HookedTrace extends Trace{
	private final IntervalRecorder recorder;
	private List<TraceNode> dependencies = new ArrayList<>();
	private List<TraceNode> nodeCache = new ArrayList<>();

	public HookedTrace(String traceId, AppJavaClassPath appJavaClassPath, IntervalRecorder recorder) {
		super(appJavaClassPath, traceId);
		this.recorder = recorder;
	}
	
	@Override
	public void addTraceNode(TraceNode node){
		super.addTraceNode(node);
		nodeCache.add(node);
		if (this.getExecutionList().size() > recorder.getThreshold()) {
			this.recorder.partialStore(getTraceId(), nodeCache); // store what hasn't been stored
			nodeCache = new ArrayList<>(); // remove from cache
			List<TraceNode> executionList = new ArrayList<>(dependencies); // set new nodes as those with invocation parent + latest node
			executionList.add(node);
			setExecutionList(executionList);
		} else {
			 if (node.getInvocationParent() != null) {
				 dependencies.add(node);
			 }
		}
	}
}
