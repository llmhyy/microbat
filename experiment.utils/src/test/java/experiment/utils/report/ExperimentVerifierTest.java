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
	public void test() throws IOException {
		String resultFile = "E:/lyly/eclipse-java-mars-clean/eclipse-for-unmodified-code/defects4j_compare.xlsx";
		String oldReport = "E:/lyly/eclipse-java-mars-clean/eclipse-for-unmodified-code/defects4j_benchmark.xlsx";
		String newReport = "E:/lyly/eclipse-java-mars-clean/eclipse-for-unmodified-code/defects4j0 - Copy.xlsx";
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
}
