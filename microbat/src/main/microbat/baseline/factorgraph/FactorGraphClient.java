package microbat.baseline.factorgraph;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * FactorGraphClient is used to communicate with the python server.
 * 
 * @author David
 *
 */
public class FactorGraphClient {
	
	/**
	 * Host of the server
	 */
	private final String HOST;
	
	/**
	 * Port of connection
	 */
	private final int PORT;
	
	/**
	 * Message to terminate the server
	 */
	private final String END_MSG;
	
	/**
	 * Sleep time between sending message
	 */
	private final int SLEEP_TIME;
	
	/**
	 * Buffer size of sending message
	 */
	private final int BUFFER_SIZE;
	
	/**
	 * Encoding method for string
	 */
	private final Charset charsets;
	
	private Socket socket;
	private DataInputStream reader;
	private DataOutputStream writer;
	
	public FactorGraphClient() {
		// Default host and port
		this("127.0.0.1", 8080);
	}
	
	public FactorGraphClient(final String host, final int port) {
		this.HOST = host;
		this.PORT = port;
		this.END_MSG = "END";
		this.SLEEP_TIME = 50;
		this.BUFFER_SIZE = (int) Math.pow(2, 20);
		this.charsets = StandardCharsets.UTF_8;
	}
	
	/**
	 * Connect to the server
	 */
	public void conntectServer() {
		try {
			this.socket = new Socket(this.HOST, this.PORT);
			this.reader = new DataInputStream(this.socket.getInputStream());
			this.writer = new DataOutputStream(this.socket.getOutputStream());
		} catch (UnknownHostException e) {
			this.socket = null;
			this.reader = null;
			this.writer = null;
			System.out.println("Error: UnknowHostException encountered");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Request the python to run belief propagation algorithm
	 * to calculate the marginal probability
	 * 
	 * @param graphStruct Graph structure message
	 * @param factors Factor message
	 * @return String response from the server
	 * @throws Exception Throw when server does not response or have wrong response
	 */
	public String requestBP(final String graphStruct, final String factors) {
		byte[] graphInput = this.strToByte(graphStruct);
		byte[] factorInput = this.strToByte(factors);
		
		byte[] response = this.request(graphInput, factorInput);
		String responst_str = this.byteToStr(response);
		
		return responst_str;
	}
	
	/**
	 * Disconnect the server
	 */
	public void disconnectServer() {
		try {
			this.reader.close();
			this.writer.close();
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			this.reader = null;
			this.writer = null;
			this.socket = null;
		}
	}
	
	/**
	 * Terminate the server
	 * @return True when successfully end the server
	 * @throws Exception
	 */
	public boolean endServer() throws Exception {
		byte[] ending_msg = this.strToByte(END_MSG);
		byte[] response = this.request(ending_msg, ending_msg);
		
		String response_str = new String(response, this.charsets);
		return response_str == this.END_MSG;
	}
	
	/**
	 * Request the response from server
	 * @param graphInput Graph structure message encoded in byte
	 * @param factorInput Factor message encoded in byte
	 * @return Response from server in byte
	 * @throws Exception When failed to connect the server
	 */
	private byte[] request(byte[] graphInput, byte[] factorInput) {
		if (this.isReady()) {
			try {
				System.out.println("Client: graphInput size: " + graphInput.length + " factorInput size: " + factorInput.length);
				
				/*
				 * Send the graph structure message and factor message
				 * separately so that it has lower chance to exceed
				 * the buffer size
				 */
				
				this.writer.write(graphInput);
				
				// Sleep for a while to ensure the next message can be send properly.
				Thread.sleep(this.SLEEP_TIME);
				
				this.writer.write(factorInput);
				
				byte[] response = new byte[this.BUFFER_SIZE];
				int integer = this.reader.read(response);
				if (integer == -1) {
					throw new RuntimeException("No response from server");
				}
				
				return response;
			} catch (IOException e) {
				System.out.println("Error: Fail to send or receive data from model server.");
				e.printStackTrace();
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			throw new RuntimeException("Socket is not ready");
		}
		return null;
	}
	
	private byte[] strToByte(final String str) {
		return str.getBytes(this.charsets);
	}
	
	private String byteToStr(final byte[] bytes) {
		return new String(bytes, this.charsets);
	}
	
	private boolean isReady() {
		return this.socket != null && this.writer != null && this.reader != null;
	}
}
