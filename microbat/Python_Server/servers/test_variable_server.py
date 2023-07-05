import sys
sys.path.append("..")

from servers.RLModelServer import RLModelServer
from gensim.models.fasttext import load_facebook_vectors

import torch
import yaml

class TestVariableServer(RLModelServer):
    def __init__(self, config):
        super(TestVariableServer, self).__init__(config)
        self.work_embedding_path = self.config["encoder.work_embedding_path"]
        print(f"Loading word embeddings from {self.work_embedding_path}")
        self.work_embeddings = load_facebook_vectors(self.work_embedding_path)

    def func(self, sock):
        variable_vectors = []
        while self.should_continoue(sock):
            variable_vector = self.recieve_variable_vector(sock)
            variable_name = self.recieve_variable_name(sock)
            variable_vectors.append((variable_name, variable_vector))
        for ref_name, ref_vector in variable_vectors:
            for target_name, target_vector in variable_vectors:
                ref_name_vector = self.gen_word_embedding(ref_name)
                target_name_vector = self.gen_word_embedding(target_name)
                print(f"ref_vector: {ref_name}")
                print(f"target vector: {target_name}")
                print(f"sim : {self.cosine_sim(ref_vector, target_vector).item()}")
                print(f"name sim: {self.cosine_sim(ref_name_vector, target_name_vector).item()}")
            break
    
    def gen_word_embedding(self, word):
        return torch.tensor(self.work_embeddings[word])
    
if __name__ == "__main__":
    config_file = "C:\\Users\\david\\git\\microbat\\microbat\\Python_Server\\servers\\configs\\test_variable_server_config.yaml"
    with open(config_file, "r") as yaml_file:
        config = yaml.safe_load(yaml_file)
    server = TestVariableServer(config)
    print("Start server ...")
    server.start()
    print("End server ...")