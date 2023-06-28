package microbat.probability.SPP.propagation;

import java.io.IOException;

import microbat.model.trace.TraceNode;
import microbat.probability.SPP.vectorization.vector.NodeVector;
import microbat.pyserver.Client;

public abstract class RLModelClient extends Client {
	
	protected final String propEndMsg = "PROP_END";
	protected final String vectorEndMsg = "VECTOR_END";
	
	public RLModelClient(String host, int port) {
		super(host, port);
	}
	
	public RLModelClient(String host, int post, boolean verbose) {
		super(host, post, verbose);
	}
	
	public void notifyPropEnd() throws IOException, InterruptedException {
		this.sendMsg(propEndMsg);
	}
	
	public void notifyVectorEnd() throws IOException, InterruptedException {
		this.sendMsg(this.vectorEndMsg);
	}
	
	public void sendNodeFeature(final TraceNode node) throws IOException, InterruptedException {
		final NodeVector vector = new NodeVector(node);
		this.sendMsg(vector.toString());
	}
	
	public void sendEmptyNodeFeature() throws IOException, InterruptedException {
		final NodeVector vector = new NodeVector();
		this.sendMsg(vector.toString());
	}
	
	public double recieveFactor() throws IOException {
		String message = this.receiveMsg();
		return Double.valueOf(message);
	}

}
