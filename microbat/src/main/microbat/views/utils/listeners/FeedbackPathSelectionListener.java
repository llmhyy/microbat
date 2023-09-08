package microbat.views.utils.listeners;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import microbat.debugpilot.NodeFeedbacksPair;
import microbat.views.PathView;
import microbat.views.utils.contentprovider.ActionPathContentProvider;

public class FeedbackPathSelectionListener implements ISelectionChangedListener {
	
	private PathView parentPathView;

	public FeedbackPathSelectionListener(PathView parentPathView) {
		this.parentPathView = parentPathView;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection iSel = event.getSelection();
		
		if (iSel instanceof StructuredSelection structuredSelection) {
			Object obj = structuredSelection.getFirstElement();
			if (obj instanceof NodeFeedbacksPair nodeFeedbackPairs) {				
				this.parentPathView.otherViewsBehaviour(nodeFeedbackPairs.getNode());
			}
		}
	}
	
}
