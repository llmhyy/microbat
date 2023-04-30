from servers import SocketServer

class BP_Server(SocketServer):
    def __init__(self, host, port):
        super().__init__(host, port)