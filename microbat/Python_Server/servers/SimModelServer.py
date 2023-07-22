from RLModelServer import RLModelServer
from servers.FeedbackFeature import FeedbackVector
from utils.Log import printMsg, genMsg
from gensim.models.fasttext import load_facebook_vectors

import yaml
import torch

class SimModelServer(RLModelServer):

    def __init__(self, config):
        super(SimModelServer, self).__init__(config)
        self.step_sim_threshold = self.config["step_sim_threshold"]
        self.var_sim_threshold = self.config["var_sim_threshold"]
        self.work_embedding_path = self.config["encoder.work_embedding_path"]
        print(f"Loading word embeddings from {self.work_embedding_path}")
        self.word_embeddings = load_facebook_vectors(self.work_embedding_path)

    def func(self, sock):
        printMsg("-------------------------------------------", SimModelServer)
        printMsg("Recieve feedbacks ...", SimModelServer)
        feedback_features = self.recieve_all_feedbacks(sock)
        while self.should_continoue(sock):
            node_order = self.recieve_node_order(sock)
            node_vector = self.recieve_node_vector(sock)
            var_vector = self.recieve_variable_vector(sock)
            var_name = self.recieve_variable_name(sock)

            predictions = []
            for feedback_feature in feedback_features:
                node_sim = self.cal_node_sim(node_vector, feedback_feature.node_vector)
                var_sim = self.cal_var_sim(var_vector, var_name, feedback_feature.variable_vector, feedback_feature.variable_name)
                prediction = self.predict(node_sim.item(), var_sim.item())
                predictions.append(prediction)
            predictions = [str(prediction) for prediction in predictions]
            assert len(predictions) != 0, genMsg("prob is an emtpy list", SimModelServer)
            self.send_predictions(sock, ",".join(predictions))
        printMsg("Finish sim calculation ...", SimModelServer)

    def predict(self, node_sim, var_sim):
        if node_sim >= self.step_sim_threshold and var_sim >= self.var_sim_threshold:
            return 0
        elif node_sim >= self.step_sim_threshold and var_sim < self.var_sim_threshold:
            return 1
        else:
            return 2
        
    def gen_word_embedding(self, word):
        return torch.tensor(self.word_embeddings[word]).to(self.device)

    def cal_var_sim(self, var_type_1, var_name_1, var_type_2, var_name_2):
        if self.is_zero(var_type_1) and self.is_zero(var_type_2):
            type_sim = torch.tensor([1.0])
        else:
            type_sim = self.cosine_sim(var_type_1, var_type_2)
        var_name_vector_1 = self.gen_word_embedding(var_name_1)
        var_name_vector_2 = self.gen_word_embedding(var_name_2)
        name_sim = self.cosine_sim(var_name_vector_1, var_name_vector_2)
        return type_sim * name_sim

    def is_zero(self, vector):
        return (vector == 0).all()
if __name__ == "__main__":
    config_path = "C:\\Users\\david\\git\\microbat\\microbat\\Python_Server\\servers\\configs\\sim_server_config.yaml"
    with open(config_path, "r") as yaml_file:
        config = yaml.safe_load(yaml_file)
    server = SimModelServer(config)
    print("Server start ...")
    server.start()
    print("Server end")