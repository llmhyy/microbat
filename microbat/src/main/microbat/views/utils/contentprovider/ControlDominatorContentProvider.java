package microbat.views.utils.contentprovider;

import org.eclipse.jface.viewers.IStructuredContentProvider;

import microbat.model.trace.TraceNode;

public class ControlDominatorContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof TraceNode traceNode) {
			return new TraceNode[] {traceNode};
		}
		return null;
	}
	
}
