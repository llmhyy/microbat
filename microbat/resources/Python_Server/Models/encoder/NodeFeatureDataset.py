import torch
import torch.nn as nn
import torch.nn.functional as F
import random
import os
import json

from torch.utils.data import Dataset
from joblib import Parallel, delayed

class NodeFeatureDataset(Dataset):
    def __init__(self, config):
        self.config = config
        self.train_data_path = self.config["data.train_data_path"]
        assert os.path.exists(self.train_data_path), f"Give data path does not exists: {self.train_data_path}"
        assert self.train_data_path.endswith(".json"), f"GIve data path is not a json file: {self.train_data_path}"
        self.num_workers = self.config["data.num_workers"]
        self.cos_sim = nn.CosineSimilarity(dim=-1)
        self.readData()


    def readData(self):
        print(f"Reading data from {self.train_data_path} ...")
        self.data = []
        self.labels = []
        self.label2data = {}
        idx = 0
        with open(self.train_data_path, "r") as file:
            for line in file:
                obj = json.loads(line)
                feature = obj["feature"]
                label = obj["label"]
                self.data.append(feature)
                self.labels.append(label)
                if label in self.label2data:
                    self.label2data[label].append(idx)
                else:
                    self.label2data[label] = [idx]
                idx += 1
        
        for label, items in self.label2data.items():
            if len(items) <= 1:
                raise Exception(f"There are only one element in label {label}, please filter it out from dataset")

        print(f"Collected {len(self.data)} data ...")

    def __len__(self):
        return len(self.data)

    def __getitem__(self, index):
        feature = self.data[index]
        feature = self.to_tensor(feature)
        pos_feature = self.get_pos_sample(index)
        neg_feature = self.get_neg_sample(index)
        label = self.labels[index]
        label = self.to_tensor(label)
        return feature, pos_feature, neg_feature, label

    def get_pos_sample(self, index):
        label = self.labels[index]
        positive_idxes = self.label2data[label][:]
        positive_idxes.remove(index) # Don't sample itself as positive sample
        positive_idx = random.sample(positive_idxes, 1)[0]
        pos_feature = self.data[positive_idx]
        return self.to_tensor(pos_feature)

    def get_neg_sample(self, index):
        label = self.labels[index]
        labels_ = list(self.label2data.keys())[:]
        labels_.remove(label)
        neg_label = random.sample(labels_, k=1)[0]
        negative_idxes = self.label2data[neg_label]
        negative_idx = random.sample(negative_idxes, 1)[0]
        neg_feature = self.data[negative_idx]
        return self.to_tensor(neg_feature)

    def to_tensor(self, feature):
        return torch.tensor(feature)
    # def __getitem__(self, index) -> torch.Tensor:
    #     feature = self.data[index]
        
    #     anchor_feature, classification_vector, code_statement = self.data[index]
        
    #     # Randomly select one postive sample and negative sample
    #     sampled_idxes = random.sample(range(len(self.data)), k=100)
    #     if index in sampled_idxes:
    #         sampled_idxes.remove(index)
    #     sampled_idxes = torch.tensor(sampled_idxes)

    #     sampled_cls_vector = [self.data[idx][1] for idx in sampled_idxes]
    #     sampled_cls_vector = torch.stack(sampled_cls_vector)
    #     # hamming_dis = (classification_vector.bool() ^ sampled_cls_vector.bool()).sum(dim=-1)

    #     cos_sim = F.cosine_similarity(
    #         F.normalize(sampled_cls_vector, p=2, dim=-1),
    #         F.normalize(classification_vector, p=2, dim=-1).unsqueeze(dim=0),
    #         dim=-1)
        
    #     pos_idx = cos_sim >= 0.5
    #     neg_idx = cos_sim < 0.5
    #     # pos_idx = hamming_dis <= 1
    #     # neg_idx = hamming_dis > 1

    #     if pos_idx.sum() == 0:
    #         print("missing positive")
    #         return torch.zeros_like(anchor_feature), torch.zeros_like(anchor_feature), torch.zeros_like(anchor_feature), torch.Tensor([4])
        
    #     if neg_idx.sum() == 0:
    #         print("missing negative")
    #         return torch.zeros_like(anchor_feature), torch.zeros_like(anchor_feature), torch.zeros_like(anchor_feature), torch.Tensor([4])

    #     pos_feature = self.data[sampled_idxes[pos_idx][0]][0]
    #     neg_feature = self.data[sampled_idxes[neg_idx][0]][0]

    #     class_id = self.cls_vector_to_id(classification_vector)
    #     return anchor_feature, pos_feature, neg_feature, class_id
    
    # def cls_vector_to_id(self, cls_vector):
    #     if (cls_vector == self.class_0).all():
    #         return torch.Tensor([0])
    #     elif (cls_vector == self.class_1).all():
    #         return torch.Tensor([1])
    #     elif (cls_vector == self.class_2).all():
    #         return torch.Tensor([2])
    #     elif (cls_vector == self.class_3).all():
    #         return torch.Tensor([3])
    #     else:
    #         return torch.Tensor([4])
