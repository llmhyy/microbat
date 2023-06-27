package microbat.probability.SPP.propagation;

public class ForwardModelClient extends RLModelClient {
	
	public ForwardModelClient() {
		super("127.0.0.2", 8082, false);
	}
	
	public ForwardModelClient(String host, int port) {
		super(host, port);
	}
	
	public ForwardModelClient(String host, int port, boolean verbose) {
		super(host, port, verbose);
	}

}
