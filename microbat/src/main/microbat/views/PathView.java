package microbat.views;

import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.TraceNode;
import microbat.probability.SPP.pathfinding.ActionPath;
import microbat.views.listeners.PathViewSelectionListener;
import microbat.views.providers.ActionPathContentProvider;
import microbat.views.providers.FeedbackNodePairLabelProvider;

// todo: node: feedback -- ui
public class PathView extends ViewPart {
	public static final String ID = "microbat.evalView.pathView";
	
	protected ListViewer listViewer;
	protected Text searchText;	
	protected ActionPath actionPath;
	protected TraceView attached = null;
	protected Button searchButton;
	
	private PathViewSelectionListener selectionListener;

	private String previousSearchExpression = "";
	
	public PathView() {
		this.selectionListener = new PathViewSelectionListener(this);
	}
	
	public void setSearchText(String expression) {		
		this.searchText.setText(expression);
		// not sure why this is after
		this.previousSearchExpression = expression;
	}

	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		parent.setLayout(layout);
		createSearchBox(parent);
		createListView(parent);
		
	}
	
	private void createSearchBox(Composite parent) {
		searchText = new Text(parent, SWT.BORDER);
		searchText.setToolTipText("search trace node by class name and line number, e.g., ClassName line:20 or just ClassName\n"
				+ "press \"enter\" for forward-search and \"shift+enter\" for backward-search.");
		FontData searchTextFont = searchText.getFont().getFontData()[0];
		searchTextFont.setHeight(10);
		searchText.setFont(new Font(Display.getCurrent(), searchTextFont));
		searchText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		addSearchTextListener(searchText);

		searchButton = new Button(parent, SWT.PUSH);
		GridData buttonData = new GridData(SWT.FILL, SWT.FILL, false, false);
		// buttonData.widthHint = 50;
		searchButton.setLayoutData(buttonData);
		searchButton.setText("Go");
		addSearchButtonListener(searchButton);		
	}
	
	
	protected void addSearchButtonListener(final Button serachButton) {
		searchButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				String searchContent = searchText.getText();
				jumpToNode(searchContent, true);
			}
		});

	}
	
	public void otherViewsBehaviour(TraceNode node) {
		// perform the jump on both correct trace view and
		// on buggy trace
		
		// check if correct view exists and perform the jump
		
	}
	
	protected void addSearchTextListener(final Text searchText) {
		searchText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 27 || e.character == SWT.CR) {

					boolean forward = (e.stateMask & SWT.SHIFT) == 0;

					String searchContent = searchText.getText();
					jumpToNode(searchContent, forward);
				}
			}
		});

	}
	
	public void jumpToNode(String searchContent, boolean next) {
		// todo: implement the node jumping functionality
		// --> route the node jumping to the buggy traceview
		if (this.attached != null) this.attached.jumpToNode(searchContent, next);
	}
	
	private void createListView(Composite parent) {
		listViewer = new ListViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);		
		listViewer.setContentProvider(new ActionPathContentProvider());
		listViewer.setLabelProvider(new FeedbackNodePairLabelProvider());
		listViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		listViewer.addPostSelectionChangedListener(this.selectionListener);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
	
	public void setActionPath(ActionPath actionPath) {
		this.actionPath = actionPath;
	}
	
	
	
	public void updateData() {
		listViewer.setInput(actionPath);
		listViewer.refresh();
	}
	
	public void attach(TraceView view) {		
		selectionListener.attachTraceView(view);
	}
}
