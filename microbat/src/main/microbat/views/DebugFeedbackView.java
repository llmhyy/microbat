package microbat.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import debuginfo.DebugInfo;
import debuginfo.NodeFeedbackPair;
import microbat.algorithm.graphdiff.GraphDiff;
import microbat.baseline.probpropagation.BeliefPropagation;
import microbat.behavior.Behavior;
import microbat.behavior.BehaviorData;
import microbat.behavior.BehaviorReporter;
import microbat.handler.BaselineHandler;
import microbat.handler.CheckingState;
import microbat.model.BreakPointValue;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.ArrayValue;
import microbat.model.value.PrimitiveValue;
import microbat.model.value.ReferenceValue;
import microbat.model.value.VarValue;
import microbat.model.value.VirtualValue;
import microbat.model.variable.Variable;
import microbat.model.variable.VirtualVar;
import microbat.recommendation.Bug;
import microbat.recommendation.BugInferer;
import microbat.recommendation.ChosenVariableOption;
import microbat.recommendation.DebugState;
import microbat.recommendation.StepRecommender;
import microbat.recommendation.UserFeedback;
import microbat.util.JavaUtil;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import microbat.util.TempVariableInfo;


public class DebugFeedbackView extends ViewPart {

	private TraceNode currentNode;
//	private TraceNode lastestNode;
	
//	private MutilThreadTraceView traceView;
	private TraceView traceView;
	
	private StepRecommender recommender = new StepRecommender(Settings.enableLoopInference);
	
	private UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_VARIABLE_VALUE);
	private String lastFeedbackType = null;
	
//	public static final String INPUT = "input";
//	public static final String OUTPUT = "output";
//	public static final String STATE = "state";
	
	/**
	 * Here, the 0th element indicates input; 1st element indicates output; and 2nd element 
	 * indicates state.
	 */
//	private CheckboxTreeViewer[] treeViewerList = new CheckboxTreeViewer[3];
	
//	private Tree inputTree;
//	private Tree outputTree;
//	private Tree stateTree;
//	
//	private CheckboxTreeViewer inputTreeViewer;
//	private CheckboxTreeViewer outputTreeViewer;
	private CheckboxTreeViewer stateTreeViewer;
	private CheckboxTreeViewer writtenVariableTreeViewer;
	private CheckboxTreeViewer readVariableTreeViewer;
	
	private CheckboxTreeViewer consequenceTreeViewer;
	
