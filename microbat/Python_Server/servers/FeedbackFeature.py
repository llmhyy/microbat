from collections import namedtuple
import torch

FeedbackFeature = namedtuple('FeadbackFeature', ['node_order', 'node_vector', 'feedback_vector', 'variable_vector', 'variable_name'])

class FeedbackVector():

    DIMENSION = 3
    CORRECT_IDX = 0
    WRONG_PATH_IDX = 1
    WRONG_VAR_IDX = 2

    def __init__(self, string, device):
        self.vector = [float(element) for element in string.split(",")]
        self.vector = torch.tensor(self.vector).to(device)
        assert len(self.vector) == FeedbackVector.DIMENSION, f"Vector should have size of 3 but {len(self.vector)} is detected"

    def is_correct_feedback(self):
        return self.vector[FeedbackVector.CORRECT_IDX] == 1.0
    
    def is_wrong_path_feedback(self):
        return self.vector[FeedbackVector.WRONG_PATH_IDX] == 1.0
    
    def is_wrong_var_feedback(self):
        return self.vector[FeedbackVector.WRONG_VAR_IDX] == 1.0

    