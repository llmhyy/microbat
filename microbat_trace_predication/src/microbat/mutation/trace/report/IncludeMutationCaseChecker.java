/**
 * 
 */
package microbat.mutation.trace.report;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

import microbat.mutation.mutation.MutationType;
import microbat.mutation.trace.dto.AnalysisParams;
import microbat.mutation.trace.dto.MutationCase;
import microbat.mutation.trace.dto.MutationCase.Column;

/**
 * @author LLT
 *
 */
public class IncludeMutationCaseChecker implements IMutationCaseChecker {
	private Set<String> testcaseNames = new HashSet<>();
	
	
	public IncludeMutationCaseChecker(String targetProject, AnalysisParams analysisParams) {
		try {
			File file;
			if (analysisParams.getMutationOutputSpace().endsWith("another")) {
				file = new File("/Users/lylytran/Projects/math-tcs2.txt");
			} else {
				file = new File("/Users/lylytran/Projects/math-tcs1.txt");
			}
			List<?> lines = FileUtils.readLines(file);
			for (Object line : lines) {
				testcaseNames.add((String)line);
			}
			System.out.println("checker-testcaseNames: " + testcaseNames.size());
			System.out.println(sav.common.core.utils.StringUtils.newLineJoin(testcaseNames));
			System.out.println();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public boolean accept(String testClass) {
		return true;
	}

	@Override
	public boolean accept(String testClass, String testMethod) {
		String tc = getTestcaseName(testClass, testMethod);
		boolean inList = testcaseNames.contains(tc);
		if (inList) {
			System.out.println(String.format("Detect_testcase: %s", tc));
		}
		return inList;
	}

	private String getTestcaseName(String testClass, String testMethod) {
		return testClass + "#" + testMethod;
	}
	
	@Override
	public boolean accept(String mutationBugId, MutationType mutationType) {
		return true;
	}
}
