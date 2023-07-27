import sys
sys.path.append("..")

import torch

from servers.RLModelServer import RLModelServer

class RLModelTrainServer(RLModelServer):

    INFERENCE_MODE = "INFERENCE_MODE"
    REWARD_MODE = "REWARD_MODE"

    def __init__(self, config):
        super(RLModelTrainServer, self).__init__(config)

    def recieve_reward(self, sock):
        message = self.recvMsg(sock)
        reward = float(message)
        reward = torch.tensor(reward).to(self.device)
        return reward
    
    