package microbat.pyserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class Client {
	
	/**
	 * Message to terminate the server
	 */
	protected static final String END_MSG = "END";
	
	/**
	 * MSG_BREAK appear at the end of each message
	 */
	protected static final String BREAK_MSG = "BREAK";
	
	/**
	 * Sleep time between sending message
	 */
	protected static final int SLEEP_TIME = 25;
	
	/**
	 * Buffer size of sending and receiving message
	 */
	protected static final int BUFFER_SIZE = (int) Math.pow(2, 20);
	
	/**
	 * Encoding method for string
	 */
	protected static final Charset charset = StandardCharsets.UTF_8;
	
	/**
	 * Host of the server
	 */
	protected final String HOST;
	
	/**
	 * Port of connection
	 */
	protected final int PORT;
	
	protected Socket socket;
	private DataInputStream reader;
	private DataOutputStream writer;
	
	public Client(final String host, final int port) {
		this.HOST = host;
		this.PORT = port;
	}
	
	/**
	 * Connect to the server
	 */
	public void conntectServer() throws UnknownHostException, IOException {
		this.socket = new Socket(this.HOST, this.PORT);
		this.reader = new DataInputStream(this.socket.getInputStream());
		this.writer = new DataOutputStream(this.socket.getOutputStream());
	}
	
	/**
	 * Disconnect the server
	 */
	public void disconnectServer() throws IOException {
		this.reader.close();
		this.writer.close();
		this.socket.close();
	}
	
	/**
	 * Terminate the server
	 * @return True when successfully end the server
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws Exception
	 */
	public boolean endServer() throws IOException, InterruptedException {
		byte[] ending_msg = this.strToByte(END_MSG);
		this.sendMsg(ending_msg);
		String response = this.byteToStr(this.receiveMsg());
		return response == Client.END_MSG;
	}
	
	/**
	 * Send message to server
	 * @param messages Messages to send
	 */
	protected void sendMsg(byte[]... messages) throws IOException, InterruptedException {
		if (this.isReady()) {
			final byte[] msgBreak = this.strToByte(Client.BREAK_MSG);
			for (byte[] message : messages) {
				if (message.length > Client.BUFFER_SIZE) {
					throw new RuntimeException("Message exceed maximum buffer size");
				}
				System.out.println("Message size: " + message.length);
				this.writer.write(message);
//				Thread.sleep(Client.SLEEP_TIME);
//				this.writer.write(msgBreak);
			}
		} else {
			throw new RuntimeException("Socket is not ready");
		}
	}
	
	protected void sendMsg(final String message) throws IOException, InterruptedException {
		final byte[] message_byte = this.strToByte(message);
		this.sendMsg(message_byte);
	}
	
	/**
	 * Receive message from server
	 * @return Message from server
	 */
	protected byte[] receiveMsg() throws IOException {
		byte[] response = new byte[Client.BUFFER_SIZE];
		int integer = this.reader.read(response);
		if (integer == -1) {
			throw new RuntimeException("No response from server");
		}
		return response;
	}
	
	protected String receiveStrMsg() throws IOException {
		byte[] message_byte = this.receiveMsg();
		return this.byteToStr(message_byte);
	}
	
	protected byte[] strToByte(final String str) {
		return str.getBytes(Client.charset);
	}
	
	protected String byteToStr(final byte[] bytes) {
		return new String(bytes, Client.charset);
	}
	
	protected boolean isReady() {
		return this.socket != null && this.writer != null && this.reader != null;
	}
}
