import torch
from gensim.models import FastText
from gensim.models.fasttext import load_facebook_vectors

def main():

    # Load pre-trained FastText model
    path = "C:\\Users\\david\\Documents\\fasttext\\wiki.en.bin"
    wv = load_facebook_vectors(path)

    # Example usage
    word = "learning"
    embedding = wv[word]
    print(embedding)



    
if __name__ == '__main__':
    main()