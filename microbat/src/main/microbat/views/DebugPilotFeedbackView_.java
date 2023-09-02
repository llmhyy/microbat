package microbat.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.ChosenVariableOption;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;

public class DebugPilotFeedbackView_ extends DebugFeedbackView {
	
	public final static String ID = "microbat.view.debugPilotFeedback_";
	
	protected List<Button> feedbackButtons = new ArrayList<>();

	protected Label outputNodeLabel;
	protected Button shouldNotExecuteButton;
	protected TreeViewer wrongOutputTreeViewer;
	protected Label tipsLabel;
	
	protected CheckboxTreeViewer avaliableFeedbacksTreeViewer;
	
    protected final int operations = DND.DROP_COPY | DND.DROP_MOVE;
    protected final Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer.getTransfer() };

	public DebugPilotFeedbackView_() {}
	
	@Override
	public void createPartControl(Composite parent) {
		GridLayout parentLayour = new GridLayout(1, true);
		parent.setLayout(parentLayour);
		this.createBody(parent);
		this.createDebugPilotGroup(parent);
	}
	
	public TraceNode getOutPutNode() {
		return this.currentNode;
	}
	
	public boolean isWrongBranch() {
		return this.shouldNotExecuteButton.getSelection();
	}
	
	public VarValue getWrongVars() {
		@SuppressWarnings("unchecked")
		List<VarValue> wrongVars = (List<VarValue>) this.wrongOutputTreeViewer.getInput();
		return wrongVars.get(0);
	}
	
	protected void createDebugPilotGroup(final Composite parent) {
		this.createDebugPilotInputGroup(parent);
		this.createDebugPilotAvaliableFeedbackGroup(parent);
	}
	
	protected void createDebugPilotInputGroup(final Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText("DebugPilot: Initialization");
		group.setLayoutData(new GridData(SWT.FILL, SWT.UP, true, false));
		
		GridLayout gridLayout = new GridLayout(2, false);
		group.setLayout(gridLayout);
		
		this.outputNodeLabel = new Label(group, SWT.NONE);
		this.outputNodeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		this.outputNodeLabel.setText(this.genOutputNodeTextContent());
		
		this.shouldNotExecuteButton = new Button(group, SWT.RADIO);
		this.shouldNotExecuteButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		this.shouldNotExecuteButton.setText("Wrong Branch");
		
		Tree tree = new Tree(group, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);		
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		GridData treeGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		treeGridData.horizontalSpan = 2;
		tree.setLayoutData(treeGridData);
		
		TreeColumn typeColumn = new TreeColumn(tree, SWT.LEFT);
		typeColumn.setAlignment(SWT.LEFT);
		typeColumn.setText("Variable Type");
		typeColumn.setWidth(100);
		
		TreeColumn nameColumn = new TreeColumn(tree, SWT.LEFT);
		nameColumn.setAlignment(SWT.LEFT);
		nameColumn.setText("Variable Name");
		nameColumn.setWidth(100);
		
		TreeColumn valueColumn = new TreeColumn(tree, SWT.LEFT);
		valueColumn.setAlignment(SWT.LEFT);
		valueColumn.setText("Variable Value");
		valueColumn.setWidth(300);
		
		TreeColumn probColumn = new TreeColumn(tree, SWT.LEFT);
		probColumn.setAlignment(SWT.LEFT);
		probColumn.setText("Probability");
		probColumn.setWidth(100);

		this.wrongOutputTreeViewer = new TreeViewer(tree);
		this.wrongOutputTreeViewer.setContentProvider(new RWVariableContentProvider(false));
		this.wrongOutputTreeViewer.setLabelProvider(new VariableLabelProvider());
		this.wrongOutputTreeViewer.addDropSupport(this.operations, this.transferTypes, new ViewerDropAdapter(wrongOutputTreeViewer) {
			@Override
			public boolean performDrop(Object data) {
				if (data instanceof IStructuredSelection selection) {
					wrongOutputTreeViewer.setInput(Arrays.stream(selection.toArray()).collect(Collectors.toCollection(ArrayList::new)));
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
				wrongOutputTreeViewer.setInput(null);
				wrongOutputTreeViewer.refresh();
			}
		});
		
		this.tipsLabel = new Label(group, SWT.NONE);
		this.tipsLabel.setAlignment(SWT.LEFT);
		this.tipsLabel.setText("Double click the output to remove it.");
		
	}
	
	protected void createDebugPilotAvaliableFeedbackGroup(final Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText("DebugPilot: Avaliable Feedbacks");
		group.setLayoutData(new GridData(SWT.FILL, SWT.UP, true, true));
		group.setLayout(new FillLayout());
		
//		GridLayout gridLayout = new GridLayout(1, false);
//		group.setLayout(gridLayout);
		
		Tree tree = new Tree(group, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.CHECK);		
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		GridLayout treeLayout = new GridLayout(1, false);
		tree.setLayout(treeLayout);
		
		TreeColumn typeColumn = new TreeColumn(tree, SWT.LEFT);
		typeColumn.setAlignment(SWT.LEFT);
		typeColumn.setText("Feedback Type");
		typeColumn.setWidth(150);
		
		TreeColumn varColumn = new TreeColumn(tree, SWT.LEFT);
		varColumn.setAlignment(SWT.LEFT);
		varColumn.setText("Variable");
		varColumn.setWidth(300);
		
		TreeColumn nextNodeColumn = new TreeColumn(tree, SWT.LEFT);
		nextNodeColumn.setAlignment(SWT.LEFT);
		nextNodeColumn.setText("Next Node");
		nextNodeColumn.setWidth(100);
		
		this.avaliableFeedbacksTreeViewer = new CheckboxTreeViewer(tree);
		this.avaliableFeedbacksTreeViewer.setContentProvider(new FeedbackContentProvider());
		this.avaliableFeedbacksTreeViewer.setLabelProvider(new FeedbackLableProvider());
	}
	
	protected String genOutputNodeTextContent() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Output Node: ");
		stringBuilder.append(this.currentNode == null ? "Not Selected" : this.currentNode.getOrder());
		return stringBuilder.toString();
	}
	
	@Override
	protected void createSubmitGroup(Composite parent) {
		Group feedbackGroup = new Group(parent, SWT.NONE);
		feedbackGroup.setText("Microbat");
		feedbackGroup.setLayoutData(new GridData(SWT.FILL, SWT.UP, true, false));
		GridLayout gl = new GridLayout(6, true);
		gl.makeColumnsEqualWidth = false;
		gl.marginWidth = 1;
		feedbackGroup.setLayout(gl);
		
		correctButton = new Button(feedbackGroup, SWT.RADIO);
		correctButton.setText("Correct");
		correctButton.setLayoutData(new GridData(SWT.LEFT, SWT.UP, true, false));
		correctButton.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent e) {}
			public void mouseDown(MouseEvent e) {
				feedback = new UserFeedback(UserFeedback.CORRECT);
				clearSelectButtonExcept(correctButton);
			}
			public void mouseDoubleClick(MouseEvent e) {}
		});

		wrongVarButton = new Button(feedbackGroup, SWT.RADIO);
		wrongVarButton.setText("Wrong Variable");
		wrongVarButton.setLayoutData(new GridData(SWT.LEFT, SWT.UP, true, false));
		wrongVarButton.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent e) {}
			public void mouseDown(MouseEvent e) {
				feedback = new UserFeedback(UserFeedback.WRONG_VARIABLE_VALUE);
				clearSelectButtonExcept(wrongVarButton);
			}
			public void mouseDoubleClick(MouseEvent e) {}
		});
		
		wrongPathButton = new Button(feedbackGroup, SWT.RADIO);
		wrongPathButton.setText("Wrong Flow");
		wrongPathButton.setLayoutData(new GridData(SWT.LEFT, SWT.UP, true, false));
		wrongPathButton.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent e) {}
			public void mouseDown(MouseEvent e) {
				feedback = new UserFeedback(UserFeedback.WRONG_PATH);
				clearSelectButtonExcept(wrongPathButton);
			}
			public void mouseDoubleClick(MouseEvent e) {}
		});
		
		unclearButton = new Button(feedbackGroup, SWT.RADIO);
		unclearButton.setText("Unclear");
		unclearButton.setLayoutData(new GridData(SWT.LEFT, SWT.UP, true, false));
		unclearButton.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent e) {}
			public void mouseDown(MouseEvent e) {
				feedback = new UserFeedback(UserFeedback.UNCLEAR);
				clearSelectButtonExcept(unclearButton);
			}
			public void mouseDoubleClick(MouseEvent e) {}
		});
		
		Button submitButton = new Button(feedbackGroup, SWT.NONE);
		submitButton.setText("Find bug!");
		submitButton.setLayoutData(new GridData(SWT.RIGHT, SWT.UP, true, false));
		submitButton.addMouseListener(new FeedbackSubmitListener());
		
		this.bugTypeInferenceButton = new Button(feedbackGroup, SWT.NONE);
		this.bugTypeInferenceButton.setText("Infer type!");
		this.bugTypeInferenceButton.setLayoutData(new GridData(SWT.RIGHT, SWT.UP, true, false));
		this.bugTypeInferenceButton.addMouseListener(new InferBugTypeListener());
		this.bugTypeInferenceButton.setEnabled(isValidToInferBugType());
		
		this.feedbackButtons.add(this.correctButton);
		this.feedbackButtons.add(this.wrongVarButton);
		this.feedbackButtons.add(this.wrongPathButton);
		this.feedbackButtons.add(this.unclearButton);
	}
	
	@Override
	protected CheckboxTreeViewer createVarGroup(SashForm variableForm, String groupName) {
		CheckboxTreeViewer checkboxTreeViewer = super.createVarGroup(variableForm, groupName);

		checkboxTreeViewer.addDragSupport(this.operations, this.transferTypes, new DragSourceAdapter() {
			
			@Override
			public void dragStart(DragSourceEvent event) {
                IStructuredSelection selection = (IStructuredSelection) checkboxTreeViewer.getSelection();
                if (selection.isEmpty()) {
                    event.doit = false;
                    return;
                }
			}
			
			@Override
			public void dragSetData(DragSourceEvent event) {
                IStructuredSelection selection = (IStructuredSelection) checkboxTreeViewer.getSelection();
                // Set the data to be transferred during the drag
                if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
                    LocalSelectionTransfer.getTransfer().setSelection(selection);
                }
			}
			
			@Override
			public void dragFinished(DragSourceEvent event) {}
		});
		
		return checkboxTreeViewer;
	}
	
	protected void clearSelectButtonExcept(final Button selectedButton) {
		for (Button button : this.feedbackButtons) {
			if (button.equals(selectedButton)) {
				continue;
			}
			button.setSelection(false);
		}
	}
	 
	@Override
	public void refresh(TraceNode node) {
		super.refresh(node);
		this.wrongOutputTreeViewer.getTree().clearAll(true);
		this.outputNodeLabel.setText(this.genOutputNodeTextContent());
		this.avaliableFeedbacksTreeViewer.setInput(this.getAllAvaliableFeedbacks());
	}
	
	protected UserFeedback[] getAllAvaliableFeedbacks() {
		List<UserFeedback> avaliableFeedbacks = new ArrayList<>();
		
		avaliableFeedbacks.add(new UserFeedback(UserFeedback.CORRECT));
		avaliableFeedbacks.add(new UserFeedback(UserFeedback.WRONG_PATH));
		
		for (VarValue readVar : this.currentNode.getReadVariables()) {
			UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_VARIABLE_VALUE);
			feedback.setOption(new ChosenVariableOption(readVar, null));
			avaliableFeedbacks.add(feedback);
		}

		return avaliableFeedbacks.toArray(new UserFeedback[0]);
	}
	
	private class FeedbackContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof UserFeedback[] userFeedbacks) {
				return userFeedbacks;
			}
			return new Object[0];
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return false;
		}
	}
	
	private class FeedbackLableProvider implements ITableLabelProvider {

		@Override
		public void addListener(ILabelProviderListener listener) {

		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {

		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof UserFeedback userFeedback) {
				switch (columnIndex) {
				case 0:
					return userFeedback.getFeedbackType();
				case 1:
					VarValue wrongVar = null;
					if (userFeedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
						wrongVar = userFeedback.getOption().getReadVar();
						return wrongVar.getVarID();
					}
					return "-";
				case 2:
					final TraceNode nextNode = TraceUtil.findNextNode(currentNode, userFeedback, traceView.getTrace());
					return nextNode == null ? "-" : String.valueOf(nextNode.getOrder());
				default:
					return null;
				}
			}
			return null;
		}
		
	}
}
