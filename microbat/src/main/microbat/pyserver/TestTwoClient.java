package microbat.pyserver;

import java.io.IOException;
import java.util.Scanner;

public class TestTwoClient {
	
	public static void main(String[] args) {
    	final String host_1 = "127.0.0.2";
    	final int port_1 = 8082;
    	TestClient client1 = new TestClient(host_1, port_1, true);
    	final String host_2 = "127.0.0.3";
    	final int port_2 = 8083;
    	TestClient client2 = new TestClient(host_2, port_2, true);
    	
    	Scanner scanner = new Scanner(System.in);
    	
    	try {
    		client1.conntectServer();
    		client2.conntectServer();
    		
    		boolean isEnd = false;
    		while (!isEnd) {
    			System.out.println("Please select \n 1 for server 1 \n 2 for server 2 \n 3 for ending server");
    			final String userInput = scanner.nextLine();
    			if (userInput.equals("1")) {
    				System.out.println("Please input the message: \n");
    				final String message = scanner.nextLine();
    				client1.sendMsg(message);
    				client1.receiveMsg();
    			} else if (userInput.equals("2")) {
    				System.out.println("Please input the message: \n");
    				final String message = scanner.nextLine();
    	    		client2.sendMsg(message);
    	    		client2.receiveMsg();
    			} else if (userInput.equals("3")) {
		    		client2.endServer();
		    		client1.endServer();
		    		client2.disconnectServer();
		    		client1.disconnectServer();
		    		isEnd = true;
    			} else {
    				System.out.println("Invalid input. Please select again");
    			}
    		}
    		
    		scanner.close();

		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
