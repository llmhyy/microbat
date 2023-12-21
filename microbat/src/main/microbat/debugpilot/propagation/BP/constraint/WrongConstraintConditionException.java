package microbat.debugpilot.propagation.BP.constraint;

/**
 * Customized runtime exception type that declare the condition for constructing belief propagation
 * constraint is wrong
 * 
 * @since 2023-20-19
 * @version 1.0
 */
public class WrongConstraintConditionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new WrongConstraintConditionException with the specified detail message.
     * 
     * @param message the detail message
     */
    public WrongConstraintConditionException(String message) {
        super(message);
    }
}
