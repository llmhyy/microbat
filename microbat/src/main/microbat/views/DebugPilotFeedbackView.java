package microbat.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.part.ViewPart;

import microbat.debugpilot.DebugPilotInfo;
import microbat.debugpilot.NodeFeedbacksPair;
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
import microbat.views.utils.lableprovider.FeedbackLabelProvider;
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
	
	/* Program outcome information */
	protected Label outputNodeLabel;
	protected Button shouldNotExecuteButton;
	protected TreeViewer wrongOutputTreeViewer;
	protected Label removeOutputLabel;
	
	/* All possible feedbacks */
	protected CheckboxTableViewer availableFeedbackViewer;
	protected Button feedbackButton;
	protected Label giveFeedbackLabel;
	protected Label nextNodeLabel;
	
    protected final int operations = DND.DROP_COPY | DND.DROP_MOVE;
    protected final Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer.getTransfer() };

    public DebugPilotFeedbackView() {

    }
	
	@Override
	public void createPartControl(Composite parent) {
		GridLayout parentLayout = new GridLayout(1, true);
		parent.setLayout(parentLayout);
		this.createReadVariablesViewer(parent);
		this.createWrittenVariableViewer(parent);
		this.createControlDominatorGroup(parent);
		this.createOutputGroup(parent);
		this.createAvaliableFeedbackView(parent);
	}

	@Override
	public void setFocus() {}
	
	public void refresh(final TraceNode currentNode, final Trace trace) {
		this.currentNode = currentNode;
		this.trace = trace;
		DebugPilotInfo.getInstance().setOutputNode(this.currentNode);
		this.refreshReadVariableViewer();
		this.refreshWrittenVariableViewer();
		this.refreshControlDominoatorViewer();
		this.refreshOutputGroup();
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
		SashForm variableForm = new SashForm(parent, SWT.VERTICAL);
		variableForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Group group = new Group(variableForm, SWT.NONE);
		group.setText("Read Variables");
		group.setLayout(new FillLayout());

		final Tree tree = this.createVarTree(group);

		TreeColumn probColumn = new TreeColumn(tree, SWT.LEFT);
		probColumn.setAlignment(SWT.LEFT);
		probColumn.setText("Correctness");
		probColumn.setWidth(100);
		
		TreeColumn costColumn = new TreeColumn(tree, SWT.LEFT);
		costColumn.setAlignment(SWT.LEFT);
		costColumn.setText("Cost");
		costColumn.setWidth(100);
		
		this.readVariableViewer = new TreeViewer(tree);
		this.readVariableViewer.addDragSupport(this.operations, this.transferTypes, this.createDragSourceAdapter(this.readVariableViewer));
		this.readVariableViewer.setLabelProvider(new VariableWithProbabilityLabelProvider());
	}
	
	protected void createWrittenVariableViewer(final Composite parent) {
		SashForm variableForm = new SashForm(parent, SWT.VERTICAL);
		variableForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Group group = new Group(variableForm, SWT.NONE);
		group.setText("Written Variables");
		group.setLayout(new FillLayout());
		
		final Tree tree = this.createVarTree(group);
		
		TreeColumn probColumn = new TreeColumn(tree, SWT.LEFT);
		probColumn.setAlignment(SWT.LEFT);
		probColumn.setText("Correctness");
		probColumn.setWidth(100);
		
		TreeColumn costColumn = new TreeColumn(tree, SWT.LEFT);
		costColumn.setAlignment(SWT.LEFT);
		costColumn.setText("Cost");
		costColumn.setWidth(100);
		
		this.writtenVariableViewer = new TreeViewer(tree);
		this.writtenVariableViewer.addDragSupport(this.operations, this.transferTypes, this.createDragSourceAdapter(this.writtenVariableViewer));
		this.writtenVariableViewer.setLabelProvider(new VariableWithProbabilityLabelProvider());
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
		
		Table table = new Table(variableForm, SWT.H_SCROLL | SWT.V_SCROLL| SWT.FULL_SELECTION | SWT.CHECK | SWT.MULTI);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayout(new FillLayout());
		
		TableColumn typeColumn = new TableColumn(table, SWT.LEFT);
		typeColumn.setAlignment(SWT.LEFT);
		typeColumn.setText("Type");
		typeColumn.setWidth(170);
		 
		TableColumn varColumn = new TableColumn(table, SWT.LEFT);
		varColumn.setAlignment(SWT.LEFT);
		varColumn.setText("Variable");
		varColumn.setWidth(100);
		
		TableColumn varValueColumn = new TableColumn(table, SWT.LEFT);
		varValueColumn.setAlignment(SWT.LEFT);
		varValueColumn.setText("Value");
		varValueColumn.setWidth(200);
		
		TableColumn nextNodeColumn = new TableColumn(table, SWT.LEFT);
		nextNodeColumn.setAlignment(SWT.LEFT);
		nextNodeColumn.setText("Next Node");
		nextNodeColumn.setWidth(90);
		
		
		this.availableFeedbackViewer = new CheckboxTableViewer(table);
		this.availableFeedbackViewer.setContentProvider(new FeedbackContentProvider());
		this.availableFeedbackViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection iSel = event.getSelection();
				if (iSel instanceof StructuredSelection structuredSelection) {
					Object obj = structuredSelection.getFirstElement();
					if (obj instanceof UserFeedback userFeedback) {				
						final TraceNode nextNode = TraceUtil.findNextNode(currentNode, userFeedback, trace);
						if (nextNode != null) {
							selectTraceViewNode(nextNode);
						}
					}
				}
			}
		});
		
		this.nextNodeLabel = new Label(parent, SWT.NONE);
		this.nextNodeLabel.setAlignment(SWT.LEFT);
		this.nextNodeLabel.setText("Click on the row to explore next node.");
		GridData labelGridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		labelGridData.horizontalSpan = 2;
		this.nextNodeLabel.setLayoutData(labelGridData);
	}
	
	protected void createGiveFeedbackGroup(final Composite parent) {
		this.giveFeedbackLabel = new Label(parent, SWT.NONE);
		this.giveFeedbackLabel.setAlignment(SWT.LEFT);
		this.giveFeedbackLabel.setText("Here the possible feedback you may give: ");
				
		this.feedbackButton = new Button(parent, SWT.NONE);
		this.feedbackButton.setText("Give Feedback");
		this.feedbackButton.setLayoutData(new GridData(SWT.RIGHT, SWT.UP, true, false));
		this.feedbackButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				UserFeedback[] feedbacks = Arrays.stream(availableFeedbackViewer.getCheckedElements()).map(obj -> {return (UserFeedback) obj;}).toArray(UserFeedback[]::new);
				NodeFeedbacksPair userFeedbacksPair = new NodeFeedbacksPair(currentNode, feedbacks);
				DebugPilotInfo.getInstance().setNodeFeedbacksPair(userFeedbacksPair);
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
	
	protected void refreshControlDominoatorViewer() {
		this.controlDominatorViewer.setInput(this.currentNode);
		this.controlDominatorViewer.refresh(true);
	}
	
	protected void refreshOutputGroup() {
		this.outputNodeLabel.setText(this.genOutputNodeTextContent());
	}
	
	protected void refreshAvailableFeedbackViewer() {
		this.availableFeedbackViewer.setLabelProvider(new FeedbackLabelProvider(this.currentNode, this.trace));
		this.availableFeedbackViewer.setInput(this.getAllAvailableFeedbacks());
		this.availableFeedbackViewer.refresh(true);
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
	
	
}
