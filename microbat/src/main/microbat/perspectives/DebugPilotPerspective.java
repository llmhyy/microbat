package microbat.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

import microbat.views.DebugPilotFeedbackView;
import microbat.views.MicroBatViews;
import microbat.views.PathView;

public class DebugPilotPerspective implements IPerspectiveFactory {

	protected static final String TOP_LEFT_LAYOUT_ID = "top_left";
	protected static final String BOTTOM_LEFT_LAYOUT_ID = "bottom_left";
	protected static final String BOTTOM_LAYOUT_ID = "bottom";
	protected static final String RIGHT_LAYOUT_ID = "right";
	
	@Override
	public void createInitialLayout(IPageLayout layout) {
		
		// Left view contain Trace View and Path View
		IFolderLayout topLeftFolder = layout.createFolder(DebugPilotPerspective.TOP_LEFT_LAYOUT_ID, IPageLayout.LEFT, 0.25f, layout.getEditorArea());
		topLeftFolder.addView(MicroBatViews.TRACE);
		
		IFolderLayout bottomLeftFolder = layout.createFolder(DebugPilotPerspective.BOTTOM_LEFT_LAYOUT_ID, IPageLayout.BOTTOM, 0.5f, DebugPilotPerspective.TOP_LEFT_LAYOUT_ID);
		bottomLeftFolder.addView(PathView.ID);
		
		IFolderLayout rightFolder = layout.createFolder(DebugPilotPerspective.RIGHT_LAYOUT_ID, IPageLayout.RIGHT, 0.6f, layout.getEditorArea());
		rightFolder.addView(DebugPilotFeedbackView.ID);
		
		IFolderLayout bottomFolder = layout.createFolder(DebugPilotPerspective.BOTTOM_LAYOUT_ID, IPageLayout.BOTTOM, 0.5f, layout.getEditorArea());
		bottomFolder.addView(MicroBatViews.REASON);
		bottomFolder.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);

		layout.setEditorAreaVisible(true);
		
	}
	

}
