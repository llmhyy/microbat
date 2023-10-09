package microbat.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.part.ViewPart;

import microbat.debugpilot.DebugPilotExecutor;
import microbat.debugpilot.DebugPilotInfo;
import microbat.debugpilot.pathfinding.FeedbackPath;
import microbat.debugpilot.userfeedback.DPUserFeedback;
import microbat.handler.callbacks.HandlerCallback;
import microbat.handler.callbacks.HandlerCallbackManager;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.model.variable.VirtualVar;
import microbat.recommendation.ChosenVariableOption;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;
import microbat.views.utils.contentprovider.ControlDominatorContentProvider;
import microbat.views.utils.contentprovider.FeedbackContentProvider;
import microbat.views.utils.contentprovider.ReadVariableContenProvider;
import microbat.views.utils.contentprovider.WrittenVariableContentProvider;
import microbat.views.utils.lableprovider.ControlDominatorLabelProvider;
import microbat.views.utils.lableprovider.DummyLabelProvider;
import microbat.views.utils.lableprovider.VariableLabelProvider;
import microbat.views.utils.manager.FeedbackSelectionManager;

public class DebugPilotFeedbackView extends ViewPart {

	public final static String ID = "microbat.view.debugPilotFeedback";
	
	/* Basic information of current state */
	protected TraceNode currentNode;
	protected Trace trace;
	
	/* Read variables information */
	protected TreeViewer readVariableViewer;
	
	/* Written variables information */
	protected TreeViewer writtenVariableViewer;
	
	/* Control dominator information*/
	protected TableViewer controlDominatorViewer;
	
	/* Input variable information*/
	protected TreeViewer relatedVariablesViewer;
//	protected Label invokeMethodLabel;
	
	/* Program outcome information */
	protected Label outputNodeLabel;
	protected Button shouldNotExecuteButton;
	protected TreeViewer wrongOutputTreeViewer;
	protected Label removeOutputLabel;
	
