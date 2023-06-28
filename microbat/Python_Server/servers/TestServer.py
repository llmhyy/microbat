from SocketServer import SocketServer

class TestServer(SocketServer):
    
    def __init__(self, host, port, verbose=False):
        super().__init__(host, port, verbose)

    def func(self, sock):
        print(f"Testing server {self._host}: {self._port} ...")
        while True:
            msg = self.recvMsg(sock)
            print(msg)
            msg = f"{msg} generated from {self._host} : {self._port}"
            self.sendMsg(sock, msg)

            if self.isEndServerMsg(msg):
                print("Recieved ending server message")
                self.endServer()
                break