package microbat.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;

import microbat.concurrent.model.ConcurrentTrace;
import microbat.concurrent.model.ConcurrentTraceNode;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

/**
 * Class representing a concurrent trace view
 * linked to a sequential trace
 * @author Gabau
 *
 */
public class ConcurrentTraceViewSeq extends ConcurrentTraceView {

	// view will not be rendered, but used for logic
//	SequentialConcurrentView linkedSequentialView;
	
	protected ConcurrentTrace inputConcurrentTrace;
	
	@Override
	public void jumpToNode(TraceNode node) {
		if (!node.getLinkedConcurrentNode().isPresent()) {
			super.jumpToNode(node);
			return;
		}
		super.jumpToNode(node.getLinkedConcurrentNode().get());
	}
	
	@Override
	protected void otherViewsBehavior(TraceNode node) {
		// send info to the linked view
		if (node.getLinkedConcurrentNode().isPresent()) {
			super.otherViewsBehavior(node.getLinkedConcurrentNode().get());
			return;
		}
		super.otherViewsBehavior(node);
	}

	@Override
	public Trace getCurrentTrace() {
		// TODO Auto-generated method stub
		return this.inputConcurrentTrace;
	}

	@Override
	public Trace getTrace() {
		// TODO Auto-generated method stub
		return this.inputConcurrentTrace;
	}
	
	@Override
	public void jumpToNode(Trace trace, int order, boolean refreshProgramState) {
		if (!(trace instanceof ConcurrentTrace)) {
			throw new RuntimeException("Wrong trace passed");
		}
		assert order > 0;
		ConcurrentTrace concTrace = (ConcurrentTrace) trace;
		ConcurrentTraceNode node = concTrace.getSequentialTrace().get(order - 1);
		otherViewsBehavior(node.getInitialTraceNode());
		TraceNode tmpNode = node.getInitialTraceNode();
		List<TraceNode> path = new ArrayList<>();
		while (tmpNode != null) {
			path.add(tmpNode);
			tmpNode = tmpNode.getAbstractionParent();
		}

		/* Update the current tree */
		curTreeViewer = getTreeViewerByThreadID(node.getCurrentThread());
		
		/** keep the original expanded list */
		Object[] expandedElements = curTreeViewer.getExpandedElements();
		for (Object obj : expandedElements) {
			TraceNode tn = (TraceNode) obj;
			path.add(tn);
		}

		TraceNode[] list = path.toArray(new TraceNode[0]);
		curTreeViewer.setExpandedElements(list);

		tmpNode = node.getInitialTraceNode();

		programmingSelection = true;
		this.refreshProgramState = refreshProgramState;
		/**
		 * This step will trigger a callback function of node selection.
		 */
		curTreeViewer.setSelection(new StructuredSelection(tmpNode), true);
		programmingSelection = false;
		this.refreshProgramState = true;

		curTreeViewer.refresh();
	}

	/**
	 * The method used to set input for sequential concurrent trace.
	 * @param trace
	 */
	public void setInput(ConcurrentTrace trace) {
		this.inputConcurrentTrace = trace;
		this.setTraceList(trace.getTraceList());
		this.setMainTrace(trace.getMainTrace());
		this.updateData();
	}
	
}
