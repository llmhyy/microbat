package microbat.views.utils.listeners;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import microbat.debugpilot.userfeedback.DPUserFeedback;
import microbat.views.PathView;

public class FeedbackPathSelectionListener implements ISelectionChangedListener {
	
	private PathView parentPathView;

	public FeedbackPathSelectionListener(PathView parentPathView) {
		this.parentPathView = parentPathView;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection iSel = event.getSelection();
//		UserBehaviorLogger.logEvent(UserBehaviorType.CHECK_PATH);
		if (iSel instanceof StructuredSelection structuredSelection) {
			Object obj = structuredSelection.getFirstElement();
			if (obj instanceof DPUserFeedback nodeFeedbackPairs) {				
				this.parentPathView.otherViewsBehaviour(nodeFeedbackPairs.getNode());
			}
		}
	}
	
}
