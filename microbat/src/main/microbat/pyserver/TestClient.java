package microbat.pyserver;

import java.io.IOException;

public class TestClient extends Client {

	public TestClient(String host, int port) {
		super(host, port);
	}
	
	public TestClient(String host, int port, boolean verbose) {
		super(host, port, verbose);
	}
	
	public static void main(String[] args) {
    	final String host = "127.0.0.1";
    	final int port = 8080;
    	TestClient client = new TestClient(host, port, true);
    	
    	String message = "message from test client";
    	try {
			client.conntectServer();
			client.sendMsg(message);

			String recievedMsg = client.receiveMsg();
			System.out.println("Message from server: " +  recievedMsg);
			client.endServer();
			client.disconnectServer();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	
	}

}
