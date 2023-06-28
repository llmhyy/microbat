import yaml
import os

from encoder.NodeFeatureDataset import NodeFeatureDataset
from encoder.Encoder import Encoder

from sklearn.manifold import TSNE
import numpy as np
import torch
import torch.nn as nn
import torch.optim as optim
import matplotlib.pyplot as plt

from torch.utils.data import DataLoader
from torch.utils.tensorboard import SummaryWriter
from PIL import Image
import torchvision.transforms as transforms

class Trainer():
    def __init__(self,config):
        self.config = config
        self.num_epoch = self.config["training.num_epoch"]
        self.lr = self.config["training.lr"]
        self.device = self.config["training.device"]
        self.save_interval = self.config["training.save_interval"]
        self.output_folder = os.path.join(self.config["training.output_path"], self.config["training.version"])
        if self.device == "cuda":
            assert torch.cuda.is_available(), "Cuda is not avaliable"
        
        self.batch_size = self.config["data.batch_size"]
        self.num_workers = self.config["data.num_workers"]
        
        self.log_interval = self.config["visualization.log_interval"]
        self.vis_sample_size = self.config["visualization.sample_size"]

        if not os.path.exists(self.output_folder):
            os.makedirs(self.output_folder)

        # Training config
        self.model = Encoder(self.config).to(device=self.device)
        self.loss_fn = nn.TripletMarginLoss(margin=1.0, p=2).to(device=self.device)
        self.optimizier = optim.Adam(self.model.parameters(), lr=self.lr)
        self.scheduler = optim.lr_scheduler.LinearLR(self.optimizier, start_factor=1.0, end_factor=0.001, total_iters=self.num_epoch)

        # Dataloader
        dataset = NodeFeatureDataset(self.config)
        self.total_step = dataset.__len__() // self.batch_size
        self.dataloader = DataLoader(dataset, 
                                     batch_size=self.batch_size, 
                                     drop_last=True,
                                     shuffle=True)

        # Visualization
        self.writer = SummaryWriter(log_dir=self.output_folder)
        self.tsne = TSNE(n_components=2)

    def train(self):
        self.model.train()
        
        step = 0
        for epoch in range(self.num_epoch):
            vis_features = []
            vis_class = []
            step_ = 0
            for anchor_feature, pos_feature, neg_feature, labels in self.dataloader:
                self.optimizier.zero_grad()
                
                anchor_embedding = self.model(anchor_feature.to(self.device))
                pos_embedding = self.model(pos_feature.to(self.device))
                neg_embedding = self.model(neg_feature.to(self.device))

                triplet_loss = self.loss_fn(anchor_embedding, pos_embedding, neg_embedding)
                
                if step % self.log_interval == 0:
                    print(f"[Training log] epoch: {epoch}/{self.num_epoch} step: {step_}/{self.total_step} triplet loss: {triplet_loss} lr: {self.scheduler.get_last_lr()}")
                    self.writer.add_scalar('Loss', triplet_loss.item(), step)

                if len(vis_features) < self.vis_sample_size:
                    B = anchor_embedding.shape[0]
                    indices = torch.randperm(B)[:self.vis_sample_size]
                    selected_features = anchor_embedding[indices]
                    selected_features = selected_features.detach().cpu()
                    selected_class = labels[indices]
                    vis_features.extend(list(selected_features))
                    vis_class.extend(list(selected_class))
                    
                triplet_loss.backward()
                self.optimizier.step()
                step += 1
                step_ += 1
            
            self.scheduler.step()

            # Visualization
            print("Visualizing ...")
            vis_features = torch.stack(vis_features)
            vis_class = torch.stack(vis_class) 
            vis = self.tsne.fit_transform(vis_features.numpy())
            plt.clf()
            plt.scatter(vis[:, 0], vis[:, 1], c=vis_class)
            image_path = os.path.join(self.output_folder, "temp.png")
            plt.savefig(image_path)
            image_rgba = plt.imread(image_path)
            image_rgb = Image.fromarray((image_rgba * 255).astype(np.uint8)).convert('RGB')
            transform = transforms.ToTensor()
            image_tensor = transform(image_rgb)
            self.writer.add_image("projected embedding", image_tensor, global_step=epoch)

            # Save model
            if epoch+1 % self.save_interval == 0:
                self.save_model(epoch+1)
            

    def save_model(self, epoch):
        output_path = os.path.join(self.output_folder, f"epoch_{epoch}.pt")
        print(f"Saving model to {output_path}")
        torch.save(self.model.state_dict(), output_path)

def main(config):
    trainer = Trainer(config=config)
    trainer.train()

if __name__ == '__main__':
    file_path = "C:\\Users\\david\\git\\microbat\\microbat\\Python_Server\\Models\\configs\\encoder_config.yaml"

    # Open the YAML file
    with open(file_path, "r") as yaml_file:
        # Load the contents of the YAML file
        config = yaml.safe_load(yaml_file)

    print("Configuration ------------------")
    for key, value in config.items():
        print(key, value)
    print("-" * 20)
    main(config)