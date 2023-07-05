import sys
sys.path.append("..")

from servers.RLModelServer import RLModelServer

import torch.nn.functional as F
import yaml

class TestSimServer(RLModelServer):
    def __init__(self, config):
        super(TestSimServer, self).__init__(config)

    def func(self, sock):
        feedback_features = self.recieve_all_feedbacks(sock)
        node_features = []
        while self.should_continoue(sock):
            node_feature = self.recieve_node_vector(sock)
            node_features.append(node_feature)

        for node_feature in node_features:
            node_order = node_feature.node_order
            node_vector = node_feature.node_vector
            for feedback_feature in feedback_features:
                feedback_order = feedback_feature.node_order
                feedback_node_vector = feedback_feature.node_vector
                similarity = self.cosine_sim(node_vector, feedback_node_vector)
                if similarity > 0.8:
                    feedback_vector = feedback_feature.feedback_vector
                    print(f"Matched node {node_order}: feedback order {feedback_order} type: {feedback_vector.vector}")


if __name__ == "__main__":
    config_file = "C:\\Users\\david\\git\\microbat\\microbat\\Python_Server\\servers\\configs\\test_sim_server_config.yaml"
    with open(config_file, "r") as yaml_file:
        config = yaml.safe_load(yaml_file)
    server = TestSimServer(config)
    print("Start server ...")
    server.start()
    print("End server ...")