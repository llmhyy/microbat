package microbat.evaluation.views;

import microbat.evaluation.model.PairList;
import microbat.model.trace.TraceNode;
import microbat.views.DebugFeedbackView;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;

public class BeforeTraceView extends TraceView {

	private PairList pairList;
	
	public BeforeTraceView() {
	}

	@Override
	protected void otherViewsBehavior(TraceNode node) {
		
		if(this.refreshProgramState){
			DebugFeedbackView feedbackView = MicroBatViews.getDebugFeedbackView();
			feedbackView.setTraceView(BeforeTraceView.this);
			feedbackView.refresh(node);			
		}
		
		markJavaEditor(node);
	}

	public PairList getPairList() {
		return pairList;
	}

	public void setPairList(PairList pairList) {
		this.pairList = pairList;
	}

}
