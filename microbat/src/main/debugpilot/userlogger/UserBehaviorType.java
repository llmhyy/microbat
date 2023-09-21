package debugpilot.userlogger;

public enum UserBehaviorType {
	CHECK_NODE(0, "Click node in trace view"),
	CHECK_PATH(1, "Click step in path view"),
	DATA_SLICING_CONFIRM(2, "Confirm a data slicing action"),
	DATA_SLICING_EXPLORE(3, "Explore other node by data slicing"),
	CONTROL_SLICING_CONFIRM(4, "Confirm a control slicing action"),
	CONTROL_SLICING_EXPLORE(5, "Explore other node by control slicing"),
	CORRECT(6, "Confirm a correct action"),
	ROOT_CAUSE(7, "Confirm a root cause"),
	START_DEBUGPILOT(8, "Start running debugpilot"),
	STOP_DEBUGPILOT(9, "Stop running debugpilot"),

	;
	
	public int id;
	public String description;
	
	private UserBehaviorType(int id, String description) {
		this.id = id;
		this.description = description;
	}
}
