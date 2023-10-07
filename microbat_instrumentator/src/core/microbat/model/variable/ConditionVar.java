package microbat.model.variable;

public class ConditionVar extends LocalVar {

	private static final long serialVersionUID = 1L;

	/**
	 * Prefix of id of condition result variable. <br><br>
	 * The id of condition result follow the format: CR_<TraceNode Order>
	 */
	public static final String CONDITION_RESULT_ID = "CR_";
	
	/**
	 * Prefix of variable name of condition result. <br><br>
	 * The variable name of condition result follow the format: ConditionResult_<TraceNode Order>
	 */
	public static final String CONDITION_RESULT_NAME = "ConditionResult_";
	
	public ConditionVar(final int nodeOrder, final int lineNumber) {
		this(ConditionVar.CONDITION_RESULT_NAME + nodeOrder, "boolean", "", lineNumber);
	}
	
	public ConditionVar(final String name, final String type, final String locationClass, final int lineNumber) {
		super(name, type, locationClass, lineNumber);
	}
	
}
