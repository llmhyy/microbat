package microbat.probability.BP;

import microbat.pyserver.Client;

/**
 * BeliefPropagationClient is used to communicate with the BP server.
 * @author David
 *
 */
public class BeliefPropagationClient extends Client {
	
	public BeliefPropagationClient() {
		super("127.0.0.1", 8080);
	}
	
	/**
	 * Request the python server to run belief propagation algorithm
	 * to calculate the marginal probability
	 * 
	 * @param graphStruct Graph structure message
	 * @param factors Factor message
	 * @return String response from the server
	 * @throws Exception Throw when server does not response or have wrong response
	 */
	public String requestBP(final String graphStruct, final String factors) {
		byte[] graphInput = this.strToByte(graphStruct);
		byte[] factorInput = this.strToByte(factors);
		
		if (graphInput.length >= Client.BUFFER_SIZE || factorInput.length >= Client.BUFFER_SIZE) {
			throw new RuntimeException("Message Exceed Max Buffer Size");
		}
		
		this.sendMsg(graphInput, factorInput);
		String responst_str = this.byteToStr(this.receiveMsg());
		
		return responst_str;
	}
}
