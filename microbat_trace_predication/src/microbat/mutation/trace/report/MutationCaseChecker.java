/**
 * 
 */
package microbat.mutation.trace.report;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import microbat.mutation.mutation.MutationType;
import tregression.io.ExcelReader;
import tregression.model.Trial;

/**
 * @author LLT
 *
 */
public class MutationCaseChecker implements IMutationCaseChecker {
	private Set<String> testClasses = new HashSet<>();
	private Set<String> testcaseNames = new HashSet<>();
	private Map<String, MutationType> bugIds = new HashMap<>();
	
	//FractionFormatTest#testParseInvalidDenominator#AbstractFormat_70_9_1
	/**
	 * org.apache.commons.math.distribution.FDistributionTest#
	 * testCumulativeProbabilityExtremes 
	 * 
	 * REMOVE_IF_CONDITION
	 * 
	 * 
	 * E:\linyun\software\eclipse-java-mars\eclipse-java-mars-clean\eclipse\
	 * trace\mutation\apache-common-math-2.2\FDistributionTest\
	 * testCumulativeProbabilityExtremes\org.apache.commons.math.distribution.
	 * FDistributionImpl_117_9_2\FDistributionImpl.java 
	 * 
	 * 117 112 128 16 0 0
	 */
	public MutationCaseChecker() {
		ExcelReader excelReader = new ExcelReader("E:/lyly/eclipse-java-mars-clean/eclipse/trace/mutation/apache-common-math-2.2");
		try {
			excelReader.readXLSX();
			Set<Trial> trials = excelReader.getSet();
			for (Trial trial : trials) {
				String testCaseName = trial.getTestCaseName();
				testClasses.add(testCaseName.split("#")[0]);
				testcaseNames.add(testCaseName);
				StringBuilder sb = new StringBuilder();
				sb.append(testCaseName.substring(testCaseName.lastIndexOf(".") + 1, testCaseName.length()));
				sb.append("#");
				String[] frag = StringUtils.split(trial.getMutatedFile(), "\\");
				String mutationId = frag[frag.length - 2];
				int idx = mutationId.lastIndexOf(".");
				if (idx > 0) {
					mutationId = mutationId.substring(idx + 1, mutationId.length());
				}
				sb.append(mutationId);
				bugIds.put(sb.toString(), MutationType.valueOf(trial.getMutationType()));
			}
			System.out.println(trials);
			System.out.println("checker-testClasses: " + testClasses.size());
			System.out.println(sav.common.core.utils.StringUtils.newLineJoin(testClasses));
			System.out.println("checker-testcaseNames: " + testcaseNames.size());
			System.out.println(sav.common.core.utils.StringUtils.newLineJoin(testcaseNames));
			System.out.println("checker-bugIds: " + bugIds.size());
			System.out.println(bugIds);
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public boolean accept(String testClass) {
		boolean accept = testClasses.contains(testClass);
		if (accept) {
			System.out.println(String.format("Detect_testClass: %s", testClass));
		}
		return accept;
	}

	@Override
	public boolean accept(String testClass, String testMethod) {
		String tc = testClass + "#" + testMethod;
		boolean accept = testcaseNames.contains(tc);
		if (accept) {
			System.out.println(String.format("Detect_testcase: %s", tc));
		}
		return accept;
	}
	
	int count = 0;
	@Override
	public boolean accept(String mutationBugId, MutationType mutationType) {
		boolean accept = bugIds.get(mutationBugId) == mutationType;
		if (accept) {
			System.out.println(String.format("Detect_mutationBugId (%d): %s", ++count, mutationBugId));
		}
		return accept;
	}
}
