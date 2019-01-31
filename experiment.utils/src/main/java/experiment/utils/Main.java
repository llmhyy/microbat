package experiment.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import experiment.utils.report.ComparedReportGenerator;
import experiment.utils.report.ExperimentReportComparisonReporter;
import experiment.utils.report.rules.IComparisonRule;
import sav.common.core.utils.AlphanumComparator;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.FileUtils;

public class Main {
	
	@Test
	public void test() throws ParseException, IOException {
		main(new String[] {
				"-keys", "data#ProjectId#Class#Method",
				"-c",
				"-input_excels", "/Users/lylytran/Projects/Evosuite/experiments/test-reports/evoTest-reports/allMethods-flag-filtered-default.xlsx",
				"/Users/lylytran/Projects/Evosuite/experiments/test-reports/evoTest-reports/allMethods.xlsx",
				"-workingFolder", "/Users/lylytran/Projects/Evosuite/experiments/test-reports/evoTest-reports",
//				"-combCmpRules", ComparisonRuleEnum.numIncr + "#Coverage", ComparisonRuleEnum.numDecr + "#Age",
				"-cmpRules", ComparisonRuleEnum.numIncr + "#Coverage", ComparisonRuleEnum.numDecr + "#Age",
				"-cmpStats"
		});
	}

	public static void main(String[] args) throws ParseException, IOException {
		Parameters params = Parameters.parse(args);
		Main main = new Main();
		switch (params.getFunction()) {
		case ALIGNMENT:
			main.alignExcels(params);
			break;
		case COMPARISON:
			String newExcelName = new File(params.getInputExcels().get(1)).getName();
			String reportFile = FileUtils.getFilePath(params.getWorkingFolder(), 
					newExcelName.substring(0, newExcelName.indexOf(".xlsx")) + 
					"_compare.xlsx");
			if (params.isStatistic()) {
				reportFile = reportFile.replace("_compare.xlsx", "_compareStatistic.txt");
			}
			File file = new File(reportFile);
			if (file.exists()) {
				file.delete();
			}
			main.compareExcels(params.getInputExcels().get(0), params.getInputExcels().get(1), 
					reportFile,
					params.getComparisonKeys(), params.getComparisonRules());
			break;
		}
	}
	
	private void alignExcels(Parameters params) {
		List<String> inputExcels = params.getInputExcels();
		if (inputExcels.isEmpty()) {
			inputExcels = listExcels(params.getWorkingFolder());
		}
		String resultFile = FileUtils.getFilePath(params.getWorkingFolder(), "align.xlsx");
		File file = new File(resultFile);
		if (file.exists()) {
			file.delete();
		}
		ComparedReportGenerator.generateReport(resultFile, inputExcels, params.getComparisonKeys());
	}
	
	public void compareExcels(String oldReport, String newReport, String resultFile, Map<String, List<String>> keys,
			List<IComparisonRule> rules) throws IOException {
		File file = new File(resultFile);
		if (file.exists()) {
			file.delete();
		}
		ExperimentReportComparisonReporter.reportChange(resultFile, oldReport, newReport, rules, keys);
	}
	
	public static List<String> listExcels(String folder) {
		File[] files = new File(folder).listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xlsx") && !name.startsWith("~");
			}
		});
		List<File> excels = CollectionUtils.toArrayList(files);
		final AlphanumComparator comparator = new AlphanumComparator();
		Collections.sort(excels, new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				return comparator.compare(o1.getName(), o2.getName());
			}
		});
		List<String> filePaths = new ArrayList<String>(); 
		for (File file : excels) {
			filePaths.add(file.getAbsolutePath());
		}
		return filePaths;
	}
}
