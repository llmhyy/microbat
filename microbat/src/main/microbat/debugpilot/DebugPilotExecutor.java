package microbat.debugpilot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.eclipse.swt.widgets.Display;

import microbat.debugpilot.pathfinding.FeedbackPath;
import microbat.debugpilot.settings.DebugPilotSettings;
import microbat.debugpilot.userfeedback.DPUserFeedback;
import microbat.debugpilot.userfeedback.DPUserFeedbackType;
import microbat.handler.PreferenceParser;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.util.TraceUtil;
import microbat.views.DebugPilotFeedbackView;
import microbat.views.DialogUtil;
import microbat.views.MicroBatViews;
import microbat.views.PathView;
import microbat.views.TraceView;

public class DebugPilotExecutor {
	
	protected PathView pathView = null;
	
	protected DebugPilotFeedbackView debugPilotFeedbackView = null;
	
	protected TraceView traceView = null;
	
	public DebugPilotExecutor() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				pathView = MicroBatViews.getPathView();
				debugPilotFeedbackView = MicroBatViews.getDebugPilotFeedbackView();
				traceView = MicroBatViews.getTraceView();
			}
		});
		
	}
	
	public void execute(final DPUserFeedback userFeedback) {
		List<DPUserFeedback> avaiableFeedbacks = this.collectAvailableFeedbacks(userFeedback);
		if (avaiableFeedbacks == null) {
			return;
		}
		
		if (userFeedback.getType() == DPUserFeedbackType.CORRECT) {
			FeedbackPath path = new FeedbackPath(avaiableFeedbacks);
			path.getFeedbacks().stream().forEach(feedback -> feedback.getNode().confirmed = true);
			this.updatePathView(path);
			if (avaiableFeedbacks.size() >= 1) {
				
				int idx = avaiableFeedbacks.indexOf(userFeedback);
				DPUserFeedback prevFeedback = avaiableFeedbacks.get(idx-1);
				
				String message = this.genOmissionMessage(userFeedback.getNode().getOrder(), prevFeedback.getNode().getOrder(), prevFeedback);
				DialogUtil.popInformationDialog(message, "DebugPilot Info");
			}
			return;
		}
		
		// Get debug pilot setting from preference
		DebugPilotSettings settings = new DebugPilotSettings();
		settings.setPropagatorSettings(PreferenceParser.getPreferencePropagatorSettings());
		settings.setPathFinderSettings(PreferenceParser.getPreferencePathFinderSettings());
		settings.setRootCauseLocatorSettings(PreferenceParser.getPrefereRootCauseLocatorSettings());
		settings.setWrongVars(new HashSet<>());
		settings.setCorrectVars(new HashSet<>());
		// Determine the output step
		// We can directly get(0) because feedbacks must have one element
		final TraceNode outputNode = avaiableFeedbacks.get(0).getNode();
		settings.setOutputNode(outputNode);
		
		// Set trace
		final Trace trace = this.traceView.getTrace();
		settings.setTrace(trace);
		
		// Set feedbacks
		settings.setFeedbackRecords(avaiableFeedbacks);
		
		final DebugPilot debugPilot = new DebugPilot(settings);
		debugPilot.multiSlicing();
		debugPilot.propagate();
		
		TraceNode rootCause = debugPilot.locateRootCause();
		if (rootCause == null) {
			DialogUtil.popErrorDialog("Cannot locate root cause.", "DebugPilot Error");
			return;
		}
		
		FeedbackPath path = debugPilot.constructPath(rootCause);
		this.updatePathView(path);
		
		Display.getDefault().syncExec(() -> {
			
			for (TraceNode nextNode : TraceUtil.findAllNextNodes(userFeedback)) {
				if (path.containFeedbackByNode(nextNode)) {
					this.pathView.focusOnNode(nextNode);
					break;
				}
			}
			
		});
	}
	
	protected List<DPUserFeedback> collectAvailableFeedbacks(final DPUserFeedback userFeedback) {

		final FeedbackPath feedbackPath = this.pathView.getFeedbackPath();
		
		if (feedbackPath != null) {
			// User need to give feedback on path
			if (!feedbackPath.containFeedbackByNode(userFeedback.getNode())) {
				DialogUtil.popErrorDialog("User should give feedback only on node in debugging plan and node: " + userFeedback.getNode().getOrder() + " is not in the plan", "Feedback Error");
				return null;
			}
		}
		
		// Collect available feedbacks and sort it
		List<DPUserFeedback> avaiableFeedbacks = new ArrayList<>();
		avaiableFeedbacks.add(userFeedback);
		if (feedbackPath != null) {
			for(DPUserFeedback dpUserFeedback : feedbackPath.getFeedbacks()) {
				if (dpUserFeedback.getNode().equals(userFeedback.getNode())) {
					break;
				}
				avaiableFeedbacks.add(dpUserFeedback);
			}
		}
		avaiableFeedbacks.sort(new Comparator<DPUserFeedback>() {
			@Override
			public int compare(DPUserFeedback feedback1, DPUserFeedback feedback2) {
				return feedback2.getNode().getOrder() - feedback1.getNode().getOrder();
			}
		});
		
		return avaiableFeedbacks;
	}
	
	protected void updatePathView(final FeedbackPath path) {
		this.pathView.updateFeedbackPath(path);

	}
	
	protected String genOmissionMessage(final int startNodeOrder, final int endNodeOrder, final DPUserFeedback prevDpUserFeedback) {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("Conflicting feedback detected:\n\n");
		strBuilder.append("TraceNode: " + startNodeOrder + " with feedback: Correct\n");
		strBuilder.append("TraceNode: " + endNodeOrder + " with feedback: " + prevDpUserFeedback + "\n\n");
		strBuilder.append("It can be omission bug or you give a wrong feedback. \n");
		strBuilder.append("DebugPilot will now scan the step to narrow down the missing scpe, or you may review the feedback you give previously.");
		return strBuilder.toString();
	}
}
