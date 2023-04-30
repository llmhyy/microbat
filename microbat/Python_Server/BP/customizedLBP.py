from .factor import *
from .factor_graph import *

STATEMENT_ID_PREFIX = "S_"

class myLBP():
    def __init__(self, pgm):
        if type(pgm) is not factor_graph:
            raise Exception('PGM is not a factor graph')
        if not pgm.is_connected():
            print("[Warning] graph is not connected")
        
        self.__t       = 0
        self.__msg     = {}
        self.__msg_new = {}
        self.__pgm     = pgm
        self.threshold = 1e-4
        
        # Initialization of messages
        # Set all the message to one
        for edge in self.__pgm.get_graph().es:
            start_index, end_index = edge.tuple[0], edge.tuple[1]
            start_name, end_name = self.__pgm.get_graph().vs[start_index]['name'], self.__pgm.get_graph().vs[end_index]['name']
            
            if self.__pgm.get_graph().vs[start_index]['is_factor']:
                self.__msg[(start_name, end_name)] = factor([end_name],   np.array([1.]*self.__pgm.get_graph().vs[end_index]['rank']))
            else:
                self.__msg[(start_name, end_name)] = factor([start_name], np.array([1.]*self.__pgm.get_graph().vs[start_index]['rank']))
            self.__msg[(end_name, start_name)] = self.__msg[(start_name, end_name)]
            
            self.__msg_new[(start_name, end_name)] = 0
            self.__msg_new[(end_name, start_name)] = 0
    
    # Get marginal propability of target variables
    def belief(self, v_names, num_iter):
        if self.__t > num_iter:
            raise Exception('Invalid number of iterations. Current number: ' + str(self.__t))
        elif self.__t < num_iter:
            self.__loop(num_iter)
        
        margProb = {}
        for v_name in v_names:
            incoming_messages = []
            for f_name_neighbor in self.__pgm.get_graph().vs[self.__pgm.get_graph().neighbors(v_name)]['name']:
                incoming_messages.append(self.get_factor2variable_msg(f_name_neighbor, v_name))
        
            prob = self.__normalize_msg(joint_distribution(incoming_messages))
            margProb[v_name] = prob.get_distribution()[1]

        return margProb
    
    # ----------------------- Variable to factor ------------
    def get_variable2factor_msg(self, v_name, f_name):
        return self.__msg[(v_name, f_name)]
    
    def __compute_variable2factor_msg(self, v_name, f_name):
        incoming_messages = []
        for f_name_neighbor in self.__pgm.get_graph().vs[self.__pgm.get_graph().neighbors(v_name)]['name']:
            if f_name_neighbor != f_name:
                incoming_messages.append(self.get_factor2variable_msg(f_name_neighbor, v_name))
        
        if not incoming_messages:
            return factor([v_name], np.array([1]*self.__pgm.get_graph().vs.find(name=v_name)['rank']))
        else:
            return self.__normalize_msg(joint_distribution(incoming_messages))
    
    # ----------------------- Factor to variable ------------
    def get_factor2variable_msg(self, f_name, v_name):
        return self.__msg[(f_name, v_name)]
    
    def __compute_factor2variable_msg(self, f_name, v_name):
        incoming_messages = [self.__pgm.get_graph().vs.find(f_name)['factor_']]
        marginalization_variables = []
        for v_name_neighbor in self.__pgm.get_graph().vs[self.__pgm.get_graph().neighbors(f_name)]['name']:
            if v_name_neighbor != v_name:
                incoming_messages.append(self.get_variable2factor_msg(v_name_neighbor, f_name))
                marginalization_variables.append(v_name_neighbor)
        return self.__normalize_msg(factor_marginalization(
            joint_distribution(incoming_messages),
            marginalization_variables
        ))
    
    # ----------------------- Other -------------------------
    def __loop(self, num_iter):
        # Message updating
        isConverge = False
        while self.__t < num_iter and not isConverge:
            for edge in self.__pgm.get_graph().es:
                start_index, end_index = edge.tuple[0], edge.tuple[1]
                start_name, end_name   = self.__pgm.get_graph().vs[start_index]['name'], self.__pgm.get_graph().vs[end_index]['name']
                if self.__pgm.get_graph().vs[start_index]['is_factor']:
                    self.__msg_new[(start_name, end_name)] = self.__compute_factor2variable_msg(start_name, end_name) if not str(start_name).startswith(STATEMENT_ID_PREFIX) else factor([start_name], np.array([0.5, 0.5]))
                    self.__msg_new[(end_name, start_name)] = self.__compute_variable2factor_msg(end_name, start_name)
                else:
                    self.__msg_new[(start_name, end_name)] = self.__compute_variable2factor_msg(start_name, end_name) if not str(start_name).startswith(STATEMENT_ID_PREFIX) else factor([start_name], np.array([0.5, 0.5]))
                    self.__msg_new[(end_name, start_name)] = self.__compute_factor2variable_msg(end_name, start_name)
            converge = True
            for (start_name, end_name), new_msg in self.__msg_new.items():
                old_msg = self.__msg[(start_name, end_name)]
                if (abs(old_msg.get_distribution() - new_msg.get_distribution()) > self.threshold).sum() == old_msg.get_distribution().size:
                    converge = False
            
            isConverge = converge
            
            self.__msg.update(self.__msg_new)
            self.__t += 1
    
    def __normalize_msg(self, message):
        return factor(message.get_variables(), message.get_distribution()/np.sum(message.get_distribution()))




