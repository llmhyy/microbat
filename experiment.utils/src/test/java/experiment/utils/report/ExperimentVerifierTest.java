/**
 * 
 */
package experiment.utils.report;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import experiment.utils.report.rules.SimulatorComparisonRule;
import experiment.utils.report.rules.TextComparisonRule;

/**
 * @author LLT
 *
 */
public class ExperimentVerifierTest {
	
	@Test
	public void compareMath() throws IOException {
		String resultFile = "E:/Shared_folder/learntest/evosuite-math/evosuite_graphCoverage_result.xlsx";
		String oldReport = "E:/Shared_folder/learntest/evosuite-math/evosuite_graphCoverage_old.xlsx";
		String newReport = "E:/Shared_folder/learntest/evosuite-math/evosuite_graphCoverage_new.xlsx";
		File file = new File(resultFile);
		if (file.exists()) {
			file.delete();
		}
		Map<String, List<String>> keys = new HashMap<String, List<String>>();
		keys.put("data", Arrays.asList("target_method"));
		ExperimentReportComparisonReporter.reportChange(resultFile, oldReport, newReport, Arrays.asList(new TextComparisonRule(null)),
				keys);
	}

	@Test
	public void test() throws IOException {
		String resultFile = "E:/lyly/eclipse-java-mars-clean/defects4j_compare.xlsx";
		String oldReport = "E:/lyly/eclipse-java-mars-clean/ben.xlsx";
		String newReport = "E:/lyly/eclipse-java-mars-clean/defects4j10.xlsx";
		File file = new File(resultFile);
		if (file.exists()) {
			file.delete();
		}
		Map<String, List<String>> keys = new HashMap<String, List<String>>();
		keys.put("data", Arrays.asList("project", "bug_ID"));
		ExperimentReportComparisonReporter.reportChange(resultFile, oldReport, newReport, Arrays.asList(new TextComparisonRule(null),
				new SimulatorComparisonRule()),
				keys);
	}
	
	
	@Test
	public void generateReportAlignment() throws IOException {
		String resultFile = "E:/Shared_folder/learntest/coverage_progress_random_merge.xlsx";
		String report0 = "E:/Shared_folder/learntest/coverage_progress_random0.xlsx";
		String report1 = "E:/Shared_folder/learntest/coverage_progress_random1.xlsx";
		String report2 = "E:/Shared_folder/learntest/coverage_progress_random2.xlsx";
		String report3 = "E:/Shared_folder/learntest/coverage_progress_random3.xlsx";
		
		File file = new File(resultFile);
		if (file.exists()) {
			file.delete();
		}
		Map<String, List<String>> keys = new HashMap<String, List<String>>();
		keys.put("data", Arrays.asList("MethodProgress"));
		ComparedReportGenerator.generateReport(resultFile, Arrays.asList(report0, report1, report2, report3), 
				keys);
	}
}
