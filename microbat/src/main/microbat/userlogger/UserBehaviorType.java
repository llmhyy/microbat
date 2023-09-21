package microbat.userlogger;

public enum UserBehaviorType {
	CHECK_NODE(0, "Check trace node"),
	DATA_SLICING(1, "Data slicing"),
	CONTROL_SLICING(2, "Control slicing"),
	FIND_BUG(3, "Click find bug button"),
	UNCLEAR(4, "Unclear"),
	CORRECT(5, "Correct");
	
	public final int id;
	public final String description;
	
	private UserBehaviorType(int id, String description) {
		this.id = id;
		this.description = description;
	}
}
