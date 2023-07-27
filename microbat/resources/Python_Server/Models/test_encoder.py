import torch
import yaml
import os
import random

from encoder.Encoder import Encoder

def main(config):
    model_path = config["eval.model_path"]
    data_folder = config["data.eval_data_path"]

    model = Encoder(config=config)
    model.load_state_dict(torch.load(model_path))
    model.eval()

    file_paths = []
    for root, _, files in os.walk(data_folder):
        for filename in files:
            filepath = os.path.join(root, filename)
            file_paths.append(filepath)

    sample = []

    for filepath in file_paths:
        with open(filepath, "r") as file:
            for line in file:
                tokens = line.split(":")
                feature_vector_str, classification_vector_str = tokens[0], tokens[1]
                code_statement = ":".join(tokens[2:])
                feature_vector = [float(num_str) for num_str in feature_vector_str.split(",")]
                feature_vector = torch.tensor(feature_vector)
                sample.append((feature_vector, code_statement))

    sample = random.sample(sample, k=100)
    feature = [s[0] for s in sample]
    feature = torch.stack(feature)

    embedding = model(feature)

    for i in range(len(sample)):
        print("--")
        print(sample[i][1])
        print(embedding[i])


if __name__ == '__main__':
    file_path = "C:\\Users\\david\\git\\microbat\\microbat\\Python_Server\\SPP\\configs\\encoder_config.yaml"

    # Open the YAML file
    with open(file_path, "r") as yaml_file:
        # Load the contents of the YAML file
        config = yaml.safe_load(yaml_file)

    print("Configuration ------------------")
    for key, value in config.items():
        print(key, value)
    print("-" * 20)
    main(config)