//	private ICheckStateListener stateListener;
//	private ICheckStateListener RWVarListener;
	private ITreeViewerListener treeListener;
	
	private Button yesButton;
	private Button noButton;
	private Button unclearButton;
	private Button wrongPathButton;
	private Button bugTypeInferenceButton;
	
	public DebugFeedbackView() {
	}
	
	public void clear(){
		this.currentNode = null;
		this.recommender = new StepRecommender(true);
	}
	
	public void setRecommender(StepRecommender recommender){
		this.recommender = recommender;
	}
	
	public void refresh(TraceNode node){
		this.currentNode = node;
		
//		BreakPointValue thisState = node.getProgramState();
//		BreakPointValue afterState = node.getAfterState();
		
//		List<GraphDiff> cons = node.getConsequences();
		
//		HierarchyGraphDiffer differ = new HierarchyGraphDiffer();
//		differ.diff(thisState, afterState);
		
//		createConsequenceContent(cons);
//		createStateContent(thisState);
		createWrittenVariableContent(node.getWrittenVariables());
		createReadVariableContect(node.getReadVariables());
		
		yesButton.setSelection(false);
		noButton.setSelection(true);
		unclearButton.setSelection(false);
		wrongPathButton.setSelection(false);
		bugTypeInferenceButton.setEnabled(isValidToInferBugType());
		
		feedback = new UserFeedback(UserFeedback.WRONG_VARIABLE_VALUE);
		VarValue readVar = null; 
		if(this.readVariableTreeViewer.getCheckedElements()!=null && this.readVariableTreeViewer.getCheckedElements().length!=0) {
			readVar = (VarValue) this.readVariableTreeViewer.getCheckedElements()[0];
		}
		
		VarValue writtenVar = null; 
		if(this.writtenVariableTreeViewer.getCheckedElements()!=null && this.writtenVariableTreeViewer.getCheckedElements().length!=0) {
			writtenVar = (VarValue) this.writtenVariableTreeViewer.getCheckedElements()[0];
		}
		
		ChosenVariableOption option = new ChosenVariableOption(readVar, writtenVar);
		feedback.setOption(option);
	}
	
	
	private void createWrittenVariableContent(List<VarValue> writtenVariables) {
		this.writtenVariableTreeViewer.setContentProvider(new RWVariableContentProvider(false));
		this.writtenVariableTreeViewer.setLabelProvider(new VariableLabelProvider());
		this.writtenVariableTreeViewer.setInput(writtenVariables);	
		
		setChecks(this.writtenVariableTreeViewer, RW);

		this.writtenVariableTreeViewer.refresh(true);
		
	}

	private void createReadVariableContect(List<VarValue> readVariables) {
		this.readVariableTreeViewer.setContentProvider(new RWVariableContentProvider(true));
		this.readVariableTreeViewer.setLabelProvider(new VariableLabelProvider());
		this.readVariableTreeViewer.setInput(readVariables);	
		
		setChecks(this.readVariableTreeViewer, RW);

		this.readVariableTreeViewer.refresh(true);
	}

	private void createConsequenceContent(List<GraphDiff> cons) {
		this.consequenceTreeViewer.setContentProvider(new ConsequenceContentProvider());
		this.consequenceTreeViewer.setLabelProvider(new ConsequenceLabelProvider());
		this.consequenceTreeViewer.setInput(cons);	
		
		setChecks(this.consequenceTreeViewer, STATE);
		
		this.consequenceTreeViewer.refresh(true);
	}

	private void createStateContent(BreakPointValue value){
		this.stateTreeViewer.setContentProvider(new VariableContentProvider());
		this.stateTreeViewer.setLabelProvider(new VariableLabelProvider());
		this.stateTreeViewer.setInput(value);	
		
		setChecks(this.stateTreeViewer, STATE);

		this.stateTreeViewer.refresh(true);
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout parentLayout = new GridLayout(1, true);
		parent.setLayout(parentLayout);

		createSubmitGroup(parent);
		createOptionaGroup(parent);
		createBody(parent);
	}

	private void createBody(Composite parent) {
		SashForm variableForm = new SashForm(parent, SWT.VERTICAL);
		variableForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

//		createVarGroup(variableForm, "Read Variables: ", INPUT);
//		createVarGroup(variableForm, "Consequences: ", OUTPUT);
//		createConsequenceGroup(variableForm, "Consequences: ");
		
		this.writtenVariableTreeViewer = createVarGroup(variableForm, "Written Variables: ");
		this.readVariableTreeViewer = createVarGroup(variableForm, "Read Variables: ");
//		this.stateTreeViewer = createVarGroup(variableForm, "States: ");

		variableForm.setWeights(new int[] {5, 5});
		
		addListener();
	}
	
	public static final String RW = "rw";
	public static final String STATE = "state";
	
	private void setChecks(CheckboxTreeViewer treeViewer, String type){
		Tree tree = treeViewer.getTree();
		for(TreeItem item: tree.getItems()){
			setChecks(item, type);
		}
	}
	
	private void setChecks(TreeItem item, String type){
		Object element = item.getData();
		if(element == null){
			return;
		}
		
		VarValue ev = null;
		if(element instanceof VarValue){
			ev = (VarValue)element;
		}
		else if(element instanceof GraphDiff){
			ev = (VarValue) ((GraphDiff)element).getChangedNode();
		}
		
		String varID = ev.getVarID();
		if(Settings.interestedVariables.contains(ev)){
			item.setChecked(true);
		}
		else{
			item.setChecked(false);
		}

		for(TreeItem childItem: item.getItems()){
			setChecks(childItem, type);
		}
	}
	
	
	class RWVarListener implements ICheckStateListener{
		private String RWType;
		
		public RWVarListener(String RWType){
			this.RWType = RWType;
		}
		
		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			Object obj = event.getElement();
			VarValue value = null;
			
			if(obj instanceof VarValue){
				Trace trace = getTraceView().getTrace();
//				Trace trace = getConcurrentTraceView().getCurTrace();
				value = (VarValue)obj;
				String varID = value.getVarID();
				
				if(!Settings.interestedVariables.contains(value)){
					Settings.interestedVariables.add(trace.getCheckTime(), value);
					
					ChosenVariableOption option = feedback.getOption();
					if(option == null){
						option = new ChosenVariableOption(null, null);
					}
					
					if(this.RWType.equals(Variable.READ)){
						option.setReadVar(value);
					}
					if(this.RWType.equals(Variable.WRITTEN)){
						option.setWrittenVar(value);
					}
					feedback.setOption(option);
					
					TempVariableInfo.variableOption = option;
					TempVariableInfo.line = currentNode.getLineNumber();
					String cuName = currentNode.getBreakPoint().getDeclaringCompilationUnitName();
					TempVariableInfo.cu = JavaUtil.findCompilationUnitInProject(cuName, null);
				}
				else{
					Settings.interestedVariables.remove(value);
				}
				
				setChecks(writtenVariableTreeViewer, RW);
				setChecks(readVariableTreeViewer, RW);
//				setChecks(stateTreeViewer, STATE);
				
				bugTypeInferenceButton.setEnabled(isValidToInferBugType());
				
				writtenVariableTreeViewer.refresh();
				readVariableTreeViewer.refresh();
//				stateTreeViewer.refresh();	
				
				//setCurrentNodeChecked(trace, currentNode);
			}
			
		}
	}

	private void addListener() {
		
		treeListener = new ITreeViewerListener() {
			
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				
				setChecks(readVariableTreeViewer, RW);
				setChecks(writtenVariableTreeViewer, RW);
//				setChecks(stateTreeViewer, STATE);
				
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						readVariableTreeViewer.refresh();
						writtenVariableTreeViewer.refresh();
//						stateTreeViewer.refresh();	
					}
				});
				
				
				bugTypeInferenceButton.setEnabled(isValidToInferBugType());
			}
			
			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				
			}
		};
		
		this.readVariableTreeViewer.addTreeListener(treeListener);
		this.writtenVariableTreeViewer.addTreeListener(treeListener);
//		this.stateTreeViewer.addTreeListener(treeListener);
		
		this.writtenVariableTreeViewer.addCheckStateListener(new RWVarListener(Variable.WRITTEN));
		this.readVariableTreeViewer.addCheckStateListener(new RWVarListener(Variable.READ));
