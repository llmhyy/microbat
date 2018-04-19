/**
 * 
 */
package microbat.mutation.trace.report;

import microbat.mutation.mutation.MutationType;

/**
 * @author LLT
 *
 */
public class EmptyMutationCaseChecker implements IMutationCaseChecker {

	@Override
	public boolean accept(String testClass) {
		return true;
	}

	@Override
	public boolean accept(String testClass, String testMethod) {
		return true;
	}

	@Override
	public boolean accept(String mutationBugId, MutationType mutationType) {
		return true;
	}

}
