package microbat.pyserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public abstract class Client {
	
	/**
	 * Message to terminate the server
	 */
	protected static final String END_SERVER_MSG_STR = "END_SERVER";
	
	/**
	 * MSG_BREAK appear at the end of each message
	 */
	protected static final String END_MSG_STR = "MSG_END";
	
	protected final byte[] END_SERVER_MSG;
	protected final byte[] END_MSG;
	
	
	/**
	 * Sleep time between sending message
	 */
	protected static final int SLEEP_TIME = 25;
	
	/**
	 * Buffer size of sending and receiving message
	 */
	protected static final int CHUNK_SIZE = 1024;
	
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
	
	protected boolean verbose = false;
	
	private final byte paddingByte = 0;
	
	
	protected Socket socket;
	private InputStream reader;
	private OutputStream writer;
	
	public Client(final String host, final int port) {
		this.HOST = host;
		this.PORT = port;
		
		byte[] end_server = this.strToByte(Client.END_SERVER_MSG_STR);
		end_server = Arrays.copyOfRange(end_server, 0, Client.CHUNK_SIZE);
		this.END_SERVER_MSG = end_server;
		
		byte[] end_message = this.strToByte(Client.END_MSG_STR);
		end_message = Arrays.copyOfRange(end_message, 0, Client.CHUNK_SIZE);
		this.END_MSG = end_message;
	}
	
	public Client(final String host, final int port, boolean verbose) {
		this(host, port);
		this.verbose = verbose;
	}
	
	/**
	 * Connect to the server
	 */
	public void conntectServer() throws UnknownHostException, IOException {
		this.socket = new Socket(this.HOST, this.PORT);
		this.reader = this.socket.getInputStream();
		this.writer = this.socket.getOutputStream();
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
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws Exception
	 */
	public void endServer() throws IOException, InterruptedException {

		this.sendMsg(this.END_MSG);
//		String response = this.byteToStr(this.receiveMsg());
//		return response == Client.END_SERVER_MSG;
	}
	
	/**
	 * Send message to server
	 * @param message Messages to send
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void sendMsg(byte[] message) throws IOException, InterruptedException {
		if (this.isReady()) {
			int start = 0;
			while (start < message.length) {
				int end = start + Client.CHUNK_SIZE;
				byte[] chunk_of_message = Arrays.copyOfRange(message, start, end);
				this.writer.write(chunk_of_message);
				start = end;
 			}
//			int offset = 0;
//			while (offset < message.length) {
//				int length = Math.min(Client.CHUNK_SIZE, message.length - offset);
//				this.writer.write(message, offset, length);
//				offset += length;
//			}
			this.writer.write(this.END_MSG);
		} else {
			throw new RuntimeException("Socket is not ready");
		}
	}
	

//	/**
//	 * Send message to server
//	 * @param messages Messages to send
//	 */
//	protected void sendMsg(byte[]... messages) throws IOException, InterruptedException {
//		if (this.isReady()) {
//			final byte[] msgBreak = this.strToByte(Client.END_MSG);
//			for (byte[] message : messages) {
//				if (message.length > Client.CHUNK_SIZE) {
//					throw new RuntimeException("Message exceed maximum buffer size");
//				}
//				System.out.println("Message size: " + message.length);
//				this.writer.write(message);
//			}
//			this.writer.write(msgBreak);
//		} else {
//			throw new RuntimeException("Socket is not ready");
//		}
//	}
	
	public void sendMsg(final String message) throws IOException, InterruptedException {
		if (this.verbose) {
			System.out.println("[SEND_MESSAGE] " + message);
		}
		final byte[] message_byte = this.strToByte(message);
		this.sendMsg(message_byte);
	}
	
	/**
	 * Receive message from server
	 * @return Message from server
	 */
	public String receiveMsg() throws IOException {
		StringBuilder strBuilder = new StringBuilder();
		byte[] message;
		while (true) {
			message = new byte[Client.CHUNK_SIZE];
			int bytesRead = this.reader.read(message);
			if (bytesRead == -1) {
				break;
			}
			String strMsg = this.byteToStr(message);
			if (strMsg.equals(Client.END_MSG_STR)) {
				break;
			}
			strBuilder.append(strMsg);
		}
		if (this.verbose) {
			System.out.println("[RECEIVE MESSAGE]: " + strBuilder.toString());
		}
		return strBuilder.toString();
//		byte[] response = new byte[Client.CHUNK_SIZE];
//		int integer = this.reader.read(response);
//		if (integer == -1) {
//			throw new RuntimeException("No response from server");
//		}
//		return response;
	}
	
//	protected String receiveStrMsg() throws IOException {
//		byte[] message_byte = this.receiveMsg();
//		return this.byteToStr(message_byte);
//	}
	
	protected byte[] strToByte(final String str) {
		return str.getBytes(Client.charset);
	}
	
	protected String byteToStr(final byte[] bytes) {
		String string = new String(bytes,Client.charset);
		string = string.trim();
		return string;
	}
	
	protected boolean isReady() {
		return this.socket != null && this.writer != null && this.reader != null;
	}
}
