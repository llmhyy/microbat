package microbat.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
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

import debugpilot.userlogger.UserBehaviorLogger;
import debugpilot.userlogger.UserBehaviorType;
import microbat.debugpilot.DebugPilotInfo;
import microbat.debugpilot.NodeFeedbacksPair;
import microbat.debugpilot.pathfinding.FeedbackPath;
import microbat.handler.callbacks.HandlerCallback;
import microbat.handler.callbacks.HandlerCallbackManager;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
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
import microbat.views.utils.lableprovider.VariableWithProbabilityLabelProvider;

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
	protected CheckboxTableViewer availableFeedbackViewer;
	protected Button feedbackButton;
	protected Label giveFeedbackLabel;
//	protected Label nextNodeLabel;
	protected TableViewerColumn typeViewerColumn;
	protected TableViewerColumn varViewerColumn;
	protected TableViewerColumn varValueViewerColumn;
	protected TableViewerColumn nextNodeViewerColumn;
	protected List<Button> nextNodeButtons = new ArrayList<>();
	
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
//		GridLayout parentLayout = new GridLayout(1, true);
//		parent.setLayout(parentLayout);
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		
		this.createOutputGroup(sashForm);
		this.createReadVariablesViewer(sashForm);
		this.createWrittenVariableViewer(sashForm);
