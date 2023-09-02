package microbat.views.utils.listeners;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import microbat.debugpilot.NodeFeedbacksPair;
import microbat.model.trace.TraceNode;
import microbat.views.PathView;
import microbat.views.TraceView;
import microbat.views.utils.contentprovider.ActionPathContentProvider;

public class PathViewSelectionListener implements ISelectionChangedListener {
	
	private PathView parentPathView = null;
	private ArrayList<TraceView> attachedTraceViews = new ArrayList<>();
	
	public PathViewSelectionListener(PathView parentPathView) {
		this.parentPathView = parentPathView;
	}
	

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection iSel = event.getSelection();
		
		if (iSel instanceof StructuredSelection) {
			StructuredSelection sel = (StructuredSelection) iSel;
			Object obj = sel.getFirstElement();
			if (obj instanceof ActionPathContentProvider.ContentWrapper) {				
				ActionPathContentProvider.ContentWrapper contentWrapper = (ActionPathContentProvider.ContentWrapper) obj;
				NodeFeedbacksPair node = contentWrapper.getNode();
				this.parentPathView.otherViewsBehaviour(node.getNode());

			}
			
		}
		
	}
	
}
