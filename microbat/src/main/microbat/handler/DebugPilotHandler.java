package microbat.handler;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import debugpilot.userlogger.UserBehaviorLogger;
import debugpilot.userlogger.UserBehaviorType;
import microbat.debugpilot.DebugPilot;
import microbat.debugpilot.DebugPilotInfo;
import microbat.debugpilot.NodeFeedbacksPair;
import microbat.debugpilot.fsc.AbstractDebugPilotState;
import microbat.debugpilot.fsc.DebugPilotFiniteStateMachine;
import microbat.debugpilot.pathfinding.FeedbackPath;
import microbat.debugpilot.propagation.spp.StepExplaination;
import microbat.debugpilot.settings.DebugPilotSettings;
import microbat.handler.callbacks.HandlerCallback;
import microbat.handler.callbacks.HandlerCallbackManager;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.ChosenVariableOption;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;
import microbat.views.DialogUtil;
import microbat.views.MicroBatViews;
import microbat.views.PathView;
import microbat.views.TraceView;

public class DebugPilotHandler extends AbstractHandler {

	public static final String JOB_FAMALY_NAME = "debugpilot";
	
	protected static final String DIALOG_INFO_TITLE = "DebugPilot Information";
	protected static final String DIALOG_ERROR_TITLE = "DebugPilot Error";
	
	protected TraceView buggyView;
	
	protected PathView pathView;
	
	protected Trace trace;
	
	protected DebugPilotInfo info = DebugPilotInfo.getInstance();
	
	protected boolean isRunningProcess = false;
	
