/**
 * 
 */
package microbat.mutation.trace.report;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVRecord;

import microbat.mutation.mutation.MutationType;
import microbat.mutation.trace.dto.AnalysisParams;
import microbat.mutation.trace.dto.MutationCase;
import microbat.mutation.trace.dto.MutationCase.Column;

/**
 * @author LLT
 *
 */
public class ExcludeMutationCaseChecker implements IMutationCaseChecker {
	private Set<String> testClasses = new HashSet<>();
	private Set<String> testcaseNames = new HashSet<>();
	
	
	public ExcludeMutationCaseChecker(String targetProject, AnalysisParams analysisParams) {
		try {
			readFromMutationCaseCsv(targetProject, analysisParams);
			System.out.println("checker-testClasses: " + testClasses.size());
			System.out.println(sav.common.core.utils.StringUtils.newLineJoin(testClasses));
			System.out.println("checker-testcaseNames: " + testcaseNames.size());
			System.out.println(sav.common.core.utils.StringUtils.newLineJoin(testcaseNames));
			System.out.println();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void readFromMutationCaseCsv(String targetProject, AnalysisParams analysisParams) throws IOException {
		String csvFilePath = null;
		if (targetProject.contains("JFreeChart")) {
			csvFilePath = "/Users/lylytran/Projects/RemoteShare/microbat-experiment/jfreechart-mutationCases.csv";
		} else if (targetProject.contains("lang")) {
			csvFilePath = "/Users/lylytran/Projects/RemoteShare/microbat-experiment/common-lang-mutationCases.csv";
		}
		List<CSVRecord> records = MutationCase.getRecords(csvFilePath);
		for (CSVRecord record : records) {
			String testClass = record.get(Column.JUNIT_CLASS_NAME);
			testClasses.add(testClass);
			testcaseNames.add(getTestcaseName(testClass, record.get(Column.TEST_METHOD)));
		}
	}

	@Override
	public boolean accept(String testClass) {
		boolean isInList = testClasses.contains(testClass);
		if (isInList) {
			System.out.println(String.format("Detect_testClass: %s", testClass));
		}
		return true;
	}

	@Override
	public boolean accept(String testClass, String testMethod) {
		String tc = getTestcaseName(testClass, testMethod);
		boolean inList = testcaseNames.contains(tc);
		if (inList) {
			System.out.println(String.format("Detect_testcase: %s", tc));
		}
		return !inList;
	}

	private String getTestcaseName(String testClass, String testMethod) {
		return testClass + "#" + testMethod;
	}
	
	@Override
	public boolean accept(String mutationBugId, MutationType mutationType) {
		return true;
	}
}