	/* All possible feedbacks */
	protected TableViewer availableFeedbackViewer;
	protected Button feedbackButton;
	protected Label giveFeedbackLabel;
//	protected Label nextNodeLabel;
	protected TableViewerColumn yesCheckboxColumn;
	protected TableViewerColumn noCheckboxColumn;
	protected TableViewerColumn typeViewerColumn;
	protected TableViewerColumn varViewerColumn;
	protected TableViewerColumn varValueViewerColumn;
	protected TableViewerColumn nextNodeViewerColumn;
	protected List<Button> nextNodeButtons = new ArrayList<>();
	protected FeedbackSelectionManager feedbackSelectionManager = new FeedbackSelectionManager();
	
	
    protected final int operations = DND.DROP_COPY | DND.DROP_MOVE;
    protected final Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer.getTransfer() };

    public DebugPilotFeedbackView() {
		HandlerCallbackManager.getInstance().registerDebugPilotTermianteCallback(new HandlerCallback() {
			@Override
			public void callBack() {
				Display.getDefault().asyncExec(new Runnable() {
				    public void run() {
				    	clearProgramOutput();
				    }
				});
			}
		});
    }
	
	@Override
	public void createPartControl(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		this.createReadVariablesViewer(sashForm);
		this.createWrittenVariableViewer(sashForm);
		this.createAvaliableFeedbackView(sashForm);
		sashForm.setWeights(10, 10, 10);
	}

	@Override
	public void setFocus() {}
	
	public void refresh(final TraceNode currentNode, final Trace trace) {
		this.currentNode = currentNode;
		this.trace = trace;
		this.refreshReadVariableViewer();
		this.refreshWrittenVariableViewer();
		this.refreshAvailableFeedbackViewer();
	}
	
	public TraceNode getOutputNode() {
		return this.currentNode;
	}
	
	public VarValue getWrongVar() {
		if (this.wrongOutputTreeViewer.getInput() instanceof ArrayList wrongVars) {
			if (wrongVars.isEmpty()) {
				return null;
			}
			return (VarValue) wrongVars.get(0);
		}
		return null;
	}
	
	public boolean isOutputWrongBranch() {
		return this.shouldNotExecuteButton.getSelection();
	}
	
	protected void createReadVariablesViewer(final Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText("Read Variables");
		group.setLayout(new FillLayout());

		final Tree tree = this.createVarTree(group);

//		TreeColumn probColumn = new TreeColumn(tree, SWT.LEFT);
//		probColumn.setAlignment(SWT.LEFT);
//		probColumn.setText("Correctness");
//		probColumn.setWidth(100);
//		
//		TreeColumn costColumn = new TreeColumn(tree, SWT.LEFT);
//		costColumn.setAlignment(SWT.LEFT);
//		costColumn.setText("Cost");
//		costColumn.setWidth(100);
		
		this.readVariableViewer = new TreeViewer(tree);
		this.readVariableViewer.addDragSupport(this.operations, this.transferTypes, this.createDragSourceAdapter(this.readVariableViewer));
		this.readVariableViewer.setLabelProvider(new VariableLabelProvider());
	}
	
	protected void createWrittenVariableViewer(final Composite parent) {
//		SashForm variableForm = new SashForm(parent, SWT.VERTICAL);
//		variableForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Group group = new Group(parent, SWT.NONE);
		group.setText("Written Variables");
		group.setLayout(new FillLayout());
		
		final Tree tree = this.createVarTree(group);
		
//		TreeColumn probColumn = new TreeColumn(tree, SWT.LEFT);
//		probColumn.setAlignment(SWT.LEFT);
//		probColumn.setText("Correctness");
//		probColumn.setWidth(100);
//		
//		TreeColumn costColumn = new TreeColumn(tree, SWT.LEFT);
//		costColumn.setAlignment(SWT.LEFT);
//		costColumn.setText("Cost");
//		costColumn.setWidth(100);
		
		this.writtenVariableViewer = new TreeViewer(tree);
//		this.writtenVariableViewer.addDragSupport(this.operations, this.transferTypes, this.createDragSourceAdapter(this.writtenVariableViewer));
		this.writtenVariableViewer.setLabelProvider(new VariableLabelProvider());
	}
	
	protected void createControlDominatorGroup(final Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText("Control Dominator");
		group.setLayoutData(new GridData(SWT.FILL, SWT.UP, true, false));
		
		GridLayout gridLayout = new GridLayout(1, false);
		group.setLayout(gridLayout);
		
		Table table = new Table(group, SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableColumn orderColumn = new TableColumn(table, SWT.LEFT);
		orderColumn.setAlignment(SWT.LEFT);
		orderColumn.setText("Node Order");
		orderColumn.setWidth(100);
		
		TableColumn probColumn = new TableColumn(table, SWT.LEFT);
		probColumn.setAlignment(SWT.LEFT);
		probColumn.setText("Condition Correctness");
		probColumn.setWidth(200);
		
		TableColumn costColumn = new TableColumn(table, SWT.LEFT);
		costColumn.setAlignment(SWT.LEFT);
		costColumn.setText("Condition Cost");
		costColumn.setWidth(200);
		
		this.controlDominatorViewer = new TableViewer(table);
		this.controlDominatorViewer.setContentProvider(new ControlDominatorContentProvider());
		this.controlDominatorViewer.setLabelProvider(new ControlDominatorLabelProvider());
	}
	
	protected void createOutputGroup(final Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText("Program Output");
		group.setLayoutData(new GridData(SWT.FILL, SWT.UP, true, false));
		
		GridLayout gridLayout = new GridLayout(2, false);
		group.setLayout(gridLayout);
		
		this.outputNodeLabel = new Label(group, SWT.NONE);
		this.outputNodeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		this.outputNodeLabel.setText(this.genOutputNodeTextContent());
		
		this.shouldNotExecuteButton = new Button(group, SWT.CHECK);
		this.shouldNotExecuteButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		this.shouldNotExecuteButton.setText("Wrong Branch");
		this.shouldNotExecuteButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DebugPilotInfo.getInstance().setOutputNodeWrongBranch(shouldNotExecuteButton.getSelection());
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		Tree tree = this.createVarTree(group);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		GridData treeGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		treeGridData.horizontalSpan = 2;
		tree.setLayoutData(treeGridData);
		
		this.wrongOutputTreeViewer = new TreeViewer(tree);
		this.wrongOutputTreeViewer.setContentProvider(new ReadVariableContenProvider(this.currentNode));
		this.wrongOutputTreeViewer.setLabelProvider(new VariableLabelProvider());
		this.wrongOutputTreeViewer.addDropSupport(this.operations, this.transferTypes, new ViewerDropAdapter(wrongOutputTreeViewer) {
			@Override
			public boolean performDrop(Object data) {
				if (data instanceof IStructuredSelection selection) {
					wrongOutputTreeViewer.setInput(Arrays.stream(selection.toArray()).collect(Collectors.toCollection(ArrayList::new)));
					VarValue[] selectedVar = Arrays.stream(selection.toArray()).map(obj -> {return (VarValue) obj;}).toArray(VarValue[]::new);
					DebugPilotInfo.getInstance().clearOutputs();
					DebugPilotInfo.getInstance().addOutputs(selectedVar);
					return true;
				}
				return false;
			}
			@Override
			public boolean validateDrop(Object target, int operation, TransferData transferType) {
				return true;
			}
		});
		this.wrongOutputTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				clearProgramOutput();
			}
		});
		
		this.removeOutputLabel = new Label(group, SWT.NONE);
		this.removeOutputLabel.setAlignment(SWT.LEFT);
		this.removeOutputLabel.setText("Double click the output to remove it.");
	}
	
	protected void createAvaliableFeedbackView(final Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText("Avaliable Feedbacks");
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridLayout gridLayout = new GridLayout(2, false);
		group.setLayout(gridLayout);
		
		this.createGiveFeedbackGroup(group);
		this.createAvailiableFeedbacksTable(group);
	}
	
	protected void createAvailiableFeedbacksTable(final Composite parent) {
		SashForm variableForm = new SashForm(parent, SWT.VERTICAL);
		GridData treeGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		treeGridData.horizontalSpan = 2;
		variableForm.setLayoutData(treeGridData);
		
//		this.availableFeedbackViewer = TableViewer.newCheckList(variableForm, SWT.H_SCROLL | SWT.V_SCROLL| SWT.FULL_SELECTION | SWT.CHECK | SWT.MULTI);
		this.availableFeedbackViewer = new TableViewer(variableForm, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		this.availableFeedbackViewer.getTable().setHeaderVisible(true);
		this.availableFeedbackViewer.getTable().setLinesVisible(true);
		
		TableColumn yesColumn = new TableColumn(this.availableFeedbackViewer.getTable(), SWT.CENTER);
		yesColumn.setAlignment(SWT.CENTER);
		yesColumn.setText("Yes");
		yesColumn.setWidth(30);
		this.yesCheckboxColumn = new TableViewerColumn(this.availableFeedbackViewer, yesColumn);
		
		TableColumn noColumn = new TableColumn(this.availableFeedbackViewer.getTable(), SWT.CENTER);
		noColumn.setAlignment(SWT.CENTER);
		noColumn.setText("No");
		noColumn.setWidth(30);
		this.noCheckboxColumn = new TableViewerColumn(this.availableFeedbackViewer, noColumn);
		
		TableColumn typeColumn = new TableColumn(this.availableFeedbackViewer.getTable(), SWT.LEFT);
		typeColumn.setAlignment(SWT.LEFT);
		typeColumn.setText("Question");
		typeColumn.setWidth(250);
		this.typeViewerColumn = new TableViewerColumn(this.availableFeedbackViewer, typeColumn);
		
		 
		TableColumn varColumn = new TableColumn(this.availableFeedbackViewer.getTable(), SWT.LEFT);
		varColumn.setAlignment(SWT.LEFT);
		varColumn.setText("Variable");
		varColumn.setWidth(90);
		this.varViewerColumn = new TableViewerColumn(this.availableFeedbackViewer, varColumn);
		
		TableColumn varValueColumn = new TableColumn(this.availableFeedbackViewer.getTable(), SWT.LEFT);
		varValueColumn.setAlignment(SWT.LEFT);
		varValueColumn.setText("Value");
		varValueColumn.setWidth(200);
		this.varValueViewerColumn = new TableViewerColumn(this.availableFeedbackViewer, varValueColumn);
		
		TableColumn nextNodeColumn = new TableColumn(this.availableFeedbackViewer.getTable(), SWT.LEFT);
		nextNodeColumn.setAlignment(SWT.LEFT);
		nextNodeColumn.setText("Explore");
		nextNodeColumn.setWidth(110);
		this.nextNodeViewerColumn = new TableViewerColumn(this.availableFeedbackViewer, nextNodeColumn);

		this.availableFeedbackViewer.setContentProvider(new FeedbackContentProvider());
		this.availableFeedbackViewer.setLabelProvider(new DummyLabelProvider());
//		this.availableFeedbackViewer.addCheckStateListener(new ICheckStateListener() {
//			@Override
//			public void checkStateChanged(CheckStateChangedEvent event) {
//				final UserFeedback checkedFeedback = (UserFeedback) event.getElement();
//				for (Object element : availableFeedbackViewer.getCheckedElements()) {
//					if (element instanceof UserFeedback userFeedback) {
//						if (!userFeedback.getFeedbackType().equals(checkedFeedback.getFeedbackType())) {
//							availableFeedbackViewer.setChecked(element, false);
//						}
//					}
//				}
//				if (checkedFeedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
//					for (Object element : availableFeedbackViewer.getCheckedElements()) {
//						if (element instanceof UserFeedback userFeedback) {
//							if (userFeedback.getFeedbackType().equals(UserFeedback.ROOTCAUSE) || userFeedback.getFeedbackType().equals(UserFeedback.CORRECT) || userFeedback.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
//								availableFeedbackViewer.setChecked(element, false);
//							}
//							if (userFeedback.getFeedbackType().equals(UserFeedback.CORRECT_VARIABLE_VALUE) && userFeedback.getOption().getReadVar().equals(checkedFeedback.getOption().getReadVar())) {
//								availableFeedbackViewer.setChecked(element, false);
//							}
//						}
//					}
//				} else if (checkedFeedback.getFeedbackType().equals(UserFeedback.CORRECT_VARIABLE_VALUE)) {
//					for (Object element : availableFeedbackViewer.getCheckedElements()) {
//						if (element instanceof UserFeedback userFeedback) {
//							if (userFeedback.getFeedbackType().equals(UserFeedback.ROOTCAUSE) || userFeedback.getFeedbackType().equals(UserFeedback.CORRECT) || userFeedback.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
//								availableFeedbackViewer.setChecked(element, false);
//							}
//							if (userFeedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE) && userFeedback.getOption().getReadVar().equals(checkedFeedback.getOption().getReadVar())) {
//								availableFeedbackViewer.setChecked(element, false);
//							}
//						}
//					}
//				} else {					
//					for (Object element : availableFeedbackViewer.getCheckedElements()) {
//						if (element instanceof UserFeedback userFeedback) {
//							if (!userFeedback.getFeedbackType().equals(checkedFeedback.getFeedbackType())) {
//								availableFeedbackViewer.setChecked(element, false);
//							}
//						}
//					}
//				}
//			}
//		});
	}
	
	protected void createRelatedVariableGroup(final Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText("Related Variables");
		group.setLayout(new FillLayout());
		
		GridLayout gridLayout = new GridLayout(1, false);
		group.setLayout(gridLayout);
		
//		this.invokeMethodLabel = new Label(group, SWT.WRAP);
//		this.invokeMethodLabel.setAlignment(SWT.LEFT);
//		this.invokeMethodLabel.setText(this.genInvoateMethodLabelContent());
//		
//		GridData labelData = new GridData(SWT.FILL, SWT.CENTER, true, false);
//		labelData.widthHint = 200; // Set to a value that accommodates your text
//		this.invokeMethodLabel.setLayoutData(labelData);
		
		final Tree tree = this.createVarTree(group);
		
//		TreeColumn probColumn = new TreeColumn(tree, SWT.LEFT);
//		probColumn.setAlignment(SWT.LEFT);
//		probColumn.setText("Correctness");
//		probColumn.setWidth(100);
//		
//		TreeColumn costColumn = new TreeColumn(tree, SWT.LEFT);
//		costColumn.setAlignment(SWT.LEFT);
//		costColumn.setText("Cost");
//		costColumn.setWidth(100);
		
		this.relatedVariablesViewer = new TreeViewer(tree);
		this.relatedVariablesViewer.setLabelProvider(new VariableLabelProvider());
	}
	
	
	protected String genInvoateMethodLabelContent() {
		final String begining = "Invocation Method: ";
		if (this.currentNode == null) {
			return begining + "None";
		} else {
			return begining + this.currentNode.getInvokingMethod();
		}
	}
	
	protected void createGiveFeedbackGroup(final Composite parent) {
		this.giveFeedbackLabel = new Label(parent, SWT.NONE);
		this.giveFeedbackLabel.setAlignment(SWT.LEFT);
		this.giveFeedbackLabel.setText("Here the possible feedback you may give: ");
				
		this.feedbackButton = new Button(parent, SWT.NONE);
		this.feedbackButton.setText("Confirm");
		this.feedbackButton.setLayoutData(new GridData(SWT.RIGHT, SWT.UP, true, false));
		this.feedbackButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!feedbackSelectionManager.isValidSelection()) {
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append("Invalid feedback is given. \n");
					stringBuilder.append("Valid feedback should contain one of the following: \n");
					stringBuilder.append("1. This step is root cause.\n");
					stringBuilder.append("2. This step is in wrong branch.\n");
					stringBuilder.append("3. This step contain wrong read variables.\n");
					stringBuilder.append("4. This step is correct.\n");
					DialogUtil.popErrorDialog(stringBuilder.toString(), "Feedback Error");
					return;
				}
				DPUserFeedback userFeedback = feedbackSelectionManager.genDpUserFeedback(currentNode);
				
				DebugPilotExecutor executor = new DebugPilotExecutor();
				executor.execute(userFeedback);
			}
		});
	
	}
	
	protected Tree createVarTree(final Composite parent) {
		final Tree tree = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		TreeColumn typeColumn = new TreeColumn(tree, SWT.LEFT);
		typeColumn.setAlignment(SWT.LEFT);
		typeColumn.setText("Type");
		typeColumn.setWidth(100);
		
		TreeColumn nameColumn = new TreeColumn(tree, SWT.LEFT);
		nameColumn.setAlignment(SWT.LEFT);
		nameColumn.setText("Name");
		nameColumn.setWidth(100);
		
		TreeColumn valueColumn = new TreeColumn(tree, SWT.LEFT);
		valueColumn.setAlignment(SWT.LEFT);
		valueColumn.setText("Value");
		valueColumn.setWidth(200);
		
		return tree;
	}
	
	protected String genOutputNodeTextContent() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Output Node: ");
		stringBuilder.append(this.currentNode == null ? "Not Selected" : this.currentNode.getOrder());
		return stringBuilder.toString();
	}
	
	protected void refreshReadVariableViewer() {
		this.readVariableViewer.setContentProvider(new ReadVariableContenProvider(this.currentNode));
		this.readVariableViewer.setInput(this.currentNode.getReadVariables());
		this.readVariableViewer.refresh(true);
	}
	
	protected void refreshWrittenVariableViewer() {
		this.writtenVariableViewer.setContentProvider(new WrittenVariableContentProvider(this.currentNode));
		this.writtenVariableViewer.setInput(this.currentNode.getWrittenVariables());
		this.writtenVariableViewer.refresh(true);
	}
	
	protected void refreshRelatedVariableGroup() {
//		this.invokeMethodLabel.setText(this.genInvoateMethodLabelContent());
		final TraceNode invokeParent = this.currentNode.getInvocationParent();
		this.relatedVariablesViewer.setContentProvider(new WrittenVariableContentProvider(invokeParent));
		this.relatedVariablesViewer.setInput(invokeParent == null ? null : invokeParent.getWrittenVariables());
		this.relatedVariablesViewer.refresh(true);
	}
	
	protected void refreshControlDominoatorViewer() {
		this.controlDominatorViewer.setInput(this.currentNode);
		this.controlDominatorViewer.refresh(true);
	}
	
	protected void refreshOutputGroup() {
		this.outputNodeLabel.setText(this.genOutputNodeTextContent());
	}
	
	protected void refreshAvailableFeedbackViewer() {
		this.disposeButtons();
		
		this.yesCheckboxColumn.setLabelProvider(new YesCheckboxLabelProvider());
		this.noCheckboxColumn.setLabelProvider(new NoCheckboxLabelProvider());
		
		this.typeViewerColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
            	if (element instanceof UserFeedback userFeedback) {
            		return genFeedbackType(userFeedback);
            	}
            	return null;
            }
		});
		
		this.varViewerColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
            	if (element instanceof UserFeedback userFeedback) {
//    				if (userFeedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE) || userFeedback.getFeedbackType().equals(UserFeedback.CORRECT_VARIABLE_VALUE)) {
            		if (userFeedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
    					VarValue wrongVar = userFeedback.getOption().getReadVar();
    					String name = wrongVar.getVarName();
    					if(wrongVar.getVariable() instanceof VirtualVar){
    						String methodName = name.substring(name.lastIndexOf(".")+1);
    						name = "return from " + methodName + "()";
    					}
    					return name;
    				}
    				return "-";
            	}
            	return null;
            }
		});
		
		this.varValueViewerColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
            	if (element instanceof UserFeedback userFeedback) {
//    				if (userFeedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE) || userFeedback.getFeedbackType().equals(UserFeedback.CORRECT_VARIABLE_VALUE)) {
            		if (userFeedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
    					return userFeedback.getOption().getReadVar().getStringValue();
    				} else {
    					return "-";
    				}
            	}
            	return null;
            }
		});
		
		this.nextNodeViewerColumn.setLabelProvider(new nextNodeButtonLabelProvider(this.currentNode, this.trace));
		
		UserFeedback[] allAvaiableFeedbacks = this.getAllAvailableFeedbacks();
		this.availableFeedbackViewer.setInput(allAvaiableFeedbacks);
		this.checkDefaultAvailableFeedbacks();
		
		this.feedbackSelectionManager.verify(allAvaiableFeedbacks);
	}
	
	protected String genFeedbackType(final UserFeedback feedback) {
		if (feedback.getFeedbackType().equals(UserFeedback.CORRECT)) {
			return "Is step correct?";
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
			return "Is step in correct branch?";
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
			return "Is this variable correct";
		} else if (feedback.getFeedbackType().equals(UserFeedback.ROOTCAUSE)) {
			return "Is step root cause?";
//		} else if (feedback.getFeedbackType().equals(UserFeedback.CORRECT_VARIABLE_VALUE)) {
//			return "Correct Variable";
		} else {
			return null;
		}
	}
	
	protected void checkDefaultAvailableFeedbacks() {
		final FeedbackPath path = MicroBatViews.getPathView().getFeedbackPath();
		if (path != null && path.containFeedbackByNode(this.currentNode)) {
			DPUserFeedback feedback = path.getFeedbackByNode(this.currentNode);
			this.feedbackSelectionManager.checkButtonBasedOnFeedback(feedback);
		}
	}
	
	
	protected DragSourceAdapter createDragSourceAdapter(final TreeViewer treeViewer) {
		return new DragSourceAdapter() {
			@Override
			public void dragStart(DragSourceEvent event) {
	            IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
	            if (selection.isEmpty()) {
	                event.doit = false;
	                return;
	            }
			}
			@Override
			public void dragSetData(DragSourceEvent event) {
	            IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
	            // Set the data to be transferred during the drag
	            if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
	                LocalSelectionTransfer.getTransfer().setSelection(selection);
	            }
			}
			@Override
			public void dragFinished(DragSourceEvent event) {}
		};
	}
	
	protected UserFeedback[] getAllAvailableFeedbacks() {
		List<UserFeedback> availableFeedbacks = new ArrayList<>();
		availableFeedbacks.add(new UserFeedback(UserFeedback.ROOTCAUSE));
		availableFeedbacks.add(new UserFeedback(UserFeedback.WRONG_PATH));
		for (VarValue readVar : this.currentNode.getReadVariables()) {
			UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_VARIABLE_VALUE);
			feedback.setOption(new ChosenVariableOption(readVar, null));
			availableFeedbacks.add(feedback);
		}
		availableFeedbacks.add(new UserFeedback(UserFeedback.CORRECT));
		return availableFeedbacks.toArray(new UserFeedback[0]);
	}
	
	public void clearProgramOutput() {
//		this.wrongOutputTreeViewer.setInput(null);
//		this.wrongOutputTreeViewer.refresh();
//		DebugPilotInfo.getInstance().clearOutputs();
	}
	
	protected void selectTraceViewNode(final TraceNode node) {
		final TraceView traceView = MicroBatViews.getTraceView();
		traceView.jumpToNode(trace, node.getOrder(), false);
		traceView.jumpToNode(node);
	}
	
	protected void registerYesCheckbox(final UserFeedback feedback, final Button checkbox) {
		this.feedbackSelectionManager.registerYesCheckbox(feedback, checkbox);
	}
	
	protected void registerNoCheckbox(final UserFeedback feedback, final Button checkbox) {
		this.feedbackSelectionManager.registerNoCheckbox(feedback, checkbox);
	}
	
	protected void registerButtons(final Button button) {
		this.nextNodeButtons.add(button);
	}
	
	protected void disposeButtons() {
		this.disposeNextButtons();
		this.disposeYesNoButtons();
	}
	
	protected void disposeNextButtons() {
		for (Button button : this.nextNodeButtons) {
			button.dispose();
		}
		this.nextNodeButtons.clear();		
	}
	
	protected void disposeYesNoButtons() {
		this.feedbackSelectionManager.dispose();
	}
	
	protected class nextNodeButtonLabelProvider extends ColumnLabelProvider {
		protected TraceNode currentNode;
		protected Trace trace;
		
		public nextNodeButtonLabelProvider(final TraceNode node, final Trace trace) {
			this.currentNode = node;
			this.trace = trace;
		}
		
		@Override
		public void update(ViewerCell cell) {
			Button button = new Button((Composite) cell.getViewerRow().getControl(), SWT.PUSH);
			final UserFeedback userFeedback = (UserFeedback) cell.getElement();

			final TraceNode nextNode = TraceUtil.findNextNode(this.currentNode, userFeedback, trace);
			String buttonText = "-";
			if (userFeedback.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
				buttonText = "Condition";
			} else if (userFeedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
				buttonText = "Definition";
			}
			
			button.setText(buttonText);
			button.setEnabled(nextNode != null);
			button.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final TraceView traceView = MicroBatViews.getTraceView();
					traceView.jumpToNode(trace, nextNode.getOrder(), false);
					traceView.jumpToNode(nextNode);
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {}
			});
			
            TableItem item = (TableItem) cell.getItem();
            TableEditor editor = new TableEditor(item.getParent());
            editor.grabHorizontal  = true;
            editor.grabVertical = true;
            editor.setEditor(button , item, cell.getColumnIndex());
            editor.layout();

			registerButtons(button);
		}
	}
	
	protected class YesCheckboxLabelProvider extends ColumnLabelProvider {
		public YesCheckboxLabelProvider() {	}
		@Override
		public void update(ViewerCell cell) {
			Button button = new Button((Composite) cell.getViewerRow().getControl(), SWT.CHECK);
			final UserFeedback userFeedback = (UserFeedback) cell.getElement();
			button.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					feedbackSelectionManager.select(userFeedback, true);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {}
				
			});
			
			TableItem item = (TableItem) cell.getItem();
            TableEditor editor = new TableEditor(item.getParent());
            editor.grabHorizontal  = true;
            editor.grabVertical = true;
            editor.setEditor(button , item, cell.getColumnIndex());
            editor.layout();
            
			registerYesCheckbox(userFeedback, button);
		}
	}
	
	protected class NoCheckboxLabelProvider extends ColumnLabelProvider {
		public NoCheckboxLabelProvider() {	}
		@Override
		public void update(ViewerCell cell) {
			Button button = new Button((Composite) cell.getViewerRow().getControl(), SWT.CHECK);
			final UserFeedback userFeedback = (UserFeedback) cell.getElement();
			button.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					feedbackSelectionManager.select(userFeedback, false);
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {}
				
			});
			TableItem item = (TableItem) cell.getItem();
            TableEditor editor = new TableEditor(item.getParent());
            editor.grabHorizontal  = true;
            editor.grabVertical = true;
            editor.setEditor(button , item, cell.getColumnIndex());
            editor.layout();
            
			registerNoCheckbox(userFeedback, button);
		}
	}
}
