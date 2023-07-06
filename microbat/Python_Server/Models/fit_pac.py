from sklearn.decomposition import TruncatedSVD

import torch
import yaml

import json
import os
import numpy as np

from typing import List

from joblib import dump, load

JSON_EXT = ".json"

def read_json_file(path:str) -> List[object]:
    """
    Read the give json file and store the context into
    a list of json objects
    """
    assert os.path.exists(path), f"Given path does not exists: {path}"
    assert path.endswith(JSON_EXT), f"Given file is not a json file: {path}"

    json_objs = []
    with open(path, "r") as file:
        for line in file :
            obj = json.loads(line)
            json_objs.append(obj)
    return json_objs

def main(config):
    data_path = config["data.train_data"]
    print(f"Reading data from {data_path}")
    json_objs = read_json_file(data_path)
    features = []
    for obj in json_objs:
        feature = obj["feature"]
        features.append(feature)
    features = np.array(features)
    k = config["data.output_dim"]
    pca = TruncatedSVD(n_components=k)
    print(f"Fitting {features.shape[0]} data")
    pca.fit(features)
    
    save_path = config["data.output_path"]
    print(f"Saving model to {save_path}")
    dump(pca, save_path)

    print(f"Getting some sample")
    pca = load(save_path)
    sample = pca.transform(features[:3, :])
    print(sample)


if __name__ == "__main__":
    yaml_file_path = "C:\\Users\\david\\git\\microbat\\microbat\\Python_Server\\Models\\configs\\pca_config.yaml"
    with open(yaml_file_path, "r") as yaml_file:
        config = yaml.safe_load(yaml_file)
    main(config)