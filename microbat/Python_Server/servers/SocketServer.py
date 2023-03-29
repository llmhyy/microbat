import socket
from abc import ABC, abstractmethod

class SocketServer(ABC):

    def __init__(self, host:str, port:str):
        self._host = host
        self._port = port
        
        # Encoding method
        self.ENCODING_METHOD = "UTF-8"
        
        # Buffer size for the message
        self.BUFFER_SIZE = pow(2, 20)
        
        self.SERVER_END = "SERVER_END"
        self.MSG_END = "MSG_END"

        self.DILIMITER_1 = ','
        self.DILIMITER_2 = "&"
        
        self._stillWorking = True
        self._conn = None
        self._addr = None

    def recvMsg(self):
        if self._conn is None:
            raise Exception("The server is not connected")
        
        res = ""
        while True:
            message = self._conn.recv(self.BUFFER_SIZE)
            if not message:
                return None
            
            message_str = self.decode(message)
            if message_str == self.MSG_END:
                break
            else:
                res += message_str
        return res

    def sendMsg(self, message):
        if self._conn is None:
            return
        message_str = self.encode(message)
        print("response:", message_str)
        self._conn.sendall(message_str)

    def encode(self, message):
        return message.encode(self.ENCODING_METHOD)
    
    def decode(self, message):
        return message.decode(self.ENCODING_METHOD)
    
    def endServer(self):
        """
        End the server and send a msg to client telling the server is ended
        """
        self._stillWorking = False
        self.sendMsg(self.SERVER_END)

    def isEndServerMsg(self, msg):
        return msg == self.SERVER_END
    
    def start(self):
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.bind((self._host, self._port))
            
            while self._stillWorking:
                print("Waiting for client ...")
                s.listen()
                self._conn, self._addr = s.accept()
                
                with self._conn:
                    print(f"Connected by {self._addr}")
                    self.func()

    @abstractmethod
    def func(self):
        raise NotImplementedError()