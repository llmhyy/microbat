import sys
sys.path.append("..")

from servers.RLModelServer import RLModelServer
import yaml

class TestFeatureServer(RLModelServer):
    def __init__(self, config):
        super(TestFeatureServer, self).__init__(config)

    def func(self, sock):
        feedback_features = self.recieve_all_feedbacks(sock)
        for feedback_feature in feedback_features:
            print("--------------------------------")
            print(feedback_feature.node_order)
            print(feedback_feature.node_vector.shape)
            print(feedback_feature.feedback_vector.vector)
            print(feedback_feature.variable_vector.shape)

if __name__ == "__main__":
    config_file = "C:\\Users\\david\\git\\microbat\\microbat\\Python_Server\\servers\\configs\\test_feedback_server_config.yaml"
    with open(config_file, "r") as yaml_file:
        config = yaml.safe_load(yaml_file)
    server = TestFeatureServer(config)
    print("Start server ...")
    server.start()
    print("End server ...")