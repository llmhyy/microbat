package microbat.probability.SPP.propagation;

public class ForwardModelClient extends RLModelClient {
	
	protected final static String defaultHost = "127.0.0.2";
	protected final static int defautlPort = 8082;
	
	public ForwardModelClient() {
		this(ForwardModelClient.defaultHost, ForwardModelClient.defautlPort, false);
	}
	
	public ForwardModelClient(boolean verbose) {
		this(ForwardModelClient.defaultHost, ForwardModelClient.defautlPort, verbose);
	}
	
	public ForwardModelClient(String host, int port) {
		this(host, port, false);
	}
	
	public ForwardModelClient(String host, int port, boolean verbose) {
		super(host, port, verbose);
	}
	
}
