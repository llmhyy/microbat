from servers.SocketServer import SocketServer

class TestServer(SocketServer):

    def __init__(self, host, port, verbose=False):
        super().__init__(host, port, verbose)
    
    def func(self):
        print("Test Server start working ...")
        while True:
            msg = self.recvMsg()
            print(msg)
            self.sendMsg(msg)

            if self.isEndServerMsg(msg):
                self.endServer()
                break

if __name__ == '__main__':
    host = "127.0.0.1"
    port = 8080
    server = TestServer(host, port, True)
    print("Server start ...")
    server.start()
    print("Server end")