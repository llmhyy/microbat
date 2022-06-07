package microbat.autofeedback;

import java.util.ArrayList;
import java.util.List;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.recommendation.UserFeedback;
import microbat.stepvectorizer.StepVectorizer;

public final class MLFeedbackGenerator extends FeedbackGenerator {
	
	/**
	 * It is used to communicate with the server.
	 */
	private ModelClient client;
	
	/**
	 * It stores all the previous trace nodes.
	 */
	private List<NodeFeedbackPair> nodeRecord;
	
	/**
	 * It vectorize trace node into vector.
	 */
	private StepVectorizer stepVectorizer;
	
	public MLFeedbackGenerator(Trace trace, AutoFeedbackMethods method) {
		super(trace, method);
		this.client = new ModelClient();
		this.nodeRecord = new ArrayList<>();
		this.stepVectorizer = new StepVectorizer(trace);
	}

	@Override
	public UserFeedback giveFeedback(TraceNode node) {
		
		// If there are no previous feedback, then ask users to give one
		if (this.nodeRecord.isEmpty()) {
			this.updateFeedbackFromView();
			if (this.feedbackFromView == null) {
				System.out.println("Please give a feedback before using the ML approach");
				return null;
			}
			this.addRecord(node, this.feedbackFromView);
			System.out.println(this.feedbackFromView);
			
			this.feedbackFromView = new UserFeedback(UserFeedback.WRONG_PATH);
			return this.feedbackFromView;
		}
		
		// Get the classification result from model for each pair and given node
		for (NodeFeedbackPair pair : this.nodeRecord) {
			TraceNode ref_node = pair.getNode();
			
			String target_vec = this.stepVectorizer.vectorize(node.getOrder()).convertToCSV();
			String ref_vec = this.stepVectorizer.vectorize(ref_node.getOrder()).convertToCSV();
			
			int result = this.client.requestClassification(target_vec, ref_vec);
			System.out.println("result = " + result);
		}
		
		this.addRecord(node, null);
		UserFeedback feedback = new UserFeedback();
		feedback.setFeedbackType(UserFeedback.WRONG_PATH);
		return feedback;
	}
	
	/**
	 * Add a node feedback pair into record
	 * @param node Node in the pair
	 * @param feedback Feedack in the pair
	 */
	private void addRecord(TraceNode node, UserFeedback feedback) {
		NodeFeedbackPair pair = new NodeFeedbackPair(node, feedback);
		this.nodeRecord.add(pair);
	}
	
	/**
	 * Add a node feedback pair into record
	 * @param pair Node feedback pair
	 */
	private void addRecord(NodeFeedbackPair pair) {
		this.nodeRecord.add(pair);
	}
	
	@Override
	public void notifyEnd() {
		this.client.endServer();
	}
}
