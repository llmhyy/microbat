from servers.SocketServer import SocketServer

class TestServer(SocketServer):

    def __init__(self, host, port):
        super(TestServer, self).__init__(host, port)
    
    def func(self):
        print("Test Server start working ...")
        while True:
            msg = self.recvMsg()
            msg = "Server recieved: "+msg
            self.sendMsg(msg)

            if self.isEndServerMsg(msg):
                self.endServer()
                break

            