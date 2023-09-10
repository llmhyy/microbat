package microbat.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import fj.P;
import microbat.debugpilot.pathfinding.FeedbackPath;
import microbat.model.trace.TraceNode;
import microbat.recommendation.UserFeedback;
import microbat.util.MicroBatUtil;
import microbat.views.utils.contentprovider.ActionPathContentProvider;
import microbat.views.utils.lableprovider.FeedbackPathContentProvider;
import microbat.views.utils.lableprovider.FeedbackPathLabelProvider;
import microbat.views.utils.listeners.FeedbackPathSelectionListener;

public class PathView extends ViewPart {
	public static final String ID = "microbat.evalView.pathView";

	protected TraceView buggyTraceView = MicroBatViews.getTraceView();
	protected ReasonView reasonView = MicroBatViews.getReasonView();

	protected Text searchText;	
	protected Button searchButton;
	
	protected TableViewer feedbackPathViewer;
	
	protected FeedbackPath feedbackPath;
	
	protected List<Button> checkButtons = new ArrayList<>();
	
	public PathView() {
	}
	
	public void setSearchText(String expression) {		
		this.searchText.setText(expression);
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		parent.setLayout(layout);
		createSearchBox(parent);
		createTableView(parent);
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
		searchButton.setLayoutData(buttonData);
		searchButton.setText("Go");
		addSearchButtonListener(searchButton);		
	}
	
	
	protected void addSearchButtonListener(final Button serachButton) {
		searchButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				String searchContent = searchText.getText();
				jumpToPath(searchContent);
			}
		});

	}
	
	public void otherViewsBehaviour(TraceNode node) {
		if (this.buggyTraceView != null) {
			this.buggyTraceView.jumpToNode(this.buggyTraceView.getTrace(), node.getOrder(), false);
			this.buggyTraceView.jumpToNode(node);
		}
		
		UserFeedback feedback = this.feedbackPath.getFeedback(node).getFirstFeedback();
		this.reasonView.refresh(node, feedback);
	}
	
	protected void addSearchTextListener(final Text searchText) {
		searchText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 27 || e.character == SWT.CR) {
					String searchContent = searchText.getText();
					jumpToPath(searchContent);
				}
			}
		});

	}
	
	public void jumpToPath(final String pathIDStr) {
		try {
			int pathID = Integer.valueOf(pathIDStr);
			this.feedbackPathViewer.setSelection(new StructuredSelection(this.feedbackPathViewer.getElementAt(pathID)), true);
			this.feedbackPathViewer.refresh();
		} catch (NumberFormatException e) {
			// Do nothing
		}
	}
	
	private void createTableView(Composite parent) {
		Table table = new Table(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 2;
		table.setLayoutData(gridData);
		
		TableColumn IdColumn = new TableColumn(table, SWT.LEFT);
		IdColumn.setAlignment(SWT.LEFT);
		IdColumn.setText("Path");
		IdColumn.setWidth(50);
		
		TableColumn TraceNodeColumn = new TableColumn(table, SWT.LEFT);
		TraceNodeColumn.setAlignment(SWT.LEFT);
		TraceNodeColumn.setText("Trace Node");
		TraceNodeColumn.setWidth(100);
		
		TableColumn predictionColumn = new TableColumn(table, SWT.LEFT);
		predictionColumn.setAlignment(SWT.LEFT);
		predictionColumn.setText("Prediction");
		predictionColumn.setWidth(200);
		
		TableColumn confirmColumn = new TableColumn(table, SWT.LEFT);
		confirmColumn.setAlignment(SWT.LEFT);
		confirmColumn.setText("Confirm");
		confirmColumn.setWidth(70);
		
		this.feedbackPathViewer = new TableViewer(table);
		this.feedbackPathViewer.addPostSelectionChangedListener(new FeedbackPathSelectionListener(this));
		this.feedbackPathViewer.setContentProvider(new FeedbackPathContentProvider());
	}
	
	@Override
	public void setFocus() {

	}
	
	public void updateFeedbackPath(final FeedbackPath feedbackPath) {
		this.feedbackPath = feedbackPath;
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		        feedbackPathViewer.setLabelProvider(new FeedbackPathLabelProvider(feedbackPath));
		        feedbackPathViewer.setInput(feedbackPath);
		        feedbackPathViewer.refresh();
		        checkButtons.clear();
		    }
		});
	}
	
	public void setBuggyView(TraceView view) {
		this.buggyTraceView = view;
	}
	
	public FeedbackPath getFeedbackPath() {
		return this.feedbackPath;
	}
	
}
