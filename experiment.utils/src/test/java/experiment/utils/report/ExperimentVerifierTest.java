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

/**
 * @author LLT
 *
 */
public class ExperimentVerifierTest {

	@Test
	public void test() throws IOException {
		String resultFile = "E:/lyly/TestResult/defects4j_compare.xlsx";
		String oldReport = "E:/lyly/TestResult/defects4j_benchmark.xlsx";
		String newReport = "E:/lyly/eclipse-java-mars-clean/eclipse/defects4j0.xlsx";
		File file = new File(resultFile);
		if (file.exists()) {
			file.delete();
		}
		Map<String, List<String>> keys = new HashMap<String, List<String>>();
		keys.put("data", Arrays.asList("project", "bug_ID"));
		ExperimentRegressionTest.reportChange(resultFile, oldReport, newReport, Arrays.asList(new TextComparationRule(null)),
				keys);
	}
}
