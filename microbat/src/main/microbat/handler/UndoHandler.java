package microbat.handler;

import microbat.Activator;
import microbat.behavior.Behavior;
import microbat.behavior.BehaviorData;
import microbat.behavior.BehaviorReporter;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.util.Settings;
import microbat.views.DebugFeedbackView;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

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
		
		Behavior behavior = BehaviorData.getOrNewBehavior(Settings.lanuchClass);
		behavior.increaseUndo();
		new BehaviorReporter(Settings.lanuchClass).export(BehaviorData.projectBehavior);
		
		return null;
	}

	private void restore(CheckingState state) throws PartInitException {
		Trace trace = MicroBatViews.getTraceView().getTrace();
		trace.setCheckTime(state.getTraceCheckTime());
		
		TraceNode currentNode = trace.getExectionList().get(state.getCurrentNodeOrder()-1);
		
		currentNode.setSuspicousScoreMap(state.getCurrentNodeSuspicousScoreMap());
		currentNode.setCheckTime(state.getCurrentNodeCheckTime());
		
		Settings.interestedVariables = state.getInterestedVariables();
		Settings.potentialCorrectPatterns = state.getPotentialCorrectPatterns();
		Settings.wrongPathNodeOrder = state.getWrongPathNodeOrder();
		
		DebugFeedbackView debugFeedbackView = (DebugFeedbackView)PlatformUI.getWorkbench().
				getActiveWorkbenchWindow().getActivePage().showView(MicroBatViews.DEBUG_FEEDBACK);
		debugFeedbackView.setRecommender(state.getRecommender());
		
		TraceView traceView = (TraceView)PlatformUI.getWorkbench().
				getActiveWorkbenchWindow().getActivePage().showView(MicroBatViews.TRACE);
		traceView.jumpToNode(trace, currentNode.getOrder());
	}
	
	
	
}
