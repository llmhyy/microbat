/**
 * 
 */
package microbat.mutation.trace.report;

import microbat.mutation.mutation.MutationType;

/**
 * @author LLT
 *
 */
public interface IMutationCaseChecker {

	boolean accept(String testClass);

	boolean accept(String testClass, String testMethod);

	boolean accept(String mutationBugId, MutationType mutationType);

}
