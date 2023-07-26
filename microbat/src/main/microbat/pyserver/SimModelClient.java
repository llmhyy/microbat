package microbat.pyserver;

public class SimModelClient extends ModelClient {

	protected final static String defaultHost = "172.26.191.20";
	protected final static int defaultPort = 8084;
	
	public SimModelClient() {
		this(SimModelClient.defaultHost, SimModelClient.defaultPort, false);
	}
	
	public SimModelClient(boolean verbose) {
		this(SimModelClient.defaultHost, SimModelClient.defaultPort, verbose);
	}
	
	public SimModelClient(String host, int port) {
		this(host, port, false);
	}
	 
	public SimModelClient(String host, int post, boolean verbose) {
		super(host, post, verbose);
	}

}
