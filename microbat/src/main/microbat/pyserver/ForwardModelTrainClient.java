package microbat.pyserver;

public class ForwardModelTrainClient extends RLModelTrainClient {

	protected final static String defaultHost = "127.0.0.2";
	protected final static int defaultPort = 8082;
	
	public ForwardModelTrainClient() {
		this(false);
	}
	
	public ForwardModelTrainClient(boolean verbose) {
		this(ForwardModelTrainClient.defaultHost, ForwardModelTrainClient.defaultPort, verbose);
	}
	
	public ForwardModelTrainClient(String host, int port) {
		this(host, port, false);
	}
	
	public ForwardModelTrainClient(String host, int port, boolean verbose) {
		super(host, port, verbose);
	}

}
