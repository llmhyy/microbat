package microbat.pyserver;

public class BackwardModelTrainClient extends RLModelTrainClient {
	
	protected final static String defaultHost = "127.0.0.3";
	protected final static int  defaultPort = 8083;
	
	public BackwardModelTrainClient() {
		this(false);
	}
	
	public BackwardModelTrainClient(boolean verbose) {
		this(BackwardModelTrainClient.defaultHost, BackwardModelTrainClient.defaultPort, verbose);
	}
	
	public BackwardModelTrainClient(String host, int port) {
		this(host, port, false);
	}
	
	public BackwardModelTrainClient(String host, int port, boolean verbose) {
		super(host, port, verbose);
	}

}
