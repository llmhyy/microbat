package microbat.debugpilot;

import java.io.IOException;
import java.util.List;

import microbat.debugpilot.pathfinding.PathFinderType;
import microbat.debugpilot.propagation.PropagatorType;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.pyserver.BackwardModelTrainClient;
import microbat.pyserver.ForwardModelTrainClient;
import microbat.pyserver.RLModelTrainClient;
import sav.common.core.Pair;

public class DebugPilotTrainer extends DebugPilot {

	protected final ForwardModelTrainClient forwardTrainClient;
	protected final BackwardModelTrainClient backwardTrainClient;
	
	public DebugPilotTrainer(Trace trace, List<VarValue> inputs, List<VarValue> outputs, TraceNode outputNode) {
		super(trace, inputs, outputs, outputNode, PropagatorType.RL_TRAIN, PathFinderType.Dijstra);
		this.forwardTrainClient = new ForwardModelTrainClient();
		this.backwardTrainClient = new BackwardModelTrainClient();
	}
	
	public void sendRewards(List<Pair<TraceNode, Double>> rewardList) {
		this.sendRewards(rewardList, this.forwardTrainClient);
		this.sendRewards(rewardList, this.backwardTrainClient);
	}
	
	public void sendRewards(List<Pair<TraceNode, Double>> rewardList, RLModelTrainClient client) {
		try {
			client.conntectServer();
			client.notifyRewardMode();
			for (Pair<TraceNode, Double> pair : rewardList) {
				final TraceNode node = pair.first();
				final double reward = pair.second();
				client.notifyContinuoue();
				client.sendReward(node, reward);
			}
			client.notifyStop();
			client.disconnectServer();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(Log.genMsg(getClass(), "Server connection problem"));
		}
	}

}
