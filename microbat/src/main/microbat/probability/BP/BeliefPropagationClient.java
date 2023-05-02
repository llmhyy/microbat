package microbat.probability.BP;

import java.io.IOException;
import java.util.List;

import microbat.probability.BP.constraint.Constraint;
import microbat.pyserver.Client;

/**
 * BeliefPropagationClient is used to communicate with the BP server.
 * @author David
 *
 */
public class BeliefPropagationClient extends Client {
	
	public BeliefPropagationClient() {
		this("127.0.0.1", 8080, false);
	}
	
	public BeliefPropagationClient(final String host, final int port) {
		this(host, port, false);
	}
	
	public BeliefPropagationClient(final String host, final int port, final boolean verbose) {
		super(host, port, verbose);
	}
	
	/**
	 * Request the python server to run belief propagation algorithm
	 * to calculate the marginal probability
	 * 
	 * @param graphStruct Graph structure message
	 * @param factors Factor message
	 * @return String response from the server
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws Exception Throw when server does not response or have wrong response
	 */
	public String requestBP(final String graphStruct, final String factors) throws IOException, InterruptedException {
		this.sendMsg(graphStruct);
		this.sendMsg(factors);
		String responst_str = this.receiveMsg();
		return responst_str;
	}
}
