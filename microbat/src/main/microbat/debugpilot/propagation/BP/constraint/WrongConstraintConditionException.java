package microbat.debugpilot.propagation.BP.constraint;

/**
 * Customized runtime exception type that declare
 * the condition for constructing belief propagation
 * constraint is wrong
 * @author David
 *
 */
public class WrongConstraintConditionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public WrongConstraintConditionException(String message) {
		super(message);
	}

}
