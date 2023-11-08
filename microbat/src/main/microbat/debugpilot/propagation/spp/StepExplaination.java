package microbat.debugpilot.propagation.spp;

public class StepExplaination {
	
	public static final String RANDOM = "Prediction made by random guessing";
	public static final String LAREST_GAP = """
			This step produce a largest drop in correctness from read variables to written variables.
			
			It means that this step make a wrong operation that make the originally correct read variables to wrong written variables.
			
			If this step contains only read or written variables, then the drop is measured by the gap between average of correctness among variables and the uncertainty 0.5.
			""";
	
	public static final String USRE_CONFIRMED = "This step is confirmed by users";
	public static final String COST = "Prediction made based on the assumsion that the variable or condition that take more comptuation cost are more likely to be wrong";
	public static final String MISS_BRANCH = "Some code is messing because a WRONG_BRANCH feedback is followed by CORRECT feedback, which mean there are some code missing that determine a new branch";
	public static final String SCANNING = "Scanning the root cause candidates one by one";
	public static final String BINARY_SEARCH = "Scanning the root cause by binary search";
	public static final String RELATED = "This step is reported because it reads the related variables";
	public static final String REF(final int order) {
		return "This step is similar to node: " + order + " therefore we assume that it has similar feedback";
	}
	public static final String REF(final int order1, final int order2) {
		return "This step is similar to node: " + order1 + " and " + order2 + ", therefore we assume that it has similar feedback";
	}
	public static final String MISS_DEF(final String varName) {
		return "Feedback of WRONG_VARAIBLE " + varName + " is followed by CORRECT feedback. It means that redefinition of variable is missing";
	}
}
