package microbat.util;

import java.util.HashSet;

import microbat.model.UserInterestedVariables;
import microbat.model.trace.PotentialCorrectPatternList;

public class Settings {
	/**
	 * the variables checked by user as wrong.
	 */
	public static UserInterestedVariables interestedVariables = new UserInterestedVariables();
	public static PotentialCorrectPatternList potentialCorrectPatterns = new PotentialCorrectPatternList();
	/**
	 * the trace order in execution trace that are marked by user as wrong-path, it is used to 
	 * check whether certain trace node is marked by user as wrong-path.
	 */
	public static HashSet<Integer> wrongPathNodeOrder = new HashSet<>();
	
}
