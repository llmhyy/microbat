package microbat.pyserver;

import java.io.IOException;

import microbat.model.trace.TraceNode;

public abstract class RLModelTrainClient extends RLModelClient {

	protected static final String inferenceMsg = "INFERENCE_MODE";
	protected static final String rewardMsg = "REWARD_MODE";
	
	public RLModelTrainClient(String host, int port) {
		this(host, port, false);
	}
	
	public RLModelTrainClient(String host, int port, boolean verbose) {
		super(host, port, verbose);
	}
	
	public void notifyInferenceMode() throws IOException, InterruptedException {
		this.sendMsg(RLModelTrainClient.inferenceMsg);
	}
	
	public void notifyRewardMode() throws IOException, InterruptedException {
		this.sendMsg(RLModelTrainClient.rewardMsg);
	}
	
	public void sendReward(final TraceNode node, final double reward) throws IOException, InterruptedException {
		this.sendReward(node.getOrder(), reward);
	}
	
	public void sendReward(final int nodeOrder, final double reward) throws IOException, InterruptedException {
		this.sendNodeOrder(nodeOrder);
		this.sendReward(reward);
	}
	
	public void sendReward(final double reward) throws IOException, InterruptedException {
		this.sendMsg(String.valueOf(reward));
	}
}
