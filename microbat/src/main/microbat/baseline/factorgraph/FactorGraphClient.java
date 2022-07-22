package microbat.baseline.factorgraph;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FactorGraphClient {
	
	private final String HOST;
	
	private final int PORT;
	
	private final String END_MSG;
	
	private final int SLEEP_TIME;
	
	private final int BUFFER_SIZE;
	
	private final Charset charsets;
	
	private Socket socket;
	private DataInputStream reader;
	private DataOutputStream writer;
	
	public FactorGraphClient() {
		this("127.0.0.1", 8080);
	}
	
	public FactorGraphClient(final String host, final int port) {
		this.HOST = host;
		this.PORT = port;
		this.END_MSG = "END";
		this.SLEEP_TIME = 50;
		this.BUFFER_SIZE = (int) Math.pow(2, 20);
		this.charsets = StandardCharsets.UTF_8;
		
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
	
	public String requestBP(final String graphStruct, final String factors) throws Exception {
		byte[] graphInput = this.strToByte(graphStruct);
		byte[] factorInput = this.strToByte(factors);
		
		byte[] response = this.request(graphInput, factorInput);
		if (response == null) {
			throw new Exception("Server return null value");
		}
		
		String responst_str = this.byteToStr(response);
		if (this.isErrorResponse(responst_str)) {
			throw new Exception("Server return error response");
		}
		
		return responst_str;
	}
	
	public void disconnectServer() {
		try {
			this.reader.close();
			this.writer.close();
			this.socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean endServer() throws Exception {
		byte[] ending_msg = this.strToByte(END_MSG);
		byte[] response = this.request(ending_msg, ending_msg);
		
		String response_str = new String(response, this.charsets);
		return response_str == this.END_MSG;
	}
	
	private byte[] request(byte[] graphInput, byte[] factorInput) throws Exception {
		if (this.isReady()) {
			try {
				this.writer.write(graphInput);
				Thread.sleep(this.SLEEP_TIME);
				this.writer.write(factorInput);
				
				byte[] response = new byte[this.BUFFER_SIZE];
				this.reader.read(response);
				
				return response;
			} catch (IOException e) {
				System.out.println("Error: Fail to send or receive data from model server.");
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("Faile to sleep");
				e.printStackTrace();
			}
		} else {
			throw new Exception("Socket is not ready");
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
	
	private boolean isErrorResponse(final String response) {
		return response == null || response == "";
	}
}
