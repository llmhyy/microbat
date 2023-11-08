package microbat.debugpilot.propagation.BP;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import microbat.debugpilot.propagation.BP.constraint.Constraint;
import microbat.pyserver.Client;

/**
 * BeliefPropagationClient is used to communicate with the BP server.
 * @author David
 *
 */
public class BeliefPropagationClient extends Client {
	
	protected final MessageProcessor messageProcessor = new MessageProcessor();
	
	public BeliefPropagationClient() {
		this("127.0.0.2", 8080, false);
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
	
	public Map<String, Double> requestBP(final Collection<Constraint> constraints) throws IOException, InterruptedException {
		this.sendGraphMsg(constraints);
		this.sendFactorMsg(constraints);
		return this.receiveResponse();
	}
	
	protected void sendGraphMsg(final Collection<Constraint> constraints) throws IOException, InterruptedException {
		for (Constraint constraint : constraints) {
			this.notifyContinuoue();
			final String graphMsg = this.messageProcessor.buildGrapMsg(constraint);
			this.sendMsg(graphMsg);
		}
		this.notifyStop();
	}
	
	protected void sendFactorMsg(final Collection<Constraint> constraints) throws IOException, InterruptedException {
		for (Constraint constraint: constraints) {
			this.notifyContinuoue();
			final String factorMsg = this.messageProcessor.buildFactorMsg(constraint);
			this.sendMsg(factorMsg);
		}
		this.notifyStop();
	}
	
	protected Map<String, Double> receiveResponse() throws IOException {
		final String response = this.receiveMsg();
		return this.messageProcessor.recieveMsg(response);
	}
}
