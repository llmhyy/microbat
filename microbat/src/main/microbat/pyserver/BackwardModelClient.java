package microbat.pyserver;


public class BackwardModelClient extends ModelClient {
	
	protected final static String defaultHost = "127.0.0.5";
	protected final static int  defaultPort = 8085;
		
	public BackwardModelClient() {
		this(BackwardModelClient.defaultHost, BackwardModelClient.defaultPort, false);
	}
	
	public BackwardModelClient(boolean verbose) {
		this(BackwardModelClient.defaultHost, BackwardModelClient.defaultPort, verbose);
	}
	
	public BackwardModelClient(String host, int port) {
		this(host, port, false);
	}
	
	public BackwardModelClient(String host, int port, boolean verbose) {
		super(host, port, verbose);
	}

}
