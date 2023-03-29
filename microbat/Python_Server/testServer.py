from SPP.testServer import TestServer

if __name__ == "__main__":
    HOST = "127.0.0.1"
    PORT = 8080
    server = TestServer(HOST, PORT)
    server.start()