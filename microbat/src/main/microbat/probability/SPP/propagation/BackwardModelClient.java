package microbat.probability.SPP.propagation;

import java.io.IOException;

import microbat.model.value.VarValue;
import microbat.probability.SPP.vectorization.vector.VariableVectorC;

public class BackwardModelClient extends RLModelClient {
	
	public BackwardModelClient() {
		super("127.0.0.3", 8083, false);
	}
	
	public BackwardModelClient(String host, int port) {
		super(host, port);
	}
	
	public BackwardModelClient(String host, int port, boolean verbose) {
		super(host, port, verbose);
	}
	
	public void sendVarFeature(final VarValue var) throws IOException, InterruptedException {
		final VariableVectorC vector = new VariableVectorC(var);
		this.sendMsg(vector.toString());
	}

}
