package microbat.autofeedback;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * Model Client is used to communicate with the pre-trained model in the Python server using Socket.
 * Model Client will send target step vector and reference step vector to the server and the server
 * will return a byte representing true or false.
 * @author David
 *
 */
public class ModelClient {
	
	/**
	 * Default Host address
	 */
	private final static String HOST = "127.0.0.1";
	/**
	 * Default port number
	 */
	private final static int PORT = 65432;
	/**
	 * Ending message to stop the server
	 */
	private final static String END = "END";
	/**
	 * Default sleeping time
	 */
	private final static int SLEEP_TIME = 500;
	
	private Socket socket;
	private DataInputStream reader;
	private DataOutputStream writer;
	
	public ModelClient() {
		this(ModelClient.HOST, ModelClient.PORT);
	}
	
	public ModelClient(String HOST, int PORT) {
		try {
			this.socket = new Socket(HOST, PORT);
			this.reader = new DataInputStream(socket.getInputStream());
			this.writer = new DataOutputStream(socket.getOutputStream());
		} catch (UnknownHostException e) {
			System.out.println("Error: UnknowHostException encountered");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Classify the target vector to be correct or wrong
	 * @param input_vec Traget step vector
	 * @param ref_vec Refeerence step vector
	 * @return 1 if server return Correct or else return 0. If error occur, -1 will be returned
	 */
	public int requestClassification(String input_vec, String ref_vec) {
		byte[] input_bytes = input_vec.getBytes(StandardCharsets.UTF_8);
		byte[] ref_bytes = ref_vec.getBytes(StandardCharsets.UTF_8);
		return this.request(input_bytes, ref_bytes);
	}
	
	/**
	 * End the server
	 */
	public void endServer() {
		byte[] ending_message = ModelClient.END.getBytes(StandardCharsets.UTF_8);
		this.request(ending_message, ending_message);
	}
	
	/**
	 * Send two given input to the server and return the response
	 * @param input1 The first bytes input
	 * @param input2 The second bytes input
	 * @return 1 if server return True or else 0. If error occur, then -1 will be return.
	 */
	private int request(byte[] input1, byte[] input2) {
		try {
			this.writer.write(input1);
			Thread.sleep(ModelClient.SLEEP_TIME); // Sleep for a while to ensure two input is properly send
			this.writer.write(input2);
			return this.reader.readBoolean() ? 1 : 0;
		} catch (IOException e) {
			System.out.println("Error: Fail to send or receive data from model server.");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Faile to sleep");
			e.printStackTrace();
		}
		return -1;
	}
}
