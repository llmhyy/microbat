from servers.SocketServer import SocketServer
from BP.factor_graph import *
from BP.customizedLBP import *

import time

class BP_Server(SocketServer):
    def __init__(self, host, port, verbose=False):
        super().__init__(host, port, verbose)
        self.DILIMITER_1 = ','
        self.DILIMITER_2 = "&"
        self.MUL_SIGN = "*"
        self.MAX_ITR = 5

    def factorLoader(self, factor_input, dilimiter):
        tokens = factor_input.split(dilimiter)
        for idx in range(0, len(tokens), 4):
            yield tokens[idx], tokens[idx+1], tokens[idx+2], tokens[idx+3]

    def checkDuplicateVar(self, str_):
        str_tokens = [i.split('(') for i in str_.split(')') if i != '']
        for token in str_tokens:
            vars = token[1].split(',')
            if len(vars) != len(set(vars)):
                print("contain duplicate variables: ", token[0])
                print(vars)
                raise ValueError('Duplicated variables: ' + token[0])
            
    def func(self, sock):

        graph_input = self.recvMsg(sock)
        if graph_input == "":
            return False
        if self.isEndServerMsg(graph_input):
            self.endServer()
            return False

        factor_input = self.recvMsg(sock)
        if self.isEndServerMsg(factor_input):
            self.endServer()
            return False

        self.checkDuplicateVar(graph_input)
        
        factorGraph = string2factor_graph(graph_input)
        predIDs_all = set()

        for order, constraintID, predIDs_str, probs_str in self.factorLoader(factor_input, self.DILIMITER_2):

            predIDs = predIDs_str.split(self.DILIMITER_1)

            predIDs_all.update(predIDs)
            predCount = len(predIDs)

            shape = [2 for _ in range(predCount)]
            shape = tuple(shape)

            probs_tokens = probs_str.split(self.DILIMITER_1)

            probs = []
            for probs_token in probs_tokens:
                probs_str, count = probs_token.split(self.MUL_SIGN)
                count = int(count)
                prob = float(probs_str)
                for _ in range(count):
                    probs.append(prob)

            probs = np.array(probs)
            probs = probs.reshape(shape)

            factorGraph.change_factor_distribution(constraintID, factor(predIDs,  probs))

        lbp = myLBP(factorGraph, verbose=True)

        start = time.time()
        margProb = lbp.belief(predIDs_all, self.MAX_ITR)
        end = time.time()

        print("Time: ", end-start)
        output_str = ""
        for predID, prob in margProb.items():
            output_str += predID + self.DILIMITER_1 + str(prob)
            output_str += self.DILIMITER_2
        output_str = output_str[:-1]
        self.sendMsg(sock, output_str)
        return True

if __name__ == '__main__':
    host = "127.0.0.1"
    port = 8080
    server = BP_Server(host, port, verbose=True)
    print("Server start ...")
    server.start()
    print("Server end")