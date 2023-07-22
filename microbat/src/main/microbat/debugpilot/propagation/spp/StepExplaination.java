package microbat.debugpilot.propagation.spp;

public class StepExplaination {
	
	public static final String RANDOM = "Random Choice";
	public static final String LAREST_GAP = "Largest Gap";
	public static final String USRE_CONFIRMED = "User Confirmed";
	public static final String COST = "Computational Cost";
	public static final String MISS_BRANCH = "Missing Branch";
	public static final String REF(final int order) {
		return "Reference to feedback on node: " + order;
	}
	public static final String REF(final int order1, final int order2) {
		return "Reference to feedback on node: " + order1 + " and " + order2;
	}
	public static final String MISS_DEF(final String varName) {
		return "Missing Assignment on " + varName;
	}
}
