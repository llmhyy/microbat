package microbat.mutation.mutation;

import java.util.Arrays;
import java.util.List;

public enum MutationType {
	REMOVE_IF_BLOCK ("Remove If Block"),
	REMOVE_ASSIGNMENT ("Remove Assignment"),
	REMOVE_IF_CONDITION ("Remove If Condition"),
	REMOVE_IF_RETURN ("Remove If Return"),
	NEGATE_IF_CONDITION ("Negate If Condition");
	
	private String text;
	private MutationType(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public static List<MutationType> getPreferenceMutationTypes() {
		return Arrays.asList(REMOVE_ASSIGNMENT, REMOVE_IF_BLOCK, REMOVE_IF_CONDITION, NEGATE_IF_CONDITION);
	}
}
