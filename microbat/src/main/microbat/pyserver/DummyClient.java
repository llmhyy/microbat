package microbat.pyserver;

public class DummyClient extends ModelClient {

	public DummyClient(String host, int port) {
		super(host, port);
	}
	
	public DummyClient(String host, int port, boolean verbose) {
		super(host, port, verbose);
	}
	
}
