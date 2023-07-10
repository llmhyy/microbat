import sys
sys.path.append("..")

from servers.RLModelServer import RLModelServer
from Models.supicion_predictor.DQN import DQN
from servers.FeedbackFeature import FeedbackVector
from utils.Log import printMsg

import yaml
import torch

class ForwardModelServer(RLModelServer):

    def __init__(self, config):
        super(ForwardModelServer, self).__init__(config=config)
        self.feedback_sim = self.config["training.feedback_sim"]
        self.device = self.config["training.device"]
        self.policy_net = DQN(self.config).to(self.device)
        self.model_path = self.config["training.load_path"]
        self.policy_net.load_state_dict(torch.load(self.model_path))
        self.policy_net = self.policy_net.float()
        self.policy_net.eval()
    
    def func(self, sock):
        printMsg("-"*20, ForwardModelServer)
        printMsg("Recieve feedbacks ...", ForwardModelServer)
        feedback_features = self.recieve_all_feedbacks(sock)
        while self.should_continoue(sock):
            node_order = self.recieve_node_order(sock)
            node_vector = self.recieve_node_vector(sock)
            related_feedbacks = []
            for feedback_feature in feedback_features:
                feedback_node_vector = feedback_feature.node_vector
                if self.cosine_sim(feedback_node_vector, node_vector) > self.feedback_sim:
                    related_feedbacks.append(feedback_feature)

            if len(related_feedbacks) == 0:
                input_feature = self.list_to_tensor([node_vector, torch.Tensor(FeedbackVector.DIMENSION)])
                prob = self.predict_prob(input_feature)
            else:
                probs = []
                for related_feedback in related_feedbacks:
                    input_feature = self.list_to_tensor([node_vector, related_feedback.feedback_vector.vector])
                    prob = self.predict_prob(input_feature)
                    probs.append(prob)
                prob = sum(probs)/len(probs)
            printMsg(f"Node: {node_order} \t forward factor: {prob.item()} \t related feedback count: {len(related_feedbacks)}", ForwardModelServer)
            self.send_prob(sock, prob.item())
        printMsg("Finish propagation ...", ForwardModelServer)

    
    def predict_prob(self, input_feature):
        prob = self.policy_net(input_feature.float())
        prob = torch.nan_to_num(prob, nan=0.5, posinf=1.0, neginf=0.0)
        prob = torch.clip(prob, min=0.0, max=1.0)
        return prob

if __name__ == "__main__":
    config_path = "C:\\Users\\david\\git\\microbat\\microbat\\Python_Server\\servers\\configs\\forward_server_config.yaml"
    with open(config_path, "r") as yaml_file:
        config = yaml.safe_load(yaml_file)
    server = ForwardModelServer(config)
    print("Server start ...")
    server.start()
    print("Server end")
