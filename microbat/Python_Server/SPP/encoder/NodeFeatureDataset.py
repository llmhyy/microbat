import torch
import torch.nn as nn
import torch.nn.functional as F
import random
import os

from torch.utils.data import Dataset
from joblib import Parallel, delayed

class NodeFeatureDataset(Dataset):
    def __init__(self, config):
        self.config = config
        self.train_data_path = self.config["data.train_data_path"]
        self.num_workers = self.config["data.num_workers"]
        self.cos_sim = nn.CosineSimilarity(dim=-1)
        self.readData()

        self.class_0 = torch.Tensor([1.0, 0.0, 0.0])
        self.class_1 = torch.Tensor([0.0, 1.0, 0.0])
        self.class_2 = torch.Tensor([0.0, 0.0, 1.0])
        self.class_3 = torch.Tensor([0.0, 1.0, 1.0])


    def readData(self):
        print(f"Reading data from {self.train_data_path} ...")
        self.data = []
        file_paths = []
        for root, _, files in os.walk(self.train_data_path):
            for file_name in files:
                file_path = os.path.join(root, file_name)
                file_paths.append(file_path)

        temp_data = Parallel(n_jobs=self.num_workers)(delayed(self.readFile)(file_path) for file_path in file_paths)
        self.data = []
        for temp_data_ in temp_data:
            self.data.extend(temp_data_)

        print(f"Collected {len(self.data)} data ...")

    def readFile(self, file_path):
        data = []
        with open(file_path, "r") as file:
            for line in file:
                tokens = line.split(":")
                feature_vector_str, classification_vector_str = tokens[0], tokens[1]
                code_statement = ":".join(tokens[2:])
                feature_vector = [float(num_str) for num_str in feature_vector_str.split(",")]
                feature_vector = torch.tensor(feature_vector)
                classification_vector = [float(num_str) for num_str in classification_vector_str.split(",")]
                classification_vector = torch.tensor(classification_vector)
                data.append((feature_vector, classification_vector, code_statement))
        return data
        

    def __len__(self):
        return len(self.data)
    
    def __getitem__(self, index) -> torch.Tensor:
        anchor_feature, classification_vector, code_statement = self.data[index]
        
        # Randomly select one postive sample and negative sample
        sampled_idxes = random.sample(range(len(self.data)), k=100)
        if index in sampled_idxes:
            sampled_idxes.remove(index)
        sampled_idxes = torch.tensor(sampled_idxes)

        sampled_cls_vector = [self.data[idx][1] for idx in sampled_idxes]
        sampled_cls_vector = torch.stack(sampled_cls_vector)
        # hamming_dis = (classification_vector.bool() ^ sampled_cls_vector.bool()).sum(dim=-1)

        cos_sim = F.cosine_similarity(
            F.normalize(sampled_cls_vector, p=2, dim=-1),
            F.normalize(classification_vector, p=2, dim=-1).unsqueeze(dim=0),
            dim=-1)
        
        pos_idx = cos_sim >= 0.5
        neg_idx = cos_sim < 0.5
        # pos_idx = hamming_dis <= 1
        # neg_idx = hamming_dis > 1

        if pos_idx.sum() == 0:
            print("missing positive")
            return torch.zeros_like(anchor_feature), torch.zeros_like(anchor_feature), torch.zeros_like(anchor_feature), torch.Tensor([4])
        
        if neg_idx.sum() == 0:
            print("missing negative")
            return torch.zeros_like(anchor_feature), torch.zeros_like(anchor_feature), torch.zeros_like(anchor_feature), torch.Tensor([4])

        pos_feature = self.data[sampled_idxes[pos_idx][0]][0]
        neg_feature = self.data[sampled_idxes[neg_idx][0]][0]

        class_id = self.cls_vector_to_id(classification_vector)
        return anchor_feature, pos_feature, neg_feature, class_id
    
    def cls_vector_to_id(self, cls_vector):
        if (cls_vector == self.class_0).all():
            return torch.Tensor([0])
        elif (cls_vector == self.class_1).all():
            return torch.Tensor([1])
        elif (cls_vector == self.class_2).all():
            return torch.Tensor([2])
        elif (cls_vector == self.class_3).all():
            return torch.Tensor([3])
        else:
            return torch.Tensor([4])
