package microbat.evaluation.views;

import microbat.model.trace.TraceNode;
import microbat.views.DebugFeedbackView;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;

public class BeforeTraceView extends TraceView {

	public BeforeTraceView() {
	}

	@Override
	protected void otherViewsBehavior(TraceNode node) {
		DebugFeedbackView feedbackView = MicroBatViews.getDebugFeedbackView();
		feedbackView.setTraceView(BeforeTraceView.this);
		feedbackView.refresh(node);
		
//		ReasonView reasonView = MicroBatViews.getReasonView();
//		reasonView.refresh(feedbackView.getRecommender());
	
		markJavaEditor(node);
	}

}
