/**
 * 
 */
package microbat.mutation.trace.report;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
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
	 * @param targetProject 
	 */
	public MutationCaseChecker(String targetProject) {
		ExcelReader excelReader = new ExcelReader(
				"/Users/lylytran/Projects/TOOLS/Eclipse/eclipse-mars/Eclipse.app/Contents/MacOS", targetProject);
//		excelReader.setFileName("/Users/lylytran/Projects/TOOLS/Eclipse/eclipse-mars/Eclipse.app/Contents/MacOS/apache-common-math-2.2_regression0.xlsx");
		excelReader.setFileName("/Users/lylytran/Projects/TOOLS/Eclipse/eclipse-mars/Eclipse.app/Contents/MacOS/common-lang_regression0.xlsx");
		
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
				if (frag.length == 1) {
					frag = StringUtils.split(trial.getMutatedFile(), "/");
				}
				String mutationId = frag[frag.length - 2];
				int idx = mutationId.lastIndexOf(".");
				if (idx > 0) {
					mutationId = mutationId.substring(idx + 1, mutationId.length());
				}
				sb.append(mutationId);
				bugIds.put(sb.toString(), MutationType.valueOf(trial.getMutationType()));
			}
//			filterBugIds();
//			bugIds.put("AbstractUnivariateStatisticTest#testTestNegative#AbstractUnivariateStatistic_197_9_1", MutationType.REMOVE_IF_CONDITION);
			
			System.out.println(trials);
			System.out.println("checker-testClasses: " + testClasses.size());
			System.out.println(sav.common.core.utils.StringUtils.newLineJoin(testClasses));
			System.out.println("checker-testcaseNames: " + testcaseNames.size());
			System.out.println(sav.common.core.utils.StringUtils.newLineJoin(testcaseNames));
			System.out.println("checker-bugIds: " + bugIds.size());
			System.out.println(bugIds);
			System.out.println();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

//	Set<String> validIds;
//	private void filterBugIds() throws IOException {
//		List validBugIds = FileUtils.readLines(new File("/Users/lylytran/Projects/microbat/testdata/bugIds.txt"));
//		validIds = new HashSet<>();
//		for (Object bugId : validBugIds) {
//			validIds.add((String)bugId);
//		}
//	}

	@Override
	public boolean accept(String testClass) {
		return true;
//		boolean accept = testClasses.contains(testClass);
//		if (accept) {
//			System.out.println(String.format("Detect_testClass: %s", testClass));
//		}
//		return accept;
	}

	@Override
	public boolean accept(String testClass, String testMethod) {
//		return true;
		String tc = testClass + "#" + testMethod;
		boolean inList = testcaseNames.contains(tc);
		if (inList) {
			System.out.println(String.format("Detect_testcase: %s", tc));
		}
		return !inList;
	}
	
	int count = 0;
	@Override
	public boolean accept(String mutationBugId, MutationType mutationType) {
		boolean inList = bugIds.get(mutationBugId) == mutationType;
		if (inList) {
			System.out.println(String.format("Detect_mutationBugId (%d): %s", ++count, mutationBugId));
		}
//		if (!inList || !validIds.contains(mutationBugId)) {
//			return true;
//		}
		
//		return false;
		return !inList;
	}
}
