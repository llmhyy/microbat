import socket
import select

from abc import ABC, abstractmethod

class SocketServer(ABC):

    def __init__(self, host:str, port:str, verbose:bool = False):
        self._host = host
        self._port = port
        
        # Encoding method
        self.ENCODING_METHOD = "UTF-8"
        
        # Buffer size for the message
        self.BUFFER_SIZE = 1024
        
        self.SERVER_END = "END_SERVER"
        self.MSG_END = "MSG_END"
        self.MSG_RECIEVED = "MSG_RECEIVED"
        
        self._stillWorking = True

        self.verbose = verbose

    def recvMsg(self, sock):
        """
        Keep receiving message until MSG_END is detected
        """

        if sock is None:
            raise Exception("The server is not connected")
        
        message = ""
        while True:
            buffer = sock.recv(self.BUFFER_SIZE)
            if not buffer or self.decode(buffer) == self.MSG_END:
                break
            message += self.decode(buffer)

        if self.verbose and message != "":
            print(f"[RECIEVED MESSAGE]: {message}")

        return message

    def sendMsg(self, sock, message):
        """
        Send message by chunk
        """

        if sock is None:
            return
        
        start, end = 0, self.BUFFER_SIZE
        while True:
            chunk_of_message = self.encode(message[start:end])
            chunk_of_message = chunk_of_message.ljust(self.BUFFER_SIZE)
            sock.sendall(chunk_of_message)
            if end >= len(message):
                break
            start = end
            end += self.BUFFER_SIZE
        
        if self.verbose:
            print(f"[SEND MESSAGE]: " + message)
        # Send the ending message
        sock.sendall(self.encode(self.MSG_END))

    def encode(self, message):
        return message.encode(self.ENCODING_METHOD)
    
    def decode(self, message):
        return message.decode(self.ENCODING_METHOD).strip('\x00')
    
    def isEndServerMsg(self, message):
        return message == self.SERVER_END
    
    def endServer(self):
        """
        End the server and send a msg to client telling the server is ended
        """
        self._stillWorking = False
    
    def start(self):

        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_address = (self._host, self._port)
        server_socket.bind(server_address)
        server_socket.listen(5)

        input_sockets = [server_socket]

        while self._stillWorking:
            readable, _, _ = select.select(input_sockets, [], [])
            for sock in readable:
                if sock is server_socket:
                    client_socket, client_address = server_socket.accept()
                    input_sockets.append(client_socket)
                    print(f"New connection from {client_address}")
                else:
                    is_active = self.func(sock)
                    if not is_active:
                        input_sockets.remove(sock)
                        sock.close()

    @abstractmethod
    def func(self):
        raise NotImplementedError()