//		this.createControlDominatorGroup(parent);
//		this.createRelatedVariableGroup(sashForm);
		this.createAvaliableFeedbackView(sashForm);
		
		sashForm.setWeights(6, 10, 10, 10);
	}

	@Override
	public void setFocus() {}
	
	public void refresh(final TraceNode currentNode, final Trace trace) {
		this.currentNode = currentNode;
		this.trace = trace;
		DebugPilotInfo.getInstance().setOutputNode(this.currentNode);
		this.refreshReadVariableViewer();
		this.refreshWrittenVariableViewer();
//		this.refreshControlDominoatorViewer();
		this.refreshOutputGroup();
//		this.refreshRelatedVariableGroup();
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
//		SashForm variableForm = new SashForm(parent, SWT.VERTICAL);
//		variableForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

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
		
		this.availableFeedbackViewer = CheckboxTableViewer.newCheckList(variableForm, SWT.H_SCROLL | SWT.V_SCROLL| SWT.FULL_SELECTION | SWT.CHECK | SWT.MULTI);
		this.availableFeedbackViewer.getTable().setHeaderVisible(true);
		this.availableFeedbackViewer.getTable().setLinesVisible(true);
		
		TableColumn typeColumn = new TableColumn(this.availableFeedbackViewer.getTable(), SWT.LEFT);
		typeColumn.setAlignment(SWT.LEFT);
		typeColumn.setText("Type");
		typeColumn.setWidth(170);
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
		nextNodeColumn.setWidth(90);
		this.nextNodeViewerColumn = new TableViewerColumn(this.availableFeedbackViewer, nextNodeColumn);

		this.availableFeedbackViewer.setContentProvider(new FeedbackContentProvider());
		this.availableFeedbackViewer.setLabelProvider(new DummyLabelProvider());
		this.availableFeedbackViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				final UserFeedback checkedFeedback = (UserFeedback) event.getElement();
				for (Object element : availableFeedbackViewer.getCheckedElements()) {
					if (element instanceof UserFeedback userFeedback) {
						if (!userFeedback.getFeedbackType().equals(checkedFeedback.getFeedbackType())) {
							availableFeedbackViewer.setChecked(element, false);
						}
					}
				}
			}
		});
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
				final FeedbackPath feedbackPath = MicroBatViews.getPathView().getFeedbackPath();
				if (!feedbackPath.contains(currentNode)) {
					DialogUtil.popErrorDialog("Please give feedback only on step in path", "DebugPilot Feedback Error");
				} else {					
					UserFeedback[] feedbacks = Arrays.stream(availableFeedbackViewer.getCheckedElements()).map(obj -> {return (UserFeedback) obj;}).toArray(UserFeedback[]::new);
					NodeFeedbacksPair userFeedbacksPair = new NodeFeedbacksPair(currentNode, feedbacks);
					DebugPilotInfo.getInstance().setNodeFeedbacksPair(userFeedbacksPair);
					
					PathView pathView = MicroBatViews.getPathView();
					final TraceNode nextNode = TraceUtil.findNextNode(currentNode, userFeedbacksPair.getFirstFeedback(), trace);
					pathView.focusOnNode(nextNode);
					
					String feedbackType = userFeedbacksPair.getFeedbackType();
					if (feedbackType.equals(UserFeedback.CORRECT)) {
						UserBehaviorLogger.logEvent(UserBehaviorType.CORRECT);
					} else if (feedbackType.equals(UserFeedback.WRONG_PATH)) {
						UserBehaviorLogger.logEvent(UserBehaviorType.CONTROL_SLICING_CONFIRM);
					} else if (feedbackType.equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
						UserBehaviorLogger.logEvent(UserBehaviorType.DATA_SLICING_CONFIRM);
					} else if (feedbackType.equals(UserFeedback.ROOTCAUSE)) {
						UserBehaviorLogger.logEvent(UserBehaviorType.ROOT_CAUSE);
					}
					
				}
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
		
		TreeColumn suspicioiusColumn = new TreeColumn(tree, SWT.LEFT);
		suspicioiusColumn.setAlignment(SWT.LEFT);
		suspicioiusColumn.setText("Supsicious");
		suspicioiusColumn.setWidth(100);
		
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
    				if (userFeedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
    					VarValue wrongVar = userFeedback.getOption().getReadVar();
    					return wrongVar.getVarName();
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
    				if (userFeedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
    					return userFeedback.getOption().getReadVar().getManifestationValue();					
    				} else {
    					return "-";
    				}
            	}
            	return null;
            }
		});
		
		this.disposeButtons();
		this.nextNodeViewerColumn.setLabelProvider(new nextNodeButtonLabelProvider(this.currentNode, this.trace));
		
		this.availableFeedbackViewer.setInput(this.getAllAvailableFeedbacks());
		this.checkDefaultAvailableFeedbacks();
		this.availableFeedbackViewer.refresh(true);
	}
	
	protected String genFeedbackType(final UserFeedback feedback) {
		if (feedback.getFeedbackType().equals(UserFeedback.CORRECT)) {
			return "CORRECT";
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
			return "WRONG_BRANCH";
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
			return "WRONG_VARIABLE";
		} else if (feedback.getFeedbackType().equals(UserFeedback.ROOTCAUSE)) {
			return "ROOT_CAUSE";
		} else {
			return null;
		}
	}
	
	protected void checkDefaultAvailableFeedbacks() {
		final FeedbackPath suggestedFeedbackPath = MicroBatViews.getPathView().getFeedbackPath();
		if (suggestedFeedbackPath.contains(this.currentNode)) {
			UserFeedback suggestedFeedback = suggestedFeedbackPath.getFeedback(this.currentNode).getFirstFeedback();
			this.availableFeedbackViewer.setCheckedElements(new Object[] {suggestedFeedback});
		} else {
			this.availableFeedbackViewer.setAllChecked(false);
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
		availableFeedbacks.add(new UserFeedback(UserFeedback.CORRECT));
		availableFeedbacks.add(new UserFeedback(UserFeedback.WRONG_PATH));
		
		for (VarValue readVar : this.currentNode.getReadVariables()) {
			UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_VARIABLE_VALUE);
			feedback.setOption(new ChosenVariableOption(readVar, null));
			availableFeedbacks.add(feedback);
		}

		availableFeedbacks.add(new UserFeedback(UserFeedback.ROOTCAUSE));
		return availableFeedbacks.toArray(new UserFeedback[0]);
	}
	
	public void clearProgramOutput() {
		this.wrongOutputTreeViewer.setInput(null);
		this.wrongOutputTreeViewer.refresh();
		DebugPilotInfo.getInstance().clearOutputs();
	}
	
	protected void selectTraceViewNode(final TraceNode node) {
		final TraceView traceView = MicroBatViews.getTraceView();
		traceView.jumpToNode(trace, node.getOrder(), false);
		traceView.jumpToNode(node);
	}
	
	protected void registerButtons(final Button button) {
		this.nextNodeButtons.add(button);
	}
	
	protected void disposeButtons() {
		for (Button button : this.nextNodeButtons) {
			button.dispose();
		}
		this.nextNodeButtons.clear();
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
			final String buttonText = nextNode == null ? "-" : String.valueOf(nextNode.getOrder());
			button.setText(buttonText);
			button.setEnabled(nextNode != null);
			button.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final TraceView traceView = MicroBatViews.getTraceView();
					traceView.jumpToNode(trace, nextNode.getOrder(), false);
					traceView.jumpToNode(nextNode);
					
					if (userFeedback.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
						UserBehaviorLogger.logEvent(UserBehaviorType.CONTROL_SLICING_EXPLORE);
					} else if (userFeedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
						UserBehaviorLogger.logEvent(UserBehaviorType.DATA_SLICING_EXPLORE);
					}
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
}
