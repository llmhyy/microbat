package microbat.mutation.mutation;

public enum MutationType {
	REMOVE_IF_BLOCK ("Remove If Block"),
	REMOVE_ASSIGNMENT ("Remove Assignment"),
	REMOVE_IF_CONDITION ("Remove If Condition"),
	NEGATE_IF_CONDITION ("Negate If Condition");
	
	private String text;
	private MutationType(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
}
