package microbat.debugpilot.userfeedback;

/**
 * Possible user feedback type for DebugPilot
 */
public enum DPUserFeedbackType {
	/**
	 * This node is root cause to the bug
	 */
	ROOT_CAUSE,
	/**
	 * This node should not be executed
	 */
	WRONG_PATH,
	/**
	 * This node contain wrong variable
	 */
	WRONG_VARIABLE,
	/**
	 * This node is correct
	 */
	CORRECT,
}
