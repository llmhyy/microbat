package microbat.pyserver;

public class ForwardModelClient extends RLModelClient {
	
	protected final static String defaultHost = "127.0.0.4";
	protected final static int defaultPort = 8084;
	
	public ForwardModelClient() {
		this(ForwardModelClient.defaultHost, ForwardModelClient.defaultPort, false);
	}
	
	public ForwardModelClient(boolean verbose) {
		this(ForwardModelClient.defaultHost, ForwardModelClient.defaultPort, verbose);
	}
	
	public ForwardModelClient(String host, int port) {
		this(host, port, false);
	}
	
	public ForwardModelClient(String host, int port, boolean verbose) {
		super(host, port, verbose);
	}
	
}
