package microbat.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import microbat.debugpilot.NodeFeedbacksPair;
import microbat.debugpilot.pathfinding.FeedbackPath;
import microbat.debugpilot.propagation.spp.StepExplaination;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.recommendation.UserFeedback;
import microbat.util.MicroBatUtil;
import microbat.views.utils.contentprovider.ActionPathContentProvider;
import microbat.views.utils.lableprovider.FeedbackNodePairLabelProvider;
import microbat.views.utils.listeners.PathViewSelectionListener;

// todo: node: feedback -- ui
public class PathView extends ViewPart {
	public static final String ID = "microbat.evalView.pathView";

	protected Text searchText;	
	protected FeedbackPath actionPath;
	protected Button searchButton;
	protected TraceView buggyTraceView = null;
	protected ReasonView reasonView = MicroBatViews.getReasonView();
	protected TableViewer table;
	
	
	private PathViewSelectionListener selectionListener;

	private String previousSearchExpression = "";
	
	protected List<Button> checkButtons = new ArrayList<>();
	
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
		if (this.buggyTraceView != null) {
			this.buggyTraceView.jumpToNode(this.buggyTraceView.getTrace(), node.getOrder(), false);
			this.buggyTraceView.jumpToNode(node);
		}
		
		UserFeedback feedback = this.actionPath.getFeedback(node).getFirstFeedback();
		this.reasonView.refresh(node, feedback);
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

		
		for (int i = 0; i < ((FeedbackPath) table.getInput()).getLength(); ++i) {
			ActionPathContentProvider.ContentWrapper content = (ActionPathContentProvider.ContentWrapper) table.getElementAt(i);
			String label = MicroBatUtil.genPathMessage(content.getNode(), content.getIndex());
			if (label.contains(searchContent)) {
				this.table.setSelection(new StructuredSelection(table.getElementAt(i)), true);
				table.refresh();
				return;
			}
		}
	}
	
	private void createTableView(Composite parent) {
		Composite tableContainer = new Composite(parent, SWT.NONE);
		TableColumnLayout tcl = new TableColumnLayout();
		tableContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		table = new TableViewer(tableContainer, SWT.BORDER | 
					SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		String[] headers = {
			"Path", "TraceNode", "Prediction", "Confirm"
		};
		int[] weights = {
			100, 100, 400, 100	
		};
		ArrayList<Function<ActionPathContentProvider.ContentWrapper, String>> functions
		 = new ArrayList<>();
		functions.add(cw -> "" + cw.getIndex());
		functions.add(cw -> "" + cw.getNode().getNode().getOrder());
		functions.add(cw -> {
			final UserFeedback feedback = cw.getNode().getFirstFeedback();
			switch (feedback.getFeedbackType()) {
			case UserFeedback.CORRECT:
				return "This step is correct";
			case UserFeedback.WRONG_PATH:
				return "This step should not be executed";
			case UserFeedback.WRONG_VARIABLE_VALUE:
				return "Wrong variable: " + feedback.getOption().getReadVar().getVarName();
			case UserFeedback.ROOTCAUSE:
				return "This step is the root cause";
			case UserFeedback.UNCLEAR:
				return "Unclear";
			}
			return "";
		});
		
		functions.add(cw -> {
			return cw.getNode().getNode().confirmed ? "Yes" : "No";
		});
		assert(functions.size() == headers.length);
		assert(weights.length == headers.length);
		
		for (int i = 0; i < functions.size(); ++i) {
			final int j = i;
			TableViewerColumn col = new TableViewerColumn(table, SWT.LEFT);
			col.getColumn().setText(headers[i]);
			col.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object object) {
					if (object instanceof ActionPathContentProvider.ContentWrapper) {
						ActionPathContentProvider.ContentWrapper cw = (ActionPathContentProvider.ContentWrapper) object;
						return functions.get(j).apply(cw);
					}
					return "";
				}
			});
//			if (i == functions.size()-1) {
//				col.setLabelProvider(new ColumnLabelProvider() {
//					@Override
//					public void update(ViewerCell cell) {
//						Button button = new Button((Composite) cell.getViewerRow().getControl(), SWT.CHECK);
////						checkButtons.add(button);
//						TableItem item = (TableItem) cell.getItem();
//						TableEditor editor = new TableEditor(item.getParent());
//						editor.grabHorizontal = true;
//						editor.grabVertical = true;
//						editor.horizontalAlignment = SWT.CENTER;
//						editor.setEditor(button, item, cell.getColumnIndex());
//						editor.layout();
//					}
//				});
//			} else {
//				col.setLabelProvider(new ColumnLabelProvider() {
//					@Override
//					public String getText(Object object) {
//						if (object instanceof ActionPathContentProvider.ContentWrapper) {
//							ActionPathContentProvider.ContentWrapper cw = (ActionPathContentProvider.ContentWrapper) object;
//							return functions.get(j).apply(cw);
//						}
//						return "";
//					}
//				});
//			}
			tcl.setColumnData(col.getColumn(), new ColumnWeightData(weights[i]));
		}
//		table.setLabelProvider(new FeedbackNodePairLabelProvider());
		table.setContentProvider(new ActionPathContentProvider());
		table.getTable().setLayoutData(GridData.FILL_BOTH);
		table.addPostSelectionChangedListener(this.selectionListener);
		table.getTable().setHeaderVisible(true);
		table.getTable().setLinesVisible(true);
		tableContainer.setLayout(tcl);
		
		
	}
	


	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
	
	public void setActionPath(FeedbackPath actionPath) {
		this.actionPath = actionPath;
	}
	
	
	
	public void updateData() {
//		listViewer.setInput(actionPath);
//		listViewer.refresh();
		this.checkButtons.clear();
		table.setInput(actionPath);
		table.refresh();
	}
	
	public void setBuggyView(TraceView view) {
		this.buggyTraceView = view;
	}
	
	
	
	public void getCheckedElement() {
		System.out.println(this.checkButtons.size());
	}
	
}