	public DebugPilotHandler() {
		HandlerCallbackManager.getInstance().registerDebugPilotTermianteCallback(new HandlerCallback() {
			@Override
			public void callBack() {
				isRunningProcess = false;
			}
		});
	}
	
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job job = new Job(DebugPilotHandler.JOB_FAMALY_NAME) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				setup();
				execute();
				return Status.OK_STATUS;
			}
			
			@Override
			public boolean belongsTo(Object family) {
				return this.getName().equals(family);
			}
		};
		job.schedule();
		return null;
	}
	
	protected void execute() {
		UserBehaviorLogger.logEvent(UserBehaviorType.START_DEBUGPILOT);
		if (this.isRunningProcess) {
			DialogUtil.popErrorDialog("DebugPilot is currently running a process. Please stop the original process before you start a new one", DebugPilotHandler.DIALOG_ERROR_TITLE);
			return;
		}
		
		
		if (!this.isDebugPilotReady()) {
			return;
		}
		
		this.isRunningProcess = true;
		this.pathView.updateFeedbackPath(new FeedbackPath());
		
		Log.printMsg(getClass(), "");
		Log.printMsg(getClass(), "---------------------------");
		Log.printMsg(getClass(), "\t Start Debug Pilot");
		Log.printMsg(getClass(), "");
		
		DebugPilotSettings settings = new DebugPilotSettings();
		settings.setPropagatorSettings(PreferenceParser.getPreferencePropagatorSettings());
		settings.setPathFinderSettings(PreferenceParser.getPreferencePathFinderSettings());
		settings.setRootCauseLocatorSettings(PreferenceParser.getPrefereRootCauseLocatorSettings());
		
		TraceNode outputNode = DebugPilotInfo.getInstance().getOutputNode();
		settings.setOutputNode(outputNode);
		
		VarValue wrongVar;
		if (DebugPilotInfo.getInstance().isOutputNodeWrongBranch()) {
			wrongVar = outputNode.getControlDominator().getConditionResult();
		} else {			
			wrongVar = DebugPilotInfo.getInstance().getOutputs().get(0);
		}
		
		TraceNode dataDominator = trace.findDataDependency(outputNode, wrongVar);
		if (dataDominator == null) {
			DialogUtil.popErrorDialog("Given output variable does not have data dominator", DIALOG_ERROR_TITLE);
			return;
		}
		
		
		Set<VarValue> wrongVarSet = new HashSet<>();
		wrongVarSet.add(wrongVar);
		settings.setWrongVars(wrongVarSet);

		settings.setTrace(this.buggyView.getTrace());
		
		NodeFeedbacksPair initFeedback;
		if (DebugPilotInfo.getInstance().isOutputNodeWrongBranch()) {
			UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_PATH);
			initFeedback = new NodeFeedbacksPair(outputNode, feedback);
		} else {
			UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_VARIABLE_VALUE);
			feedback.setOption(new ChosenVariableOption(wrongVar, null));
			initFeedback = new NodeFeedbacksPair(outputNode, feedback);			
		}
		
		// Initialize DebugPilot
		final DebugPilot debugPilot = new DebugPilot(settings);
		final DebugPilotFiniteStateMachine fsm = new DebugPilotFiniteStateMachine(debugPilot);
		fsm.setState(new PropagationState(fsm, initFeedback));
		while (!fsm.isEnd()) {
			fsm.handleFeedback();
		}
		
	}
	
	protected void setup() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				buggyView = MicroBatViews.getTraceView();
				pathView = MicroBatViews.getPathView();
				pathView.setBuggyView(buggyView);
				trace = buggyView.getTrace();
			}
		});
	}
	
	protected void updatePathView(final FeedbackPath path) {
		this.pathView.updateFeedbackPath(path);
	}
	
	protected void updatePathView(final FeedbackPath path, final boolean focusOnFirstNode) {
		this.updatePathView(path);
		if (path.getLength() > 1 && focusOnFirstNode) {
			final TraceNode node = path.get(0).getNode();
			this.pathView.focusOnNode(node);
		}
	}
	
	protected void updateView() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				buggyView.updateData();			
			}
		});
	}
	
	protected NodeFeedbacksPair waitForFeedback(FeedbackPath path) {
		final DebugPilotInfo info = DebugPilotInfo.getInstance();
		boolean isValidFeedback = false;
		NodeFeedbacksPair userPair = null;
		while (!isValidFeedback) {
//			info.waifForFeedbacksPairOrStop();
			this.waitForFeedback();
			userPair = info.getNodeFeedbackPair();
			info.clearNodeFeedbackPairs();
			
			if (userPair == null) {
				// Null is also a valid feedback indicating stop of progress
				break;
			}
			isValidFeedback = this.checkIsValidFeedback(userPair, path);
		}
		return userPair;
	}
	
	protected void waitForFeedback() {
		final DebugPilotInfo info = DebugPilotInfo.getInstance();
		while (!info.isFeedbackUpdate() && this.isRunningProcess) {
			try {
				Thread.sleep(200);
			} catch (Exception e) {}
		}
		info.setFeedbackUpdate(false);
	}
	
	protected boolean checkIsValidFeedback(final NodeFeedbacksPair pair, final FeedbackPath path) {
		// Feedback is invalid when there are conflicting feedback
		Objects.requireNonNull(pair, Log.genMsg(getClass(), "Given feedbacksPair cannot not null"));
		if (pair.getFeedbacks().size() == 1) {
			return true;
		}
		
//		String feedbackType = null;
//		for (UserFeedback feedback : pair.getFeedbacks()) {
//			if (feedbackType == null) {
//				feedbackType = feedback.getFeedbackType();
//			} else if (!feedbackType.equals(feedback.getFeedbackType())) {
//				DialogUtil.popErrorDialog("You give conflicting feedback on node " + pair.getNode().getOrder(), DebugPilotHandler.DIALOG_ERROR_TITLE);
//				return false;
//			}
//		}
		
		if (!path.contains(pair.getNode())) {
			DialogUtil.popErrorDialog("Please give the feedback on node that inside the path. Node: " + pair.getNode().getOrder() + " is not lie in path.", DebugPilotHandler.DIALOG_ERROR_TITLE);
			return false;
		}
		
		return true;
	}

	/**
		 * DebugPilot is ready when: <br>
		 * <br>
		 * 1. Trace is generated <br>
		 * 2. DebugPilot info is ready <br>
		 * <br>
		 * Info is ready when user indicate either wrong variable or output node is in wrong branch <br>
		 * <br>
		 * If output node is in wrong branch, then the wrong variable is be empty<br>
		 * otherwise, there must be at least one wrong variable<br>
		 * 
	 * @return True if debug pilot is ready
	 */
	protected boolean isDebugPilotReady() {
		final Trace buggyTrace = this.buggyView.getTrace();
		if (buggyTrace == null) {
			DialogUtil.popErrorDialog("Trace is not generated.", DebugPilotHandler.DIALOG_ERROR_TITLE);
			return false;
		}
		
		TraceNode outputNode = this.info.getOutputNode();
		if (outputNode == null) {
			DialogUtil.popErrorDialog("Output node is empty. You can select the output node by selecting it in the trace view.", DebugPilotHandler.DIALOG_ERROR_TITLE);
			return false;
		}
		
		if (info.isOutputNodeWrongBranch()) {
			if (!this.info.getOutputs().isEmpty()) {
				DialogUtil.popErrorDialog("When the output node should not be executed, there should not be wrong variables. Please double click the variables to erase them", DebugPilotHandler.DIALOG_ERROR_TITLE);
				return false;
			}
		} else {
			if (this.info.getOutputs().isEmpty()) {
				DialogUtil.popErrorDialog("Please select one wrong variable to start DebugPilot. If the output node should not be executed, then please click the wrong branch button", DebugPilotHandler.DIALOG_ERROR_TITLE);
				return false;
			}
		}
		
		return true;
	}
	
	protected boolean canLeadToNextNode(final NodeFeedbacksPair pair) {
		for (UserFeedback feedback : pair.getFeedbacks()) {
			TraceNode nextNode = TraceUtil.findNextNode(pair.getNode(), feedback, this.buggyView.getTrace());
			if (nextNode == null) {
				return false;
			}
		}
		return true;
	}
	
	protected class PropagationState extends AbstractDebugPilotState {

		protected Stack<NodeFeedbacksPair> userFeedbackRecords;
		
		public PropagationState(DebugPilotFiniteStateMachine stateMachine) {
			super(stateMachine);
			this.userFeedbackRecords = new Stack<NodeFeedbacksPair>();
		}
		
		public PropagationState(DebugPilotFiniteStateMachine stateMachine, final NodeFeedbacksPair initFeedbacksPair) {
			this(stateMachine);
			this.userFeedbackRecords.add(initFeedbacksPair);
		}
		
		public PropagationState(DebugPilotFiniteStateMachine stateMachine, Stack<NodeFeedbacksPair> userFeedbackRecords) {
			super(stateMachine);
			this.userFeedbackRecords = userFeedbackRecords;
		}

		@Override
		public void handleFeedback() {
			final DebugPilot debugPilot = this.stateMachine.getDebugPilot();
			debugPilot.updateFeedbacks(this.userFeedbackRecords);  
			debugPilot.multiSlicing();
			debugPilot.propagate();
			
			TraceNode rootCause = debugPilot.locateRootCause();
			FeedbackPath feedbackPath =  debugPilot.constructPath(rootCause);
			updatePathView(feedbackPath);

			NodeFeedbacksPair userFeedbacksPair = waitForFeedback(feedbackPath);
			if (userFeedbacksPair == null) {
				// User click the stop button
				this.stateMachine.setState(new EndState(this.stateMachine));
			} else if (userFeedbacksPair.getFeedbackType().equals(UserFeedback.ROOTCAUSE)) {
				// User find the root cause
				feedbackPath.replacePair(userFeedbacksPair);
				feedbackPath.removePathAfterNode(userFeedbacksPair.getNode());
				feedbackPath.forEach(pair -> pair.getNode().confirmed = true);
				updatePathView(feedbackPath);
				DialogUtil.popInformationDialog("Root Cause is found. Debugging process end.", DIALOG_INFO_TITLE);
				this.stateMachine.setState(new EndState(this.stateMachine));
			} else if (userFeedbacksPair.getFeedbackType().equals(UserFeedback.CORRECT)) {
				// Omission bug detected
				final int index = feedbackPath.getIndexOf(userFeedbacksPair.getNode());
				NodeFeedbacksPair prevFeedbacksPair = feedbackPath.get(index-1);
				feedbackPath.replacePair(userFeedbacksPair);
				if (prevFeedbacksPair.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
					this.stateMachine.setState(new ControlOmissionState(this.stateMachine, feedbackPath, userFeedbacksPair.getNode(), prevFeedbacksPair.getNode(), prevFeedbacksPair.getFirstFeedback(), buggyView.getTrace()));
				} else if (prevFeedbacksPair.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
					this.stateMachine.setState(new DataOmissionState(this.stateMachine, feedbackPath, userFeedbacksPair.getNode(), prevFeedbacksPair.getNode(), prevFeedbacksPair.getFirstWrongFeedback(), buggyView.getTrace()));
				} else {
					throw new RuntimeException("Unhandled feedback");
				}
			} else if (!canLeadToNextNode(userFeedbacksPair)) {
				final int index = feedbackPath.getIndexOf(userFeedbacksPair.getNode());
				NodeFeedbacksPair prevFeedbacksPair = feedbackPath.get(index-1);
				if (prevFeedbacksPair.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
					this.stateMachine.setState(new ControlOmissionState(this.stateMachine, feedbackPath, userFeedbacksPair.getNode(), prevFeedbacksPair.getNode(), prevFeedbacksPair.getFirstFeedback(), buggyView.getTrace()));
				} else if (prevFeedbacksPair.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
					this.stateMachine.setState(new DataOmissionState(this.stateMachine, feedbackPath, userFeedbacksPair.getNode(), prevFeedbacksPair.getNode(), prevFeedbacksPair.getFirstWrongFeedback(), buggyView.getTrace()));
				} else {
					throw new RuntimeException("Unhandled feedback");
				}
			} else {
				Stack<NodeFeedbacksPair> newFeedbackRecords = this.constructFeedbackRecords(userFeedbacksPair, feedbackPath);
				this.stateMachine.setState(new PropagationState(this.stateMachine, newFeedbackRecords));
			}
		}
	}
	
	protected class EndState extends AbstractDebugPilotState {

		public EndState(DebugPilotFiniteStateMachine stateMachine) {
			super(stateMachine);
		}

		@Override
		public void handleFeedback() {
			this.stateMachine.setEnd(true);
			isRunningProcess = false;
			
		}
	}
	
	protected abstract class OmissionState extends AbstractDebugPilotState {

		protected final FeedbackPath initFeedbackPath;
		protected final TraceNode startNode;
		protected final TraceNode endNode;
		protected final UserFeedback prevFeedback;
		protected final Trace trace;
		
		public OmissionState(DebugPilotFiniteStateMachine stateMachine, final FeedbackPath path, final TraceNode startNode, final TraceNode endNode, final UserFeedback prevUserFeedback, final Trace trace) {
			super(stateMachine);
			this.initFeedbackPath = path;
			this.startNode = startNode;
			this.endNode = endNode;
			this.prevFeedback = prevUserFeedback;
			this.trace = trace;
		}
		
		@Override
		public void handleFeedback() {
			initFeedbackPath.removePathAfterNode(this.startNode);
		}
		
		protected boolean handleFeedback(final NodeFeedbacksPair feedbacksPair) {
			if (feedbacksPair == null) {
				throw new RuntimeException("Got null feedback");
			}
			
			if (feedbacksPair.getFeedbackType().equals(UserFeedback.ROOTCAUSE)) {
				DialogUtil.popInformationDialog("Root Cause is found. Debugging process end.", DIALOG_INFO_TITLE);
				this.stateMachine.setState(new EndState(this.stateMachine));
				return true;
			}
			
			final TraceNode node = feedbacksPair.getNode();
			if (this.initFeedbackPath.contains(node)) {
				final String feedbackType = feedbacksPair.getFeedbackType();
				
				if (feedbackType.equals(UserFeedback.WRONG_PATH) || feedbackType.equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
					Stack<NodeFeedbacksPair> newFeedbackRecords = this.constructFeedbackRecords(feedbacksPair, this.initFeedbackPath);
					this.stateMachine.setState(new PropagationState(this.stateMachine, newFeedbackRecords));
					return true;
				}
				
				if (feedbackType.equals(UserFeedback.CORRECT)) {
					final int index = this.initFeedbackPath.getIndexOf(node);
					NodeFeedbacksPair prevFeedbacksPair = this.initFeedbackPath.get(index-1);
					if (prevFeedbacksPair.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
						this.stateMachine.setState(new ControlOmissionState(stateMachine, initFeedbackPath, node, prevFeedbacksPair.getNode(), prevFeedbacksPair.getFirstFeedback(), trace));
					} else if (prevFeedbacksPair.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
						this.stateMachine.setState(new DataOmissionState(stateMachine, initFeedbackPath, node, prevFeedbacksPair.getNode(), prevFeedbacksPair.getFirstWrongFeedback(), trace));
					} else {
						throw new RuntimeException("Unhandled feedback");
					}
					return true;
				}
			}
			
			return false;
		}
		
		protected List<TraceNode> sortNodeList(final List<TraceNode> list) {
			return list.stream().sorted(
					new Comparator<TraceNode>() {
						@Override
						public int compare(TraceNode node1, TraceNode node2) {
							return node1.getOrder() - node2.getOrder();
						}
					}
				).toList();
		}
	}
	
	protected class ControlOmissionState extends OmissionState {

		public ControlOmissionState(DebugPilotFiniteStateMachine stateMachine, FeedbackPath path, TraceNode startNode,
				TraceNode endNode, UserFeedback prevUserFeedback, Trace trace) {
			super(stateMachine, path, startNode, endNode, prevUserFeedback, trace);
		}

		@Override
		public void handleFeedback() {
			super.handleFeedback();
			
			// Deep copy the initial feedback path in case user redo the process
			final FeedbackPath feedbackPath = new FeedbackPath(this.initFeedbackPath);
			startNode.reason = StepExplaination.MISS_BRANCH;
			endNode.reason = StepExplaination.MISS_BRANCH;
			startNode.confirmed = true;
			endNode.confirmed = true;
			for (NodeFeedbacksPair pair : feedbackPath) {
				if (pair.getNode().getOrder()>endNode.getOrder()) {
					pair.getNode().confirmed = true;
				}
			}
			updatePathView(feedbackPath);
			
			final int startOrder = startNode.getOrder();
			final int endOrder = endNode.getOrder();
			List<TraceNode> candidateNodes = trace.getExecutionList().stream().filter(node -> node.getOrder() > startOrder && node.getOrder() < endOrder).toList();
			
			if (candidateNodes.isEmpty()) {
				String message = this.genOmissionMessage(startNode, endNode);
				DialogUtil.popInformationDialog(message, DebugPilotHandler.DIALOG_INFO_TITLE);
				NodeFeedbacksPair userFeedbacksPair = waitForFeedback(feedbackPath);
				super.handleFeedback(userFeedbacksPair);
			} else {
				List<TraceNode> sortedList = this.sortNodeList(candidateNodes);
				int left = 0;
				int right = sortedList.size();
				while (left <= right) {
					int mid = left + (right - left) / 2;
					final TraceNode node = sortedList.get(mid);
					node.reason = StepExplaination.BINARY_SEARCH;
					feedbackPath.addPairByOrder(node, new UserFeedback(UserFeedback.ROOTCAUSE));
					updatePathView(feedbackPath);
					NodeFeedbacksPair userFeedbacksPair = waitForFeedback(feedbackPath);
					if (super.handleFeedback(userFeedbacksPair)) {
						return;
					}
					
					if (userFeedbacksPair.getFeedbackType().equals(UserFeedback.CORRECT)) {
						left = mid+1;
					} else {
						right = mid-1;
					}
					node.reason = StepExplaination.USRE_CONFIRMED;
					node.confirmed = true;
					feedbackPath.replacePair(userFeedbacksPair);
					updatePathView(feedbackPath);
				}
				DialogUtil.popInformationDialog("No more node can be proposed. You may restart the process by giving correct feedback on node: " + startNode.getOrder(), DebugPilotHandler.DIALOG_INFO_TITLE);
				NodeFeedbacksPair userFeedbacksPair = waitForFeedback(feedbackPath);
				super.handleFeedback(userFeedbacksPair);
			}
		}
		
		protected String genOmissionMessage(final TraceNode startNode, final TraceNode endNode) {
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append("Conflicting feedback detected:\n\n");
			strBuilder.append("TraceNode: " + startNode.getOrder() + " with feedback: Correct\n");
			strBuilder.append("TraceNode: " + endNode.getOrder() + " with feedback: " + this.prevFeedback + "\n\n");
			strBuilder.append("It can be omission bug or you give a wrong feedback. \n");
			strBuilder.append("DebugPilot will now scan the step to narrow down the missing scpe, or you may review the feedback you give previously.");
			return strBuilder.toString();
		}
		
	}
	
	protected class DataOmissionState extends OmissionState {

		public DataOmissionState(DebugPilotFiniteStateMachine stateMachine, FeedbackPath path, TraceNode startNode,
				TraceNode endNode, UserFeedback prevUserFeedback, Trace trace) {
			super(stateMachine, path, startNode, endNode, prevUserFeedback, trace);
		}

		@Override
		public void handleFeedback() {
			super.handleFeedback();
			
			// Deep copy the initial feedback path in case user redo the process
			final FeedbackPath feedbackPath = new FeedbackPath(this.initFeedbackPath);
			startNode.reason = StepExplaination.MISS_BRANCH;
			endNode.reason = StepExplaination.MISS_BRANCH;
			startNode.confirmed = true;
			endNode.confirmed = true;
			for (NodeFeedbacksPair pair : feedbackPath) {
				if (pair.getNode().getOrder()>endNode.getOrder()) {
					pair.getNode().confirmed = true;
				}
			}
			updatePathView(feedbackPath);
			
			final int startOrder = startNode.getOrder();
			final int endOrder = endNode.getOrder();
			List<TraceNode> candidateNodes = trace.getExecutionList().stream().filter(node -> node.getOrder() > startOrder && node.getOrder() < endOrder).toList();
			
			if (candidateNodes.isEmpty()) {
				String message = this.genOmissionMessage(startNode.getOrder(), endNode.getOrder());
				DialogUtil.popInformationDialog(message, DebugPilotHandler.DIALOG_INFO_TITLE);
				NodeFeedbacksPair userFeedbacksPair = waitForFeedback(feedbackPath);
				super.handleFeedback(userFeedbacksPair);
			} else {
				List<TraceNode> sortedCandidateList = this.sortNodeList(candidateNodes);
				List<TraceNode> relatedCandidateList = sortedCandidateList.stream().filter(node -> node.isReadVariablesContains(this.prevFeedback.getOption().getReadVar().getVarID())).toList();
				int beginOrder = sortedCandidateList.get(0).getOrder();
				int lastOrder = sortedCandidateList.get(sortedCandidateList.size()-1).getOrder();
				
				for (TraceNode node : relatedCandidateList) {
					node.reason = StepExplaination.RELATED;
					feedbackPath.addPairByOrder(node, new UserFeedback(UserFeedback.UNCLEAR));
					updatePathView(feedbackPath);
					System.out.println("Please give a feedback on node: " + node.getOrder());
					NodeFeedbacksPair pair = waitForFeedback(feedbackPath);
					if (super.handleFeedback(pair)) {
						return;
					}
					pair.getNode().confirmed = true;
					feedbackPath.replacePair(pair);
					if (pair.getFeedbackType().equals(UserFeedback.CORRECT)) {
						beginOrder = pair.getNode().getOrder();
					} else {
						lastOrder = pair.getNode().getOrder();
						break;
					}
				}
				
				DialogUtil.popInformationDialog(this.genOmissionMessage(beginOrder, lastOrder), DebugPilotHandler.DIALOG_INFO_TITLE);
				NodeFeedbacksPair userFeedbacksPair = waitForFeedback(feedbackPath);
				super.handleFeedback(userFeedbacksPair);
			}
		}
		
		protected String genOmissionMessage(final int startNodeOrder, final int endNodeOrder) {
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append("Conflicting feedback detected:\n\n");
			strBuilder.append("TraceNode: " + startNode.getOrder() + " with feedback: Correct\n");
			strBuilder.append("TraceNode: " + endNode.getOrder() + " with feedback: " + this.prevFeedback + "\n\n");
			strBuilder.append("It can be omission bug or you give a wrong feedback. \n");
			strBuilder.append("DebugPilot will now scan the step to narrow down the missing scpe, or you may review the feedback you give previously.");
			return strBuilder.toString();
		}
		
	}
	
}
