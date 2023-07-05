package microbat.probability.SPP.propagation;

import java.io.IOException;

import microbat.model.value.VarValue;
import microbat.probability.SPP.vectorization.vector.VariableVectorC;

public class BackwardModelClient extends RLModelClient {
	
	protected final static String defaultHost = "127.0.0.3";
	protected final static int  defaultPort = 8083;
		
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
