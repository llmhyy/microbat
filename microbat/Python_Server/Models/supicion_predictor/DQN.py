import torch
import torch.nn as nn

class DQN(nn.Module):
    def __init__(self, config):
        super(DQN, self).__init__()
        self.config = config
        self.input_size = self.config["data.input_size"]
        self.hidden_sizes = self.config["data.hidden_sizes"]
        assert len(self.hidden_sizes) >=1, f"[DQN] Hidden layer size should not be empty"

        self.fc1 = nn.Linear(self.input_size, self.hidden_sizes[0])
        self.hidden_layers = nn.ModuleList()
        for i in range(len(self.hidden_sizes)-1):
            self.hidden_layers.append(
                nn.Linear(self.hidden_layers[i], self.hidden_sizes[i+1])
            )
        self.fc_out = nn.Linear(self.hidden_sizes[-1], 1)
        self.activation = nn.ReLU()
        self.output_activate = nn.Sigmoid()

    def forward(self, x):
        x = self.activation(self.fc1(x))
        for layer in self.hidden_layers:
            x = self.activation(layer(x))
        out = self.output_activate(self.fc_out(x))
        return out