from RLModelServer import RLModelServer
from Models.supicion_predictor.DQN import DQN
from servers.FeedbackFeature import FeedbackVector
from utils.Log import printMsg
from gensim.models.fasttext import load_facebook_vectors

import torch
import yaml

class BackwardModelServer(RLModelServer):

    CONDITION_RESULT_NAME = "ConditionResult_"

    def __init__(self, config):
        super(BackwardModelServer, self).__init__(config)
        self.feedback_sim = self.config["training.feedback_sim"]
        self.device = self.config["training.device"]
        self.policy_net = DQN(self.config).to(self.device)
        self.model_path = self.config["training.load_path"]
        self.policy_net.load_state_dict(torch.load(self.model_path))
        self.policy_net = self.policy_net.float()
        self.policy_net.eval()
        self.var_type_sim = self.config["training.var_type_sim"]
        self.var_name_sim = self.config["training.var_name_sim"]
        self.work_embedding_path = self.config["encoder.work_embedding_path"]
        print(f"Loading word embeddings from {self.work_embedding_path}")
        self.word_embeddings = load_facebook_vectors(self.work_embedding_path)
        
    def func(self, sock):
        printMsg("-------------------------------------------", BackwardModelServer)
        printMsg("Recieve feedbacks ...", BackwardModelServer)
        feedback_features = self.recieve_all_feedbacks(sock)
        while self.should_continoue(sock):
            node_order = self.recieve_node_order(sock)
            node_vector = self.recieve_node_vector(sock)
            var_vector = self.recieve_variable_vector(sock)
            var_name = self.recieve_variable_name(sock)

            related_feedbacks = []
            for feedback_feature in feedback_features:
                feedback_node_vector = feedback_feature.node_vector
                if self.cosine_sim(feedback_node_vector, node_vector) > self.feedback_sim:
                    related_feedbacks.append(feedback_feature)

            if len(related_feedbacks) == 0:
                input_feature = self.list_to_tensor([node_vector, var_vector, torch.tensor([0.0]), torch.Tensor(FeedbackVector.DIMENSION)])
                prob = self.predict_prob(input_feature)
            else:
                probs = []
                for related_feedback in related_feedbacks:
                    node_sim = self.cal_node_sim(node_vector, related_feedback.node_vector)
                    var_sim = self.cal_var_sim(var_vector, var_name, related_feedback.variable_vector, related_feedback.variable_name)
                    input_feature = self.list_to_tensor([node_vector, var_vector, node_sim, var_sim])
                    prob = self.predict_prob(input_feature)
                    probs.append(prob)
                prob = sum(probs) / len(probs)

            printMsg(f"Node: {node_order} \t backward factor: {prob.item()} \trelated feedback count: {len(related_feedbacks)}", BackwardModelServer)
            self.send_prob(sock, prob.item())

        printMsg("Finish propagation ...", BackwardModelServer)
    
    def predict_prob(self, input_feature):
        prob = self.policy_net(input_feature.float())
        prob = torch.nan_to_num(prob, nan=0.5, posinf=1.0, neginf=0.0)
        prob = torch.clip(prob, min=0.0, max=1.0)
        return prob
    
    def gen_word_embedding(self, word):
        return torch.tensor(self.word_embeddings[word]).to(self.device)

    # def cal_reward(self, predict, feedback_feature, ref_var_type, ref_var_name, ref_var_name_vector):
    #     feedback_vector = feedback_feature.feedback_vector
    #     target_var_type = feedback_feature.variable_vector
    #     target_var_name = feedback_feature.variable_name
    #     target_var_name_vector = self.gen_word_embedding(feedback_feature.variable_name)
    #     if feedback_vector.is_correct_feedback():
    #         expected = 0
    #     elif feedback_vector.is_wrong_path_feedback():
    #         if self.is_condition_result(ref_var_type, ref_var_name, target_var_type, target_var_name):
    #             expected = 1
    #         else:
    #             expected = 0.5
    #     elif feedback_vector.is_wrong_var_feedback():
    #         if self.is_condition_result(ref_var_name):
    #             expected = 0
    #         elif self.is_same_var(ref_var_type, ref_var_name_vector, target_var_type, target_var_name_vector):
    #             expected = 1
    #         else:
    #             expected = 0
    #     return 1 - (expected-predict)**2

    # def is_same_var(self, ref_var_type, ref_var_name_vector, target_var_type, target_var_name_vector):
    #     return self.cosine_sim(ref_var_name_vector, target_var_name_vector) > self.var_name_sim and self.cosine_sim(ref_var_type, target_var_type) > self.var_type_sim
    
    # def is_condition_result(self, var_name):
    #     return var_name.startswith(BackwardModelServer.CONDITION_RESULT_NAME)

if __name__ == "__main__":
    config_path = "C:\\Users\\david\\git\\microbat\\microbat\\Python_Server\\servers\\configs\\backward_server_config.yaml"
    with open(config_path, "r") as yaml_file:
        config = yaml.safe_load(yaml_file)
    server = BackwardModelServer(config)
    print("Server start ...")
    server.start()
    print("Server end")