package microbat.codeanalysis.runtime;

public class StepLimitException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String ERROR_MSG = "Step Limit Exceeded";

	public StepLimitException() {
		super(ERROR_MSG);
	}

}