//		this.stateTreeViewer.addCheckStateListener(new RWVarListener(Variable.READ));
		
	}

	private CheckboxTreeViewer createVarGroup(SashForm variableForm, String groupName) {
		Group varGroup = new Group(variableForm, SWT.NONE);
		varGroup.setText(groupName);
		varGroup.setLayout(new FillLayout());

		Tree tree = new Tree(varGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.CHECK);		
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

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
		
		return new CheckboxTreeViewer(tree);
//		this.stateTreeViewer = new CheckboxTreeViewer(tree);
//		if(type.equals(INPUT)){
//			this.treeViewerList[0] = new CheckboxTreeViewer(tree);
//		}
//		else if(type.equals(OUTPUT)){
//			this.treeViewerList[1] = new CheckboxTreeViewer(tree);
//		}
//		else if(type.equals(STATE)){
//			this.treeViewerList[2] = new CheckboxTreeViewer(tree);
//		}
	}
	
	private void createConsequenceGroup(SashForm variableForm, String groupName) {
		Group varGroup = new Group(variableForm, SWT.NONE);
		varGroup.setText(groupName);
		varGroup.setLayout(new FillLayout());

		Tree tree = new Tree(varGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.CHECK);		
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		TreeColumn typeColumn = new TreeColumn(tree, SWT.LEFT);
		typeColumn.setAlignment(SWT.LEFT);
		typeColumn.setText("Variable Type");
		typeColumn.setWidth(100);
		
		TreeColumn nameColumn = new TreeColumn(tree, SWT.LEFT);
		nameColumn.setAlignment(SWT.LEFT);
		nameColumn.setText("Variable Name");
		nameColumn.setWidth(100);
		
		TreeColumn newValueColumn = new TreeColumn(tree, SWT.LEFT);
		newValueColumn.setAlignment(SWT.LEFT);
		newValueColumn.setText("New Value");
		newValueColumn.setWidth(130);
		
		TreeColumn oldValueColumn = new TreeColumn(tree, SWT.LEFT);
		oldValueColumn.setAlignment(SWT.LEFT);
		oldValueColumn.setText("Old Value");
		oldValueColumn.setWidth(130);
		
		this.consequenceTreeViewer = new CheckboxTreeViewer(tree);
	}
	
	private void createOptionaGroup(Composite parent) {
		Group feedbackGroup = new Group(parent, SWT.NONE);
		feedbackGroup.setText("Options and Explanation");
		feedbackGroup.setLayoutData(new GridData(SWT.FILL, SWT.UP, true, false));
		GridLayout gl = new GridLayout(1, true);
		gl.makeColumnsEqualWidth = false;
		gl.marginWidth = 1;
		feedbackGroup.setLayout(gl);
		
		final Button enableButton = new Button(feedbackGroup, SWT.CHECK);
		enableButton.setSelection(Settings.enableLoopInference);
		enableButton.setText("Enable Loop Inference");
		enableButton.setLayoutData(new GridData(SWT.LEFT, SWT.UP, true, false));
		enableButton.addSelectionListener(new SelectionAdapter() {
			@Override
		    public void widgetSelected(SelectionEvent e)
		    {
		        if (enableButton.getSelection()){
		        	Settings.enableLoopInference = true;
		        }
		        else{
		        	Settings.enableLoopInference = false;	        	
		        }
		        
		        recommender.setEnableLoopInference(Settings.enableLoopInference);
		    }
		});
	}

	private void createSubmitGroup(Composite parent) {
		Group feedbackGroup = new Group(parent, SWT.NONE);
		feedbackGroup.setText("Are all variables in this step correct?");
		feedbackGroup.setLayoutData(new GridData(SWT.FILL, SWT.UP, true, false));
		GridLayout gl = new GridLayout(6, true);
		gl.makeColumnsEqualWidth = false;
		gl.marginWidth = 1;
		feedbackGroup.setLayout(gl);
		
		yesButton = new Button(feedbackGroup, SWT.RADIO);
		yesButton.setText(" Yes");
		yesButton.setLayoutData(new GridData(SWT.LEFT, SWT.UP, true, false));
		yesButton.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				feedback = new UserFeedback(UserFeedback.CORRECT);
			}

			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		noButton = new Button(feedbackGroup, SWT.RADIO);
		noButton.setText("Wrong-Var");
		noButton.setLayoutData(new GridData(SWT.LEFT, SWT.UP, true, false));
		noButton.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				feedback = new UserFeedback(UserFeedback.WRONG_VARIABLE_VALUE);
			}

			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		
		wrongPathButton = new Button(feedbackGroup, SWT.CHECK);
		wrongPathButton.setText("Wrong-Flow");
		wrongPathButton.setLayoutData(new GridData(SWT.LEFT, SWT.UP, true, false));
		wrongPathButton.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				feedback = new UserFeedback(UserFeedback.WRONG_PATH);
			}

			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		
		unclearButton = new Button(feedbackGroup, SWT.RADIO);
		unclearButton.setText(" Unclear");
		unclearButton.setLayoutData(new GridData(SWT.LEFT, SWT.UP, true, false));
		unclearButton.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				feedback = new UserFeedback(UserFeedback.UNCLEAR);
			}

			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		
		
		
