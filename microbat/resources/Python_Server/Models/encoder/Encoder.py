import torch
import torch.nn as nn

class Encoder(nn.Module):

    def __init__(self, config):
        super(Encoder, self).__init__()
        self.config = config
        self.input_size = self.config["data.input_size"]
        self.hidden_sizes = self.config["data.hidden_sizes"]
        self.output_size = self.config["data.output_size"]
        assert len(self.hidden_sizes) >= 1, f"[Encoder] Hidden layer size should not be empty "
        self.fc1 = nn.Linear(self.input_size, self.hidden_sizes[0])
        self.hidden_layers = nn.ModuleList()
        for i in range(len(self.hidden_sizes)-1):
            self.hidden_layers.append(
                nn.Linear(self.hidden_sizes[i], self.hidden_sizes[i+1])
            )
        self.fc_out = nn.Linear(self.hidden_sizes[-1], self.output_size)
        self.activation = nn.Tanh()

    def forward(self, x):
        x = self.activation(self.fc1(x))
        for layer in self.hidden_layers:
            x = self.activation(layer(x))
        out = self.fc_out(x)
        return out



