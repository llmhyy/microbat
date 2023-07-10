import sys
sys.path.append("..")

from servers.SocketServer import SocketServer
from Models.encoder.Encoder import Encoder
from servers.FeedbackFeature import FeedbackVector, FeedbackFeature
from gensim.models.fasttext import load_facebook_vectors

from joblib import load
import yaml
import torch
import os
import torch.nn.functional as F


class RLModelServer(SocketServer):
    
    def __init__(self, config):
        self.config = config
        host = self.config["server.host"]
        port = self.config["server.port"]
        verbose = self.config["server.verbose"]
        super(RLModelServer, self).__init__(host, port, verbose)
        self.use_pca = self.config["encoder.pca"]
        self.encoder = self.load_encoder()
        self.continueMsg = "CONTINUE"
        self.stopMsg = "STOP"
        self.device = self.config["training.device"]
    
    def func(self, sock):
        raise NotImplementedError()
    
    def should_continoue(self, sock):
        message = self.recvMsg(sock)
        if message == self.continueMsg:
            return True
        elif message == self.stopMsg:
            return False
        elif self.isEndServerMsg(message):
            self.endServer()
            return False
        else:
            return False

    def recieve_node_vector(self, sock):
        feature = []
        while self.should_continoue(sock):
            message = self.recvMsg(sock)
            vector = self.str_to_tensor(message)
            vector = self.gen_embedding(vector)
            feature.append(vector)
        feature = torch.stack(feature, dim=0).to(self.device)
        feature = feature.reshape(-1)
        return feature

    def recieve_variable_vector(self, sock):
        message = self.recvMsg(sock)
        vector = self.str_to_tensor(message)
        return vector
    
    def recieve_variable_name(self, sock):
        return self.recvMsg(sock)
        
    def recieve_feedback_vector(self, sock):
        message = self.recvMsg(sock)
        return FeedbackVector(message, self.device)
    
    def recieve_feedback_feature(self, sock):
        node_order = self.recieve_node_order(sock)
        node_vector = self.recieve_node_vector(sock)
        feedback_vector = self.recieve_feedback_vector(sock)
        variable_vector = self.recieve_variable_vector(sock)
        variable_name = self.recieve_variable_name(sock)
        return FeedbackFeature(node_order, node_vector, feedback_vector, variable_vector, variable_name)
    
    def recieve_all_feedbacks(self, sock):
        feedbacks = []
        while self.should_continoue(sock):
            feedback = self.recieve_feedback_feature(sock)
            feedbacks.append(feedback)
        return feedbacks
    
    def gen_embedding(self, vector):
        if self.use_pca:
            embedding = self.encoder.transform(vector.detach().cpu().numpy().reshape(1, -1))
            return torch.tensor(embedding).to(self.device)
        else:
            return self.encoder(vector)
        
    def recieve_node_order(self, sock):
        message = self.recvMsg(sock)
        order = int(message)
        return order
        
    def receive_sim(self, sock):
        message = self.recvMsg(sock)
        sim = float(message)
        sim = torch.tensor(sim).to(self.device)
        return sim
        
    def load_encoder(self):
        if self.use_pca:
            return self.load_pca()
        else:
            return self.load_encoder_model()

    def load_pca(self):
        return load(self.config["encoder.pca_path"])
    
    def load_encoder_model(self):
        encoder_config = self.load_yaml_config(self.config["encoder.config_path"])
        encoder = Encoder(encoder_config)
        encoder.load_state_dict(torch.load(self.config["encoder.encoder_path"]))
        encoder.eval()
        return encoder
    
    def load_yaml_config(self, yaml_file):
        assert os.path.exists(yaml_file), f"Yaml file does not exits {yaml_file}"
        assert yaml_file.endswith(".yaml"), f"It is not yaml file {yaml_file}"
        with open(yaml_file, "r") as file:
            return yaml.safe_load(file)

    def str_to_tensor(self, string):
        vector = [float(substring) for substring in string.split(",")]
        vector = torch.tensor(vector).float().to(self.device)
        return vector
    
    def send_prob(self, sock, prob):
        self.sendMsg(sock, str(prob))

    def cal_node_sim(self, node_1, node_2):
        return self.cosine_sim(node_1, node_2)
    
    def cosine_sim(self, vector_1, vector_2):
        vector_1 = vector_1.view(1, -1)
        vector_2 = vector_2.view(1, -1)
        return F.cosine_similarity(vector_1, vector_2)
    
    def list_to_tensor(self, list_of_tensors):
        for i in range(len(list_of_tensors)):
            list_of_tensors[i] = list_of_tensors[i].to(self.device)
        return torch.cat(list_of_tensors,dim=0).reshape(-1).to(self.device)