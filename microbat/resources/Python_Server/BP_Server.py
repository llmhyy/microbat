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
        
    def func(self, sock):

        graph_str = self.recvGraphMsg(sock)
        self.checkDuplicateVar(graph_str)
        factorGraph = string2factor_graph(graph_str)

        factor_messages = self.recvFactorMsg(sock)
        
        predIDs_all = set()
        for constrain_id, var_ids, probs in factor_messages:
            predIDs_all.update(var_ids)
            factorGraph.change_factor_distribution(constrain_id, factor(var_ids, probs))

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
    
    def recvGraphMsg(self, sock):
        print("Start receive graph message ...")
        graph_str = ""
        while self.should_continoue(sock):
            message = self.recvMsg(sock)
            graph_str += message
        print("Finish receive graph message ...")
        return graph_str
    
    def recvFactorMsg(self, sock):
        print("Start factor messages ....")
        factor_messages = []
        while self.should_continoue(sock):
            message = self.recvMsg(sock)
            node_order, constraint_id, var_ids_str, probs_str = self.splitFactorMsg(message)
            var_ids = var_ids_str.split(self.DILIMITER_1)
            var_count = len(var_ids)
            probs = self.gen_probs_array(probs_str, var_count)
            factor_messages.append((constraint_id, var_ids, probs))
        print("Finish receive factor message ...")
        return factor_messages
    
    def gen_probs_array(self, probs_str, pred_count):
        shape = tuple([2 for _ in range(pred_count)])
        prob_tokens = probs_str.split(self.DILIMITER_1)
        probs = []
        for prob_token in prob_tokens:
            prob_str, count = prob_token.split(self.MUL_SIGN)
            count = int(count)
            prob = float(prob_str)
            for _ in range(count):
                probs.append(prob)
        probs = np.array(probs).reshape(shape)
        return probs
    
    def splitFactorMsg(self, factor_message):
        node_order, constraint_id, var_ids_str, probs_str = factor_message.split(self.DILIMITER_2)
        return node_order, constraint_id, var_ids_str, probs_str
    
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

if __name__ == '__main__':
    host = "127.0.0.1"
    port = 8080
    server = BP_Server(host, port)
    print("Server start ...")
    server.start()
    print("Server end")