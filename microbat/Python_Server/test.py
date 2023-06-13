import torch
import os
import math

def main():

    path = "E:\david\FeatureDataset"
    filepaths = []
    for root, _, files in os.walk(path):
        for file_name in files:
            file_path = os.path.join(root, file_name)
            filepaths.append(file_path)

    class_vector_dict = {}
    for filepath in filepaths:
        print(f"Loading data from {filepath}")
        with open(filepath, 'r') as file:
            for line in file:
                tokens = line.split(":")
                cls_vector = tokens[1]
                if cls_vector in class_vector_dict:
                    class_vector_dict[cls_vector]+=1
                else:
                    class_vector_dict[cls_vector] = 1
    
    print(class_vector_dict)


    
if __name__ == '__main__':
    main()