//		Label holder = new Label(feedbackGroup, SWT.NONE);
//		holder.setText("");

		Button submitButton = new Button(feedbackGroup, SWT.NONE);
		submitButton.setText("Find bug!");
		submitButton.setLayoutData(new GridData(SWT.RIGHT, SWT.UP, true, false));
		submitButton.addMouseListener(new FeedbackSubmitListener());
		
		Button baselineButton = new Button(feedbackGroup, SWT.NONE);
		baselineButton.setText("Baseline");
		baselineButton.setLayoutData(new GridData(SWT.RIGHT, SWT.UP, true, false));
		baselineButton.addMouseListener(new BaselineButtonListener());
		
		Button inputButton = new Button(feedbackGroup, SWT.NONE);
		inputButton.setText("Inputs");
		inputButton.setLayoutData(new GridData(SWT.RIGHT, SWT.UP, true, false));
		inputButton.addMouseListener(new AddInputsListener());
		
		Button outputButton = new Button(feedbackGroup, SWT.NONE);
		outputButton.setText("Outputs");
		outputButton.setLayoutData(new GridData(SWT.RIGHT, SWT.UP, true, false));
		outputButton.addMouseListener(new AddOutputsListener());
		
		Button clearIOButton = new Button(feedbackGroup, SWT.NONE);
		clearIOButton.setText("clear IO");
		clearIOButton.setLayoutData(new GridData(SWT.RIGHT, SWT.UP, true, false));
		clearIOButton.addMouseListener(new ClearVarsListener());
		
		Button printIOButton = new Button(feedbackGroup, SWT.NONE);
		printIOButton.setText("IO");
		printIOButton.setLayoutData(new GridData(SWT.RIGHT, SWT.UP, true, false));
		printIOButton.addMouseListener(new ShowIOListener());
		
		Button rootCauseFoundButton = new Button(feedbackGroup, SWT.NONE);
		rootCauseFoundButton.setText("Root Cause");
		rootCauseFoundButton.setLayoutData(new GridData(SWT.RIGHT, SWT.UP, true, false));
		rootCauseFoundButton.addMouseListener(new RootCauseFoundListener());
				
		bugTypeInferenceButton = new Button(feedbackGroup, SWT.NONE);
		bugTypeInferenceButton.setText("Infer type!");
		bugTypeInferenceButton.setLayoutData(new GridData(SWT.RIGHT, SWT.UP, true, false));
		bugTypeInferenceButton.addMouseListener(new InferBugTypeListener());
		bugTypeInferenceButton.setEnabled(isValidToInferBugType());
	}
	
	private void setCurrentNodeChecked(Trace trace, TraceNode currentNode) {
		int checkTime = trace.getCheckTime()+1;
		currentNode.setCheckTime(checkTime);
		trace.setCheckTime(checkTime);
	}
	
	private boolean isValidToInferBugType(){
		if(currentNode != null){
			return true;
			
//			boolean flag1 = currentNode.getReadVarCorrectness(Settings.interestedVariables, true)==TraceNode.READ_VARS_CORRECT &&
//					currentNode.getWittenVarCorrectness(Settings.interestedVariables, true)==TraceNode.WRITTEN_VARS_INCORRECT;
//			boolean flag2 = recommender.getState()==SuspiciousNodeRecommender.BINARY_SEARCH;
//		
//			boolean flag3 = false;
//			TraceNode landMarkNode = recommender.getRange().getBinaryLandMark();
//			if(landMarkNode != null){
//				flag3 = currentNode.getOrder() == landMarkNode.getOrder();
//			}
//			
//			boolean flag4 = recommender.getState()==SuspiciousNodeRecommender.SKIP;
			
//			return (flag1 && flag2 && flag3) || (flag1 && flag4);
//			
//			return flag1;
		}
		else{
			return false;
		}
	}
	
	class TestingButtonListener implements MouseListener {

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseDown(MouseEvent e) {
			final Trace trace = getTraceView().getCurrentTrace();
//			final Trace trace = getConcurrentTraceView().getCurTrace();
			
			Job job = new Job("searching for suspicious step...") {
				
				private void jumpToNode(Trace trace, TraceNode suspiciousNode) {
//					TraceView view = MicroBatViews.getTraceView();
					getTraceView().jumpToNode(trace, suspiciousNode.getOrder(), true);
//					getConcurrentTraceView().jumpToNode(trace, suspiciousNode.getOrder(), true);
				}
				
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					
					BeliefPropagation encoder = new BeliefPropagation(trace);
					encoder.setFlag(true);

					encoder.encode();
					
					TraceNode errorNode = encoder.getMostErroneousNode();
					System.out.println("Error Node: " + errorNode.getOrder());
					Display.getDefault().asyncExec(new Runnable(){
						@Override
						public void run() {
							jumpToNode(trace, errorNode);	
						}
					});
					
					return Status.OK_STATUS;
				}
			};
			job.schedule();
			
		}

		@Override
		public void mouseUp(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
	class BaselineButtonListener implements MouseListener {
		
		@Override
		public void mouseDoubleClick(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseDown(MouseEvent e) {
			UserFeedback feedback = new UserFeedback();
			if (yesButton.getSelection()) {
				feedback.setFeedbackType(UserFeedback.CORRECT);
			} else if (wrongPathButton.getSelection()) {
				feedback.setFeedbackType(UserFeedback.WRONG_PATH);
			} else {
				feedback.setFeedbackType(UserFeedback.WRONG_VARIABLE_VALUE);
				List<VarValue> selectedReadVars = getSelectedReadVars();
				List<VarValue> selectedWriteVars = getSelectedWriteVars();
				if (selectedReadVars.isEmpty() && selectedWriteVars.isEmpty()) {
					throw new RuntimeException("No selected variables");
				}
				VarValue selectedReadVar = null;
				if (!selectedReadVars.isEmpty()) {
					selectedReadVar = selectedReadVars.get(0);
				}
				
				VarValue selectedWriteVar = null;
				if (!selectedWriteVars.isEmpty()) {
					selectedReadVar = selectedWriteVars.get(0);
				}
				feedback.setOption(new ChosenVariableOption(selectedReadVar, selectedWriteVar));
			}
			BaselineHandler.setManualFeedback(feedback, currentNode);
			StepwisePropagationHandler.setManualFeedback(feedback, currentNode);
		}

		@Override
		public void mouseUp(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class FeedbackSubmitListener implements MouseListener{
		public void mouseUp(MouseEvent e) {}
		public void mouseDoubleClick(MouseEvent e) {}
		
		private void openChooseFeedbackDialog(){
			MessageBox box = new MessageBox(PlatformUI.getWorkbench()
					.getDisplay().getActiveShell());
			box.setMessage("Please tell me whether this step is correct or not!");
			box.open();
		}
		
		private void openWrongVariableDialog(){
			MessageBox box = new MessageBox(PlatformUI.getWorkbench()
					.getDisplay().getActiveShell());
			box.setMessage("Please select a variable");
			box.open();
		}
		
		private void openBugFoundDialog(){
			MessageBox box = new MessageBox(PlatformUI.getWorkbench()
					.getDisplay().getActiveShell());
			box.setMessage("Correct read variable with incorrect written variable, You have found the bug!");
			box.open();
		}
		
		private void openReconfirmDialog(final String message){
			
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					Status status = new Status(IStatus.ERROR, "My Plug-in ID", 0,
				            "Conflict Choice", null);
					ErrorDialog.openError(PlatformUI.getWorkbench()
							.getDisplay().getActiveShell(), "Conflict Choice", message, status);
				}
			});
			
		}
		
		private boolean isValidForRecommendation(){
			int readVarCorrectness = currentNode.getReadVarCorrectness(Settings.interestedVariables, true);
			int writtenVarCorrectness = currentNode.getWittenVarCorrectness(Settings.interestedVariables, true);
			boolean existWrittenVariable = !currentNode.getWrittenVariables().isEmpty();
			boolean existReadVariable = !currentNode.getReadVariables().isEmpty();
			
			String feedbackType = feedback.getFeedbackType();
			
			if(feedbackType.equals(UserFeedback.WRONG_PATH) || feedbackType.equals(UserFeedback.UNCLEAR)){
				return true;
			}
			
//			if(existWrittenVariable && existReadVariable && writtenVarCorrectness==TraceNode.WRITTEN_VARS_INCORRECT
//					&& readVarCorrectness==TraceNode.READ_VARS_CORRECT){
//				openBugFoundDialog();
//				return false;
//			}
//			else if(existWrittenVariable && existReadVariable && writtenVarCorrectness==TraceNode.WRITTEN_VARS_CORRECT 
//					&& readVarCorrectness==TraceNode.READ_VARS_INCORRECT){
//				String message = "It seems that this step is correct and it takes "
//						+ "some incorrect read variables while produces correct output (written variable), "
//						+ "are you really sure?";
//				openReconfirmDialog(message);
//				return false;
//			}
//			else if(((existWrittenVariable && writtenVarCorrectness==TraceNode.WRITTEN_VARS_INCORRECT) ||
//					(existReadVariable && readVarCorrectness==TraceNode.READ_VARS_INCORRECT))
//					&& feedbackType.equals(UserFeedback.CORRECT)){
//				String message = "Some variables are marked incorrect, but your feedback is marked correct (\"Yes\" choice), "
//						+ "are you really sure?";
//				openReconfirmDialog(message);
//				return false;
//			}
			else if(lastFeedbackType != null && lastFeedbackType.equals(UserFeedback.WRONG_PATH) && feedbackType.equals(UserFeedback.CORRECT)){
				String message = "The lastest node has wrong path, but you now tell me that no variable "
						+ "in this conditional statement is wrong, are you really sure?";
				openReconfirmDialog(message);
				return false;
			}
			
			return true;
		}

		public void mouseDown(MouseEvent e) {
			if (feedback == null) {
				openChooseFeedbackDialog();
			} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)
					&& feedback.getOption()==null) {
				openWrongVariableDialog();
			}
			else {
				final Trace trace = getTraceView().getCurrentTrace();
//				final Trace trace = getConcurrentTraceView().getCurTrace();
				
				Job job = new Job("searching for suspicious step...") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						
						boolean isValidForRecommendation = isValidForRecommendation();
						if(isValidForRecommendation){
							String feedbackType = feedback.getFeedbackType();
							
							CheckingState state = new CheckingState();
							state.recordCheckingState(currentNode, recommender, trace, Settings.interestedVariables, 
									Settings.wrongPathNodeOrder, Settings.potentialCorrectPatterns);
							Settings.checkingStateStack.push(state);
							
							if(!feedbackType.equals(UserFeedback.UNCLEAR)){
								setCurrentNodeChecked(trace, currentNode);		
								updateVariableCheckTime(trace, currentNode);
							}
							
							collectBehavior(feedbackType);
							
							if(feedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE) && feedback.getOption() == null){
								List<VarValue> list = currentNode.getWrongReadVars(Settings.interestedVariables);
								if(!list.isEmpty()){
									ChosenVariableOption option = new ChosenVariableOption(list.get(0), null);
									feedback.setOption(option);
								}
							}
							
							final TraceNode suspiciousNode = recommender.recommendNode(trace, currentNode, feedback);
							lastFeedbackType = feedbackType;
							
							if(recommender.getState()==DebugState.BINARY_SEARCH || recommender.getState()==DebugState.SKIP){
								Behavior behavior = BehaviorData.getOrNewBehavior(Settings.launchClass);
								behavior.increaseSkip();
								new BehaviorReporter(Settings.launchClass).export(BehaviorData.projectBehavior);
							}
							
							if(suspiciousNode != null){
								Display.getDefault().asyncExec(new Runnable(){
									@Override
									public void run() {
										jumpToNode(trace, suspiciousNode);	
									}
								});
							}
						}
						
						return Status.OK_STATUS;
					}
				};
				
				job.schedule();
				
			}
		}
		
		private void collectBehavior(String feedbackType) {
			Behavior behavior = BehaviorData.getOrNewBehavior(Settings.launchClass);
			if(feedbackType.equals(UserFeedback.CORRECT)){
				behavior.increaseCorrectFeedback();
			}
			else if(feedbackType.equals(UserFeedback.WRONG_VARIABLE_VALUE)){
				behavior.increaseWrongValueFeedback();
			}
			else if(feedbackType.equals(UserFeedback.WRONG_PATH)){
				behavior.increaseWrongPathFeedback();
			}
			else if(feedbackType.equals(UserFeedback.UNCLEAR)){
				behavior.increaseUnclearFeedback();
			}
			new BehaviorReporter(Settings.launchClass).export(BehaviorData.projectBehavior);;
		}
		
		private void updateVariableCheckTime(Trace trace, TraceNode currentNode) {
			for(VarValue var: currentNode.getReadVariables()){
				if(Settings.interestedVariables.contains(var)){
					Settings.interestedVariables.add(trace.getCheckTime(), var);
				}
			}
			
			for(VarValue var: currentNode.getWrittenVariables()){
				String varID = var.getVarID();
				if(Settings.interestedVariables.contains(var)){
					Settings.interestedVariables.add(trace.getCheckTime(), var);
				}
			}
		}
		
		
		private void jumpToNode(Trace trace, TraceNode suspiciousNode) {
//			TraceView view = MicroBatViews.getTraceView();
			getTraceView().jumpToNode(trace, suspiciousNode.getOrder(), true);
//			getConcurrentTraceView().jumpToNode(trace, suspiciousNode.getOrder(), true);
		}
	}
	
	class InferBugTypeListener implements MouseListener{

		@Override
		public void mouseDoubleClick(MouseEvent e) {}
		@Override
		public void mouseUp(MouseEvent e) {}

		@Override
		public void mouseDown(MouseEvent e) {
			BugInferer inferer = new BugInferer();
			Bug bug = inferer.infer(currentNode, recommender);
			
			MessageBox box = new MessageBox(PlatformUI.getWorkbench()
					.getDisplay().getActiveShell());
			box.setMessage(bug.getMessage());
			box.open();
		}

		
	}

	@Override
	public void setFocus() {

	}
	
	public TraceView getTraceView() {
		if(traceView == null){
			traceView = MicroBatViews.getTraceView();
		}
		
		return traceView;
	}
	
	public void setTraceView(TraceView traceView) {
		this.traceView = traceView;
	}
	
	@SuppressWarnings("unchecked")
	class RWVariableContentProvider implements ITreeContentProvider{
		/**
		 * rw is true means read, and rw is false means write.
		 */
		boolean rw;
		
		public RWVariableContentProvider(boolean rw) {
			this.rw = rw;
		}
		
		@Override
		public void dispose() {
			
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof ArrayList){
				ArrayList<VarValue> elements = (ArrayList<VarValue>)inputElement;
				return elements.toArray(new VarValue[0]);
			}
			
			return null;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if(parentElement instanceof ReferenceValue){
				ReferenceValue parent = (ReferenceValue)parentElement;
				
				List<VarValue> children = ((ReferenceValue)parentElement).getChildren();
				if(children == null){
					String varID = parent.getVarID();
					varID = Variable.truncateSimpleID(varID);
//					varID = varID.substring(0, varID.indexOf(":"));
					
					VarValue vv = null;
					/** read */
					if(rw){
						vv = currentNode.getProgramState().findVarValue(varID);
					}
					/** write */
					else{
						if(currentNode.getStepOverNext() != null){
							vv = currentNode.getStepOverNext().getProgramState().findVarValue(varID);
						}
						
						if(currentNode.getStepInNext() != null){
							vv = currentNode.getStepInNext().getProgramState().findVarValue(varID);
						}
					}
					
					if(vv != null){
						List<VarValue> retrievedChildren = vv.getAllDescedentChildren();
						MicroBatUtil.assignWrittenIdentifier(retrievedChildren, currentNode);
						
						parent.setChildren(vv.getChildren());
						return vv.getChildren().toArray(new VarValue[0]);
					}
				}
				else{
					return parent.getChildren().toArray(new VarValue[0]);
				}
			}
			
			return null;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			Object[] children = getChildren(element);
			if(children==null || children.length==0){
				return false;
			}
			else{
				return true;
			}
		}
		
	}
	
	class ConsequenceContentProvider implements ITreeContentProvider{

		@Override
		public void dispose() {
			
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof List){
				List<GraphDiff> diffs = (List<GraphDiff>) inputElement;
				Object[] elements = diffs.toArray(new GraphDiff[0]);
				
				return elements;
			}
			
			return null;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return null;
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
	
	class ConsequenceLabelProvider implements ITableLabelProvider{
		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		
		public String getColumnText(Object ele, int columnIndex) {
			
			if(ele instanceof GraphDiff){
				GraphDiff diff = (GraphDiff)ele;
				
				VarValue before = (VarValue)diff.getNodeBefore();
				VarValue after = (VarValue)diff.getNodeAfter();
				
				VarValue element = (before != null) ? before : after;
				if(element == null){
					System.err.println("both before and empty of a diff are empty");
					return null;
				}
				else{
					if(element instanceof ArrayValue){
						ArrayValue value = (ArrayValue)element;
						switch(columnIndex){
						case 0: return "array[" + value.getComponentType() + "]";
						case 1: return value.getVariablePath();
						case 2: 
							if(after != null){
								return "id = " + String.valueOf(((ArrayValue)after).getReferenceID());
							}
							else{
								return "NULL";
							}
						case 3: 
							if(before != null){
								return "id = " + String.valueOf(((ArrayValue)before).getReferenceID());
							}
							else{
								return "NULL";
							}
						}
					}
					else if(element instanceof ReferenceValue){
						ReferenceValue value = (ReferenceValue)element;
						switch(columnIndex){
						case 0: 
							if(value.getClassType() != null){
								return value.getConciseTypeName();						
							}
							else{
								return "array";
							}
						case 1: 
							return value.getVariablePath();
						case 2: 
							if(after != null){
								return "id = " + String.valueOf(((ReferenceValue)after).getReferenceID());
							}
							else{
								return "NULL";
							}
						case 3: 
							if(before != null){
								return "id = " + String.valueOf(((ReferenceValue)before).getReferenceID());
							}
							else{
								return "NULL";
							}
						}
					}
					else if(element instanceof PrimitiveValue){
						PrimitiveValue value = (PrimitiveValue)element;
						switch(columnIndex){
						case 0: return value.getPrimitiveType();
						case 1: return value.getVariablePath();
						case 2: 
							if(after != null){
								return ((PrimitiveValue)after).getManifestationValue();
							}
							else{
								return "NULL";
							}
						case 3: 
							if(before != null){
								return ((PrimitiveValue)before).getManifestationValue();
							}
							else{
								return "NULL";
							}
						}
					}
				}
			}
			
			return null;
		}
	}
	
	class VariableContentProvider implements ITreeContentProvider{
		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof BreakPointValue){
				BreakPointValue value = (BreakPointValue)inputElement;
				return value.getChildren().toArray(new VarValue[0]);
			}
			else if(inputElement instanceof ReferenceValue){
				ReferenceValue value = (ReferenceValue)inputElement;
				VarValue[] list = value.getChildren().toArray(new VarValue[0]);
				if(list.length != 0){
					return list;
				}
				else{
					return null;
				}
			}
			
			return null;
		}

		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if(element instanceof ReferenceValue){
				ReferenceValue rValue = (ReferenceValue)element;
				List<VarValue> children = rValue.getChildren();
				return children != null && !children.isEmpty();
			}
			return false;
		}
		
	}

	class VariableLabelProvider implements ITableLabelProvider{

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex) {
			if(element instanceof VarValue){
				VarValue varValue = (VarValue)element;
				switch(columnIndex){
				case 0: 
					String type = varValue.getType();
					if(type.contains(".")){
						type = type.substring(type.lastIndexOf(".")+1, type.length());
					}
					return type;
				case 1: 
					String name = varValue.getVarName();
					if(varValue.getVariable() instanceof VirtualVar){
						String methodName = name.substring(name.indexOf(":")+1);
						name = "return from " + methodName + "()";
					}
					return name;
				case 2: 
					String value = varValue.getManifestationValue();
					String aliasVarID = varValue.getAliasVarID();
					if(aliasVarID != null){
						return value + (" aliasID:" + aliasVarID);
					}
					return value;
				case 3:
					return String.format("%.2f", varValue.getProbability());
				}
			}
			
//			if(element instanceof ReferenceValue){
//				ReferenceValue value = (ReferenceValue)element;
//				switch(columnIndex){
//				case 0: 
//					if(value.getClassType() != null){
//						return value.getConciseTypeName();						
//					}
//				case 1: 
//					String name = value.getVarName();
//					return name;
//				case 2: return value.getMessageValue();
//				}
//			}
//			else if(element instanceof ArrayValue){
//				ArrayValue value = (ArrayValue)element;
//				switch(columnIndex){
//				case 0: return "array[" + value.getComponentType() + "]";
//				case 1: 
//					String name = value.getVarName();
//					if(value.isRoot() && value.isField()){
//						name = "this." + name;
//					}
//					return name;
//				case 2: return value.getMessageValue();
//				}
//			}
//			else if(element instanceof PrimitiveValue){
//				PrimitiveValue value = (PrimitiveValue)element;
//				switch(columnIndex){
//				case 0: return value.getPrimitiveType();
//				case 1: 
//					String name = value.getVarName();
//					if(value.isRoot() && value.isField()){
//						name = "this." + name;
//					}
//					return name;
//				case 2: return value.getStrVal() + " (id=" + value.getVarID() + ")";
//				}
//			}
//			else if(element instanceof VirtualValue){
//				
//			}
			return null;
		}
		
	}
	
	class VariableCheckStateProvider implements ICheckStateProvider{

		@Override
		public boolean isChecked(Object element) {
			
			VarValue value = null;
			if(element instanceof VarValue){
				value = (VarValue)element;
			}
			else if(element instanceof GraphDiff){
				value = (VarValue) ((GraphDiff)element).getChangedNode();
			}
			
			if(currentNode != null){
//				BreakPoint point = node.getBreakPoint();
//				InterestedVariable iVar = new InterestedVariable(point.getDeclaringCompilationUnitName(), 
//						point.getLineNo(), value);
				if(Settings.interestedVariables.contains(value)){
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean isGrayed(Object element) {
			return false;
		}
		
	}
	
	public StepRecommender getRecommender(){
		return this.recommender;
	}
	
//	private List<ExecValue> filterVariable(BreakPointValue value, List<Variable> criteria){
//	ArrayList<ExecValue> list = new ArrayList<>();
//	for(ExecValue ev: value.getChildren()){
//		if(variableListContain(criteria, ev.getVarId())){
//			list.add(ev);
//		}
//	}
//	return list;
//}

//private boolean variableListContain(List<Variable> variables,
//		String varId) {
//	for(Variable var: variables){
//		if(var.getId().equals(varId)){
//			return true;
//		}
//	}
//	return false;
//}
	
	/**
	 * Update the feedback view based on the given feedback. Created by David
	 * @param feedback Feedback that the update referencing to.
	 */
	public void updateFeedbackView(UserFeedback feedback) {
		
		// Un-check all the feedback first
		this.yesButton.setSelection(false);
		this.noButton.setSelection(false);
		this.unclearButton.setSelection(false);
		this.wrongPathButton.setSelection(false);
		this.uncheckAllVar();
		
		switch(feedback.getFeedbackType()) {
		case UserFeedback.CORRECT:
			this.yesButton.setSelection(true);
			return;
		case UserFeedback.UNCLEAR:
			this.unclearButton.setSelection(true);
			return;
		case UserFeedback.WRONG_PATH:
			this.wrongPathButton.setSelection(true);
			return;
		case UserFeedback.WRONG_VARIABLE_VALUE:
			this.noButton.setSelection(true);
			
			// Check the wrong variable element in the tree
			ChosenVariableOption option = feedback.getOption();
			VarValue wrongVar = option.getReadVar();
			this.readVariableTreeViewer.setChecked(wrongVar, true);
			return;
			
		default:
			break;
		}
	}
	
	public UserFeedback getFeedback() {
		return this.feedback;
	}
	
	/**
	 * Un-check all the variable in the read variable tree and write variable tree
	 */
	private void uncheckAllVar() {
		for (Object element : this.readVariableTreeViewer.getCheckedElements()) {
			this.readVariableTreeViewer.setChecked(element, false);
		}
		
		for (Object element : this.writtenVariableTreeViewer.getCheckedElements()) {
			this.writtenVariableTreeViewer.setChecked(element, false);
		}
	}
	
	private List<VarValue> getSelectedVars() {
		List<VarValue> vars = new ArrayList<>();
		vars.addAll(this.getSelectedReadVars());
		vars.addAll(this.getSelectedWriteVars());
		return vars;
	}

	private List<VarValue> getSelectedReadVars() {
		List<VarValue> vars = new ArrayList<>();
		
		Object[] readObjList = this.readVariableTreeViewer.getCheckedElements();
		for (Object object : readObjList) {
			if (object instanceof VarValue) {
				VarValue input = (VarValue) object;
				vars.add(input);
			}
		}
		
		return vars;
	}
	
	private List<VarValue> getSelectedWriteVars() {
		List<VarValue> vars = new ArrayList<>();
		Object[] writeObjList = this.writtenVariableTreeViewer.getCheckedElements();
		for (Object object : writeObjList) {
			if (object instanceof VarValue) {
				VarValue output = (VarValue) object;
				vars.add(output);
			}
		}
		return vars;
	}
	
	private class AddOutputsListener implements MouseListener {

		@Override
		public void mouseDoubleClick(MouseEvent e) {}

		@Override
		public void mouseDown(MouseEvent e) {
			List<VarValue> outputs = getSelectedVars();
			DebugInfo.addOutputs(outputs);
		}

		@Override
		public void mouseUp(MouseEvent e) {}
	}
	
	private class AddInputsListener implements MouseListener {

		@Override
		public void mouseDoubleClick(MouseEvent e) {}

		@Override
		public void mouseDown(MouseEvent e) {
			List<VarValue> inputs = getSelectedVars();
			DebugInfo.addInputs(inputs);
		}

		@Override
		public void mouseUp(MouseEvent e) {}
	}
	
	private class ClearVarsListener implements MouseListener {

		@Override
		public void mouseDoubleClick(MouseEvent e) {}

		@Override
		public void mouseDown(MouseEvent e) {
			DebugInfo.clearData();
		}

		@Override
		public void mouseUp(MouseEvent e) {	}
		
	}
	
	private class ShowIOListener implements MouseListener {

		@Override
		public void mouseDoubleClick(MouseEvent e) {}

		@Override
		public void mouseDown(MouseEvent e) {

			DebugInfo.printInputs();
			DebugInfo.printOutputs();
		}

		@Override
		public void mouseUp(MouseEvent e) {	}
		
	}
	
	private class RootCauseFoundListener implements MouseListener {

		@Override
		public void mouseDoubleClick(MouseEvent e) {}

		@Override
		public void mouseDown(MouseEvent e) {
			DebugInfo.setRootCauseFound(true);
		}

		@Override
		public void mouseUp(MouseEvent e) {}
		
	}
	
}
