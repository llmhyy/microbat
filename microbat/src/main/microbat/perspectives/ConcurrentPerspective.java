package microbat.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

import microbat.views.MicroBatViews;


/**
 *  This class is meant to serve as an example for how various contributions 
 *  are made to a perspective. Note that some of the extension point id's are
 *  referred to as API constants while others are hardcoded and may be subject 
 *  to change. 
 */
public class ConcurrentPerspective implements IPerspectiveFactory {
	
	private IPageLayout factory;

	public ConcurrentPerspective() {
		super();
	}

	public void createInitialLayout(IPageLayout factory) {
		this.factory = factory;
		addViews();
	}

	private void addViews() {
		// Creates the overall folder layout. 
		// Note that each new Folder uses a percentage of the remaining EditorArea.
		IFolderLayout bottom =
				factory.createFolder(
						"bottom", //NON-NLS-1
						IPageLayout.BOTTOM,
						0.55f,
						factory.getEditorArea());
		bottom.addView(MicroBatViews.CONCURRENT_TRACE);
		bottom.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
		
		IFolderLayout topLeft =
				factory.createFolder(
						"topLeft",
						IPageLayout.LEFT,
						0.15f,
						factory.getEditorArea());
		topLeft.addView(MicroBatViews.REASON);
		topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);
		
		IFolderLayout topRight =
				factory.createFolder(
						"topRight",
						IPageLayout.RIGHT,
						0.60f,
						factory.getEditorArea());
		topRight.addView(MicroBatViews.DEBUG_FEEDBACK); 
		
	}

}
