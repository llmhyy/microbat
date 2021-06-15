package microbat.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;

import microbat.behavior.Behavior;
import microbat.behavior.BehaviorData;
import microbat.behavior.BehaviorReporter;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.util.Settings;
import microbat.views.DebugFeedbackView;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;

public class UndoHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(!Settings.checkingStateStack.isEmpty()){
			CheckingState state = Settings.checkingStateStack.pop();
			
			try {
				restore(state);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
		
		Behavior behavior = BehaviorData.getOrNewBehavior(Settings.launchClass);
		behavior.increaseUndo();
		new BehaviorReporter(Settings.launchClass).export(BehaviorData.projectBehavior);
		
		return null;
	}

	private void restore(CheckingState state) throws PartInitException {
		Trace trace = MicroBatViews.getTraceView().getTrace();
		trace.setCheckTime(state.getTraceCheckTime());
		
		TraceNode currentNode = trace.getExecutionList().get(state.getCurrentNodeOrder()-1);
		
		currentNode.setSuspicousScoreMap(state.getCurrentNodeSuspicousScoreMap());
		currentNode.setCheckTime(state.getCurrentNodeCheckTime());
		
		Settings.interestedVariables = state.getInterestedVariables();
		Settings.potentialCorrectPatterns = state.getPotentialCorrectPatterns();
		Settings.wrongPathNodeOrder = state.getWrongPathNodeOrder();
		
		DebugFeedbackView debugFeedbackView = MicroBatViews.getDebugFeedbackView();
		debugFeedbackView.setRecommender(state.getRecommender());
		
		TraceView traceView = MicroBatViews.getTraceView();
		traceView.jumpToNode(trace, currentNode.getOrder(), true);
	}
	
	
	
}
