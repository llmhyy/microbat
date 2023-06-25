from TestServer import TestServer

def main():
    host, port = "127.0.0.3", 8083
    server = TestServer(host, port, True)
    print("Server start ...")
    server.start()
    print("Server end")

if __name__ == '__main__':
    main()

